package droute;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import static java.nio.charset.StandardCharsets.US_ASCII;

public final class WebServer {
    private final ServerSocket serverSocket;

    public WebServer(WebHandler handler, String bindHost, int port) throws IOException {
        this.serverSocket = new ServerSocket(port, -1, InetAddress.getByName(bindHost));
    }

    void run() throws IOException {
        while (!serverSocket.isClosed()) {
            Socket socket = serverSocket.accept();
            Connection connnection = new Connection(socket);
            Thread thread = new Thread(connnection);
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

        public void run() {
            try {
                HttpRequestParser parser = new HttpRequestParser();
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

        void respond(WebResponse response) throws IOException {
            OutputStream out = new BufferedOutputStream(socket.getOutputStream());

            writeResponseHeader(response, out);
            writeResponseBody(response, out);

            out.flush();
            out.close();
        }

        static void writeResponseHeader(WebResponse response, OutputStream out) throws IOException {
            Writer w = new OutputStreamWriter(out, US_ASCII);

            w.write("HTTP/1.1 ");
            w.write(Integer.toString(response.status()));
            w.write(" ");
            w.write(WebStatus.reasonPhrase(response.status()));

            for (Map.Entry<String, String> entry : response.headers().entries()) {
                w.write(entry.getKey());
                w.write(": ");
                w.write(entry.getValue());
                w.write("\r\n");
            }

            w.write("\r\n");
        }

        static void writeResponseBody(WebResponse response, OutputStream out) throws IOException {
            response.body().writeBody(out);
        }

    }

}
