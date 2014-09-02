package droute;

import java.util.Map;

public interface Response {
	public static final Response NEXT_HANDLER = new Response.Impl(0, null, null);
	
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

	public static Response notFound(Object body) {
		return new Impl(404, Headers.EMPTY, body);
	}

	static class Impl implements Response {
		final int status;
		final Headers headers;
		final Object body;
		
		Impl(int status, Headers headers, Object body) {
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
