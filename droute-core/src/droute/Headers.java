package droute;

import java.util.Map;

/**
 * A Map which is case-insensitive for lookups but preserves both case and order of keys.
 */
public class Headers extends ArrayMap<String, String> {

	public static final Headers EMPTY = new Headers(new Object[0]);

	public Headers(Object[] array) {
		super(array);
	}
	
	public Headers(Map<String, String> map) {
		super(map);
	}

	public static Headers of(String... headersAndValues) {
		return new Headers(headersAndValues);
	}
	
	@Override
	protected int indexOf(Object obj) {
		if (obj instanceof String) {
			String key = (String) obj;
			for (int i = 0; i < array.length; i += 2) {
				Object candidate = array[i];
				if (candidate instanceof String
						&& key.equalsIgnoreCase((String) candidate)) {
					return i;
				}
			}
		}
		return -1;
	}

	@Override
	public Headers with(String key, String value) {
		return new Headers(arrayWith(key, value));
	}

	@Override
	public Headers without(String key) {
		Object[] array = arrayWithout(key);
		if (array == this.array) {
			return this;
		} else {
			return new Headers(array);
		}
	}
}
