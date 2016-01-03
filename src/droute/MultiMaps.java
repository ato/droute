package droute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class MultiMaps {
    static <K,V> void addEntry(Map<K,List<V>> map, K key, V value) {
        List<V> values = map.get(key);
        if (values == null) {
            values = new ArrayList<>();
            map.put(key, values);
        }
        values.add(value);
    }
}
