package droute.v2;

import java.io.*;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;

public interface Request {

    /**
     * The original request object from the underlying web server or web framework.
     * When using the Java Servlet Framework this is a HttpServletRequest.
     */
    Object raw();

    /**
     * The original response object from the underlying web server or web framework.
     * When using the Java Servlet Framework this is a HttpServletResponse.
     */
    Object rawResponse();

    /**
     * The HTTP request method such as "GET" or "POST".
     */
    String method();

    /**
     * The HTTP request headers.
     */
    MultiMap headers();

    /**
     * Returns a map of the decoded query string parameters.
     */
    MultiMap queryMap();

    /**
     * Returns the HTTP body as an InputStream.
     */
    InputStream bodyStream();

    /**
     * Returns the protocol scheme ("http" or "https").
     */
    String scheme();

    /**
     * The local address the client is connected to.
     */
    InetAddress localAddress();

    /**
     * The local port number the client is connected to.
     */
    int localPort();

    /**
     * The address of the remote client.
     */
    InetAddress remoteAddress();

    /**
     * The port number of the remote client.
     */
    int remotePort();

    /**
     * The raw query string.
     */
    String queryString();

    /**
     * The path of this request relative to the context root (excluding the query string).
     *
     * For example if the application is mounted at /bakery and /bakery/scones/lemonade was requested, this returns
     * "/scones/lemonade".
     */
    String path();

    /**
     * The path to the root of the web application.
     */
    String contextPath();

    //-----------------------------------------------------------------
    // Convenience Methods
    //-----------------------------------------------------------------

    /**
     * Returns the URI of the request. The host will be calculated from the host header.
     */
    default URI uri() {
        try {
            return new URI(scheme(), null, RequestImpl.determineHost(this), localPort(), path(), queryString(), null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The root URI of the web application.
     *
     * Note that web applications may be accesible via multiple hostnames or
     * protocols. This will return the root URI appropriate to this request.
     */
    default URI contextUri() {
        try {
            return new URI(scheme(), null, RequestImpl.determineHost(this), localPort(), contextPath(), null, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Interpret the request body as form data. This will consume the body stream.
     */
    default MultiMap formMap() {
        String contentType = header("Content-Type");
        if ("application/x-www-form-urlencoded".equals(contentType)) {
            try {
                return RequestImpl.parseFormData(bodyStream());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        throw new UnsupportedOperationException("Content-Type is not application/x-www-form-urlencoded");
    }

    /**
     * Returns the parsed contents of the Cookie header.
     */
    default MultiMap cookies() {
        return RequestImpl.parseCookieHeader(header("Cookie"));
    }

    /**
     * Returns the first matching query string parameter.
     */
    default String query(String key) {
        return queryMap().getFirst(key);
    }

    /**
     * Returns the first matching form data parameter.
     */
    default String form(String key) {
        return formMap().getFirst(key);
    }

    /**
     * Returns the first matching HTTP header.
     */
    default String header(String name) {
        return headers().getFirst(name);
    }

    /**
     * Returns the first matching cookie.
     */
    default String cookie(String name) {
        return cookies().getFirst(name);
    }

}
