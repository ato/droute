package droute.v2;

import java.util.*;

class LinkedTreeMultiMap<K,V> implements MultiMap<K,V> {
    private int size = 0;
    private final LinkedTreeMap<K, List<V>> map;

    public LinkedTreeMultiMap(Comparator<? super K> comparator) {
        this.map = new LinkedTreeMap<>(comparator);
    }

    public LinkedTreeMultiMap() {
        this.map = new LinkedTreeMap<>();
    }

    @Override
    public List<V> get(K key) {
        return map.get(key);
    }

    public V getFirst(K key) {
        List<V> values = get(key);
        return values.isEmpty() ? null : values.get(0);
    }

    @Override
    public void put(K key, V value) {
        List<V> values = map.get(key);
        if (values != null) {
            values.add(value);
        } else {
            values = new ArrayList<>();
            values.add(value);
            map.put(key, values);
        }
        size++;
    }

    @Override
    public Map<K, List<V>> asMap() {
        return Collections.unmodifiableMap(map);
    }

    @Override
    public int size() {
        return size;
    }
}
