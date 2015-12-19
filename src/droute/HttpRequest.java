package droute;

import java.io.InputStream;
import java.net.InetSocketAddress;

class HttpRequest extends AbstractWebRequest {

    private final String method;
    private final String path;
    private final String queryString;
    private final String scheme;
    private final InetSocketAddress remoteAddress;
    private final InetSocketAddress localAddress;
    private final String contextPath;
    private final MultiMap<String,String> headers;
    private final InputStream bodyStream;

    public HttpRequest(String method, String path, String queryString, String scheme, InetSocketAddress remoteAddress, InetSocketAddress localAddress, String contextPath, MultiMap<String,String> headers, InputStream bodyStream) {
        this.method = method;
        this.path = path;
        this.queryString = queryString;
        this.scheme = scheme;
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
    public MultiMap<String,String> headers() {
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
