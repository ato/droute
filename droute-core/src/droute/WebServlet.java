package droute;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public final class WebServlet extends HttpServlet {
    private final WebHandler handler;

    public WebServlet(WebHandler handler) {
        this.handler = handler;
    }

    @Override
    protected void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {
        WebRequest request = new ServletWebRequest(servletRequest, servletResponse);
        WebResponse response = handler.handle(request);
        respond(response, servletResponse);
    }

    @Override
    public void destroy() {
        try {
            handler.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to close handler", e);
        }
    }

    /**
     * Sends a WebResponse via a given HttpServletResponse.
     */
    public static void respond(WebResponse response, HttpServletResponse servletResponse) throws IOException {
        servletResponse.setStatus(response.status());
        for (Map.Entry<String, String> header : response.headers().entries()) {
            servletResponse.addHeader(header.getKey(), header.getValue());
        }
        response.body().writeBody(servletResponse.getOutputStream());
    }
}
