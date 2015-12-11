package droute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

class Route implements WebHandler {
    private final static Pattern KEY_PATTERN = Pattern.compile(":([a-z_][a-zA-Z0-9_]*)|\\*");

    private final String method;
    private final WebHandler handler;
    private final String pattern;
    private final Pattern re;
    private final List<String> keys = new ArrayList<>();

    Route(String method, String pattern, WebHandler handler, String... paramsAndRegexs) {
        this.method = method;
        this.handler = handler;
        this.pattern = pattern;
        this.re = compile(paramsAndRegexsToMap(paramsAndRegexs));
    }

    @Override
    public WebResponse handle(WebRequest request) {
        if (method == null || method.equalsIgnoreCase(request.method())) {
            Matcher m = re.matcher(request.path());
            if (m.matches()) {
                MultiMap<String,String> params = new LinkedTreeMultiMap<>();

                for (int i = 0; i < m.groupCount(); i++) {
                    String key = keys.get(i);
                    params.put(key, m.group(i + 1));
                }
                try {
                    return handler.handle(request);
                } finally {

                }
            }
        }
        return WebResponses.NEXT_HANDLER;
    }

    private Pattern compile(Map<String, String> keyRegexs) {
        StringBuilder out = new StringBuilder();
        Matcher m = KEY_PATTERN.matcher(pattern);
        int pos = 0;
        while (m.find(pos)) {
            out.append(Pattern.quote(pattern.substring(pos, m.start())));
            String key = m.group(1);
            if (key == null) { // wildcard
                out.append("(.*?)");
                key = "*";
            } else {
                out.append('(');
                out.append(keyRegexs.getOrDefault(key, "[^/,;?]+"));
                out.append(')');
            }
            keys.add(key);
            pos = m.end();
        }
        out.append(Pattern.quote(pattern.substring(pos)));
        return Pattern.compile(out.toString());
    }

    private static Map<String, String> paramsAndRegexsToMap(String[] pairs) {
        if (pairs.length % 2 != 0) {
            throw new IllegalArgumentException("paramsAndRegexs must consist of param name, regex pairs");
        }
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            try { // syntax check
                Pattern.compile(pairs[i + 1]);
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException("url param " + pairs[i] + " has invalid regex:" + e.getDescription(), e);
            }
            map.put(pairs[i], pairs[i + 1]);
        }
        return map;
    }

    private String prettyRequestMethod() {
        return method == null ? "ANY" : method;
    }

    @Override
    public String toString() {
        return prettyRequestMethod() + "(" + pattern + ", " + handler + ")";
    }
}
