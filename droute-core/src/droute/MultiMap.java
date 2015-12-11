package droute;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface MultiMap<K, V> {

    List<V> get(K key);

    V getFirst(K key);

    void put(K key, V value);

    Map<K, List<V>> asMap();

    int size();

    List<V> replaceValues(K key, Collection<V> values);

    Iterable<Map.Entry<K,V>> entries();
}
