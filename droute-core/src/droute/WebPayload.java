package droute;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public interface WebPayload {
    void writeBody(OutputStream out) throws IOException;

    static WebPayload wrap(String string) {
        return out -> {
            OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
            writer.write(string);
            writer.flush();
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
