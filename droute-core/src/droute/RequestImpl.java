package droute;

import java.io.InputStream;
import java.net.InetAddress;

class RequestImpl extends AbstractWebRequest {

    private final String method;
    private final String path;
    private final String queryString;
    private final String scheme;
    private final InetAddress remoteAddress;
    private final int remotePort;
    private final InetAddress localAddress;
    private final int localPort;
    private final String contextPath;
    private final MultiMap<String,String> headers;
    private final InputStream bodyStream;

    public RequestImpl(String method, String path, String queryString, String scheme, InetAddress remoteAddress, int remotePort, InetAddress localAddress, int localPort, String contextPath, MultiMap<String,String> headers, InputStream bodyStream) {
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
    public String toString() {
        return "RequestImpl{" +
                "method='" + method + '\'' +
                ", path='" + path + '\'' +
                ", queryString='" + queryString + '\'' +
                ", scheme='" + scheme + '\'' +
                ", remoteAddress=" + remoteAddress +
                ", remotePort=" + remotePort +
                ", localAddress=" + localAddress +
                ", localPort=" + localPort +
                ", contextPath='" + contextPath + '\'' +
                ", headers=" + headers +
                ", bodyStream=" + bodyStream +
                ", cachedFormMap=" + cachedFormMap +
                ", cachedQueryMap=" + cachedQueryMap +
                ", cachedCookies=" + cachedCookies +
                '}';
    }

}
