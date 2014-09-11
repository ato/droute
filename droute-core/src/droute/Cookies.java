package droute;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Cookies {
	private static final String COOKIE_NAME = "[^\\p{Cntrl}()<>@,;:\\\\\"/\\[\\]?={} \t]+";
	private static final Pattern RE_COOKIE_NAME = Pattern.compile(COOKIE_NAME);
	private static final String COOKIE_OCTET = "[\\x21\\x23-\\x2B\\x2D-\\x3A\\x3C-\\x5B\\x5D-\\x7E]";
	private static final String COOKIE_VALUE = "(?:(" + COOKIE_OCTET + "*)|\"(" + COOKIE_OCTET + "*)\")";
	private static final Pattern RE_COOKIE_PAIR = Pattern.compile("(" + COOKIE_NAME + ")=" + COOKIE_VALUE);
		
	public static Map<String,String> parse(String cookieHeader) {
		Map<String,String> cookies = new HashMap<String,String>();
		if (cookieHeader != null) {
			String pairs[] = cookieHeader.split("; ");
			for (String pair : pairs) {
				Matcher m = RE_COOKIE_PAIR.matcher(pair);
				if (m.matches()) {
					String name = m.group(1);
					String value = m.group(2);
					if (value == null) {
						value = m.group(3);
					}
					try {
						cookies.put(name, URLDecoder.decode(value, "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		return cookies;
	}

	public static Map<String,String> parse(Request request) {
		Cookies state = request.state(Cookies.class);
		if (state == null) {
			state = new Cookies(parse(request.header("Cookie")));
			request.setState(state);
		}
		return state.cookies;
	}
	
	public static String get(Request request, String cookieName) {
		return parse(request).get(cookieName);
	}
		
	public static String encode(String name, String value, CookieAttribute... attributes) {
		if (!RE_COOKIE_NAME.matcher(name).matches()) {
			throw new IllegalArgumentException("Illegal cookie name: " + name);
		}
		StringBuilder out = new StringBuilder();
		out.append(name);
		out.append('=');
		try {
			out.append(URLEncoder.encode(value, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		
		for (CookieAttribute attr: attributes) {
			if (attr == null) {
				continue;
			}
			out.append("; ");
			out.append(attr.name());
			String attrValue = attr.value();
			if (attrValue != null) {
				out.append("=");
				out.append(attrValue);
			}
		}
		return out.toString();
	}
	
	public static Response set(Response response, String name, String value, CookieAttribute... attributes) {
		return response.withHeader("Set-Cookie", encode(name, value, attributes));
	}
	
	public static CookieAttribute expires(String cookieDate) {
		return new StandardAttribute("Expires", cookieDate);
	}

	public static CookieAttribute maxAge(long deltaSeconds) {
		return new StandardAttribute("Max-Age", Long.toString(deltaSeconds));
	}

	private static final CookieAttribute SECURE = new StandardAttribute("Secure"); 
	public static CookieAttribute secure() {
		return SECURE;
	}
	
	public static CookieAttribute autosecure(Request request) {
		return "https".equals(request.uri().getScheme()) ? SECURE : null;
	}

	public static CookieAttribute path(String path) {
		return new StandardAttribute("Path", path);
	}

	public static CookieAttribute domain(String domain) {
		return new StandardAttribute("Domain", domain);
	}

	private static final CookieAttribute HTTP_ONLY = new StandardAttribute("HttpOnly"); 
	public static CookieAttribute httpOnly() {
		return HTTP_ONLY;
	}
	
	private static class StandardAttribute implements CookieAttribute {
		final String name;
		final String value;

		StandardAttribute(String name) {
			this(name, null);
		}

		StandardAttribute(String name, String value) {
			this.name = name;
			this.value = value;
		}
		
		public String name() {
			return name;
		}
		
		public String value() {
			return value;
		}
		
		public String toString() {
			return "CookieAttribute[" + name + (value != null ? "=" + value : "") + "]";
		}
	}
	
	final Map<String,String> cookies;
	
	private Cookies(Map<String, String> cookies) {
		this.cookies = cookies;
	}
}
