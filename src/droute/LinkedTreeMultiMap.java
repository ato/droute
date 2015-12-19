package droute;

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
        return values == null || values.isEmpty() ? null : values.get(0);
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

    @Override
    public List<V> replaceValues(K key, Collection<V> values) {
        List<V> oldValues = map.get(key);
        map.put(key, new ArrayList<V>(values));
        return oldValues;
    }

    @Override
    public Iterable<Map.Entry<K, V>> entries() {
        return () -> new Iterator<Map.Entry<K, V>>() {
            Iterator<Map.Entry<K, List<V>>> outer = map.entrySet().iterator();
            Iterator<V> inner = null;
            K key;

            @Override
            public boolean hasNext() {
                while (inner == null || !inner.hasNext()) {
                    if (outer.hasNext()) {
                        Map.Entry<K, List<V>> entry = outer.next();
                        key = entry.getKey();
                        inner = entry.getValue().iterator();
                    } else {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public Map.Entry<K, V> next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                return new Map.Entry<K, V>() {
                    V value = inner.next();

                    @Override
                    public K getKey() {
                        return key;
                    }

                    @Override
                    public V getValue() {
                        return value;
                    }

                    @Override
                    public V setValue(V value) {
                        throw new UnsupportedOperationException("TODO: not implemented");
                    }
                };
            }
        };
    }

    @Override
    public boolean containsKey(K key) {
        List<V> values = get(key);
        return values != null && !values.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("{");
        for (Map.Entry<K, V> entry : entries()) {
            if (out.length() > 1) {
                out.append(", ");
            }
            out.append(entry.getKey());
            out.append("=");
            out.append(entry.getValue());
        }
        out.append("}");
        return out.toString();
    }
}
