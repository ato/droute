package droute.v2;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

public final class WebHandlerServlet extends HttpServlet {
    private final WebHandler handler;

    public WebHandlerServlet(WebHandler handler) {
        this.handler = handler;
    }

    @Override
    protected void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {
        WebRequest request = new ServletWebRequest(servletRequest, servletResponse);
        WebResponse response = handler.handle(request);
        sendServletResponse(response, servletResponse);
    }

    public static void sendServletResponse(WebResponse response, HttpServletResponse servletResponse) throws IOException {
        servletResponse.setStatus(response.status());
        for (Map.Entry<String, String> header : response.headers()) {
            servletResponse.addHeader(header.getKey(), header.getValue());
        }
        response.body().writeBody(servletResponse.getOutputStream());
    }
}
