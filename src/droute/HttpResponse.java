package droute;

import java.util.*;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

public final class HttpResponse {
    private static final BitSet HEADER_NAME_CHARS = charBitSet("!#$%&'*+-.0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz~^`|");
    private final Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final List<String> cookies = new ArrayList<>();
    private int status = 200;
    private HttpPayload body;

    public HttpResponse() {
    }

    /**
     * Copy constructor.
     */
    public HttpResponse(HttpResponse response) {
        this.status = response.status;
        this.body = response.body;
        headers.putAll(response.headers);
        cookies.addAll(response.cookies);
    }

    private static BitSet charBitSet(String chars) {
        BitSet bitset = new BitSet();
        for (int i = 0; i < chars.length(); i++) {
            bitset.set(chars.charAt(i));
        }
        return bitset;
    }

    private static void validateHeaderName(String name) {
        for (int i = 0; i < name.length(); i++) {
            if (!HEADER_NAME_CHARS.get(name.charAt(i))) {
                throw new IllegalArgumentException("Illegal character in HTTP response header field name: " + name.charAt(i));
            }
        }
    }

    private static void validateHeaderValue(String value) {
        for (int i = 0; i < value.length(); i++) {
            int c = value.charAt(i) & 0xFFFF;
            if ((c != '\t' && c < 32) || c > 255) {
                throw new IllegalArgumentException("Illegal character in HTTP response header field value: " + c);
            }
        }
    }

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

    public Map<String, String> headers() {
        return Collections.unmodifiableMap(headers);
    }

    public void addHeader(String key, String value) {
        validateHeaderName(key);
        validateHeaderValue(value);
        if (key.equalsIgnoreCase("Set-Cookie")) {
            cookies.add(value);
        } else {
            String prev = headers.get(key);
            headers.put(key, prev != null ? prev + ", " + value : value);
        }
    }

    public void setHeader(String key, String value) {
        validateHeaderName(key);
        validateHeaderValue(value);
        if (key.equalsIgnoreCase("Set-Cookie")) {
            cookies.clear();
            cookies.add(value);
        } else {
            headers.put(key, value);
        }
    }

    public void setStatus(int status) {
        if (status < 100 || status > 999) {
            throw new IllegalArgumentException("Illegal HTTP status code: " + status + " (must be 3 digits)");
        }
        this.status = status;
    }

    public void setBody(HttpPayload body) {
        this.body = body;
    }

    public HttpPayload body() {
        return body;
    }

    byte[] serializeHeader() {
        StringBuilder header = new StringBuilder();

        header.append("HTTP/1.1 ");
        header.append(Integer.toString(status));
        header.append(" ");
        header.append(HttpStatus.reasonPhrase(status));
        header.append("\r\n");

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            header.append(entry.getKey());
            header.append(": ");
            header.append(entry.getValue());
            header.append("\r\n");
        }

        for (String cookie : cookies) {
            header.append("Set-Cookie: ");
            header.append(cookie);
        }

        header.append("\r\n");

        return header.toString().getBytes(ISO_8859_1);
    }

    List<String> cookies() {
        return Collections.unmodifiableList(cookies);
    }
}
