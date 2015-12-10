package droute.v2;

import droute.Response;

import java.util.List;

public class Router implements Handler {
    private final List<Handler> routes;

    Router(List<Handler> routes) {
        this.routes = routes;
    }

    @Override
    public Response handle(Request request) {
        for (Handler route : routes) {
            Response result = route.handle(request);
            if (result != Response.NEXT_HANDLER) {
                return result;
            }
        }
        return Response.NEXT_HANDLER;
    }
}
