package droute;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface WebPayload {
    /**
     * Writes this payload to an OuputStream.
     */
    void writeTo(OutputStream out) throws IOException;

    /**
     * Returns the length of this payload in bytes.  If the length cannot be determined until the
     * payload is actually written to a stream return -1.
     *
     * The default implementation returns -1.
     */
    default long length() {
        return -1;
    }

    static WebPayload wrap(byte[] bytes) {
        return new WebPayload() {
            @Override
            public void writeTo(OutputStream out) throws IOException {
                out.write(bytes);
            }

            @Override
            public long length() {
                return bytes.length;
            }
        };
    }

    static WebPayload wrap(InputStream stream) {
        return out -> {
            byte[] buf = new byte[8192];
            while (true) {
                int n = stream.read(buf);
                if (n == -1) break;
                out.write(buf, 0, n);
            }
        };
    }
}
