package org.meshy.leanhttp;

import java.io.IOException;
import java.net.ServerSocket;

import static org.meshy.leanhttp.HttpResponses.*;

public class SampleWebapp {
    public static void main(String[] args) throws IOException {
        HttpRouter routes = new HttpRouter();
        routes.onGet("/", req -> ok("hello, " + req.query("name")));
        new HttpServer(routes, new ServerSocket(8080)).serve();
    }
}
