package droute;

import static droute.Response.response;
import static droute.Route.GET;
import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class TestRoute {

	@Test
	public void multipleUrlParamsShouldWork() {
		Handler handler = GET("/:a/:b/:c", request -> {
			assertEquals("foo", request.param("a"));
			assertEquals("000", request.param("b"));
			assertEquals("bar", request.param("c"));
			return response("matched");
		});
		Response response = handler.handle(new MockRequest("/foo/000/bar"));
		assertEquals("matched", response.body());
	}


    static class MockRequest implements Request {
		String path;
		MultiMap params = new MultiMap();
        MultiMap urlParams = new MultiMap();
        MultiMap queryParams = new MultiMap();
        MultiMap formParams = new MultiMap();
        HashMap<String,String> headers = new HashMap<>();
		
		public MockRequest(String path) {
			this.path = path;
		}
		
		@Override
		public String method() {
			return "GET";
		}

		@Override
		public String path() {
			return path;
		}

		@Override
		public String contextPath() {
			return "/";
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
		public Object raw() {
			return null;
		}

		@Override
		public Map<String, String> headers() {
			return headers;
		}

		@Override
		public void setState(Object state) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public <T> T state(Class<T> state) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public URI uri() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public URI contextUri() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String postBody() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}

}
