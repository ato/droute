package droute;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.ClosedChannelException;

import static java.nio.charset.StandardCharsets.US_ASCII;

/**
 * Writes output via HTTP chunked transfer encoding.
 * <p/>
 * Note: Closing the ChunkedOutputStream will write a terminating chunk and then flush but does not close the
 * underlying stream.
 */
class ChunkedOutputStream extends FilterOutputStream {
    private static final byte[] TERMINATING_CHUNK = "0\r\n\r\n".getBytes(US_ASCII);
    private static final byte[] SINGLE_BYTE_HEADER = "1\r\n".getBytes(US_ASCII);
    private static final byte[] CRLF = "\r\n".getBytes(US_ASCII);

    private boolean open = true;

    public ChunkedOutputStream(OutputStream outputStream) {
        super(outputStream);
    }

    @Override
    public void write(int i) throws IOException {
        if (!open) {
            throw new ClosedChannelException();
        }

        out.write(SINGLE_BYTE_HEADER);
        out.write(i);
        out.write(CRLF);
    }

    @Override
    public void write(byte[] bytes, int off, int len) throws IOException {
        if (len == 0) {
            return;
        } else if (off < 0 || len < 0 || off > bytes.length || off + len > bytes.length || off + len < 0) {
            throw new IndexOutOfBoundsException();
        } else if (!open) {
            throw new ClosedChannelException();
        }

        String header = Integer.toHexString(len) + "\r\n";
        out.write(header.getBytes(US_ASCII));
        out.write(bytes, off, len);
        out.write(CRLF);
    }

    @Override
    public void close() throws IOException {
        if (open) {
            open = false;
            out.write(TERMINATING_CHUNK);
            out.flush();
        }
    }
}
