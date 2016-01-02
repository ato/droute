package droute;

import java.io.IOException;

import static droute.HttpResponses.ok;

public class SampleWebapp {
    public static void main(String[] args) throws IOException {
        final HttpRouter router = new HttpRouter();
        router.on("GET", "/", new HttpHandler() {
            @Override
            public HttpResponse handle(HttpRequest request) throws IOException {
                return ok("hello world.");
            }});
        try (HttpServer server = new HttpServer(router, "localhost", 8080)) {
            server.serve();
        }
    }
}
