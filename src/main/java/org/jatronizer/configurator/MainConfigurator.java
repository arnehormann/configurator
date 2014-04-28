package org.jatronizer.configurator;

import java.util.*;

public class MainConfigurator {

	public static interface ModuleVisitor {
		void visit(String name, String prefix, String description);
	}

	public static interface ParameterVisitor {
		void visit(String key, String argKey, String envKey, String defaultValue, String value, String description,
		           String ...optionDescPairs);
	}

	private static class Key {
		String key;
		String env;
		String arg;
		int configurator;
	}

	private static String prepKey(String key, char sep) {
		// first change each not alphanumeric char to sep
		// then prefix each capital letter with sep
		return key.replaceAll("[^A-Za-z0-9]", "" + sep).replaceAll("([A-Z])", sep + "\\1");
	}

	/**
	 * maps a single key to the default form of argument key names.
	 * An argument key is prefixed with a specified prefix and converted to lower case.
	 * Wherever upper case is used in the key it is interpreted as a word
	 * boundary and a dash ('-') is inserted as a separator in front of it.
	 * Dashes ('-') are used instead of any character that does not match [a-zA-Z0-9].
	 * english alphabet.
	 * For example, "con_figVar" with a prefix of "-" will become "-con-fig-var".
	 * @param prefix argument key prefix (e.g. "-")
	 * @param key configuration parameter key
	 */
	public static String asArgKey(String prefix, String key) {
		if (prefix == null) {
			prefix = "";
		}
		return prepKey(prefix + key, '-').toLowerCase();
	}

	/**
	 * maps a single key to the default form of environment key names.
	 * An environment key is prefixed with a specified prefix and converted to
	 * upper case. Wherever upper case is used in the key it is interpreted as a word
	 * boundary and an underscore ('_') is inserted as a separator in front of it.
	 * Underscores ('_') are used instead of any character that does not match [a-zA-Z0-9].
	 * For example, "con-figVar" with a prefix of "abc_" will become "ABC_CON_FIG_VAR".
	 * @param prefix unique prefix for environment variables.
	 *               The prefix can be used to avoid conflicts with other environment variables.
	 * @param key configuration parameter key
	 */
	public static String asEnvKey(String prefix, String key) {
		if (prefix == null) {
			prefix = "";
		}
		return prepKey(prefix + key, '_').toUpperCase();
	}

	private Configurator[] configurators;
	private String[] keys;
	private int[] configuratorMap; // key index to configurator index
	private Map<String, String> argkeys;
	private Map<String, String> envkeys;
	private String[] unknownArgs;
	private final String argPrefix;
	private final String argAssign;
	private final String envPrefix;

	public MainConfigurator(String envPrefix, String[] args, Object... configurations) {
		this.argPrefix = "-";
		this.argAssign = "=";
		this.envPrefix = envPrefix == null ? "" : envPrefix;
		// create sorted array of configurators
		Configurator[] configurators = new Configurator[configurations.length];
		for (int i = 0; i < configurations.length; i++) {
			configurators[i] = Configurator.control(configurations[i]);
		}
		Arrays.sort(configurators, new Comparator<Configurator>() {
			public int compare(Configurator o1, Configurator o2) {
				return o1.prefix().compareTo(o2.prefix());
			}
		});
		this.configurators = configurators;
		// create sorted array of available keys and detect key collisions
		ArrayList<Key> keylist = new ArrayList<Key>();
		for (int i = 0; i < configurators.length; i++) {
			Configurator configurator = configurators[i];
			String[] keys = configurator.keys();
			for (String key : keys) {
				Key k = new Key();
				k.key = key;
				k.arg = asArgKey(argPrefix, key);
				k.env = asEnvKey(envPrefix, key);
				k.configurator = i;
				keylist.add(k);
			}
		}
		Key[] keys = keylist.toArray(new Key[keylist.size()]);
		Arrays.sort(keys, new Comparator<Key>() {
			public int compare(Key o1, Key o2) {
				return o1.key.compareTo(o2.key);
			}
		});
		// generate maps and detect key collisions
		int[] configuratorMap = new int[keys.length];
		String lastKey = null;
		String lastArg = null;
		String lastEnv = null;
		Map<String, String> argkeys = new HashMap<String, String>(keys.length, 1.0f);
		Map<String, String> envkeys = new HashMap<String, String>(keys.length, 1.0f);
		for (int i = 0; i < keys.length; i++) {
			Key k = keys[i];
			if (k.key.equals(lastKey) || k.arg.equals(lastArg) || k.env.equals(lastEnv)) {
				// a rather generic Exception, but it's targeting developers and not users.
				throw new ConfigurationException("duplicate key " + k.key + " for key, argument or environment variable");
			}
			lastKey = k.key;
			lastArg = k.arg;
			lastEnv = k.env;
			// update mappings
			configuratorMap[i] = k.configurator;
			argkeys.put(k.arg, k.key);
			envkeys.put(k.env, k.key);
		}
		this.configuratorMap = configuratorMap;
		this.argkeys = argkeys;
		this.envkeys = envkeys;
		// split arguments into key=value map with valid keys and rest
		Map<String, String> argpairs = new HashMap<String, String>(args.length, 1.0f);
		ArrayList<String> argrest = new ArrayList<String>();
		for (String arg : args) {
			String[] pair = arg.split(argAssign, 2);
			if (pair.length == 2 && argkeys.containsKey(pair[0])) {
				argpairs.put(pair[0], pair[1]);
			} else {
				argrest.add(arg);
			}
		}
		// get environment variables
		Map<String, String> envpairs = new HashMap<String, String>(System.getenv());
		// set arguments and environment variables
		for (int i = 0; i < keys.length; i++) {
			Key key = keys[i];
			Configurator conf = configurators[configuratorMap[i]];
			String value = argpairs.get(key.arg);
			if (value != null) {
				conf.set(key.key, value);
				argpairs.remove(key.arg); // update argmap to see unused args
				continue;
			}
			value = envpairs.get(key.env);
			if (value != null) {
				conf.set(key.key, value);
				continue;
			}
		}
		this.unknownArgs = argrest.toArray(new String[argrest.size()]);
	}

	public String[] unknownArgs() {
		return unknownArgs.clone();
	}

	public void walk(ModuleVisitor mf, ParameterVisitor pf) {
		for (Configurator conf : configurators) {
			mf.visit(conf.name(), conf.prefix(), conf.description());
			for (String key : conf.keys()) {
				String[] enumNames = conf.options(key);
				String[] enumPairs = new String[2 * enumNames.length];
				for (int i = 0; i < enumNames.length; i++) {
					enumPairs[2*i] = enumNames[i];
					enumPairs[2*i+1] = conf.description(key, enumNames[i]);
				}
				pf.visit(
						key, asArgKey(argPrefix, key), asEnvKey(envPrefix, key),
						conf.defaultValue(key), conf.value(key),
						conf.description(key),
						enumPairs
				);
			}
		}
	}
}
