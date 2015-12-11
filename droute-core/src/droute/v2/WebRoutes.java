package droute.v2;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public final class WebRoutes {
    private ArrayList<WebHandler> list;

    public WebRouter compile() {
        return new WebRouter(list);
    }

    public WebRoutes add(WebHandler handler) {
        list.add(handler);
        return this;
    }

    public WebRoutes addAll(WebRoutes routes) {
        list.addAll(routes.list);
        return this;
    }

    public WebRoutes HEAD(String pathPattern, WebRouteHandler handler, String... paramsAndRegexs) {
        return add(new WebRoute("HEAD", pathPattern, handler, paramsAndRegexs));
    }

    public WebRoutes GET(String pathPattern, WebRouteHandler handler, String... paramsAndRegexs) {
        return add(new WebRoute("GET", pathPattern, handler, paramsAndRegexs));
    }

    public WebRoutes POST(String pathPattern, WebRouteHandler handler, String... paramsAndRegexs) {
        return add(new WebRoute("POST", pathPattern, handler, paramsAndRegexs));
    }

    public WebRoutes PUT(String pathPattern, WebRouteHandler handler, String... paramsAndRegexs) {
        return add(new WebRoute("PUT", pathPattern, handler, paramsAndRegexs));
    }

    public WebRoutes DELETE(String pathPattern, WebRouteHandler handler, String... paramsAndRegexs) {
        return add(new WebRoute("DELETE", pathPattern, handler, paramsAndRegexs));
    }

    public WebRoutes OPTIONS(String pathPattern, WebRouteHandler handler, String... paramsAndRegexs) {
        return add(new WebRoute("OPTIONS", pathPattern, handler, paramsAndRegexs));
    }

    public WebRoutes PATCH(String pathPattern, WebRouteHandler handler, String... paramsAndRegexs) {
        return add(new WebRoute("PATCH", pathPattern, handler, paramsAndRegexs));
    }

    public WebRoutes ANY(String pathPattern, WebRouteHandler handler, String... paramsAndRegexs) {
        return add(new WebRoute(null, pathPattern, handler, paramsAndRegexs));
    }

    public WebRoutes resources(String urlPrefix, String resourcesRoot) {
        return GET(urlPrefix + "/*", (request) -> {
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
