package droute;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

public final class HttpServer implements Runnable, Closeable {
    private final ServerSocket serverSocket;
    private final WebHandler handler;
    private final Set<Connection> connections = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public HttpServer(WebHandler handler, ServerSocket serverSocket) {
        this.handler = handler;
        this.serverSocket = serverSocket;
    }

    public HttpServer(WebHandler handler, int port) throws IOException {
        this(handler, new ServerSocket(port));
    }

    public HttpServer(WebHandler handler, String bindHost, int port) throws IOException {
        this(handler, new ServerSocket(port, -1, InetAddress.getByName(bindHost)));
    }

    /**
     * Accepts incoming connections in a loop until close() is called.
     *
     * @throws IOException if an IO error occured when accepting connections
     */
    void serve() throws IOException {
        while (!serverSocket.isClosed()) {
            Socket socket;
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                if (e.getMessage().equals("Socket closed")) {
                    break; // regular shutdown
                } else {
                    throw e;
                }
            }

            Connection connection = new Connection(socket);
            connections.add(connection);
            Thread thread = new Thread(connection);
            thread.setName(getClass().getName() + " connection from " + socket.getRemoteSocketAddress());
            thread.setDaemon(true);
            thread.start();
        }
    }

    /**
     * Returns the local address of the socket this server is listening on.
     */
    InetSocketAddress localAddress() {
        return (InetSocketAddress) serverSocket.getLocalSocketAddress();
    }

    @Override
    public void run() {
        try {
            serve();
        } catch (IOException e) {
            e.printStackTrace();
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Closes the listening socket and all open connections.
     *
     * @throws IOException if a socket could not be closed
     */
    public void close() throws IOException {
        serverSocket.close();

        for (Connection connection : connections) {
            connection.close();
        }
    }

    class Connection implements Runnable, Closeable {
        final Socket socket;
        final HttpRequestParser parser = new HttpRequestParser();
        final InputStream in;
        final byte[] buffer = new byte[8192];
        int bufPos = 0, bufEnd = 0;

        Connection(Socket socket) throws IOException {
            this.socket = socket;
            in = socket.getInputStream();
        }

        public void run() {
            try {
                while (fillBuffer()) {
                    bufPos += parser.parse(buffer, bufPos, bufEnd - bufPos);

                    if (parser.isError()) {
                        sendResponse(WebResponses.response(400, "Bad Request"));
                        break;
                    } else if (parser.isFinished()) {
                        ByteArrayInputStream bufStream = new ByteArrayInputStream(buffer, bufPos, bufEnd - bufPos);

                        WebRequest request = createRequest(parser, new SequenceInputStream(bufStream, in));
                        WebResponse response = handle(request);

                        sendResponse(response);

                        consumeRemainingRequestBody(request);

                        bufPos = bufEnd - bufStream.available();
                        parser.reset();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                connections.remove(this);
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void consumeRemainingRequestBody(WebRequest request) throws IOException {
            request.bodyStream().skip(Long.MAX_VALUE);
        }

        private WebResponse handle(WebRequest request) throws IOException {
            WebResponse response;
            try {
                response = handler.handle(request);
            } catch (WebResponseException e) {
                response = e.response;
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
                response = WebResponses.response(500, "Uncaught Exception");
            }

            if (response == null) {
                response = WebResponses.response(500, "Handler returned null");
            }
            return response;
        }

        private boolean fillBuffer() throws IOException {
            if (socket.isClosed()) {
                return false;
            }
            if (bufPos >= bufEnd) {
                bufEnd = in.read(buffer);
                if (bufEnd == -1) {
                    return true;
                }
            }
            return false;
        }

        private WebRequest createRequest(HttpRequestParser parser, InputStream in) {
            String contentLengthField = parser.fields.getFirst("Content-Length");
            long contentLength = contentLengthField == null ? 0 : Long.parseLong(contentLengthField);
            BoundedInputStream bodyStream = new BoundedInputStream(in, contentLength);

            int i = parser.target.indexOf('?');
            String path, query;
            if (i == -1) {
                path = parser.target;
                query = null;
            } else {
                path = parser.target.substring(0, i);
                query = parser.target.substring(i + 1);
            }

            return new HttpRequest(parser.method, path, query, "http",
                    (InetSocketAddress) socket.getRemoteSocketAddress(),
                    (InetSocketAddress) socket.getLocalSocketAddress(), "/", parser.fields, bodyStream);
        }

        void sendResponse(WebResponse response) throws IOException {
            OutputStream out = new BufferedOutputStream(socket.getOutputStream());

            populateDefaultResponseHeaders(response);
            sendResponseHeader(response, out);
            sendResponseBody(response, out);

            out.flush();
            out.close();
        }

        private void populateDefaultResponseHeaders(WebResponse response) {
            MultiMap<String, String> headers = response.headers();

            if (!headers.containsKey("Date")) {
                headers.put("Date", WebResponses.formatHttpDate(System.currentTimeMillis()));
            }

            headers.put("Connection", "close");
        }

        void sendResponseHeader(WebResponse response, OutputStream out) throws IOException {
            StringBuilder header = new StringBuilder();

            header.append("HTTP/1.1 ");
            header.append(Integer.toString(validateStatusCode(response.status())));
            header.append(" ");
            header.append(WebStatus.reasonPhrase(response.status()));
            header.append("\r\n");

            for (Map.Entry<String, String> entry : response.headers().entries()) {
                header.append(validateFieldName(entry.getKey()));
                header.append(": ");
                header.append(validateFieldValue(entry.getValue()));
                header.append("\r\n");
            }

            header.append("\r\n");

            out.write(header.toString().getBytes(ISO_8859_1));
        }


        void sendResponseBody(WebResponse response, OutputStream out) throws IOException {
            response.body().writeBody(out);
        }

        public void close() throws IOException {
            this.socket.close();
        }
    }

    static final BitSet TOKEN_CHARS = charBitSet("!#$%&'*+-.0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz~^`|");

    static BitSet charBitSet(String chars) {
        BitSet bitset = new BitSet();
        for (int i = 0; i < chars.length(); i++) {
            bitset.set(chars.charAt(i));
        }
        return bitset;
    }

    static int validateStatusCode(int code) {
        if (code < 100 || code > 999) {
            throw new IllegalArgumentException("Illegal HTTP status code: " + code + " (must be 3 digits)");
        }
        return code;
    }

    static String validateFieldName(String name) {
        for (int i = 0; i < name.length(); i++) {
            if (!TOKEN_CHARS.get(name.charAt(i))) {
                throw new IllegalArgumentException("Illegal character in HTTP response header field name: " + name.charAt(i));
            }
        }
        return name;
    }

    static String validateFieldValue(String value) {
        for (int i = 0; i < value.length(); i++) {
            int c = value.charAt(i) & 0xFFFF;
            if (c < 32 || c > 255) {
                throw new IllegalArgumentException("Illegal character in HTTP response header field value: " + c);
            }
        }
        return value;
    }
}
