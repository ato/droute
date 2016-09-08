package org.meshy.leanhttp;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

/**
 * Factory methods for constructing WebResponse instances.
 * <p/>
 * The static methods and constants in this class are intended to be statically imported:
 * <p/>
 * {@code import static org.meshy.leanhttp.WebResponses.*}
 */
public final class HttpResponses {
    private HttpResponses() {
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

    public static HttpResponse response(int status, HttpPayload body) {
        HttpResponse response = new HttpResponse();
        response.setStatus(status);
        response.setBody(body);
        return response;
    }

    public static HttpResponse response(int status, InputStream body) {
        return response(status, new InputStreamPayload(body));
    }

    public static HttpResponse response(int status, String body) {
        return response(status, new ByteArrayPayload(body.getBytes(UTF_8)));
    }

    public static HttpResponse ok(InputStream body) {
        return response(HttpStatus.OK, body);
    }

    public static HttpResponse ok(String body) {
        return response(HttpStatus.OK, body);
    }

    /**
     * Returns a 404 Not Found response.
     */
    public static HttpResponse notFound(String body) {
        return response(HttpStatus.NOT_FOUND, body);
    }

    public static HttpResponse notFound(InputStream body) {
        return response(HttpStatus.NOT_FOUND, body);
    }

    /**
     * Sends an empty response with the given status code and Location header. If appropriate you should
     * use {@link #seeOther(String)}, {@link #redirect(String)} or {@link #movedPermanently(String)}
     * instead of calling this method directly.
     */
    public static HttpResponse redirect(int status, String location) {
        Objects.requireNonNull(status, "location");
        HttpResponse response = new HttpResponse();
        response.setStatus(status);
        response.setHeader("Location", location);
        return response;
    }

    /**
     * Returns a 303 See Other response. Even if the initial request was a POST the browser will fetch
     * the redirect location using GET.
     */
    public static HttpResponse seeOther(String location) {
        return redirect(HttpStatus.SEE_OTHER, location);
    }

    /**
     * Returns a 307 Temporary Redirect response. The browser should repeat the request against the given
     * URL. However as this is temporary, future requests should still be made against the original URL.
     * <p/>
     * Note that {@link #seeOther(String)} should instead be used when redirecting to a new page after
     * processing a POST.
     */
    public static HttpResponse redirect(String location) {
        return redirect(HttpStatus.TEMPORARY_REDIRECT, location);
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
    public static HttpResponse movedPermanently(String location) {
        return redirect(HttpStatus.MOVED_PERMANENTLY, location);
    }

    /**
     * Sends a classpath resource to the client.  Sets the Last-Modified, Content-Length and Content-Type
     * headers when possible.
     */
    public static HttpResponse resource(URL resource) throws IOException {
        Objects.requireNonNull(resource, "resource");

        URLConnection conn = resource.openConnection();
        long lastModified = conn.getLastModified();
        long length = conn.getContentLengthLong();
        String type = guessContentType(resource.getPath());

        HttpResponse response = ok(conn.getInputStream());
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
    public static void halt(HttpResponse response) {
        throw new HttpResponseException(response);
    }

    static String formatHttpDate(long epochMillis) {
        return RFC_1123_DATE_TIME.format(OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC));
    }

    /**
     * Constructs a handler that returns a copy of the given response.
     */
    public static HttpHandler handler(final HttpResponse response) {
        return new HttpHandler() {
            @Override
            public HttpResponse handle(HttpRequest request) throws IOException {
                return new HttpResponse(response);
            }
        };
    }
}