package droute.v2;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

public final class WebResponses {
    private static final Map<String,String> contentTypes = new HashMap<String,String>();

    static {
        contentTypes.put("css", "text/css");
        contentTypes.put("js", "application/javascript");
        contentTypes.put("png", "image/png");
        contentTypes.put("gif", "image/gif");
        contentTypes.put("jpg", "image/jpeg");
        contentTypes.put("woff", "application/font-woff");
        contentTypes.put("ttf", "application/font-sfnt");
        contentTypes.put("svg", "image/svg+xml");
        contentTypes.put("eot", "application/vnd.ms-fontobject");
    }

    public static String fromExtension(String filename) {
        int i = filename.lastIndexOf('.') + 1;
        if (i >= 0) {
            String ext = filename.substring(i);
            return contentTypes.get(ext);
        }
        return null;
    }

    private WebResponses() {}

    public static final WebResponse NEXT_HANDLER = response(500, "Next handler");

    private final static int STATUS_OK = 200;

    /*
	 * Convenience constructors
	 */

    public static WebResponse response(int status, WebResponseBody body) {
        WebResponse response = new WebResponse();
        response.setStatus(status);
        response.setBody(body);
        return response;
    }

    public static WebResponse response(int status, InputStream body) {
        return response(status, WebResponseBody.wrap(body));
    }

    public static WebResponse response(int status, String body) {
        return response(status, WebResponseBody.wrap(body));
    }

    public static WebResponse ok(InputStream body) {
        return response(STATUS_OK, body);
    }

    public static WebResponse ok(String body) {
        return response(STATUS_OK, body);
    }

    /**
     * Returns a 404 Not Found response.
     */
    public static WebResponse notFound(String body) {
        return response(404, body);
    }

    public static WebResponse notFound(InputStream body) {
        return response(404, body);
    }

    /**
     * Sends an empty response with the given status code and Location header. If appropriate you should
     * use {@link #seeOther(String)}, {@link #redirect(String)} or {@link #permanentRedirect(String)}
     * instead of calling this method directly.
     */
    public static WebResponse redirect(int status, String location) {
        Objects.requireNonNull(status, "location");
        WebResponse response = new WebResponse();
        response.setStatus(status);
        response.setHeader("Location", location);
        return response;
    }

    /**
     * Returns a 303 See Other response. Even if the initial request was a POST the browser will fetch
     * the redirect location using GET.
     */
    public static WebResponse seeOther(String location) {
        return redirect(303, location);
    }

    /**
     * Returns a 307 Temporary Redirect response. The browser should repeat the request against the given
     * URL. However as this is temporary, future requests should still be made against the original URL.
     *
     * Note that {@link #seeOther(String)} should instead be used when redirecting to a new page after
     * processing a POST.
     */
    public static WebResponse redirect(String location) {
        return redirect(307, location);
    }

    /**
     * Returns a 307 Permanent Redirect response. The browser should repeat the request against the given
     * URL. As this is permanent, future requests should still be made against the new URL.  Browsers often
     * cache this redirect and search engines often replace the original URL with the redirect target in
     * their indexes.
     *
     * Note that {@link #seeOther(String)} should instead be used when redirecting to a new page after
     * processing a POST.
     */
    public static WebResponse permanentRedirect(String location) {
        return redirect(301, location);
    }

    /**
     * Sends a classpath resource to the client.  Sets the Last-Modified, Content-Length and Content-Type
     * headers when possible.
     */
    public static WebResponse resource(URL resource) throws IOException {
        Objects.requireNonNull(resource, "resource");

        URLConnection conn = resource.openConnection();
        long lastModified = conn.getLastModified();
        long length = conn.getContentLengthLong();
        String type = ContentTypes.fromExtension(resource.getPath());

        WebResponse response = ok(conn.getInputStream());
        if (lastModified != 0) {
            response.addHeader("Last-Modified", formatHttpDate(lastModified));
        }
        if (length != -1) {
            response.addHeader("Content-Length", Long.toString(length));
        }
        if (type != null) {
            response.addHeader("Content-Type", type);
        }
        return response;
    }

    private static String formatHttpDate(long epochMillis) {
        return RFC_1123_DATE_TIME.format(OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC));
    }
}
