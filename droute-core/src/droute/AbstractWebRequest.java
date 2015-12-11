package droute;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class AbstractWebRequest implements WebRequest {
    private static final Pattern HOST_RE = Pattern.compile("([a-z0-9.-]+|\\[[a-f0-9]*:[a-f0-9:]+\\])(:\\d+)?");
    private static final String COOKIE_NAME = "[^\\p{Cntrl}()<>@,;:\\\\\"/\\[\\]?={} \t]+";
    private static final String COOKIE_OCTET = "[\\x21\\x23-\\x2B\\x2D-\\x3A\\x3C-\\x5B\\x5D-\\x7E]";
    private static final String COOKIE_VALUE = "(?:(" + COOKIE_OCTET + "*)|\"(" + COOKIE_OCTET + "*)\")";
    private static final Pattern RE_COOKIE_PAIR = Pattern.compile("(" + COOKIE_NAME + ")=" + COOKIE_VALUE);

    protected MultiMap<String,String> cachedFormMap = null;
    protected MultiMap<String,String> cachedQueryMap = null;
    protected MultiMap<String,String> cachedCookies = null;
    protected MultiMap<String,String> params = new LinkedTreeMultiMap<>();

    @Override
    public MultiMap<String, String> params() {
        return params;
    }

    static String determineHost(WebRequest request) {
        String host = request.header("Host");
        if (host == null) {
            Matcher m = HOST_RE.matcher(host);
            if (m.matches()) {
                return m.group(1);
            }
        }
        return request.localAddress().getHostName();
    }

    static MultiMap<String,String> parseCookieHeader(String cookieHeader) {
        MultiMap<String,String> cookies = new LinkedTreeMultiMap<>();
        if (cookieHeader != null) {
            for (String pair : cookieHeader.split("; *")) {
                Matcher m = AbstractWebRequest.RE_COOKIE_PAIR.matcher(pair);
                if (m.matches()) {
                    cookies.put(m.group(1), m.group(2));
                }
            }
        }
        return cookies;
    }

    static MultiMap<String,String> parseFormData(InputStream in) throws IOException {
        MultiMap<String,String> map = new LinkedTreeMultiMap<>();
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

    @Override
    public MultiMap<String,String> queryMap() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public MultiMap<String,String> formMap() {
        if (cachedFormMap == null) {
            cachedFormMap = WebRequest.super.formMap();
        }
        return cachedFormMap;
    }

    @Override
    public MultiMap<String,String> cookies() {
        if (cachedCookies == null) {
            cachedCookies = WebRequest.super.cookies();
        }
        return cachedCookies;
    }
}
