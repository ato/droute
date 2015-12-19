package droute;

import java.util.Arrays;

public final class WebResponse {
    private int status = 200;
    private WebPayload body;
    private final MultiMap<String,String> headers = new LinkedTreeMultiMap<>(String.CASE_INSENSITIVE_ORDER);

    @Override
    public String toString() {
        return "WebResponse{" +
                "status=" + status +
                ", body=" + body +
                ", headers=" + headers +
                '}';
    }

    public int status() {
        return status;
    }

    public MultiMap<String,String> headers() {
        return headers;
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void setHeader(String key, String value) {
        headers.replaceValues(key, Arrays.asList(value));
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setBody(WebPayload body) {
        this.body = body;
    }

    public WebPayload body() {
        return body;
    }


}
