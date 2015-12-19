package droute;

public class WebResponseException extends RuntimeException {
    final WebResponse response;

    public WebResponseException(WebResponse response) {
        this.response = response;
    }
}
