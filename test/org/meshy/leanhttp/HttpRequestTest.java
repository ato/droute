package org.meshy.leanhttp;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class HttpRequestTest {

    private static Map<String,String> headers;
    private static HttpRequest request;

    @BeforeClass
    public static void setUp() throws Exception {
        headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Cookie", "weather=rain; __TINY_MOON=1");
        headers.put("Host", "totally bogus, whoopsie");
        request = new HttpRequest("GET", "/path", "one&foo=bar&&baz=&", "http", "HTTP/1.1",
                InetSocketAddress.createUnresolved("127.0.0.1", 1234), InetSocketAddress.createUnresolved("127.0.0.1", 80),
                "/app", headers, new ByteArrayInputStream("number=1&number=2&mouse=on&wicket[]&&soccer".getBytes(StandardCharsets.UTF_8)));


    }

    @Test
    public void test() {
        assertEquals("bar", request.query("foo").get());
        assertEquals(3, request.queryMap().size());
        assertEquals("", request.query("one").get());
        assertEquals("", request.query("baz").get());

        assertEquals(Arrays.asList("1", "2"), request.form("number").asList());

        assertEquals(1, request.cookie("__TINY_MOON").asInt());
        assertEquals("rain", request.cookies().get("weather"));

        assertEquals("http://127.0.0.1/path?one&foo=bar&&baz=&", request.uri().toString());
    }
}