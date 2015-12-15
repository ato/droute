package droute;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static droute.WebStatus.*;

/**
 * Factory methods for constructing WebResponse instances.
 * <p/>
 * The static methods and constants in this class are intended to be statically imported:
 * <p/>
 * {@code import static droute.WebResponses.*}
 */
public final class WebResponses {
    public static final WebResponse NEXT_HANDLER = response(INTERNAL_SERVER_ERROR, "Next handler");

    private WebResponses() {
    }

    private static String extension(String filename) {
        int i = filename.lastIndexOf('.') + 1;
        return i == -1 ? null : filename.substring(i);
    }

    private static String guessContentType(String filename) {
        switch (extension(filename)) {
            case "txt":
                return "text/plain";
            case "css":
                return "text/css";
            case "js":
                return "application/javascript";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "jpg":
                return "image/jpeg";
            case "woff":
                return "application/font-woff";
            case "svg":
                return "image/svg+xml";
            case "eot":
                return "application/vnd.ms-fontobject";
            default:
                return null;
        }
    }

    /*
     * Convenience constructors
	 */

    public static WebResponse response(int status, WebPayload body) {
        WebResponse response = new WebResponse();
        response.setStatus(status);
        response.setBody(body);
        return response;
    }

    public static WebResponse response(int status, InputStream body) {
        return response(status, WebPayload.wrap(body));
    }

    public static WebResponse response(int status, String body) {
        return response(status, WebPayload.wrap(body));
    }

    public static WebResponse ok(InputStream body) {
        return response(OK, body);
    }

    public static WebResponse ok(String body) {
        return response(OK, body);
    }

    /**
     * Returns a 404 Not Found response.
     */
    public static WebResponse notFound(String body) {
        return response(NOT_FOUND, body);
    }

    public static WebResponse notFound(InputStream body) {
        return response(NOT_FOUND, body);
    }

    /**
     * Sends an empty response with the given status code and Location header. If appropriate you should
     * use {@link #seeOther(String)}, {@link #redirect(String)} or {@link #movedPermanently(String)}
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
        return redirect(SEE_OTHER, location);
    }

    /**
     * Returns a 307 Temporary Redirect response. The browser should repeat the request against the given
     * URL. However as this is temporary, future requests should still be made against the original URL.
     * <p/>
     * Note that {@link #seeOther(String)} should instead be used when redirecting to a new page after
     * processing a POST.
     */
    public static WebResponse redirect(String location) {
        return redirect(TEMPORARY_REDIRECT, location);
    }

    /**
     * Returns a 301 Moved Permanently response. The browser should repeat the request against the given
     * URL. As this is permanent, future requests should still be made against the new URL.  Browsers often
     * cache this redirect and search engines often replace the original URL with the redirect target in
     * their indexes.
     * <p/>
     * Note that {@link #seeOther(String)} should be used when redirecting to a new page after
     * processing a POST.
     */
    public static WebResponse movedPermanently(String location) {
        return redirect(MOVED_PERMANENTLY, location);
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
        String type = guessContentType(resource.getPath());

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

    /**
     * Halts processing immediately and returns the given response.
     *
     * Throws a WebResponseException to unwind back to the web server.
     */
    public static void halt(WebResponse response) {
        throw new WebResponseException(response);
    }

    static String formatHttpDate(long epochMillis) {
        return RFC_1123_DATE_TIME.format(OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC));
    }
}
