package droute.v2;

import droute.Response;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import static droute.Response.resource;

public class Routes {
    private ArrayList<Handler> list;

    public Router compile() {
        return new Router(list);
    }

    public Routes add(Handler handler) {
        list.add(handler);
        return this;
    }

    public Routes addAll(Routes routes) {
        list.addAll(routes.list);
        return this;
    }

    public Routes HEAD(String pathPattern, RouteHandler handler, String... paramsAndRegexs) {
        return add(new Route("HEAD", pathPattern, handler, paramsAndRegexs));
    }

    public Routes GET(String pathPattern, RouteHandler handler, String... paramsAndRegexs) {
        return add(new Route("GET", pathPattern, handler, paramsAndRegexs));
    }

    public Routes POST(String pathPattern, RouteHandler handler, String... paramsAndRegexs) {
        return add(new Route("POST", pathPattern, handler, paramsAndRegexs));
    }

    public Routes PUT(String pathPattern, RouteHandler handler, String... paramsAndRegexs) {
        return add(new Route("PUT", pathPattern, handler, paramsAndRegexs));
    }

    public Routes DELETE(String pathPattern, RouteHandler handler, String... paramsAndRegexs) {
        return add(new Route("DELETE", pathPattern, handler, paramsAndRegexs));
    }

    public Routes OPTIONS(String pathPattern, RouteHandler handler, String... paramsAndRegexs) {
        return add(new Route("OPTIONS", pathPattern, handler, paramsAndRegexs));
    }

    public Routes PATCH(String pathPattern, RouteHandler handler, String... paramsAndRegexs) {
        return add(new Route("PATCH", pathPattern, handler, paramsAndRegexs));
    }

    public Routes ANY(String pathPattern, RouteHandler handler, String... paramsAndRegexs) {
        return add(new Route(null, pathPattern, handler, paramsAndRegexs));
    }

    public Routes resources(String urlPrefix, String resourcesRoot) {
        return GET(urlPrefix + "/*", (request) -> {
            String path = resourcesRoot + "/" + request.param("*").replace("../", "");
            URL url = Thread.currentThread().getContextClassLoader().getResource(path);
            if (url != null) {
                try {
                    return resource(url);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                return Response.NEXT_HANDLER;
            }
        });
    }

}
