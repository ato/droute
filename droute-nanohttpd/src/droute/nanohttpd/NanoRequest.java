package droute.nanohttpd;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import droute.Headers;
import droute.MultiMap;
import droute.Request;
import droute.nanohttpd.NanoHTTPD.IHTTPSession;

public class NanoRequest implements Request {

	private final IHTTPSession raw;
	private final MultiMap params, urlParams, queryParams, formParams;
    private final Map<String,String> headers;
	private final Map<Class<?>,Object> state;
	private final URI uri;
	private final String contextPath;
	private final Map<String,String> files;
	
	public NanoRequest(IHTTPSession session, Map<String, String> files) {
		this.raw = session;
		this.files = files;
		params = new MultiMap();
        params.putAll(session.getParms());
		params.putAll(session.getFormParms());
		queryParams = session.getParms();
		formParams = session.getFormParms();
		urlParams = new MultiMap();
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

	public String postBody() {
		return files.get("postBody");
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
	public MultiMap params() {
		return params;
	}

	@Override
	public MultiMap urlParams() {
		return urlParams;
	}

	@Override
	public MultiMap queryParams() {
		return queryParams;
	}

	@Override
	public MultiMap formParams() {
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
