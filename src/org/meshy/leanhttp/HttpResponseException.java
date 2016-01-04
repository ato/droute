package org.meshy.leanhttp;

public class HttpResponseException extends RuntimeException {
    final HttpResponse response;

    public HttpResponseException(HttpResponse response) {
        this.response = response;
    }
}
