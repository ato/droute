package droute;

import java.util.Map;

public interface Request {
	String method();
	String path();
	Map<String,String> params();
	Map<String,String> urlParams();
	Map<String,String> queryParams();
	Map<String,String> formParams();
	Object raw();
	
	default String param(String key) {
		return params().get(key);
	}

	default String urlParam(String key) {
		return urlParams().get(key);
	}

	default String queryParam(String key) {
		return queryParams().get(key);
	}
	
	default String formParam(String key) {
		return formParams().get(key);
	}
}