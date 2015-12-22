package droute;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public interface WebRequest {

    /**
     * The HTTP request method such as "onGET" or "POST".
     */
    String method();

    /**
     * The HTTP request headers.
     */
    MultiMap<String, String> headers();

    /**
     * Returns a map of the decoded query string parameters.
     */
    default MultiMap<String, String> queryMap() {
        try {
            return HttpRequest.parseFormData(new ByteArrayInputStream(queryString().getBytes(StandardCharsets.US_ASCII)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a map of parameters extracted from the URI during routing.
     */
    MultiMap<String,String> params();

    /**
     * Returns the HTTP body as an InputStream.
     */
    InputStream bodyStream() throws IOException;

    /**
     * Returns the protocol scheme ("http" or "https").
     */
    String scheme();

    /**
     * The local address the client is connected to.
     */
    InetSocketAddress localAddress();

    /**
     * The address of the remote client.
     */
    InetSocketAddress remoteAddress();

    /**
     * The raw query string.
     */
    String queryString();

    /**
     * The path of this request relative to the context root (excluding the query string).
     * <p/>
     * For example if the application is mounted at /bakery and /bakery/scones/lemonade was requested, this returns
     * "/scones/lemonade".
     */
    String path();

    /**
     * The path to the root of the web application.
     */
    String contextPath();

    /**
     * Returns the protocol version this request was made using. For example: "HTTP/1.0".
     */
    String protocol();

    //-----------------------------------------------------------------
    // Convenience Methods
    //-----------------------------------------------------------------

    /**
     * Returns the URI of the request. The host will be calculated from the host header.
     */
    default URI uri() {
        try {
            return new URI(scheme(), null, HttpRequest.determineHost(this), localAddress().getPort(), path(), queryString(), null);
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
    default URI contextUri() {
        try {
            return new URI(scheme(), null, HttpRequest.determineHost(this), localAddress().getPort(), contextPath(), null, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Interpret the request body as form data. This will consume the body stream.
     */
    default MultiMap<String, String> formMap() {
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
    default MultiMap<String, String> cookies() {
        return header("Cookie").map(HttpRequest::parseCookieHeader).orElseGet(LinkedTreeMultiMap::new);
    }

    /**
     * Returns the first matching query string parameter.
     */
    default Optional<String> query(String key) {
        return Optional.ofNullable(queryMap().getFirst(key));
    }

    /**
     * Returns the first matching form data parameter.
     */
    default Optional<String> form(String key) {
        return Optional.ofNullable(formMap().getFirst(key));
    }

    /**
     * Returns the first matching HTTP header.
     */
    default Optional<String> header(String name) {
        return Optional.ofNullable(headers().getFirst(name));
    }

    /**
     * Returns the first matching cookie.
     */
    default Optional<String> cookie(String name) {
        return Optional.ofNullable(cookies().getFirst(name));
    }

    /**
     * Returns the first matching URL parameter.
     */
    default Optional<String> param(String key) {
        return Optional.ofNullable(params().getFirst(key));
    }

    /**
     * Replaces the URL parameter multimap.
     */
    void setParams(MultiMap<String, String> params);
}
