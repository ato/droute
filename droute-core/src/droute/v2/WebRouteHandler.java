package droute.v2;

public interface WebRouteHandler {
    WebResponse handle(RoutedWebRequest request);
}
