package droute.legacy;

import droute.v2.OldMultiMap;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class RequestBuilder  {
    final String method;
    final URI contextUri, uri;
    final OldMultiMap params = new OldMultiMap();
    final OldMultiMap queryParams = new OldMultiMap();
    final OldMultiMap formParams = new OldMultiMap();
    final OldMultiMap urlParams = new OldMultiMap();
    Headers headers = Headers.EMPTY;
    final Map stateMap = new HashMap<>();

    public RequestBuilder(String method, String path) {
        this.method = method;
        this.contextUri = URI.create("http://localhost");
        this.uri = contextUri.resolve(path);
    }

    public RequestBuilder formParam(String key, String value) {
        formParams.put(key, value);
        params.put(key, value);
        return this;
    }

    public RequestBuilder queryParam(String key, String value) {
        queryParams.put(key, value);
        params.put(key, value);
        return this;
    }

    public RequestBuilder urlParam(String key, String value) {
        urlParams.put(key, value);
        params.put(key, value);
        return this;
    }

    public RequestBuilder cookie(String name, String value) {
        String cookie = headers.get("Cookie");
        if (cookie == null) {
            cookie = "";
        } else {
            cookie += ";";
        }
        cookie += name + "=" + value;
        headers = headers.with("Cookie", cookie);
        return this;
    }

    public static RequestBuilder get(String path) {
        return new RequestBuilder("onGET", path);
    }

    public Request build() {
        return new Request() {
            @Override
            public Object raw() {
                return null;
            }

            @Override
            public String method() {
                return method;
            }

            @Override
            public URI uri() {
                return uri;
            }

            @Override
            public Map<String, String> headers() {
                return headers;
            }

            @Override
            public URI contextUri() {
                return contextUri;
            }

            @Override
            public OldMultiMap params() {
                return params;
            }

            @Override
            public OldMultiMap urlParams() {
                return urlParams;
            }

            @Override
            public OldMultiMap queryParams() {
                return queryParams;
            }

            @Override
            public OldMultiMap formParams() {
                return formParams;
            }

            @Override
            public String postBody() {
                return null;
            }

            @Override
            public void setState(Object state) {
                stateMap.put(state.getClass(), state);
            }

            @Override
            public <T> T state(Class<T> state) {
                return (T) stateMap.get(state);
            }
        };
    }

    public static RequestBuilder post(String path) {
        return new RequestBuilder("POST", path);
    }
}
