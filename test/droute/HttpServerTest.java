package droute;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;

import static droute.WebResponses.ok;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.junit.Assert.*;

public class HttpServerTest {

    @Test
    public void testParser() {
        HttpRequestParser parser = new HttpRequestParser();
        String text = "GET /hello/world?1=2 HTTP/1.0\r\nHost: localhost:80\r\nBanana:fruit\r\nBANANA: vegetable\r\n\r\n";
        byte[] data = text.getBytes(US_ASCII);

        assertFalse(parser.isError());
        assertFalse(parser.isFinished());

        // split the parsing into parts to make sure we're handling that properly
        parser.parse(data, 0, 7);
        parser.parse(data, 7, 0);
        parser.parse(data, 7, 1);
        parser.parse(data, 8, data.length - 8);

        assertFalse(parser.isError());
        assertTrue(parser.isFinished());
        assertEquals(3, parser.fields.size());
        assertEquals("/hello/world", parser.path);
        assertEquals("1=2", parser.query);
        assertEquals("localhost:80", parser.fields.getFirst("Host"));
        assertEquals(Arrays.asList("fruit", "vegetable"), parser.fields.get("bAnAnA"));
        assertEquals("HTTP/1.0", parser.version);
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
        HttpRequestParser parser = new HttpRequestParser();
        for (String testString : testStrings) {
            byte[] data = testString.getBytes(US_ASCII);

            parser.reset();
            parser.parse(data, 0, data.length);

            assertTrue(parser.isError());
        }
    }

    @Test
    public void testInteroperability() throws IOException, InterruptedException {
        WebHandler handler = req -> {
            assertEquals("/hello", req.path());
            return ok("OK");
        };
        try (HttpServer server = new HttpServer(handler, "localhost", 0)) {
            new Thread(server).start();

            URL url = new URL("http", server.localAddress().getHostString(), server.localAddress().getPort(), "/hello");
            try (BufferedReader rdr = new BufferedReader(new InputStreamReader(url.openStream()))) {
                assertEquals("OK", rdr.readLine());
            }
        }
    }
}
