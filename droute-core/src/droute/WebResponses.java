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

/**
 * Factory methods for constructing WebResponse instances.
 * <p/>
 * The static methods and constants in this class are intended to be statically imported:
 * <p/>
 * {@code import static droute.WebResponses.*}
 */
public final class WebResponses {
    /*
     * Status codes
     */
    public final static int CONTINUE = 100;
    public final static int SWITCHING_PROTOCOLS = 101;
    public final static int OK = 200;
    public final static int ACCEPTED = 202;
    public final static int NON_AUTHORATIVE = 203;
    public final static int NO_CONTENT = 204;
    public final static int RESET_CONTENT = 205;
    public final static int MULTIPLE_CHOICES = 300;
    public final static int MOVED_PERMANENTLY = 301;
    public final static int FOUND = 302;
    public final static int SEE_OTHER = 303;
    public final static int NOT_MODIFIED = 304;
    public final static int USE_PROXY = 305;
    public final static int TEMPORARY_REDIRECT = 307;
    public final static int BAD_REQUEST = 400;
    public final static int UNAUTHORIZED = 401;
    public final static int PAYMENT_REQUIRED = 402;
    public final static int FORBIDDEN = 403;
    public final static int NOT_FOUND = 404;
    public final static int METHOD_NOT_ALLOWED = 405;
    public final static int NOT_ACCEPTABLE = 406;
    public final static int PROXY_AUTHENTICATION_REQUIRED = 407;
    public final static int REQUEST_TIMEOUT = 408;
    public final static int CONFLICT = 409;
    public final static int GONE = 410;
    public final static int LENGTH_REQUIRED = 411;
    public final static int PRECONDITION_FAILED = 412;
    public final static int PAYLOAD_TOO_LARGE = 413;
    public final static int URI_TOO_LONG = 414;
    public final static int UNSUPPORTED_MEDIA_TYPE = 415;
    public final static int REQUESTED_RANGE_NOT_SATISFIABLE = 416;
    public final static int EXPECTATION_FAILED = 417;
    public final static int IM_A_TEAPOT = 418;
    public final static int UPGRADE_REQUIRED = 426;
    public final static int INTERNAL_SERVER_ERROR = 500;
    public final static int NOT_IMPLEMENTED = 501;
    public final static int BAD_GATEWAY = 502;
    public final static int SERVICE_UNAVAILABLE = 503;
    public final static int GATEWAY_TIMEOUT = 504;
    public final static int HTTP_VERSION_NOT_SUPPORTED = 505;

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

    private static String formatHttpDate(long epochMillis) {
        return RFC_1123_DATE_TIME.format(OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC));
    }
}
