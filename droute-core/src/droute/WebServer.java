package droute;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public final class WebServer {
    private final ServerSocket serverSocket;

    public WebServer(WebHandler handler, String bindHost, int port) throws IOException {
        this.serverSocket = new ServerSocket(port, -1, InetAddress.getByName(bindHost));
    }

    void run() throws IOException {
        while (!serverSocket.isClosed()) {
            Socket socket = serverSocket.accept();
            Connection connnection = new COnnection(socket);
            Thread thread = new Thread(this::serve);
            thread.setName("WebServer connection from " + socket.getRemoteSocketAddress());
            thread.setDaemon(true);
            thread.start();
        }
    }

    static class Connection implements Runnable {
        final Socket socket;

        Connection(Socket socket) {
            this.socket = socket;
        }

        void run() {
            try {
                Parser parser = new Parser();
                byte[] data = new byte[8192];
                InputStream in = socket.getInputStream();
                while (true) {
                    int n = in.read(data);
                    if (n == -1) {
                        break;
                    }
                    int off = parser.parse(data, 0, n);
                    if (parser.isError()) {
                        respond(WebResponses.response(400, "Bad Request"));
                    } else if (parser.isFinished()) {
                        long contentLength = parser.contentLength();
                        String transferEncoding = parser.fields.getFirst("Transfer-Encoding");

                        // dispatch
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        void respond(WebResponse response) {
            socket.getOutputStream();
        }
    }

    static class Parser {
        private static final byte[] SYMBOLS = buildSymbolTable();
        private static final byte[] TRANSITIONS = {
                /* v   tok   url   com   ':'   ' '   '\r' '\n'  non-printable */
                0x7e, 0x10, 0x7e, 0x10, 0x7e, 0x21, 0x7e, 0x7e, 0x7e, /* state 0: method */
                0x7e, 0x7e, 0x11, 0x11, 0x11, 0x32, 0x33, 0x7e, 0x7e, /* state 1: request-target */
                0x7e, 0x7e, 0x12, 0x12, 0x12, 0x7e, 0x43, 0x7e, 0x7e, /* state 2: http-version */
                0x7e, 0x7e, 0x7e, 0x7e, 0x7e, 0x7e, 0x7e, 0x04, 0x7e, /* state 3: request-line newline */
                0x7e, 0x15, 0x7e, 0x15, 0x7e, 0x7e, 0x09, 0x7e, 0x7e, /* state 4: start of field-name */
                0x7e, 0x15, 0x7e, 0x15, 0x56, 0x7e, 0x7e, 0x7e, 0x7e, /* state 5: field-name */
                0x17, 0x17, 0x17, 0x17, 0x17, 0x06, 0x68, 0x7e, 0x7e, /* state 6: field-value leading spaces */
                0x17, 0x17, 0x17, 0x17, 0x17, 0x17, 0x68, 0x7e, 0x7e, /* state 7: field-value */
                0x7e, 0x7e, 0x7e, 0x7e, 0x7e, 0x7e, 0x7e, 0x04, 0x7e, /* state 8: field newline */
                0x7e, 0x7e, 0x7e, 0x7e, 0x7e, 0x7e, 0x7e, 0x7f, 0x7e, /* state 9: final newline */
                /*
                 * entry form 0xAS: A - action, S - next state
                 * final states: e - error, f - finished
                 */
        };

        int state;
        StringBuilder buffer;
        String method;
        String target;
        String version;
        String fieldName;
        LinkedTreeMultiMap<String,String> fields;

        Parser() {
            reset();
        }

        public void reset() {
            state = 0;
            buffer = new StringBuilder();
            method = null;
            target = null;
            version = null;
            fieldName = null;
            fields = new LinkedTreeMultiMap<>(String.CASE_INSENSITIVE_ORDER);
        }

        public int parse(byte[] data, int offset, int length) {
            int end = offset + length;
            for (int i = 0; i < length; i++) {
                int b = data[i] & 0xff;
                int symbol = SYMBOLS[b];
                int action = TRANSITIONS[state * 9 + symbol];

                switch (action >> 4) {
                    case 0: // no action
                        break;
                    case 1: // push current byte
                        buffer.append((char)b); // iso-8859-1 -> utf-16
                        break;
                    case 2: // end of method
                        method = buffer.toString();
                        buffer.setLength(0);
                        break;
                    case 3: // end of request-target
                        target = buffer.toString();
                        buffer.setLength(0);
                        break;
                    case 4: // end of http-version
                        version = buffer.toString();
                        buffer.setLength(0);
                        break;
                    case 5: // end of field-name
                        fieldName = buffer.toString();
                        buffer.setLength(0);
                        break;
                    case 6: // end of field-value
                        fields.put(fieldName, buffer.toString());
                        fieldName = null;
                        buffer.setLength(0);
                        break;
                    case 7: // halt parsing
                        state = action & 0xf;
                        return i - offset;
                    default:
                        throw new IllegalStateException();
                }

                state = action & 0xf;
            }
            return length;
        }

        public boolean isError() {
            return state == 0xe;
        }

        public boolean isFinished() {
            return state == 0xf;
        }

        private static byte[] buildSymbolTable() {
            byte[] symbols = new byte[256];
            fill(symbols, "^`|", 1); // token only
            fill(symbols, "(),/:;=?@[]", 2); // url only
            fill(symbols, "!#$%&'*+-.0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz~", 3); // common
            for (int i = 0; i < 32; i++) {
                symbols[i] = 8; // non-printable
            }
            symbols[127] = 8; // DEL (non-printable)
            symbols[':'] = 4;
            symbols[' '] = 5;
            symbols['\r'] = 6;
            symbols['\n'] = 7;
            symbols['\t'] = 0; // tab is allowed in field values
            return symbols;
        }

        private static void fill(byte[] array, String chars, int value) {
            for (int i = 0; i < chars.length(); i++) {
                array[chars.charAt(i)] = (byte) value;
            }
        }

        public long contentLength() {
            String value = fields.getFirst("Content-Length");
            return value == null ? 0 : Long.parseLong(value);
        }
    }
}
