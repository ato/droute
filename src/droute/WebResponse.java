package droute;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Map;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

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
        validateFieldName(key);
        validateFieldValue(value);
        headers.put(key, value);
    }

    public void setHeader(String key, String value) {
        validateFieldName(key);
        validateFieldValue(value);
        headers.replaceValues(key, Arrays.asList(value));
    }

    public void setStatus(int status) {
        if (status < 100 || status > 999) {
            throw new IllegalArgumentException("Illegal HTTP status code: " + status + " (must be 3 digits)");
        }
        this.status = status;
    }

    public void setBody(WebPayload body) {
        this.body = body;
    }

    public WebPayload body() {
        return body;
    }

    byte[] serializeHeader()  {
        StringBuilder header = new StringBuilder();

        header.append("HTTP/1.1 ");
        header.append(Integer.toString(status));
        header.append(" ");
        header.append(HttpStatus.reasonPhrase(status));
        header.append("\r\n");

        for (Map.Entry<String, String> entry : headers.entries()) {
            header.append(entry.getKey());
            header.append(": ");
            header.append(entry.getValue());
            header.append("\r\n");
        }

        header.append("\r\n");

        return header.toString().getBytes(ISO_8859_1);
    }

    private static final BitSet TOKEN_CHARS = charBitSet("!#$%&'*+-.0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz~^`|");

    private static BitSet charBitSet(String chars) {
        BitSet bitset = new BitSet();
        for (int i = 0; i < chars.length(); i++) {
            bitset.set(chars.charAt(i));
        }
        return bitset;
    }

    private static void validateFieldName(String name) {
        for (int i = 0; i < name.length(); i++) {
            if (!TOKEN_CHARS.get(name.charAt(i))) {
                throw new IllegalArgumentException("Illegal character in HTTP response header field name: " + name.charAt(i));
            }
        }
    }

    private static void validateFieldValue(String value) {
        for (int i = 0; i < value.length(); i++) {
            int c = value.charAt(i) & 0xFFFF;
            if ((c != '\t' && c < 32) || c > 255) {
                throw new IllegalArgumentException("Illegal character in HTTP response header field value: " + c);
            }
        }
    }

}
