package droute;

import droute.v2.MultiMap;

import java.net.URI;
import java.util.Map;

public interface Request {

	/**
	 * The raw-request object from the underlying web server or web framework.
	 * When using the Java servlet framework this is a HttpServletRequest.
	 */
	Object raw();

	/**
	 * The HTTP request method such as "GET" or "POST".
	 */
	String method();

	/**
	 * The URI requested.
	 */
	URI uri();

	/**
	 * The HTTP request headers.
	 */
	Map<String, String> headers();

	/**
	 * The root URI of the web application.
	 * 
	 * Note that web applications may be accesible via multiple hostnames or
	 * protocols. This will return the root URI appropriate to this request.
	 */
	URI contextUri();

	/**
	 * A combined map of all URL, query and form parameters.
	 */
	MultiMap params();

	MultiMap urlParams();

	MultiMap queryParams();

	MultiMap formParams();
	
	String postBody();

	void setState(Object state);

	<T> T state(Class<T> state);

	default String param(String key) {
		return params().getFirst(key);
	}

	default String urlParam(String key) {
		return urlParams().getFirst(key);
	}

	default String queryParam(String key) {
		return queryParams().getFirst(key);
	}

	default String formParam(String key) {
		return formParams().getFirst(key);
	}

	default String header(String name) {
		return headers().get(name);
	}

	/**
	 * The path of this request relative to the context root.
	 */
	default String path() {
		return contextUri().relativize(uri()).getPath();
	}

	default String contextPath() {
		return contextUri().getPath();
	}
}