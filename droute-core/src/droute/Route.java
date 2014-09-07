package droute;

import static droute.Response.resource;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Route implements Handler {
	private final String requestMethod;
	private final Handler handler;
	private final String pattern;
	private final Pattern re;
	private final List<String> keys = new ArrayList<>();

	public Route(String requestMethod, String pattern, Handler handler, String... paramsAndRegexs) {
		this.requestMethod = requestMethod;
		this.handler = handler;
		this.pattern = pattern;
		this.re = compile(paramsAndRegexsToMap(paramsAndRegexs));
	}

	@Override
	public Response handle(Request request) {
		if (requestMethod == null || requestMethod.equals(request.method())) {
			Matcher m = re.matcher(request.path());
			if (m.matches()) {
				for (int i = 0; i < m.groupCount(); i++) {
					String key = keys.get(i);
					request.params().put(key, m.group(i + 1));
					request.urlParams().put(key, m.group(i + 1));
				}
				return handler.handle(request);
			}
		}
		return Response.NEXT_HANDLER;
	}
	
	private final static Pattern KEY_PATTERN = Pattern.compile(":([a-z_][a-zA-Z0-9_]*)|\\*");
	
	private Pattern compile(Map<String,String> keyRegexs) {
		StringBuilder out = new StringBuilder();
		Matcher m = KEY_PATTERN.matcher(pattern);
		int pos = 0;
		while (m.find(pos)) {
			out.append(Pattern.quote(pattern.substring(pos, m.start())));
			String key = m.group(1);
			if (key == null) { // wildcard
				out.append("(.*?)");
				key = "*";
			} else {
				out.append('(');
				out.append(keyRegexs.getOrDefault(key, "[^/,;?]+"));
				out.append(')');
			}
			keys.add(key);
			pos = m.end();
		}
		out.append(Pattern.quote(pattern.substring(pos)));
		return Pattern.compile(out.toString());
	}
	
	private static Map<String,String> paramsAndRegexsToMap(String[] pairs) {
		if (pairs.length % 2 != 0) {
			throw new IllegalArgumentException("paramsAndRegexs must consist of param name, regex pairs");
		}
		Map<String,String> map = new HashMap<>();
		for (int i = 0; i < pairs.length; i += 2) {
			try { // syntax check
				Pattern.compile(pairs[i + 1]);
			} catch (PatternSyntaxException e) {
				throw new IllegalArgumentException("url param " + pairs[i] + " has invalid regex:" + e.getDescription(), e);
			}
			map.put(pairs[i], pairs[i + 1]);
		}
		return map;
	}
	
	private String prettyRequestMethod() {
		return requestMethod == null ? "ANY" : requestMethod; 
	}
	
	@Override
	public String toString() {
		return prettyRequestMethod() + "(" + pattern + ", " + handler + ")";
	}
	
	public static Handler HEAD(String pattern, Handler handler, String... paramsAndRegexs) {
		return new Route("HEAD", pattern, handler, paramsAndRegexs);
	}

	public static Handler GET(String pattern, Handler handler, String... paramsAndRegexs) {
		return new Route("GET", pattern, handler, paramsAndRegexs);
	}

	public static Handler POST(String pattern, Handler handler, String... paramsAndRegexs) {
		return new Route("POST", pattern, handler, paramsAndRegexs);
	}

	public static Handler PUT(String pattern, Handler handler, String... paramsAndRegexs) {
		return new Route("PUT", pattern, handler, paramsAndRegexs);
	}

	public static Handler DELETE(String pattern, Handler handler, String... paramsAndRegexs) {
		return new Route("DELETE", pattern, handler, paramsAndRegexs);
	}

	public static Handler OPTIONS(String pattern, Handler handler, String... paramsAndRegexs) {
		return new Route("OPTIONS", pattern, handler, paramsAndRegexs);
	}

	public static Handler PATCH(String pattern, Handler handler, String... paramsAndRegexs) {
		return new Route("PATCH", pattern, handler, paramsAndRegexs);
	}

	public static Handler ANY(String pattern, Handler handler, String... paramsAndRegexs) {
		return new Route(null, pattern, handler, paramsAndRegexs);
	}
	
	public static Handler resources(String urlPrefix, String resourcesRoot) {
		return GET(urlPrefix + "/*", (request) -> {
			String path = resourcesRoot + "/" + request.param("*").replace("../", "");
			URL url = Thread.currentThread().getContextClassLoader().getResource(path);
			if (url != null) {
				try {
					return resource(url);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}			
			} else {
				return Response.NEXT_HANDLER;
			}
		});
	}
	
	public static Handler notFound(Object body) {
		return request -> Response.response(404, body);
	}
	
	public static Handler routes(Handler... routes) {
		return new Routes(routes);
	}
	
	private static class Routes implements Handler {
		private final Handler[] routes;
		
		Routes(Handler... routes) {
			this.routes = routes;
		}
		
		@Override
		public Response handle(Request request) {
			for (Handler route : routes) {
				Response result = route.handle(request);
				if (result != Response.NEXT_HANDLER) {
					return result;
				}
			}
			return Response.NEXT_HANDLER;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("routes(");
			for (int i = 0; i < sb.length(); i++) {
				sb.append(routes[i].toString());
				if (i + 1 < sb.length()) {
					sb.append(",\n       ");
				}
			}
			sb.append(")");
			return sb.toString();
		}
	}
}
