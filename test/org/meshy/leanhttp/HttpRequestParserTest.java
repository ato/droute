package org.meshy.leanhttp;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HttpRequestParserTest {
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
}