package droute;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public final class WebRouter implements WebHandler {
    private final List<WebHandler> routes = new ArrayList<>();

    @Override
    public WebResponse handle(WebRequest request) {
        for (WebHandler route : routes) {
            WebResponse result = route.handle(request);
            if (result != WebResponses.NEXT_HANDLER) {
                return result;
            }
        }
        return WebResponses.NEXT_HANDLER;
    }

    public void addHandler(WebHandler handler) {
        routes.add(handler);
    }

    public void on(String method, String pathPattern, WebHandler handler, String... paramsAndRegexs) {
        addHandler(new Route(method, pathPattern, handler, paramsAndRegexs));
    }

    public void onHEAD(String pathPattern, WebHandler handler, String... paramsAndRegexs) {
        on("HEAD", pathPattern, handler, paramsAndRegexs);
    }

    public void onGET(String pathPattern, WebHandler handler, String... paramsAndRegexs) {
        on("GET", pathPattern, handler, paramsAndRegexs);
    }

    public void onPOST(String pathPattern, WebHandler handler, String... paramsAndRegexs) {
        on("POST", pathPattern, handler, paramsAndRegexs);
    }

    public void onPUT(String pathPattern, WebHandler handler, String... paramsAndRegexs) {
        on("PUT", pathPattern, handler, paramsAndRegexs);
    }

    public void onDELETE(String pathPattern, WebHandler handler, String... paramsAndRegexs) {
        on("DELETE", pathPattern, handler, paramsAndRegexs);
    }

    public void onOPTIONS(String pathPattern, WebHandler handler, String... paramsAndRegexs) {
        on("OPTIONS", pathPattern, handler, paramsAndRegexs);
    }

    public void onANY(String pathPattern, WebHandler handler, String... paramsAndRegexs) {
        on(null, pathPattern, handler, paramsAndRegexs);
    }

    public void resources(String urlPrefix, String resourcesRoot) {
        onGET(urlPrefix + "/*", (request) -> {
            String path = resourcesRoot + "/" + request.param("*").replace("../", "");
            URL url = Thread.currentThread().getContextClassLoader().getResource(path);
            if (url != null) {
                try {
                    return WebResponses.resource(url);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                return WebResponses.NEXT_HANDLER;
            }
        });
    }
}
