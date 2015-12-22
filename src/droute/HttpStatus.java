package droute;

/**
 * HTTP Status codes
 */
public final class HttpStatus {
    public final static int CONTINUE = 100;
    public final static int SWITCHING_PROTOCOLS = 101;
    public final static int OK = 200;
    public final static int CREATED = 201;
    public final static int ACCEPTED = 202;
    public final static int NON_AUTHORITATIVE = 203;
    public final static int NO_CONTENT = 204;
    public final static int RESET_CONTENT = 205;
    public final static int PARTIAL_CONTENT = 206;
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
    public final static int RANGE_NOT_SATISFIABLE = 416;
    public final static int EXPECTATION_FAILED = 417;
    public final static int IM_A_TEAPOT = 418;
    public final static int UPGRADE_REQUIRED = 426;
    public final static int INTERNAL_SERVER_ERROR = 500;
    public final static int NOT_IMPLEMENTED = 501;
    public final static int BAD_GATEWAY = 502;
    public final static int SERVICE_UNAVAILABLE = 503;
    public final static int GATEWAY_TIMEOUT = 504;
    public final static int HTTP_VERSION_NOT_SUPPORTED = 505;

    private HttpStatus() {
    }

    /**
     * Returns the recommended (RFC7231 6.1) reason phrase for a given HTTP status code.
     *
     * @return a reason phrase or null if the status code is unknown
     */
    public static String reasonPhrase(int statusCode) {
        switch (statusCode) {
            case CONTINUE:
                return "Continue";
            case SWITCHING_PROTOCOLS:
                return "Switching Protocols";
            case OK:
                return "OK";
            case CREATED:
                return "Created";
            case ACCEPTED:
                return "Accepted";
            case NON_AUTHORITATIVE:
                return "Non-Authoritative Information";
            case NO_CONTENT:
                return "No Content";
            case RESET_CONTENT:
                return "Reset Content";
            case PARTIAL_CONTENT:
                return "Partial Content";
            case MULTIPLE_CHOICES:
                return "Multiple Choices";
            case MOVED_PERMANENTLY:
                return "Moved Permanently";
            case FOUND:
                return "Found";
            case SEE_OTHER:
                return "See Other";
            case NOT_MODIFIED:
                return "Not Modified";
            case USE_PROXY:
                return "Use Proxy";
            case TEMPORARY_REDIRECT:
                return "Temporary Redirect";
            case BAD_REQUEST:
                return "Bad Request";
            case UNAUTHORIZED:
                return "Unauthorized";
            case PAYMENT_REQUIRED:
                return "Payment Required";
            case FORBIDDEN:
                return "Forbidden";
            case NOT_FOUND:
                return "Not Found";
            case METHOD_NOT_ALLOWED:
                return "Method Not Allowed";
            case NOT_ACCEPTABLE:
                return "Not Acceptable";
            case PROXY_AUTHENTICATION_REQUIRED:
                return "Proxy Authentication Required";
            case REQUEST_TIMEOUT:
                return "Request Timeout";
            case CONFLICT:
                return "Conflict";
            case GONE:
                return "Gone";
            case LENGTH_REQUIRED:
                return "Length Required";
            case PRECONDITION_FAILED:
                return "Precondition Failed";
            case PAYLOAD_TOO_LARGE:
                return "Payload Too Large";
            case URI_TOO_LONG:
                return "URI Too Long";
            case UNSUPPORTED_MEDIA_TYPE:
                return "Unsupported Media Type";
            case RANGE_NOT_SATISFIABLE:
                return "Range Not Satisfiable";
            case EXPECTATION_FAILED:
                return "Expectation Failed";
            case IM_A_TEAPOT:
                return "I'm a teapot";
            case UPGRADE_REQUIRED:
                return "Upgrade Required";
            case INTERNAL_SERVER_ERROR:
                return "Internal Server Error";
            case NOT_IMPLEMENTED:
                return "Not Implemented";
            case BAD_GATEWAY:
                return "Bad Gateway";
            case SERVICE_UNAVAILABLE:
                return "Service Unavailable";
            case GATEWAY_TIMEOUT:
                return "Gateway Timeout";
            case HTTP_VERSION_NOT_SUPPORTED:
                return "HTTP Version Not Supported";
            default:
                return null;
        }
    }
}
