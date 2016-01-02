package droute;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Map;

class SimpleHttpRequest extends AbstractHttpRequest {

    private final String method;
    private final String path;
    private final String queryString;
    private final String scheme;
    private final InetSocketAddress remoteAddress;
    private final InetSocketAddress localAddress;
    private final String contextPath;
    private final Map<String,String> headers;
    private final InputStream bodyStream;
    private final String protocol;

    public SimpleHttpRequest(String method, String path, String queryString, String scheme, String protocol, InetSocketAddress remoteAddress, InetSocketAddress localAddress, String contextPath, Map<String, String> headers, InputStream bodyStream) {
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

    @Override
    public String method() {
        return method;
    }

    @Override
    public Map<String, String> headers() {
        return headers;
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
    public InetSocketAddress localAddress() {
        return localAddress;
    }

    @Override
    public InetSocketAddress remoteAddress() {
        return remoteAddress;
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

}
