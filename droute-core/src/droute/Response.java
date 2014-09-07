package droute;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

public interface Response {
	public static final Response NEXT_HANDLER = new Response.Impl(500, Headers.EMPTY, "Next handler");
	
	int status();
	Map<String, String> headers();
	Object body();

	default String header(String header) {
		return headers().get(header);
	}

	default Response withStatus(int status) {
		return response(status, headers(), body());
	}

	default Response withHeader(String header, String value) {
		Map<String, String> map = headers();
		Headers headers = map instanceof Headers ? (Headers)map : new Headers(map);
		return new Impl(status(), headers.with(header, value), body());
	}

	default Response withHeaders(Map<String,String> headers) {
		Headers hheaders = headers instanceof Headers ? (Headers)headers : new Headers(headers);
		return new Impl(status(), hheaders, body());
	}

	default Response withBody(Object body) {
		return response(status(), headers(), body());
	}

	public static Response response(int status, Map<String,String> headers, Object body) {
		if (headers == null) {
			return new Impl(status, Headers.EMPTY, body);			
		} else if (headers instanceof Headers) {
			return new Impl(status, (Headers)headers, body);
		} else {
			return new Impl(status, new Headers(headers), body);
		}
	}

	public static Response response(int status, Object body) {
		return new Impl(status, Headers.EMPTY, body);
	}

	public static Response response(Object body) {
		return new Impl(200, Headers.EMPTY, body);
	}

	public static Response redirect(String url) {
		return new Impl(302, Headers.of("Location", url), null);
	}
	
	public static Response resource(URL resource) throws IOException {
		if (resource == null) {
			return response(404, "Resource not found");
		}
		URLConnection conn = resource.openConnection();
		Headers headers = Headers.EMPTY;
		long lastModified = conn.getLastModified();
		if (lastModified != 0) {
			headers = headers.with("Last-Modified", Impl.httpDate(lastModified));
		}
		long length = conn.getContentLengthLong();
		if (length != -1) {
			headers = headers.with("Content-Length", Long.toString(length));
		}
		String type = ContentTypes.fromExtension(resource.getPath());
		if (type != null) {
			headers = headers.with("Content-Type", type);
		}
		return new Impl(200, headers, conn.getInputStream());
	}
	
	public static Response render(String view, Object model) {
		return response(new ModelAndView(model, view));
	}

	public static Response render(String view, Object... modelEntries) {
		if (modelEntries.length % 2 != 0) {
			throw new IllegalArgumentException("modelEntries must have an equal number of keys and values");
		}
		Map<Object,Object> model = new HashMap<>();
		for (int i = 0; i < modelEntries.length; i += 2) {
			model.put(modelEntries[i], modelEntries[i + 1]);
		}
		return render(view, model);
	}
	
	static class Impl implements Response {
		final int status;
		final Headers headers;
		final Object body;
		
		static String httpDate(long epochMillis) {
			return RFC_1123_DATE_TIME.format(OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC));
		}
		
		Impl(int status, Headers headers, Object body) {
			if (headers == null) {
				throw new IllegalArgumentException("headers can't be null");
			}
			this.status = status;
			this.headers = headers;
			this.body = body;
		}

		@Override
		public int status() {
			return status;
		}

		@Override
		public Map<String, String> headers() {
			return headers;
		}

		@Override
		public Object body() {
			return body;
		}

		@Override
		public Impl withStatus(int status) {
			return new Impl(status, headers, body);
		}

		@Override
		public Impl withHeader(String header, String value) {
			return new Impl(status, headers.with(header, value), body);
		}

		@Override
		public Response withHeaders(Map<String, String> headers) {
			if (headers instanceof Headers) {
				return new Impl(status, (Headers)headers, body);
			} else {
				return new Impl(status, new Headers(headers), body);
			}
		}

		@Override
		public Impl withBody(Object body) {
			return new Impl(status, headers, body);
		}
	}
}
