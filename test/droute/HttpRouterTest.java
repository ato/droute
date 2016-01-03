package droute;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.*;

import static droute.HttpRequests.*;
import static droute.HttpResponses.ok;
import static org.junit.Assert.assertEquals;

public class HttpRouterTest {

    @Test
    public void testClose() throws Exception {
        // when two handlers are registered
        final List<String> closes = new ArrayList<>();
        HttpRouter router = new HttpRouter();
        router.on(HttpRequests.GET, "/apple", new CloseableHandler() {
            @Override
            public void close() throws Exception {
                closes.add("apple");
            }

            @Override
            public HttpResponse handle(HttpRequest request) throws IOException {
                return ok("apple");
            }
        });
        router.on(GET, "/banana", new CloseableHandler() {
            @Override
            public void close() throws Exception {
                closes.add("banana");
            }

            @Override
            public HttpResponse handle(HttpRequest request) throws IOException {
                return ok("banana");
            }
        });

        // on close
        router.close();

        assertEquals("it should have closed the handlers in reverser order",
                Arrays.asList("banana", "apple"), closes);

    }
    
    private HttpHandler handler(final HttpResponse response) {
        return new HttpHandler() {
            @Override
            public HttpResponse handle(HttpRequest request) throws IOException {
                return response;
            }
        };
    }

    @Test
    public void testHandle() throws IOException {
        long start = System.currentTimeMillis();
        HttpRouter router = new HttpRouter();
        router.on(GET, "/simple", handler(ok("simple-get")));
        router.on(OPTIONS, "/simple", handler(ok("simple-options")));
        router.on(PUT, "/simple", handler(ok("simple-put")));
        router.on(DELETE, "/simple", handler(ok("simple-delete")));
        router.on(HEAD, "/simple", handler(ok("simple-head")));
        router.on(OPTIONS, "/simple", handler(ok("simple-options")));
        router.on(PATCH, "/simple", handler(ok("simple-patch")));
        router.on(POST, "/simple", handler(ok("simple-post")));
        router.on("WIBBLE", "/wibbler", handler(ok("wibbled")));
        router.on(null, "/wibbler", handler(ok("any")));
        router.on(GET, "/triple/<a>/<b>/<c>", new HttpHandler() {
            @Override
            public HttpResponse handle(HttpRequest req) throws IOException {
                assertEquals("antelope", req.param("a").get());
                assertEquals("banana", req.param("b").get());
                assertEquals("cabbage", req.param("c").get());
                assertEquals("question=yes&quality=3", req.queryString());
                assertEquals("yes", req.query("question").get());
                assertEquals("3", req.query("quality").get());
                return ok("triple");
            }
        });
        router.on(GET, "/sesame/<letters:[a-z]+>", handler(ok("letters")));
        router.on(GET, "/sesame/<numbers:[0-9]+>", handler(ok("numbers")));
        router.resources("/assets", "droute/assets");

        assertBody("triple", router.handle(request(GET, "/triple/antelope/banana/cabbage?question=yes&quality=3")));
        assertBody("simple-get", router.handle(request(GET, "/simple")));
        assertBody("simple-options", router.handle(request(OPTIONS, "/simple")));
        assertBody("simple-put", router.handle(request(PUT, "/simple")));
        assertBody("simple-post", router.handle(request(POST, "/simple")));
        assertBody("simple-delete", router.handle(request(DELETE, "/simple")));
        assertBody("simple-head", router.handle(request(HEAD, "/simple")));
        assertBody("simple-patch", router.handle(request(PATCH, "/simple")));
        assertBody("wibbled", router.handle(request("WIbbLE", "/wibbler")));
        assertBody("any", router.handle(request("WOBBLE", "/wibbler")));
        assertBody("letters", router.handle(request(GET, "/sesame/abc")));
        assertBody("numbers", router.handle(request(GET, "/sesame/123")));
        assertEquals(null, router.handle(request(GET, "/sesame/---")));

        {
            HttpResponse response = router.handle(request(GET, "/assets/sample.txt"));
            assertEquals("text/plain", response.headers().get("Content-Type"));
            assertBody("Sample text file", response);
        }

        assertEquals(null, router.handle(request(GET, "/assets/doesntexist")));
        assertEquals(null, router.handle(request(GET, "/doesntexist")));
    }
    
    private static void assertBody(String expected, HttpResponse actual) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        actual.body().writeTo(buf);
        assertEquals(expected, buf.toString("UTF-8"));
    }

    private HttpRequest request(String method, String uri) {
        URI parsed = URI.create(uri);
        Map<String,String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        return new HttpRequest(method, parsed.getPath(), parsed.getQuery(), "http", "HTTP/1.1", null, null, "/", headers, null);
    }

}
