package droute.v2;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface MultiMap<K, V> {

    List<V> get(K key);

    V getFirst(K key);

    void put(K key, V value);

    Map<K, List<V>> asMap();

    int size();

    void replaceValues(V key, Collection<V> values);

    Iterable<Map.Entry<String,String>> entries();
}
