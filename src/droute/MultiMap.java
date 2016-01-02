package droute;

import java.util.*;

public class MultiMap<K, V> {
    private Map<K, List<V>> map;

    public MultiMap(Map<K, List<V>> map) {
        this.map = map;
    }

    public List<V> set(K key, V value) {
        ArrayList<V> list = new ArrayList<>();
        list.add(value);
        return map.put(key, list);
    }

    public void add(K key, V value) {
        List<V> values = map.get(key);
        if (values != null) {
            values.add(value);
        } else {
            values = new ArrayList<>();
            values.add(value);
            map.put(key, values);
        }
    }

    public V getFirst(K key) {
        List<V> values = getAll(key);
        return values != null ? values.get(0) : null;
    }

    public List<V> getAll(K key) {
        return map.get(key);
    }

    public Map<K, List<V>> asMap() {
        return map;
    }

    public Iterable<Map.Entry<K, V>> entries() {
        return new Iterable<Map.Entry<K, V>>() {
            @Override
            public Iterator<Map.Entry<K, V>> iterator() {
                return new EntryIterator();
            }
        };
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    class EntryIterator implements Iterator<Map.Entry<K, V>> {
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
                    List<V> values = set(key, value);
                    return values != null ? values.get(0) : null;
                }
            };
        }
    }
}
