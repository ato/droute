package droute;

public class HttpResponseException extends RuntimeException {
    final HttpResponse response;

    public HttpResponseException(HttpResponse response) {
        this.response = response;
    }
}
