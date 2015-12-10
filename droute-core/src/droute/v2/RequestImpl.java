package droute.v2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class RequestImpl implements Request {

    private final Object raw;
    private final Object rawResponse;
    private final String method;
    private final String path;
    private final String queryString;
    private final String scheme;
    private final InetAddress remoteAddress;
    private final int remotePort;
    private final InetAddress localAddress;
    private final int localPort;
    private final String contextPath;
    private final MultiMap headers;
    private final InputStream bodyStream;

    private MultiMap formMap = null;
    private MultiMap queryMap = null;
    private MultiMap cookies = null;

    public RequestImpl(Object raw, Object rawResponse, String method, String path, String queryString, String scheme, InetAddress remoteAddress, int remotePort, InetAddress localAddress, int localPort, String contextPath, MultiMap headers, InputStream bodyStream) {
        this.raw = raw;
        this.rawResponse = rawResponse;
        this.method = method;
        this.path = path;
        this.queryString = queryString;
        this.scheme = scheme;
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
        this.localAddress = localAddress;
        this.localPort = localPort;
        this.contextPath = contextPath;
        this.headers = headers;
        this.bodyStream = bodyStream;
    }

    @Override
    public Object raw() {
        return raw;
    }

    @Override
    public Object rawResponse() {
        return rawResponse;
    }

    @Override
    public String method() {
        return method;
    }

    static String determineHost(Request request) {
        String host = request.header("Host");
        if (host == null) {
            Matcher m = HOST_RE.matcher(host);
            if (m.matches()) {
                return m.group(1);
            }
        }
        return request.localAddress().getHostName();
    }

    @Override
    public MultiMap headers() {
        return headers;
    }

    @Override
    public MultiMap queryMap() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public MultiMap formMap() {
        if (formMap == null) {
            formMap = Request.super.formMap();
        }
        return formMap;
    }

    @Override
    public InputStream bodyStream() {
        return bodyStream;
    }

    @Override
    public String scheme() {
        return scheme;
    }

    @Override
    public InetAddress localAddress() {
        return localAddress;
    }

    @Override
    public int localPort() {
        return localPort;
    }

    @Override
    public InetAddress remoteAddress() {
        return remoteAddress;
    }

    @Override
    public int remotePort() {
        return remotePort;
    }

    @Override
    public String queryString() {
        return queryString;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public String contextPath() {
        return contextPath;
    }

    @Override
    public MultiMap cookies() {
        if (cookies == null) {
            cookies = Request.super.cookies();
        }
        return cookies;
    }

    @Override
    public String toString() {
        return "Impl{" +
                "raw=" + raw +
                ", method='" + method + '\'' +
                ", path='" + path + '\'' +
                ", contextPath='" + contextPath + '\'' +
                ", headers=" + headers +
                ", formMap=" + formMap +
                ", queryMap=" + queryMap +
                '}';
    }

    private static final Pattern HOST_RE = Pattern.compile("([a-z0-9.-]+|\\[[a-f0-9]*:[a-f0-9:]+\\])(:\\d+)?");
    private static final String COOKIE_NAME = "[^\\p{Cntrl}()<>@,;:\\\\\"/\\[\\]?={} \t]+";
    private static final String COOKIE_OCTET = "[\\x21\\x23-\\x2B\\x2D-\\x3A\\x3C-\\x5B\\x5D-\\x7E]";
    private static final String COOKIE_VALUE = "(?:(" + COOKIE_OCTET + "*)|\"(" + COOKIE_OCTET + "*)\")";
    private static final Pattern RE_COOKIE_PAIR = Pattern.compile("(" + COOKIE_NAME + ")=" + COOKIE_VALUE);

    static MultiMap parseCookieHeader(String cookieHeader) {
        MultiMap cookies = new MultiMap();
        if (cookieHeader != null) {
            for (String pair : cookieHeader.split("; *")) {
                Matcher m = RequestImpl.RE_COOKIE_PAIR.matcher(pair);
                if (m.matches()) {
                    cookies.put(m.group(1), m.group(2));
                }
            }
        }
        return cookies;
    }

    static MultiMap parseFormData(InputStream in) throws IOException {
        MultiMap map = new MultiMap();
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        String key = null;
        while (true) {
            int b = in.read();
            if (b == '&' || b == -1) {
                String value;
                if (key == null) {
                    key = buf.toString("US-ASCII");
                    value = "";
                } else {
                    value = buf.toString("US-ASCII");
                }

                if (!key.isEmpty() || !value.isEmpty()) {
                    map.put(URLDecoder.decode(key, "UTF-8"), URLDecoder.decode(value, "UTF-8"));
                }

                buf.reset();

                if (b == -1) {
                    break;
                }
            } else if (b == '=' && key == null) {
                key = buf.toString();
                buf.reset();
            } else {
                buf.write(b);
            }
        }
        return map;
    }
}
