package org.meshy.leanhttp;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.meshy.leanhttp.HttpResponses.resource;

public final class HttpRouter implements HttpHandler, AutoCloseable {
    private final List<HttpHandler> routes = new ArrayList<>();

    /**
     * Closes and removes all handlers. Handlers are closed in the reverse of the
     * order they were added in.
     *
     * @throws Exception if a handler could not be closed.
     */
    @Override
    public void close() throws Exception {
        ListIterator<HttpHandler> it = routes.listIterator(routes.size());
        while (it.hasPrevious()) {
            HttpHandler handler = it.previous();
            if (handler instanceof AutoCloseable) {
                ((AutoCloseable)handler).close();
            }
            it.remove();
        }
    }

    @Override
    public HttpResponse handle(HttpRequest request) throws IOException {
        for (HttpHandler route : routes) {
            HttpResponse result = route.handle(request);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public void addHandler(HttpHandler handler) {
        routes.add(handler);
    }

    public void on(String method, String pathPattern, HttpHandler handler) {
        addHandler(new Route(method, pathPattern, handler));
    }

    public void resources(String urlPrefix, String resourcesRoot) {
        on(HttpRequests.GET, urlPrefix + "/<resource:.+>", new ResourcesHandler(resourcesRoot));
    }

    static class ResourcesHandler implements HttpHandler {

        private final String resourcesRoot;

        ResourcesHandler(String resourcesRoot) {
            this.resourcesRoot = resourcesRoot;
        }

        @Override
        public HttpResponse handle(HttpRequest request) throws IOException {
            String path = resourcesRoot + "/" + request.param("resource").get().replace("../", "");
            URL url = Thread.currentThread().getContextClassLoader().getResource(path);
            return url == null ? null : resource(url);
        }
    }

    static class Route implements HttpHandler, AutoCloseable {
        private final static Pattern KEY_PATTERN = Pattern.compile("<([a-z_][a-zA-Z0-9_]*)(?::([^>]*))?>");

        private final String method;
        private final HttpHandler handler;
        private final String pattern;
        private final Pattern re;
        private final List<String> keys = new ArrayList<>();

        Route(String method, String pattern, HttpHandler handler) {
            this.method = method;
            this.handler = handler;
            this.pattern = pattern;
            this.re = compile();
        }

        @Override
        public HttpResponse handle(HttpRequest request) throws IOException {
            if (method == null || method.equalsIgnoreCase(request.method())) {
                Matcher m = re.matcher(request.path());
                if (m.matches()) {
                    Map<String, List<String>> oldParams = request.params();
                    Map<String, List<String>> params = new HashMap<String,List<String>>();

                    for (int i = 0; i < m.groupCount(); i++) {
                        String key = keys.get(i);
                        MultiMaps.addEntry(params, key, m.group(i + 1));
                    }

                    try {
                        request.setParams(params);
                        return handler.handle(request);
                    } finally {
                        request.setParams(oldParams);
                    }
                }
            }
            return null;
        }

        @Override
        public void close() throws Exception {
            if (handler instanceof AutoCloseable) {
                ((AutoCloseable)handler).close();
            }
        }

        private Pattern compile() {
            StringBuilder out = new StringBuilder();
            Matcher m = KEY_PATTERN.matcher(pattern);
            int pos = 0;
            while (m.find(pos)) {
                String key = m.group(1);
                String regex = m.group(2);
                if (regex == null) {
                    regex = "[^/,;?]+";
                }

                out.append(Pattern.quote(pattern.substring(pos, m.start())));
                out.append('(');
                out.append(regex);
                out.append(')');

                keys.add(key);
                pos = m.end();
            }

            out.append(Pattern.quote(pattern.substring(pos)));
            return Pattern.compile(out.toString());
        }
    }
}
