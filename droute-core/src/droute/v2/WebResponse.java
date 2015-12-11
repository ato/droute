package droute.v2;

import java.util.Arrays;
import java.util.Map;

public final class WebResponse {
    private int status = 200;
    private WebResponseBody body;
    private final MultiMap<String,String> headers = new LinkedTreeMultiMap<>(String.CASE_INSENSITIVE_ORDER);

    public int status() {
        return status;
    }

    public Iterable<Map.Entry<String,String>> headers() {
        return headers.entries();
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

    public void setBody(WebResponseBody body) {
        this.body = body;
    }

    public WebResponseBody body() {
        return body;
    }


}
