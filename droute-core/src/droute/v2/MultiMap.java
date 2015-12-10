package droute.v2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiMap extends HashMap<String, List<String>> {

    public void put(String key, String value) {
        List<String> list = get(key);
        if (list == null) {
            list = new ArrayList<>();
            put (key, list);
        }
        list.add(value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends List<String>> m) {
        for (Entry<? extends String, ? extends List<String>> entry : m.entrySet()) {
            List<String> list = get(entry.getKey());
            if (list != null) {
                list.addAll(entry.getValue());
            } else {
                put(entry.getKey(), new ArrayList<String>(entry.getValue()));
            }
        }
    }

    public String getFirst(String key) {
        List<String> list = get(key);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }
}
