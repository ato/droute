package droute;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static droute.WebRequests.GET;
import static droute.WebResponses.resource;

public final class WebRouter implements WebHandler {
    private final List<WebHandler> routes = new ArrayList<>();

    /**
     * Closes and removes all handlers. Handlers are closed in the reverse of the
     * order they were added in.
     *
     * @throws Exception if a handler could not be closed.
     */
    @Override
    public void close() throws Exception {
        ListIterator<WebHandler> it = routes.listIterator(routes.size());
        while (it.hasPrevious()) {
            it.previous().close();
            it.remove();
        }
    }

    @Override
    public WebResponse handle(WebRequest request) throws IOException {
        for (WebHandler route : routes) {
            WebResponse result = route.handle(request);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public void addHandler(WebHandler handler) {
        routes.add(handler);
    }

    public void on(String method, String pathPattern, WebHandler handler) {
        addHandler(new Route(method, pathPattern, handler));
    }

    public void resources(String urlPrefix, String resourcesRoot) {
        on(GET, urlPrefix + "/<resource:.+>", (request) -> {
            String path = resourcesRoot + "/" + request.param("resource").get().replace("../", "");
            URL url = Thread.currentThread().getContextClassLoader().getResource(path);
            return url == null ? null : resource(url);
        });
    }

    static class Route implements WebHandler {
        private final static Pattern KEY_PATTERN = Pattern.compile("<([a-z_][a-zA-Z0-9_]*)(?::([^>]*))?>");

        private final String method;
        private final WebHandler handler;
        private final String pattern;
        private final Pattern re;
        private final List<String> keys = new ArrayList<>();

        Route(String method, String pattern, WebHandler handler) {
            this.method = method;
            this.handler = handler;
            this.pattern = pattern;
            this.re = compile();
        }

        @Override
        public WebResponse handle(WebRequest request) throws IOException {
            if (method == null || method.equalsIgnoreCase(request.method())) {
                Matcher m = re.matcher(request.path());
                if (m.matches()) {
                    MultiMap<String,String> oldParams = request.params();
                    MultiMap<String,String> params = new LinkedTreeMultiMap<>();

                    for (int i = 0; i < m.groupCount(); i++) {
                        String key = keys.get(i);
                        params.put(key, m.group(i + 1));
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
            handler.close();
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
