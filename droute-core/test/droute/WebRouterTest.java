package droute;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static droute.WebResponses.NEXT_HANDLER;
import static droute.WebResponses.ok;
import static org.junit.Assert.assertEquals;

public class WebRouterTest {

    @Test
    public void testClose() throws Exception {
        // when two handlers are registered
        List<String> closes = new ArrayList<>();
        WebRouter router = new WebRouter();
        router.onGET("/apple", new WebHandler() {
            @Override
            public void close() throws Exception {
                closes.add("apple");
            }

            @Override
            public WebResponse handle(WebRequest request) throws IOException {
                return ok("apple");
            }
        });
        router.onGET("/banana", new WebHandler() {
            @Override
            public void close() throws Exception {
                closes.add("banana");
            }

            @Override
            public WebResponse handle(WebRequest request) throws IOException {
                return ok("banana");
            }
        });

        // on close
        router.close();

        assertEquals("it should have closed the handlers in reverser order",
                Arrays.asList("banana", "apple"), closes);

    }

    @Test
    public void testHandle() throws IOException {
        long start = System.currentTimeMillis();
        WebRouter router = new WebRouter();
        router.onGET("/simple", req -> ok("simple-get"));
        router.onOPTIONS("/simple", req -> ok("simple-options"));
        router.onPUT("/simple", req -> ok("simple-put"));
        router.onDELETE("/simple", req -> ok("simple-delete"));
        router.onHEAD("/simple", req -> ok("simple-head"));
        router.onOPTIONS("/simple", req -> ok("simple-options"));
        router.onPATCH("/simple", req -> ok("simple-patch"));
        router.onPOST("/simple", req -> ok("simple-post"));
        router.on("WIBBLE", "/wibbler", req -> ok("wibbled"));
        router.onANY("/wibbler", req -> ok("any"));
        router.onGET("/triple/<a>/<b>/<c>", req -> {
            assertEquals("antelope", req.param("a"));
            assertEquals("banana", req.param("b"));
            assertEquals("cabbage", req.param("c"));
            assertEquals("question=yes&quality=3", req.queryString());
            assertEquals("yes", req.query("question"));
            assertEquals("3", req.query("quality"));
            return ok("triple");
        });
        router.onGET("/sesame/<letters:[a-z]+>", req -> ok("letters"));
        router.onGET("/sesame/<numbers:[0-9]+>", req -> ok("numbers"));
        router.resources("/assets", "droute/assets");

        assertBody("triple", router.handle(GET("/triple/antelope/banana/cabbage?question=yes&quality=3")));
        assertBody("simple-get", router.handle(GET("/simple")));
        assertBody("simple-options", router.handle(OPTIONS("/simple")));
        assertBody("simple-put", router.handle(PUT("/simple")));
        assertBody("simple-post", router.handle(POST("/simple")));
        assertBody("simple-delete", router.handle(DELETE("/simple")));
        assertBody("simple-head", router.handle(HEAD("/simple")));
        assertBody("simple-patch", router.handle(PATCH("/simple")));
        assertBody("wibbled", router.handle(request("WIbbLE", "/wibbler")));
        assertBody("any", router.handle(request("WOBBLE", "/wibbler")));
        assertBody("letters", router.handle(GET("/sesame/abc")));
        assertBody("numbers", router.handle(GET("/sesame/123")));
        assertEquals(NEXT_HANDLER, router.handle(GET("/sesame/---")));

        {
            WebResponse response = router.handle(GET("/assets/sample.txt"));
            assertEquals("text/plain", response.headers().getFirst("Content-Type"));
            assertBody("Sample text file", response);
        }

        assertEquals(NEXT_HANDLER, router.handle(GET("/assets/doesntexist")));
        assertEquals(NEXT_HANDLER, router.handle(GET("/doesntexist")));
    }

    private static void assertBody(String expected, WebResponse actual) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        actual.body().writeBody(buf);
        assertEquals(expected, buf.toString("UTF-8"));
    }

    private WebRequest request(String method, String uri) {
        URI parsed = URI.create(uri);
        MultiMap<String,String> headers = new LinkedTreeMultiMap<>(String.CASE_INSENSITIVE_ORDER);
        return new HttpRequest(method, parsed.getPath(), parsed.getQuery(), "http", null, null, "/", headers, null);
    }
    private WebRequest GET(String uri) {
        return request("GET", uri);
    }
    private WebRequest PUT(String uri) {
        return request("PUT", uri);
    }
    private WebRequest DELETE(String uri) {
        return request("DELETE", uri);
    }
    private WebRequest OPTIONS(String uri) {
        return request("OPTIONS", uri);
    }
    private WebRequest HEAD(String uri) {
        return request("HEAD", uri);
    }
    private WebRequest PATCH(String uri) {
        return request("PATCH", uri);
    }
    private WebRequest POST(String uri) {
        return request("POST", uri);
    }
}
