package droute;

import static droute.Response.NEXT_HANDLER;
import static droute.Response.response;
import static droute.Route.GET;
import static org.junit.Assert.*;

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
		Map<String,String> params = new HashMap<>();
		Map<String,String> urlParams = new HashMap<>();
		Map<String,String> queryParams = new HashMap<>();
		Map<String,String> formParams = new HashMap<>();
		
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
		public Object raw() {
			return null;
		}
		
	}

}
