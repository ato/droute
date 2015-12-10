package droute.v2;

import droute.Response;

public interface RouteHandler {
    Response handle(RoutedRequest request);
}
