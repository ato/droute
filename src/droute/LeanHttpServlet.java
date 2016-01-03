package droute;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

/**
 * An adapter for using LeanHttp on top of a servlet container. Instantiates an instance of the class given by the
 * 'handlerClass' init parameter using reflection. The handler class must have a public default constructor.  If the
 * handler implements AutoCloseable then its close() method will be called when the servler is destroyed.
 * <p/>
 * If necessary, the underlying HttpServletRequest and HttpServletResponse objects from the container may be accessed
 * from inside the handler like so:
 * <code>
 *     if (request instanceof ServletHttpRequest) {
 *         HttpServletRequest servletRequest = ((ServletHttpRequest)request).servletRequest()
 *         HttpServletResponse servletResponse = ((ServletHttpResponse)request).servletResponse()
 *     }
 * </code>
 */
public final class LeanHttpServlet extends HttpServlet {
    private HttpHandler handler;

    @Override
    public void init() throws ServletException {
        String className = getServletConfig().getInitParameter("handlerClass");
        if (className == null && handler == null) {
            throw new ServletException("LeanHttpServlet requires a handlerClass init parameter");
        }
        try {
            Class<?> clazz = Class.forName(className);
            this.handler = (HttpHandler) clazz.newInstance();
        } catch (ClassNotFoundException e) {
            throw new ServletException("Couldn't find handlerClass: " + className, e);
        } catch (InstantiationException e) {
            throw new ServletException("Couldn't construct handlerClass: " + className, e);
        } catch (IllegalAccessException e) {
            throw new ServletException("Couldn't construct handlerClass: " + className, e);
        } catch (ClassCastException e) {
            throw new ServletException("handlerClass doesn't implement HttpHandler: " + className, e);
        }
    }

    public LeanHttpServlet() {
    }

    public LeanHttpServlet(HttpHandler handler) {
        this.handler = handler;
    }

    private Map<String, String> copyHeaders(HttpServletRequest servletRequest) {
        Map<String,String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
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
    protected void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {
        HttpRequest request = new HttpRequest(
                servletRequest.getMethod(),
                servletRequest.getPathInfo(),
                servletRequest.getQueryString(),
                servletRequest.getScheme(),
                servletRequest.getProtocol(),
                InetSocketAddress.createUnresolved(servletRequest.getRemoteAddr(), servletRequest.getRemotePort()),
                InetSocketAddress.createUnresolved(servletRequest.getLocalAddr(), servletRequest.getLocalPort()),
                servletRequest.getContextPath(),
                copyHeaders(servletRequest),
                servletRequest.getInputStream()
        );
        HttpResponse response = handler.handle(request);
        respond(response, servletResponse);
    }

    @Override
    public void destroy() {
        try {
            if (handler instanceof AutoCloseable) {
                ((AutoCloseable)handler).close();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to close handler", e);
        }
    }

    /**
     * Sends a WebResponse via a given HttpServletResponse.
     */
    public static void respond(HttpResponse response, HttpServletResponse servletResponse) throws IOException {
        servletResponse.setStatus(response.status());
        for (Map.Entry<String, String> header : response.headers().entrySet()) {
            servletResponse.addHeader(header.getKey(), header.getValue());
        }
        for (String cookie : response.cookies()) {
            servletResponse.addHeader("Set-Cookie", cookie);
        }
        response.body().writeTo(servletResponse.getOutputStream());
    }
}
