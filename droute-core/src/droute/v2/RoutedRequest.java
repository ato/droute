package droute.v2;

public interface RoutedRequest extends Request {

    /**
     * Returns the original request object.
     */
    Request unwrap();

    /**
     * Returns a map of parameters extracted from the URI during routing.
     */
    MultiMap params();

    /**
     * Returns the first matching URL parameter.
     */
    default String param(String key) {
        return params().getFirst(key);
    }

}
