package droute;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static droute.HttpResponses.ok;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.junit.Assert.*;

public class HttpServerTest {
    String text = "GET /hello/world?1=2 HTTP/1.0\r\nHost: localhost:80\r\nBanana:fruit\r\nBANANA: vegetable\r\n\r\n";
    byte[] data = text.getBytes(US_ASCII);

    @Test
    public void testParser() {
        HttpRequestParser parser = new HttpRequestParser();

        assertFalse(parser.isError());
        assertFalse(parser.isFinished());

        // split the parsing into parts to make sure we're handling that properly
        parser.parse(data, 0, 7);
        parser.parse(data, 7, 0);
        parser.parse(data, 7, 1);
        parser.parse(data, 8, data.length - 8);

        assertFalse(parser.isError());
        assertTrue(parser.isFinished());
        assertEquals(2, parser.headers.size());
        assertEquals("/hello/world", parser.path);
        assertEquals("1=2", parser.query);
        assertEquals("localhost:80", parser.headers.get("Host"));
        assertEquals("fruit, vegetable", parser.headers.get("bAnAnA"));
        assertEquals("HTTP/1.0", parser.version);
    }

    @Test(expected = IllegalStateException.class)
    public void reusingAFinishedParserShouldFail() {
        HttpRequestParser parser = new HttpRequestParser();
        parser.parse(data, 0, data.length);
        parser.parse(data, 0, data.length);
    }

    @Test(expected = IllegalStateException.class)
    public void reusingAnErroredParserShouldFail() {
        HttpRequestParser parser = new HttpRequestParser();
        parser.parse("bogus\0".getBytes(StandardCharsets.ISO_8859_1), 0, 6);
        parser.parse(data, 0, data.length);
    }

    @Test
    public void testParseErrors() {
        String[] testStrings = new String[]{
                "GET\r\n\r\n",
                "GET <pizza>\r\n\r\n",
                "GET / HTTP/1.0\n\n",
                "GET /\r\n\n",
                "GET /\r\nall cookies\r\n\r\n",
                "GET /\r\nall cookies:mine\r\n\r\n",
                "GET /\r\nnull:null=\0null\r\n\r\n",
        };
        for (String testString : testStrings) {
            byte[] data = testString.getBytes(US_ASCII);

            HttpRequestParser parser = new HttpRequestParser();
            parser.parse(data, 0, data.length);
            assertTrue(parser.isError());
        }
    }

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
