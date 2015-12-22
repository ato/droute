package droute;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static droute.WebRequests.HTTP10;

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
    public void serve() throws IOException {
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
    public InetSocketAddress localAddress() {
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
        final InetSocketAddress remoteAddress;
        final InetSocketAddress localAddress;
        final HttpRequestParser parser = new HttpRequestParser();
        final InputStream in;
        final OutputStream out;
        final byte[] buffer = new byte[8192];
        int bufPos = 0, bufEnd = 0;

        Connection(Socket socket) throws IOException {
            this.socket = socket;
            in = socket.getInputStream();
            out = new BufferedOutputStream(socket.getOutputStream());
            localAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
            remoteAddress = (InetSocketAddress) socket.getLocalSocketAddress();
        }

        public void run() {
            try {
                while (fillBuffer()) {
                    bufPos += parser.parse(buffer, bufPos, bufEnd - bufPos);

                    if (parser.isError()) {
                        System.err.println("parse error");
                        sendResponse(null, WebResponses.response(400, "Bad Request"));
                        break;
                    } else if (parser.isFinished()) {
                        ByteArrayInputStream bufStream = new ByteArrayInputStream(buffer, bufPos, bufEnd - bufPos);

                        WebRequest request = createRequest(parser, new SequenceInputStream(bufStream, in));
                        WebResponse response = handle(request);

                        consumeRemainingRequestBody(request);
                        bufPos = bufEnd - bufStream.available();

                        sendResponse(request, response);

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
            if (bufPos >= bufEnd) {
                bufEnd = in.read(buffer);
                bufPos = 0;
            }
            return bufEnd != -1 && !socket.isClosed();
        }

        private WebRequest createRequest(HttpRequestParser parser, InputStream in) {
            String contentLengthField = parser.fields.getFirst("Content-Length");
            long contentLength = contentLengthField == null ? 0 : Long.parseLong(contentLengthField);
            BoundedInputStream bodyStream = new BoundedInputStream(in, contentLength);
            return new HttpRequest(parser.method, parser.path, parser.query, "http",
                    parser.version != null ? parser.version.toUpperCase(Locale.US) : HTTP10,
                    remoteAddress, localAddress,
                    "/", parser.fields, bodyStream);
        }

        void sendResponse(WebRequest request, WebResponse response) throws IOException {
            if (!response.headers().containsKey("Date")) {
                response.setHeader("Date", WebResponses.formatHttpDate(System.currentTimeMillis()));
            }

            long payloadLength = response.body().length();
            if (payloadLength >= 0) {
                response.setHeader("Content-Length", Long.toString(response.body().length()));
            }

            if (request.protocol().equals(HTTP10)) {
                if (payloadLength >= 0 && request.header("Connection").orElse("close").equalsIgnoreCase("keep-alive")) {
                    response.setHeader("Connection", "keep-alive");
                    sendResponseWithKnownLength(response, payloadLength);
                } else {
                    sendResponseAndClose(response);
                }
            } else { // HTTP/1.1
                if (request.header("Connection").orElse("keep-alive").equalsIgnoreCase("close")) {
                    response.setHeader("Connection", "close");
                    sendResponseAndClose(response);
                } else if (payloadLength >= 0) {
                    sendResponseWithKnownLength(response, payloadLength);
                } else {
                    sendResponseWithChunkedEncoding(response);
                }
            }
        }

        private void sendResponseAndClose(WebResponse response) throws IOException {
            out.write(response.serializeHeader());
            response.body().writeTo(out);
            out.flush();
            teardown();
        }

        private void sendResponseWithKnownLength(WebResponse response, long payloadLength) throws IOException {
            out.write(response.serializeHeader());
            response.body().writeTo(out);
            out.flush();
        }

        private void sendResponseWithChunkedEncoding(WebResponse response) throws IOException {
            response.setHeader("Transfer-Encoding", "chunked");
            out.write(response.serializeHeader());
            try (ChunkedOutputStream chunkedOut = new ChunkedOutputStream(out)) {
                response.body().writeTo(chunkedOut);
            }
            out.flush();
        }

        /**
         * Shut our end of the connection then discard all input until the client closes.
         *
         * @see <a href="https://tools.ietf.org/html/rfc7230#section-6.6">RFC7230 section 6.6</a>
         */
        private void teardown() {
            try {
                socket.shutdownOutput();
            } catch (IOException e) {
                // no worries
            }

            try {
                while (!socket.isInputShutdown()) {
                    int n = in.read(buffer, 0, buffer.length);
                    if (n < 0) {
                        break;
                    }
                }
            } catch (IOException e) {
                // no worries
            }

            try {
                socket.close();
            } catch (IOException e) {
                // no worries
            }
        }

        /**
         * Immediately close the connection. Potentially interrupting an in-progress read or write.
         */
        public void close() throws IOException {
            this.socket.close();
        }
    }
}
