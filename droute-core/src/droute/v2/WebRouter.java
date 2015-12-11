package droute.v2;

import java.util.List;

public final class WebRouter implements WebHandler {
    private final List<WebHandler> routes;

    WebRouter(List<WebHandler> routes) {
        this.routes = routes;
    }

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
}
