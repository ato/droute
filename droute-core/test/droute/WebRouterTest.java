package droute;

import static droute.legacy.Response.response;
import static droute.legacy.Route.GET;
import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import droute.legacy.Handler;
import droute.legacy.Request;
import droute.legacy.Response;
import droute.v2.OldMultiMap;
import org.junit.Test;

public class WebRouterTest {

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
		OldMultiMap params = new OldMultiMap();
        OldMultiMap urlParams = new OldMultiMap();
        OldMultiMap queryParams = new OldMultiMap();
        OldMultiMap formParams = new OldMultiMap();
        HashMap<String,String> headers = new HashMap<>();
		
		public MockRequest(String path) {
			this.path = path;
		}
		
		@Override
		public String method() {
			return "onGET";
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
		public OldMultiMap params() {
			return params;
		}

		@Override
		public OldMultiMap urlParams() {
			return urlParams;
		}

		@Override
		public OldMultiMap queryParams() {
			return queryParams;
		}

		@Override
		public OldMultiMap formParams() {
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
