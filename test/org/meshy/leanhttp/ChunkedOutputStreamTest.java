package org.meshy.leanhttp;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class ChunkedOutputStreamTest {

    @Test
    public void test() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ChunkedOutputStream cos = new ChunkedOutputStream(baos);
        cos.write('H');
        cos.write(new byte[0]);
        cos.write("123456789abcdef".getBytes(StandardCharsets.UTF_8));
        cos.close();
        String data = baos.toString("UTF-8");
        assertEquals("1\r\nH\r\nf\r\n123456789abcdef\r\n0\r\n\r\n", data);
    }

    @Test(expected = ClosedChannelException.class)
    public void testCloseWrite1() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ChunkedOutputStream cos = new ChunkedOutputStream(baos);
        cos.close();
        cos.write('H');
    }

    @Test(expected = ClosedChannelException.class)
    public void testCloseWrite2() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ChunkedOutputStream cos = new ChunkedOutputStream(baos);
        cos.close();
        cos.write("hello".getBytes(StandardCharsets.UTF_8));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testOutOfBounds() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ChunkedOutputStream cos = new ChunkedOutputStream(baos);
        cos.write("hello".getBytes(StandardCharsets.UTF_8), 0, 6);
    }
}