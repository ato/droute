package droute;

import java.io.IOException;

import static droute.WebResponses.ok;

public class SampleWebapp {
    public static void main(String[] args) throws IOException {
        WebRouter router = new WebRouter();
        router.on("GET", "/", req -> ok("Hello world."));
        try (HttpServer server = new HttpServer(router, "localhost", 8080)) {
            server.serve();
        }
    }
}
