package org.meshy.leanhttp;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URL;

import static org.meshy.leanhttp.HttpResponses.ok;
import static org.junit.Assert.assertEquals;

public class HttpServerTest {
    @Test
    public void testInteroperability() throws IOException, InterruptedException {
        HttpHandler handler = new HttpHandler() {
            @Override
            public HttpResponse handle(HttpRequest request) throws IOException {
                assertEquals("/hello", request.path());
                return ok("OK");
            }
        };
        try (HttpServer server = new HttpServer(handler, new ServerSocket(0, -1, InetAddress.getLoopbackAddress()))) {
            new Thread(server).start();

            URL url = new URL("http", server.localAddress().getHostString(), server.localAddress().getPort(), "/hello");
            try (BufferedReader rdr = new BufferedReader(new InputStreamReader(url.openStream()))) {
                assertEquals("OK", rdr.readLine());
            }
        }
    }
}
