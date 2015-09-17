package org.jatronizer.configurator;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores errors.
 * An ErrorMap starts empty and can be filled. The methods {@link #fput} and {@link #fputAll} return a mutable,
 * modifiable instance when the first value is inserted.
 */
class ErrorMap extends HashMap<String, String> {

	public static ErrorMap EMPTY = new EmptyMap();

	private static class EmptyMap extends ErrorMap {
		public EmptyMap() {
			super();
		}

		public ErrorMap fput(String key, String value) {
			ErrorMap map = new ErrorMap();
			return map.fput(key, value);
		}

		public ErrorMap fputAll(Map<String, String> values) {
			if (values.isEmpty()) {
				return this;
			}
			ErrorMap map = new ErrorMap();
			return map.fputAll(values);
		}
	}

	private ErrorMap() {
		super();
	}

	/**
	 * Adds a key-value pair to the entries and returns a map containing all these pairs.
	 * @param key the map key.
	 * @param value the map value. If the key was already in the map, the old value is overwritten.
	 * @return a map containing the original and the added new key-value pairs.
	 */
	public ErrorMap fput(String key, String value) {
		super.put(key, value);
		return this;
	}

	/**
	 * Adds all key-value pairs to the entries and returns a map containing all these pairs.
	 * @param values the map with all key-value pairs that should be added.
	 *               Existing entries with the same key are overwritten.
	 * @return a map containing the original and the added new key-value pairs.
	 */
	public ErrorMap fputAll(Map<String, String> values) {
		if (!values.isEmpty()) {
			this.putAll(values);
		}
		return this;
	}
}
