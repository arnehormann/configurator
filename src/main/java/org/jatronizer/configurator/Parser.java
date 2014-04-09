package org.jatronizer.configurator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

/**
 * Parser provides support functions to parse command line arguments and convert them
 * and environment variables into a form usable by a Configurator, a <code>Map&lt;String, String&gt;</code>.
 */
public final class Parser {

	public static final class Args {
		public Map<String, String> parsed;
		private ArrayList<String> rest;

		public Args(int capacity) {
			this(new HashMap<String, String>(capacity, 1.0f));
		}

		Args(Map<String, String> map) {
			parsed = map;
			rest = new ArrayList<String>();
		}

		/**
		 * retrieves all keys that could not be processed and are not
		 * contained in <code>parsed</code>.
		 */
		public String[] rest() {
			return rest.toArray(new String[rest.size()]);
		}
	}

	private static String prepKey(String key, char sep) {
		// first change each '-', '_', '.', :', '/' to sep
		// then prefix each capital letter with sep
		return key.replaceAll("[\\-_.:/]", "" + sep).replaceAll("([A-Z])", sep + "\\1");
	}

	/**
	 * retrieves a mapping from keys to the default form of argument key names.
	 * An argument key is prefixed with a specified prefix and converted to lower case.
	 * Wherever upper case is used in the key it is interpreted as a word
	 * boundary and a dash ('-') is inserted as a separator in front of it.
	 * Dashes ('-') are used instead of any of these key chars: '_', '.', ':', '/'.
	 * For example, "con_figVar" with a prefix of "-" will become "-con-fig-var".
	 * The result is intended to be used with mapKeys.
	 * @param prefix argument key prefix (e.g. "-")
	 * @param keys names of the configuration parameter keys as retrieved with
	 *               Configurator.keys
	 */
	public static Map<String, String> asArgKeys(String prefix, String...keys) {
		Map<String, String> result = new HashMap<String, String>(keys.length, 1.0f);
		if (prefix == null) {
			prefix = "";
		}
		for (String key : keys) {
			String newKey = prepKey(prefix + key, '-').toLowerCase();
			result.put(newKey ,key);
		}
		return result;
	}

	/**
	 * retrieves a mapping from keys to the default form of environment key names.
	 * An environment key is prefixed with a specified prefix and converted to
	 * upper case. Wherever upper case is used in the key it is interpreted as a word
	 * boundary and an underscore ('_') is inserted as a separator in front of it.
	 * Underscores ('_') are used instead of any of these key chars: '-', '.', ':', '/'.
	 * For example, "con-figVar" with a prefix of "abc_" will become "ABC_CON_FIG_VAR".
	 * The result is intended to be used with mapKeys.
	 * @param prefix unique prefix for environment variables.
	 *               The prefix can be used to avoid conflicts with other environment variables.
	 * @param keys names of the configuration parameter keys as retrieved with
	 *               Configurator.keys
	 */
	public static Map<String, String> asEnvKeys(String prefix, String...keys) {
		Map<String, String> result = new HashMap<String, String>(keys.length, 1.0f);
		if (prefix == null) {
			prefix = "";
		}
		for (String key : keys) {
			String newKey = prepKey(prefix + key, '_').toUpperCase();
			result.put(newKey, key);
		}
		return result;
	}

	/**
	 * retrieves a new map where all keys from map are replaced with their corresponding values in
	 * keyMap. If a key does not occur in keyMap, it is not included in the result.
	 * @return the map (parsed) and the keys not in keyMap (rest)
	 */
	public static Args mapKeys(Map<String, String> keyMap, Map<String, String> map) {
		return mapKeys(keyMap, new Args(map));
	}

	/**
	 * retrieves a new map where all keys from map are replaced with their corresponding values in
	 * keyMap. If a key does not occur in keyMap, it is not included in the result.
	 * @return the map (parsed) and the keys not in keyMap (rest)
	 */
	public static Args mapKeys(Map<String, String> keyMap, Args args) {
		Map<String, String> map = args.parsed;
		Args result = new Args(map.size());
		Map<String, String> pairs = result.parsed;
		Collection<String> rest = result.rest;
		if (args.rest != null) {
			rest.addAll(args.rest);
		}
		for (Map.Entry<String, String> mapping : map.entrySet()) {
			String key = mapping.getKey();
			if (keyMap.containsKey(key)) {
				String newKey = keyMap.get(key);
				pairs.put(newKey, map.get(key));
			} else {
				rest.add(key);
			}
		}
		return result;
	}

	/**
	 * splits Strings at the first occurrence of a separator and returns a map with the first half
	 * as key and the second half as value.
	 * args without the separator are ignored.
	 * @param separator separates key and value (e.g. "=")
	 * @param args the strings to be split
	 * @return the argument map and the unparsed arguments
	 */
	public static Args parse(String separator, String...args) {
		Args result = new Args(args.length);
		Map<String, String> pairs = result.parsed;
		Collection<String> rest = result.rest;
		for (String arg : args) {
			String[] pair = arg.split(separator, 2);
			if (pair.length == 2) {
				pairs.put(pair[0], pair[1]);
			} else {
				rest.add(arg);
			}
		}
		return result;
	}
}
