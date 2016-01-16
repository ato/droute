package org.meshy.leanhttp;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpRequest {

    private static final Pattern HOST_RE = Pattern.compile("([a-z0-9.-]+|\\[[a-f0-9]*:[a-f0-9:]+\\])(:\\d+)?");
    private static final String COOKIE_NAME = "[^\\p{Cntrl}()<>@,;:\\\\\"/\\[\\]?={} \t]+";
    private static final String COOKIE_OCTET = "[\\x21\\x23-\\x2B\\x2D-\\x3A\\x3C-\\x5B\\x5D-\\x7E]";
    private static final String COOKIE_VALUE = "(?:(" + COOKIE_OCTET + "*)|\"(" + COOKIE_OCTET + "*)\")";
    private static final Pattern RE_COOKIE_PAIR = Pattern.compile("(" + COOKIE_NAME + ")=" + COOKIE_VALUE);
    private final String method;
    private final String path;
    private final String queryString;
    private final String scheme;
    private final InetSocketAddress remoteAddress;
    private final InetSocketAddress localAddress;
    private final String contextPath;
    private final Map<String, String> headers;
    private final InputStream bodyStream;
    private final String protocol;
    private final Map<Class, Object> extensions = new HashMap<>();
    protected Map<String, String> cachedCookies = null;
    protected Map<String, List<String>> cachedFormMap = null;
    protected Map<String, List<String>> cachedQueryMap = null;
    protected Map<String, List<String>> params = new HashMap<>();

    public HttpRequest(String method, String path, String queryString, String scheme, String protocol, InetSocketAddress remoteAddress, InetSocketAddress localAddress, String contextPath, Map<String, String> headers, InputStream bodyStream) {
        this.method = method;
        this.path = path;
        this.queryString = queryString;
        this.scheme = scheme;
        this.protocol = protocol;
        this.remoteAddress = remoteAddress;
        this.localAddress = localAddress;
        this.contextPath = contextPath;
        this.headers = headers;
        this.bodyStream = bodyStream;
    }

    static String determineHost(HttpRequest request) {
        String host = request.header("Host").orElse(null);
        if (host != null) {
            Matcher m = HOST_RE.matcher(host);
            if (m.matches()) {
                return m.group(1);
            }
        }
        return request.localAddress().getHostName();
    }

    static Map<String, String> parseCookieHeader(String cookieHeader) {
        Map<String, String> cookies = new HashMap<>();
        if (cookieHeader != null) {
            for (String pair : cookieHeader.split("; *")) {
                Matcher m = HttpRequest.RE_COOKIE_PAIR.matcher(pair);
                if (m.matches()) {
                    cookies.put(m.group(1), m.group(2));
                }
            }
        }
        return cookies;
    }

    static Map<String, List<String>> parseFormData(InputStream in) throws IOException {
        Map<String, List<String>> map = new TreeMap<>();
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
                    MultiMaps.addEntry(map, URLDecoder.decode(key, "UTF-8"), URLDecoder.decode(value, "UTF-8"));
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

    /**
     * The HTTP request method such as "onGET" or "POST".
     */
    public String method() {
        return method;
    }

    /**
     * The HTTP request headers.
     */
    public Map<String, String> headers() {
        return headers;
    }

    /**
     * Returns the HTTP body as an InputStream.
     */
    public InputStream bodyStream() {
        return bodyStream;
    }

    /**
     * Returns the protocol scheme ("http" or "https").
     */
    public String scheme() {
        return scheme;
    }

    /**
     * The local address the client is connected to.
     */
    public InetSocketAddress localAddress() {
        return localAddress;
    }

    /**
     * The address of the remote client.
     */
    public InetSocketAddress remoteAddress() {
        return remoteAddress;
    }

    /**
     * The raw query string.
     */
    public String queryString() {
        return queryString;
    }

    /**
     * The path of this request relative to the context root (excluding the query string).
     * <p/>
     * For example if the application is mounted at /bakery and /bakery/scones/lemonade was requested, this returns
     * "/scones/lemonade".
     */
    public String path() {
        return path;
    }

    /**
     * The path to the root of the web application.
     */
    public String contextPath() {
        return contextPath;
    }

    /**
     * Returns the protocol version this request was made using. For example: "HTTP/1.0".
     */
    public String protocol() {
        return protocol;
    }

    @Override
    public String toString() {
        return "RequestImpl{" +
                "method='" + method + '\'' +
                ", path='" + path + '\'' +
                ", queryString='" + queryString + '\'' +
                ", scheme='" + scheme + '\'' +
                ", remoteAddress=" + remoteAddress +
                ", localAddress=" + localAddress +
                ", contextPath='" + contextPath + '\'' +
                ", headers=" + headers +
                ", bodyStream=" + bodyStream +
                ", cachedFormMap=" + cachedFormMap +
                ", cachedQueryMap=" + cachedQueryMap +
                ", cachedCookies=" + cachedCookies +
                '}';
    }

    /**
     * Returns a map of parameters extracted from the URI during routing.
     */
    public Map<String, List<String>> params() {
        return params;
    }

    /**
     * Returns a map of the decoded query string parameters.
     */
    public Map<String, List<String>> queryMap() {
        if (cachedQueryMap == null) {
            cachedQueryMap = parseQueryMap();
        }
        return cachedQueryMap;
    }

    public Map<String, List<String>> formMap() {
        if (cachedFormMap == null) {
            cachedFormMap = parseFormMap();
        }
        return cachedFormMap;
    }

    public Map<String, String> cookies() {
        if (cachedCookies == null) {
            cachedCookies = parseCookies();
        }
        return cachedCookies;
    }

    protected Map<String, List<String>> parseQueryMap() {
        try {
            if (queryString() == null) {
                return Collections.emptyMap();
            }
            return HttpRequest.parseFormData(new ByteArrayInputStream(queryString().getBytes(StandardCharsets.US_ASCII)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the URI of the request. The host will be calculated from the host header.
     */
    public URI uri() {
        try {
            return new URI(scheme(), null, HttpRequest.determineHost(this), portForUri(), path(), queryString(), null);
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
    public URI contextUri() {
        try {
            return new URI(scheme(), null, HttpRequest.determineHost(this), portForUri(), contextPath(), null, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private int portForUri() {
        int port = localAddress().getPort();
        if ((port == 80 && "http".equals(scheme())) || (port == 443 && "https".equals(scheme()))) {
            port = -1;
        }
        return port;
    }

    /**
     * Interpret the request body as form data. This will consume the body stream.
     */
    protected Map<String, List<String>> parseFormMap() {
        String contentType = header("Content-Type").orElse(null);
        if ("application/x-www-form-urlencoded".equals(contentType)) {
            try {
                return HttpRequest.parseFormData(bodyStream());
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
            return HttpRequest.parseCookieHeader(value);
        } else {
            return new HashMap<>();
        }
    }

    /**
     * Returns the first matching query string parameter.
     */
    public MultiParameter query(String key) {
        return new MultiParameter("query parameter", key, queryMap().get(key));
    }

    /**
     * Returns the first matching form data parameter.
     */
    public MultiParameter form(String key) {
        return new MultiParameter("form parameter", key, formMap().get(key));
    }

    /**
     * Returns the first matching HTTP header.
     */
    public Parameter header(String name) {
        return new Parameter("request header", name, headers().get(name));
    }

    /**
     * Returns the first matching cookie.
     */
    public Parameter cookie(String name) {
        return new Parameter("cookie", name, cookies().get(name));
    }

    /**
     * Returns the first matching URL parameter.
     */
    public MultiParameter param(String key) {
        return new MultiParameter("URL parameter", key, params().get(key));
    }

    /**
     * Replaces the URL parameter multimap.
     */
    public void setParams(Map<String, List<String>> params) {
        this.params = params;
    }

    /**
     * Return a request extensions for the given type. Extensions are a type-safe way for middleware to associate
     * additional information with a request.
     * <p/>
     * For example you could define a session class:
     * <code>
     *     class Session {
     *         String id;
     *         String username;
     *     }
     * </code>
     * And then populate it with a middleware handler:
     * <code>
     *     sessionHandler = request -> {
     *         String sessionId = request.cookie("sessionId").orElse(null);
     *
     *         Session session;
     *         if (sessionId == null) {
     *             session = sessionStore.newSession();
     *         } else {
     *             session = sesionStore.get(sessionId);
     *         }

     *         request.extend(session);
     *
     *         Response response = nextHandler.handle(request);
     *
     *         if (sessionId == null) {
     *             response.cookie("sessionId", session.id);
     *         }
     *
     *         return response;
     *     };
     * </code>
     * The session can then be later retrieved:
     * <code>
     *     router.on(GET, "/greeting", request -> ok("Hello, " + request.extension(Session.class).username));
     * </code>
     */
    public <T> T extension(Class<T> extensionClass) {
        return (T) extensions.get(extensionClass);
    }

    /**
     * Add a request extension. Shorthand for <code>request.extend(extension.getClass(), extension)</code>
     */
    public void extend(Object extension) {
        extensions.put(extension.getClass(), extension);
    }

    /**
     * Add a request extension with a given type. Use this method if you want to use an interface or superclass as the
     * extension's type.
     */
    public <T> void extend(Class<T> extensionClass, T extension) {
        extensions.put(extensionClass, extension);
    }
}
