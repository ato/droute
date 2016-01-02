package droute;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

public interface HttpRequest {

    /**
     * The HTTP request method such as "onGET" or "POST".
     */
    String method();

    /**
     * The HTTP request headers.
     */
    Map<String, String> headers();

    URI uri();

    URI contextUri();

    MultiMap<String,String> formMap();

    Map<String, String> cookies();

    /**
     * Returns a map of the decoded query string parameters.
     */
    MultiMap<String, String> queryMap();

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

    Optional<String> query(String key);

    Optional<String> form(String key);

    Optional<String> header(String name);

    Optional<String> cookie(String name);

    Optional<String> param(String key);

    /**
     * Replaces the URL parameter multimap.
     */
    void setParams(MultiMap<String, String> params);
}
