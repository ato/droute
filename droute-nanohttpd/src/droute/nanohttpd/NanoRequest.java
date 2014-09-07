package droute.nanohttpd;

import java.util.HashMap;
import java.util.Map;

import droute.Request;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public class NanoRequest implements Request {

	private final IHTTPSession raw;
	private final Map<String,String> params, urlParams, queryParams, formParams;
	
	public NanoRequest(IHTTPSession session) {
		this.raw = session;
		params = new HashMap<String,String>(session.getParms());
		queryParams = new HashMap<String,String>(session.getParms());
		formParams = new HashMap<String,String>(session.getParms());
		urlParams = new HashMap<String,String>();
	}

	@Override
	public String method() {
		return raw.getMethod().name();
	}

	@Override
	public String path() {
		return raw.getUri();
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
		return "/";
	}

}
