package droute;

import java.io.IOException;
import java.io.OutputStream;

public class ByteArrayPayload implements HttpPayload {
    final byte[] bytes;

    public ByteArrayPayload(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        out.write(bytes);
    }

    @Override
    public long length() {
        return bytes.length;
    }
}
