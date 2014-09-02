package droute;

import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings("unchecked")
class ArrayMap<K, V> implements Map<K,V> {
	private static final ArrayMap<?, ?> EMPTY = new ArrayMap<>(new Object[0]);

	public static <K,V> ArrayMap<K, V> emptyMap() {
		return (ArrayMap<K, V>) EMPTY;
	}
	
	public static <K,V> ArrayMap<K, V> of(Object... keysAndValues) {
		return new ArrayMap<>(keysAndValues);
	}
	
	protected final Object[] array;	
	
	ArrayMap(Object[] array) {
		if (array.length % 2 != 0) {
			throw new IllegalArgumentException("an array of even legnth is required");
		}
		this.array = array;
	}

	ArrayMap(Map<K, V> map) {
		array = new Object[map.size() * 2];
		int i = 0;
		for (Map.Entry<K, V> entry : map.entrySet()) {
			array[i++] = entry.getKey();
			array[i++] = entry.getValue();
		}
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("ArrayMaps cannot be modified in place");
	}
	
	protected int indexOf(Object key) {
		if (key != null) {
			for (int i = 0; i < array.length; i += 2) {
				if (key.equals(array[i])) {
					return i;
				}
			}
		}
		return -1;
	}

	@Override
	public boolean containsKey(Object key) {
		return indexOf(key) >= 0;
	}
	
	protected Object[] arrayWith(K key, V value) {
		int i = indexOf(key);
		if (i == -1) {
			int size = this.array.length; 
			Object[] array = new Object[size + 2];
			System.arraycopy(this.array, 0, array, 0, size);
			array[size] = key; 
			array[size + 1] = value;
			return array;
		} else {
			Object[] array = this.array.clone();
			array[i] = key;
			array[i + 1] = value;
			return array;
		}
	}
	
	public ArrayMap<K,V> with(K key, V value) {
		return new ArrayMap<K,V>(arrayWith(key, value));
	}
	
	protected Object[] arrayWithout(K key) {
		int i = indexOf(key);
		if (i == -1) {
			return array;
		} else {
			int newSize = this.array.length - 2;
			Object[] array = new Object[newSize];
			System.arraycopy(this.array, 0, array, 0, i);
			System.arraycopy(this.array, i + 2, array, i, newSize);
			return array;
		}
	}

	public ArrayMap<K,V> without(K key) {
		Object[] array = arrayWithout(key);
		if (array == this.array) {
			return this;
		} else {
			return new ArrayMap<K,V>(array);
		}
	}
	
	@Override
	public boolean containsValue(Object value) {
		for (int i = 1; i < array.length; i += 2) {
			if (Objects.equals(array[i], value)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return new AbstractSet<java.util.Map.Entry<K, V>>() {

			@Override
			public Iterator<java.util.Map.Entry<K, V>> iterator() {
				return new Iterator<java.util.Map.Entry<K, V>>() {
					int i = 0;
					
					@Override
					public boolean hasNext() {
						return i < array.length;
					}

					@Override
					public java.util.Map.Entry<K, V> next() {
						final int pos = i;
						i += 2;
						return new Map.Entry<K, V>() {

							@Override
							public K getKey() {
								return (K)array[pos];
							}

							@Override
							public V getValue() {
								return (V)array[pos + 1];
							}

							@Override
							public V setValue(V value) {
								return put(getKey(), value);
							}
						};
					}
				};
			}

			@Override
			public int size() {
				return array.length / 2;
			}
		};
	}

	@Override
	public V get(Object key) {
		int i = indexOf(key);
		if (i >= 0) {
			return (V)array[i + 1];
		} else {
			return null;
		}
	}

	@Override
	public boolean isEmpty() {
		return array.length != 0;
	}

	@Override
	public Set<K> keySet() {
		return new AbstractSet<K>() {

			@Override
			public Iterator<K> iterator() {
				return new Iterator<K>() {
					int i = 0;
					
					@Override
					public boolean hasNext() {
						return i < array.length;
					}

					@Override
					public K next() {
						K k = (K)array[i];
						i += 2;
						return k;
					}
				};
			}

			@Override
			public int size() {
				return array.length / 2;
			}
			
		};
	}

	@Override
	public V put(K key, V value) {
		throw new UnsupportedOperationException("ArrayMaps cannot be modified in place");
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		throw new UnsupportedOperationException("ArrayMaps cannot be modified in place");
	}

	@Override
	public V remove(Object key) {
		throw new UnsupportedOperationException("ArrayMaps cannot be modified in place");
	}

	@Override
	public int size() {
		return array.length / 2;
	}

	@Override
	public Collection<V> values() {
		return new AbstractList<V>() {

			@Override
			public V get(int i) {
				return (V)array[i / 2 + 1];
			}

			@Override
			public int size() {
				return array.length / 2;
			}
			
		};
	}
}
