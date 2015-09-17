package org.jatronizer.configurator;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.*;

final class ConfigSupport {

	// Static class without instances, constructor is hidden
	private ConfigSupport() {}

	/**
	 * Fetches all fields annotated with {@link Parameter} from {@code configuration}.
	 * @param configuration An instance with {@code Parameter} annotated fields, must not be {@code null}.
	 * @param keyPrefix A prefix for all keys representing the parameters.
	 * @return The configuration parameters contained in the specified configuration.
	 */
	public static ConfigParameter[] fetchParameters(Object configuration, String keyPrefix) {
		Class cc = configuration.getClass();
		Field[] fields = cc.getDeclaredFields();
		ArrayList<ConfigParameter> conf = new ArrayList<ConfigParameter>(fields.length);
		if (keyPrefix == null) {
			keyPrefix = "";
		}
		for (Field f : fields) {
			Parameter p = f.getAnnotation(Parameter.class);
			if (p != null) {
				String key = p.key();
				if ("".equals(key)) {
					key = f.getName();
				}
				conf.add(ConfigParameterField.create(
						configuration,
						f,
						keyPrefix + key,
						p.tag(),
						p.converter()
				));
			}
		}
		if (conf.isEmpty()) {
			throw new ConfigException(
					"configure " +
					cc +
					" has no fields annotated with " +
					Parameter.class.getSimpleName()
			);
		}
		ConfigParameter[] parameters = conf.toArray(new ConfigParameter[conf.size()]);
		Arrays.sort(parameters, new Comparator<ConfigParameter>() {
			public int compare(ConfigParameter o1, ConfigParameter o2) {
				return o1.key().compareTo(o2.key());
			}
		});
		return parameters;
	}

	public interface KeyFormatter {
		String from(String key);
	}

	public enum KeyFormat implements KeyFormatter {
		arg {
			public String from(String key) {
				return formatKey(key, "-").replaceAll("--+", "-").toLowerCase();
			}
		},
		env {
			public String from(String key) {
				return formatKey(key, "_").replaceAll("__+", "-").toUpperCase();
			}
		};

		private static String formatKey(String key, String separator) {
			return key
					// prefix each sequence of capital letters with separator
					.replaceAll("([A-Z]+)", separator + "\\1")
					// change all non alphanumeric char sequences to separator
					.replaceAll("[^A-Za-z0-9]+", separator)
			;
		}
	}

	/**
	 * Retrieves the description of elem if it is annotated with {@link Description}.
	 * @param elem The element with a {@code Description} annotation.
	 * @return value of the {@code Description} or {@code ""};
	 */
	public static String description(AnnotatedElement elem) {
		Description d = elem.getAnnotation(Description.class);
		if (d == null) {
			return "";
		}
		return d.value();
	}

	public static String[] collisions(KeyFormatter format, String[] keys) {
		HashMap<String, String> map = new HashMap<String, String>(keys.length, 1.0f);
		ArrayList<String> collisions = new ArrayList<String>();
		for (String key : keys) {
			String mapped = format.from(key);
			String clashing = map.get(mapped);
			if (clashing != null) {
				if (!collisions.contains(clashing)) {
					collisions.add(clashing);
				}
				collisions.add(key);
			}
			map.put(mapped, key);
		}
		String[] result = collisions.toArray(new String[collisions.size()]);
		Arrays.sort(result);
		return result;
	}

	public static int values(
			Map<String, String> dest,
			String[] keys,
			String keyPrefix,
			KeyFormatter format,
			Map<String, String> src) {
		if (keyPrefix == null) {
			keyPrefix = "";
		}
		int numSet = 0;
		for (String key : keys) {
			String mapped = format.from(keyPrefix + key);
			String value = src.get(mapped);
			if (value != null) {
				if (dest != null) {
					dest.put(key, value);
				}
				numSet++;
			}
		}
		return numSet;
	}

	public static int parseValues(
			Map<String, String> dest,
			Collection<String> destUnused,
			String[] keys,
			String keyPrefix,
			KeyFormatter format,
			String[] src) {
		if (keyPrefix == null) {
			keyPrefix = "";
		}
		HashMap<String, String> map = new HashMap<String, String>(keys.length, 1.0f);
		for (String key : keys) {
			map.put(format.from(keyPrefix + key), key);
		}
		int numSet = 0;
		for (String raw : src) {
			String[] pair = raw.split("=", 2);
			if (pair.length == 2 && map.containsKey(pair[0])) {
				if (dest != null) {
					dest.put(map.get(pair[0]), pair[1]);
				}
				numSet++;
			} else if (destUnused != null) {
				destUnused.add(raw);
			}
		}
		return numSet;
	}
}
