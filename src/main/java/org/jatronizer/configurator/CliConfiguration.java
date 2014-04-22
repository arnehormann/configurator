package org.jatronizer.configurator;

import java.io.PrintStream;
import java.util.*;

public class CliConfiguration {

	private static class Key {
		String key;
		String env;
		String arg;
		int configurator;
	}

	private static enum AnsiMode {
		//reset(0),
		bold(1),
		//blink(5),
		//reverse(7),
		fgBlack(30),
		fgRed(31),
		fgGreen(32),
		fgYellow(33),
		fgBlue(34),
		fgMagenta(35),
		fgCyan(36),
		fgWhite(37),
		bgBlack(40),
		bgRed(41),
		bgGreen(42),
		bgYellow(43),
		bgBlue(44),
		bgMagenta(45),
		bgCyan(46),
		bgWhite(47);

		public final String code;

		private AnsiMode(int code) {
			this.code = "" + code;
		}
	}

	private static String wrap(String text, AnsiMode...mode) {
		if (mode.length == 0) {
			return text;
		}
		String result = "\u001b[" + mode[0].code;
		for (int i = 1; i < mode.length; i++) {
			result += ";" + mode[i].code;
		}
		return result + "m" + text + "\u001b[0m";
	}

	private static final String TEXT_KEY;
	private static final String TEXT_ARG_KEY;
	private static final String TEXT_ENV_KEY;
	private static final String TEXT_DESCRIPTION;
	private static final String TEXT_VALUE;
	private static final String TEXT_DEFAULT;

	static {
		// see http://en.wikipedia.org/wiki/ANSI_escape_code
		if ("1".equals(System.getenv("CLICOLOR"))) {
			TEXT_KEY     = wrap("%1$s",AnsiMode.fgWhite,  AnsiMode.bgGreen);
			TEXT_ARG_KEY = wrap("%2$s",AnsiMode.fgWhite,  AnsiMode.bgCyan);
			TEXT_ENV_KEY = wrap("%3$s",AnsiMode.fgWhite,  AnsiMode.bgBlue);
			TEXT_DESCRIPTION  = "%4$s";
			TEXT_VALUE   = wrap("%5$s",AnsiMode.fgYellow, AnsiMode.bgBlack);
			TEXT_DEFAULT = wrap("%6$s",AnsiMode.fgCyan,   AnsiMode.bgBlack);
		} else {
			TEXT_KEY         = "%1$s";
			TEXT_ARG_KEY     = "%2$s";
			TEXT_ENV_KEY     = "%3$s";
			TEXT_DESCRIPTION = "%4$s";
			TEXT_VALUE       = "%5$s";
			TEXT_DEFAULT     = "%6$s";
		}
	}

	private static String prepKey(String key, char sep) {
		// first change each '-', '_', '.', :', '/' to sep
		// then prefix each capital letter with sep
		return key.replaceAll("[\\-_.:/]", "" + sep).replaceAll("([A-Z])", sep + "\\1");
	}

	/**
	 * maps a single key to the default form of argument key names.
	 * An argument key is prefixed with a specified prefix and converted to lower case.
	 * Wherever upper case is used in the key it is interpreted as a word
	 * boundary and a dash ('-') is inserted as a separator in front of it.
	 * Dashes ('-') are used instead of any of these key chars: '_', '.', ':', '/'.
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
	 * Underscores ('_') are used instead of any of these key chars: '-', '.', ':', '/'.
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

	private static void print(
			PrintStream out,
			String key, String argkey, String envkey,
			String description,
			String value, String defaultValue) {
		String format = TEXT_KEY + " / " + TEXT_ARG_KEY + " / " + TEXT_ENV_KEY + "\n";
		if (!"".equals(description)) {
			format += "\t" + TEXT_DESCRIPTION + "\n";
		}
		if (value == null) {
			if (defaultValue != null) {
				format += "\tdefault: '" + TEXT_DEFAULT + "'\n";
			}
		} else {
			if (defaultValue == null) {
				format += "\tvalue: '" + TEXT_VALUE + "'\n";
			} else if (value.equals(defaultValue)) {
				format += "\tvalue: '" + TEXT_VALUE + "' (default)\n";
			} else {
				format += "\tvalue: '" + TEXT_VALUE + "', default: '" + TEXT_DEFAULT + "'\n";
			}
		}
		out.printf(format, key, argkey, envkey, description, value, defaultValue);
	}

	private Configurator[] configurators;
	private String[] keys;
	private int[] configuratorMap; // key index to configurator index
	private Map<String, String> argkeys;
	private Map<String, String> envkeys;
	private String[] unknownArgs;

	public CliConfiguration(String envPrefix, String[] args, Object...configurations) {
		final String argstart = "-";
		final String argsep = "=";
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
				k.arg = asArgKey(argstart, key);
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
				throw new ReflectionException("duplicate key " + k.key + " for key, argument or environment variable");
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
		// split arguments into key=value map and rest
		Map<String, String> argpairs = new HashMap<String, String>(args.length, 1.0f);
		ArrayList<String> argrest = new ArrayList<String>();
		for (String arg : args) {
			String[] pair = arg.split(argsep, 2);
			if (pair.length == 2) {
				argpairs.put(pair[0], pair[1]);
			} else {
				argrest.add(arg);
			}
		}
		// get environment variables
		Map<String, String> envpairs = new HashMap<String, String>(System.getenv());
		// process arguments and environment variables
		for (int i = 0; i < keys.length; i++) {
			Key key = keys[i];
			Configurator conf = configurators[configuratorMap[i]];
			String value = argpairs.get(key.arg);
			if (value != null) {
				conf.process(new ChangeEvent(key.key, value));
				argpairs.remove(key.arg); // update argmap to see unused args
				continue;
			}
			value = envpairs.get(key.env);
			if (value != null) {
				conf.process(new ChangeEvent(key.key, value));
				continue;
			}
		}
		for (String argkey : argpairs.keySet()) {
			// put remainder back into unprocessed arguments
			argrest.add(argkey + argsep + argpairs.get(argkey));
		}
		this.unknownArgs = argrest.toArray(new String[argrest.size()]);
	}

	public String[] unknownArgs() {
		return unknownArgs.clone();
	}

	public void printHelp(PrintStream out) {
		for (Configurator conf : configurators) {
			for (String key : conf.keys()) {
				print(
					out,
					key, argkeys.get(key), envkeys.get(key),
					conf.description(key),
					conf.value(key),
					conf.defaultValue(key)
				);
			}
		}
	}

	public void printCurrent(PrintStream out, boolean all) {
		for (Configurator conf : configurators) {
			for (String key : conf.keys()) {
				String value = conf.value(key);
				String defaultValue = conf.defaultValue(key);
				if (all || (value != defaultValue || (value != null && value.equals(defaultValue)))) {
					out.printf("%s=%s\n", key, value);
				}
			}
		}
	}
}
