package org.meshy.leanhttp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class LeanHttp {
    String host = "127.0.0.1";
    int port = 8080;
    HttpRouter router = new HttpRouter();

    public void host(String host) {
        this.host = host;
    }

    public void port(int port) {
        this.port = port;
    }

    public void get(String path, HttpHandler handler) {
        router.on("GET", path, handler);
    }

    public void post(String path, HttpHandler handler) {
        router.on("POST", path, handler);
    }

    public void delete(String path, HttpHandler handler) {
        router.on("DELETE", path, handler);
    }

    public void put(String path, HttpHandler handler) {
        router.on("PUT", path, handler);
    }

    public void serve() throws IOException {
        HttpServer server = new HttpServer(router, new ServerSocket(port, -1, InetAddress.getByName(host)));
        server.serve();
    }
}
