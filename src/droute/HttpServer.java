package droute;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static droute.WebRequests.HTTP10;
import static droute.WebRequests.HTTP11;

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
            long payloadLength = response.body().length();
            boolean knownLength = payloadLength >= 0;
            boolean closing = clientWantsUsToClose(request);
            boolean chunking = !closing && !knownLength && request.protocol().equals(HTTP11);

            /*
             * if we can't use chunked encoding and also don't know the payload length
             * we have no choice but to indicate end of payload by closing
             */
            if (!chunking && !knownLength) {
                closing = true;
            }

            if (!response.headers().containsKey("Date")) {
                response.setHeader("Date", WebResponses.formatHttpDate(System.currentTimeMillis()));
            }

            if (closing) {
                response.setHeader("Connection", "close");
            } else if (request.protocol().equals(HTTP10)) {
                response.setHeader("Connection", "keep-alive");
            }

            if (chunking) {
                response.setHeader("Transfer-Encoding", "chunked");
            }

            if (knownLength) {
                response.setHeader("Content-Length", Long.toString(payloadLength));
            }

            out.write(response.serializeHeader());

            if (chunking) {
                try (ChunkedOutputStream chunkedOut = new ChunkedOutputStream(out)) {
                    response.body().writeTo(chunkedOut);
                }
            } else {
                response.body().writeTo(out);
            }

            out.flush();

            if (closing) {
                teardown();
            }
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

    /**
     * Determines whether we should close the connection after responding to this request.
     *
     * @return true if the connection should be closed
     * @see <a href="https://tools.ietf.org/html/rfc7230#section-6.3">RFC7230 section 6.3</a>
     */
    static private boolean clientWantsUsToClose(WebRequest request) {
        if (request == null) {
            return true;
        }
        Optional<String> connection = request.header("Connection");
        if (connection.isPresent() && connection.get().equalsIgnoreCase("close")) {
            return true;
        } else if (request.protocol().equals(HTTP11)) {
            return false;
        } else if (request.protocol().equals(HTTP10) &&
                connection.isPresent() && connection.get().equalsIgnoreCase("keep-alive")) {
            return false;
        } else {
            return true;
        }
    }
}
