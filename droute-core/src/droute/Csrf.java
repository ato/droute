package droute;

import static droute.Response.response;

public class Csrf {
	/**
	 * Retrieves the CSRF token associated with a request. If there is none, a
	 * new token will be generated and a Set-Cookie header will be appended to
	 * the response with its value.
	 */
	public static String token(Request request) {
		Csrf state = request.state(Csrf.class);
		if (state == null) {
			throw new IllegalStateException("Csrf.token() must be called from a handler wrapped in Csrf.protect()");
		}
		return state.token();
	}
	
	/**
	 * Middleware which provides "Double Submit Cookie" Cross Site Request
	 * Forgery protection. Uses "cstfToken" as the cookie name and form
	 * parameter name.
	 */
	public static Handler protect(Handler handler) {
		return protect(handler, "csrfToken", "csrfToken");
	}
	
	/**
	 * Middleware which provides "Double Submit Cookie" Cross Site Request. All
	 * non GET or HEAD requests will be denied unless they contain the same CSRF
	 * token in a cookie and form parameter.
	 */
	public static Handler protect(Handler handler, String cookieName, String formParam) {
		return request -> {
			String cookieToken = Cookies.get(request, cookieName);
			if (!Tokens.isSane(cookieName)) {
				cookieToken = null;
			}
			Csrf state = new Csrf(cookieToken);
			request.setState(state);
			if (!request.method().equals("GET") &&
					!request.method().equals("HEAD")) {
				if (cookieToken == null) {
					return response(400, "The cross-site request forgery protection cookie (" + cookieName + ") is missing or invalid.  Ensure your browser has cookies enabled and refresh the form.");
				}
				String formToken = request.header("X-Csrf-Token");
				if (formToken == null) {
					formToken = request.formParam(formParam);
				}
				if (!state.isTokenValid(request.formParam(formToken))) {
					return response(400, "The cross-site request forgery protection form parameter (" + formParam + ") is missing or invalid.");
				}
			}
			Response response = handler.handle(request);
			if (cookieToken == null && state.token != null) {
				return Cookies.set(response, "csrfToken", state.token,
						Cookies.path(request.contextPath()),
						Cookies.autosecure(request));
			}
			return response;
		};
	}
	
	String token;

	private Csrf(String token) {
		this.token = token;
	}

	boolean isTokenValid(String token) {
		return this.token != null && this.token.equals(token);
	}

	synchronized String token() {
		if (token == null) {
			token = Tokens.generate();
		}
		return token;
	}
}
