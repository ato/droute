package droute.v2;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;

public class RoutedRequestImpl implements RoutedRequest {
    final Request request;
    final MultiMap params;

    RoutedRequestImpl(Request request, MultiMap params) {
        this.request = request;
        this.params = params;
    }

    @Override
    public String toString() {
        return "Impl{" +
                "request=" + request +
                ", params=" + params +
                '}';
    }

    @Override
    public Request unwrap() {
        return request;
    }

    @Override
    public MultiMap params() {
        return params;
    }

    /*
     * Delegated methods
     */

    @Override
    public Object raw() {
        return request.raw();
    }

    @Override
    public Object rawResponse() {
        return request.rawResponse();
    }

    @Override
    public String method() {
        return request.method();
    }

    @Override
    public URI uri() {
        return request.uri();
    }

    @Override
    public MultiMap headers() {
        return request.headers();
    }

    @Override
    public URI contextUri() {
        return request.contextUri();
    }

    @Override
    public MultiMap queryMap() {
        return request.queryMap();
    }

    @Override
    public InputStream bodyStream() {
        return request.bodyStream();
    }

    @Override
    public String scheme() {
        return request.scheme();
    }

    @Override
    public InetAddress localAddress() {
        return request.localAddress();
    }

    @Override
    public int localPort() {
        return request.localPort();
    }

    @Override
    public InetAddress remoteAddress() {
        return request.remoteAddress();
    }

    @Override
    public int remotePort() {
        return request.remotePort();
    }

    @Override
    public String queryString() {
        return request.queryString();
    }

    @Override
    public MultiMap formMap() {
        return request.formMap();
    }

    @Override
    public MultiMap cookies() {
        return request.cookies();
    }

    @Override
    public String query(String key) {
        return request.query(key);
    }

    @Override
    public String form(String key) {
        return request.form(key);
    }

    @Override
    public String header(String name) {
        return request.header(name);
    }

    @Override
    public String path() {
        return request.path();
    }

    @Override
    public String contextPath() {
        return request.contextPath();
    }

    @Override
    public String cookie(String name) {
        return request.cookie(name);
    }
}
