package droute.v2;

public interface RoutedWebRequest extends WebRequest {

    /**
     * Returns the original request object.
     */
    WebRequest unwrap();

    /**
     * Returns a map of parameters extracted from the URI during routing.
     */
    MultiMap<String,String> params();

    /**
     * Returns the first matching URL parameter.
     */
    default String param(String key) {
        return params().getFirst(key);
    }

}
