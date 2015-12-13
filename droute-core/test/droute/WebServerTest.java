package droute;

import org.junit.Test;

import java.util.Arrays;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.junit.Assert.*;

public class WebServerTest {

    @Test
    public void testParser() {
        WebServer.Parser parser = new WebServer.Parser();
        String text = "GET /hello/world?1=2 HTTP/1.0\r\nHost: localhost:80\r\nBanana:fruit\r\nBANANA: vegetable\r\n\r\n";
        byte[] data = text.getBytes(US_ASCII);

        assertFalse(parser.isError());
        assertFalse(parser.isFinished());

        parser.parse(data, 0, data.length);

        assertFalse(parser.isError());
        assertTrue(parser.isFinished());
        assertEquals(3, parser.fields.size());
        assertEquals("/hello/world?1=2", parser.target);
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
        WebServer.Parser parser = new WebServer.Parser();
        for (String testString : testStrings) {
            byte[] data = testString.getBytes(US_ASCII);

            parser.reset();
            parser.parse(data, 0, data.length);

            assertTrue(parser.isError());
        }
    }

}
