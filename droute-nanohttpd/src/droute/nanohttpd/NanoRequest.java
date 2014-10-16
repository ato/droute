package droute.nanohttpd;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import droute.Headers;
import droute.Request;
import droute.nanohttpd.NanoHTTPD.IHTTPSession;

public class NanoRequest implements Request {

	private final IHTTPSession raw;
	private final Map<String,String> params, urlParams, queryParams, formParams, headers;
	private final Map<Class<?>,Object> state;
	private final URI uri;
	private final String contextPath;
	
	public NanoRequest(IHTTPSession session) {
		this.raw = session;
		params = new HashMap<String,String>(session.getParms());
		queryParams = new HashMap<String,String>(session.getParms());
		formParams = new HashMap<String,String>(session.getParms());
		urlParams = new HashMap<String,String>();
		headers = new Headers(session.getHeaders());
		state = new HashMap<>();
		String path = session.getUri();
		String xForwardedPath = headers.get("x-forwarded-path");
		if (xForwardedPath != null && path.startsWith(xForwardedPath)) {
			path = path.substring(xForwardedPath.length());
			contextPath = xForwardedPath + "/";
		} else {
			contextPath = "/";
		}
		try {
			uri = new URI("http", headers.get("Host"), path, session.getQueryParameterString(), null);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String method() {
		return raw.getMethod().name();
	}

	@Override
	public String path() {
		return uri.getPath();
	}

	@Override
	public Map<String, String> params() {
		return params;
	}

	@Override
	public Map<String, String> urlParams() {
		return urlParams;
	}

	@Override
	public Map<String, String> queryParams() {
		return queryParams;
	}

	@Override
	public Map<String, String> formParams() {
		return formParams;
	}
	
	@Override
	public IHTTPSession raw() {
		return raw;
	}

	@Override
	public String contextPath() {
		return contextPath;
	}

	@Override
	public Map<String, String> headers() {
		return headers;
	}

	@Override
	public void setState(Object state) {
		this.state.put(state.getClass(), state);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T state(Class<T> stateType) {
		return (T)this.state.get(stateType);
	}

	@Override
	public URI uri() {
		return uri;
	}

	@Override
	public URI contextUri() {
		return uri.resolve(contextPath());
	}

}
