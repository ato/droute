package droute;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import static droute.HttpResponses.ok;

public class SampleWebapp {
    public static void main(String[] args) throws IOException {
        final HttpRouter router = new HttpRouter();
        router.on("GET", "/", new HttpHandler() {
            @Override
            public HttpResponse handle(HttpRequest request) throws IOException {
                return ok("hello, " + request.query("name").orElse("world") + ".");
            }});
        try (HttpServer server = new HttpServer(router, new ServerSocket(8080, -1, InetAddress.getLoopbackAddress()))) {
            server.serve();
        }
    }
}
