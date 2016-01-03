package droute;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

public class BoundedInputStreamTest {

    @Test
    public void test() throws IOException {
        InputStream bais = new ByteArrayInputStream("hello world".getBytes(UTF_8));
        InputStream bis = new BoundedInputStream(bais, 5);
        byte[] buf = new byte[128];

        int c = bis.read();
        assertEquals('h', c);

        int nread = bis.read(buf);
        assertEquals("ello", new String(buf, 0, nread, UTF_8));

        assertEquals(-1, bis.read());
        assertEquals(-1, bis.read(buf));

        bis.close();
    }
}