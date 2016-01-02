package droute;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class AbstractHttpRequest implements HttpRequest {
    private static final Pattern HOST_RE = Pattern.compile("([a-z0-9.-]+|\\[[a-f0-9]*:[a-f0-9:]+\\])(:\\d+)?");
    private static final String COOKIE_NAME = "[^\\p{Cntrl}()<>@,;:\\\\\"/\\[\\]?={} \t]+";
    private static final String COOKIE_OCTET = "[\\x21\\x23-\\x2B\\x2D-\\x3A\\x3C-\\x5B\\x5D-\\x7E]";
    private static final String COOKIE_VALUE = "(?:(" + COOKIE_OCTET + "*)|\"(" + COOKIE_OCTET + "*)\")";
    private static final Pattern RE_COOKIE_PAIR = Pattern.compile("(" + COOKIE_NAME + ")=" + COOKIE_VALUE);

    protected Map<String,String> cachedCookies = null;
    protected MultiMap<String,String> cachedFormMap = null;
    protected MultiMap<String,String> cachedQueryMap = null;
    protected MultiMap<String,String> params = new MultiMap<>(new HashMap<String,List<String>>());

    @Override
    public MultiMap<String, String> params() {
        return params;
    }

    static String determineHost(HttpRequest request) {
        Optional<String> host = request.header("Host");
        if (host.isPresent()) {
            Matcher m = HOST_RE.matcher(host.get());
            if (m.matches()) {
                return m.group(1);
            }
        }
        return request.localAddress().getHostName();
    }

    static Map<String, String> parseCookieHeader(String cookieHeader) {
        Map<String,String> cookies = new HashMap<>();
        if (cookieHeader != null) {
            for (String pair : cookieHeader.split("; *")) {
                Matcher m = AbstractHttpRequest.RE_COOKIE_PAIR.matcher(pair);
                if (m.matches()) {
                    cookies.put(m.group(1), m.group(2));
                }
            }
        }
        return cookies;
    }

    static MultiMap<String,String> parseFormData(InputStream in) throws IOException {
        MultiMap<String,String> map = new MultiMap<>(new TreeMap<String,List<String>>());
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
                    map.add(URLDecoder.decode(key, "UTF-8"), URLDecoder.decode(value, "UTF-8"));
                }

                buf.reset();
                key = null;

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
        if (cachedQueryMap == null) {
            cachedQueryMap = parseQueryMap();
        }
        return cachedQueryMap;
    }

    @Override
    public MultiMap<String,String> formMap() {
        if (cachedFormMap == null) {
            cachedFormMap = parseFormMap();
        }
        return cachedFormMap;
    }

    @Override
    public Map<String, String> cookies() {
        if (cachedCookies == null) {
            cachedCookies = parseCookies();
        }
        return cachedCookies;
    }

    //-----------------------------------------------------------------
    // Convenience Methods
    //-----------------------------------------------------------------

    protected MultiMap<String, String> parseQueryMap() {
        try {
            return SimpleHttpRequest.parseFormData(new ByteArrayInputStream(queryString().getBytes(StandardCharsets.US_ASCII)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Returns the URI of the request. The host will be calculated from the host header.
     */
    @Override
    public URI uri() {
        try {
            return new URI(scheme(), null, SimpleHttpRequest.determineHost(this), localAddress().getPort(), path(), queryString(), null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The root URI of the web application.
     * <p/>
     * Note that web applications may be accesible via multiple hostnames or
     * protocols. This will return the root URI appropriate to this request.
     */
    @Override
    public URI contextUri() {
        try {
            return new URI(scheme(), null, SimpleHttpRequest.determineHost(this), localAddress().getPort(), contextPath(), null, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Interpret the request body as form data. This will consume the body stream.
     */
    protected MultiMap<String, String> parseFormMap() {
        String contentType = header("Content-Type").orElse(null);
        if ("application/x-www-form-urlencoded".equals(contentType)) {
            try {
                return SimpleHttpRequest.parseFormData(bodyStream());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        throw new UnsupportedOperationException("Content-Type is not application/x-www-form-urlencoded");
    }

    /**
     * Returns the parsed contents of the Cookie header.
     */
    protected Map<String, String> parseCookies() {
        String value = headers().get("Cookie");
        if (value != null) {
            return SimpleHttpRequest.parseCookieHeader(value);
        } else {
            return new HashMap<>();
        }
    }

    /**
     * Returns the first matching query string parameter.
     */
    @Override
    public Optional<String> query(String key) {
        return Optional.ofNullable(queryMap().getFirst(key));
    }

    /**
     * Returns the first matching form data parameter.
     */
    @Override
    public Optional<String> form(String key) {
        return Optional.ofNullable(formMap().getFirst(key));
    }

    /**
     * Returns the first matching HTTP header.
     */
    @Override
    public Optional<String> header(String name) {
        return Optional.ofNullable(headers().get(name));
    }

    /**
     * Returns the first matching cookie.
     */
    @Override
    public Optional<String> cookie(String name) {
        return Optional.ofNullable(cookies().get(name));
    }

    /**
     * Returns the first matching URL parameter.
     */
    @Override
    public Optional<String> param(String key) {
        return Optional.ofNullable(params().getFirst(key));
    }


    @Override
    public void setParams(MultiMap<String, String> params) {
        this.params = params;
    }
}
