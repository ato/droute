package org.meshy.leanhttp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class InputStreamPayload implements HttpPayload {
    private final InputStream stream;

    public InputStreamPayload(InputStream stream) {
        this.stream = stream;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        byte[] buf = new byte[8192];
        while (true) {
            int n = stream.read(buf);
            if (n == -1) break;
            out.write(buf, 0, n);
        }
    }

    @Override
    public long length() {
        return -1;
    }
}
