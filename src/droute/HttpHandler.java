package droute;

import java.io.IOException;

public interface HttpHandler {
    HttpResponse handle(HttpRequest request) throws IOException;
}
