package droute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Enumeration;

public class ServletWebRequest extends AbstractWebRequest {
    private final HttpServletRequest servletRequest;
    private final HttpServletResponse servletResponse;

    private MultiMap<String,String> headers = null;

    public ServletWebRequest(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;
    }

    public HttpServletRequest servletRequest() {
        return servletRequest;
    }

    public HttpServletResponse servletResponse() {
        return servletResponse;
    }

    @Override
    public String method() {
        return servletRequest.getMethod();
    }

    @Override
    public MultiMap<String, String> headers() {
        if (headers == null) {
            headers = copyHeaders();
        }
        return headers;
    }

    private MultiMap<String, String> copyHeaders() {
        MultiMap<String,String> headers = new LinkedTreeMultiMap<>(String.CASE_INSENSITIVE_ORDER);
        Enumeration names = servletRequest.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = (String)names.nextElement();
            Enumeration values = servletRequest.getHeaders(name);
            while (values.hasMoreElements()) {
                headers.put(name, (String)values.nextElement());
            }
        }
        return headers;
    }

    @Override
    public InputStream bodyStream() throws IOException {
        return servletRequest.getInputStream();
    }

    @Override
    public String scheme() {
        return servletRequest.getScheme();
    }

    @Override
    public InetSocketAddress localAddress() {
        return InetSocketAddress.createUnresolved(servletRequest.getLocalAddr(), servletRequest.getLocalPort());
    }

    @Override
    public InetSocketAddress remoteAddress() {
        return InetSocketAddress.createUnresolved(servletRequest.getRemoteAddr(), servletRequest.getRemotePort());
    }

    @Override
    public String queryString() {
        return servletRequest.getQueryString();
    }

    @Override
    public String path() {
        return servletRequest.getPathInfo();
    }

    @Override
    public String contextPath() {
        return servletRequest.getContextPath();
    }

    @Override
    public String protocol() {
        return servletRequest.getProtocol();
    }
}
