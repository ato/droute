package droute;

import java.io.IOException;
import java.io.OutputStream;

public interface HttpPayload {
    /**
     * Writes this payload to an OuputStream.
     */
    void writeTo(OutputStream out) throws IOException;

    /**
     * Returns the length of this payload in bytes.  If the length cannot be determined until the
     * payload is actually written to a stream return -1.
     */
    long length();
}
