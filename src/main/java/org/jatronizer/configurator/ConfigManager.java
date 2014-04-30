package org.jatronizer.configurator;

import static org.jatronizer.configurator.ConfigSupport.KeyFormat.*;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.*;

public final class ConfigManager {

	// Can not be instantiated
	private ConfigManager() {}

	private static final String ARG_PREFIX = "-";

	/**
	 * HelpPrinter provides a default format to display a help text on the command line.
	 * An instance can be passed to {@link MultiConfigurator#walk(ConfigurationVisitor)}.
	 */
	private static class HelpPrinter implements ConfigurationVisitor {

		private final OutputStream out;
		private final String envVarPrefix;

		/**
		 * Create a new HelpPrinter printing to {@link java.lang.System#err}.
		 * @param envVarPrefix Prefix used when looking up keys in environment variables.
		 */
		public HelpPrinter(String envVarPrefix) {
			this(null, envVarPrefix);
		}

		/**
		 * Create a new HelpPrinter printing to {@code out}.
		 * @param out Help text is printed here; when {@code out} is {@code null}, {@link java.lang.System#err} is used.
		 * @param envVarPrefix Prefix used when looking up keys in environment variables.
		 */
		public HelpPrinter(OutputStream out, String envVarPrefix) {
			this.out = out == null ? System.err : out;
			this.envVarPrefix = envVarPrefix == null ? "" : envVarPrefix;
		}

		private void forceWrite(String text) {
			try {
				out.write(text.getBytes());
			} catch (IOException e) {
				throw new ConfigurationException(e);
			}
		}

		public void visitModule(String name, String description, Configurator configurator) {
			if ("".equals(name)) {
				return;
			}
			String text = "Module " + name + ":";
			if (!"".equals(description)) {
				text += " " + description;
			}
			text += "\n";
			forceWrite(text);
		}

		public void visitParameter(ConfigParameter parameter, String value) {
			final String EMPTY = "                                ";
			String key = parameter.key();
			String text = "arg[" + arg.from(ARG_PREFIX + key) + "] / env[" + env.from(envVarPrefix + key) + "]\n";
			String description = parameter.description();
			if (!"".equals(description)) {
				text += "\t" + description + "\n";
			}
			String defaultValue = parameter.defaultValue();
			if (value == null) {
				if (defaultValue != null) {
					text += "\tdefault: \"" + defaultValue + "\"\n";
				}
			} else {
				if (defaultValue == null) {
					text += "\tvalue: '" + value + "'\n";
				} else if (value.equals(defaultValue)) {
					text += "\tvalue: '" + value + "' (default)\n";
				} else {
					text += "\tvalue: '" + value + "', default: '" + defaultValue + "'\n";
				}
			}
			String[] options = parameter.options();
			if (options.length > 0) {
				text += "\tavailable values:\n";
				int longest = 0;
				for (String option : options) {
					if (option.length() > longest) {
						longest = option.length();
					}
				}
				for (String option : options) {
					text += "\t  ";
					int paddingNeeded = longest - option.length();
					while (paddingNeeded > EMPTY.length()) {
						text += EMPTY;
						paddingNeeded -= EMPTY.length();
					}
					text += EMPTY.substring(0, longest) + option;
					String pdesc = parameter.description(option);
					if (!"".equals(pdesc)) {
						text += "  " + pdesc;
					}
					text += "\n";
				}
			}
			forceWrite(text);
		}
	}

	/**
	 * Creates a {@code ConfigParameter} using the specified details.
	 * This function can be used as an alternative for external fields not annotated with {@link Parameter}
	 * or if the code in question should not have external dependencies.
	 * Using {@code @Parameter} is strongly preferred.
	 * @param configuration An instance with the parameter field, must not be {@code null}.
	 * @param field The parameter field, must not be {@code null}.
	 * @param key The key representing this parameter; name of the field if {@code null}.
	 * @param converterClass The converter between String and the field type, {@code null} for the default.
	 * @param <C> Type of the configuration.
	 * @param <P> Type of the configuration parameter.
	 */
	public static <C,P> ConfigParameter<C,P> parameter(
			C configuration, Field field, String key, Class<P> converterClass) {
		if (configuration == null) {
			throw new NullPointerException("configuration is null");
		}
		if (field == null) {
			throw new NullPointerException("field is null");
		}
		return ConfigParameterField.create(configuration, field, key, converterClass);
	}

	/**
	 * Fetches all fields annotated with {@link Parameter} from {@code configuration}.
	 * @param configuration An instance with {@code Parameter} annotated fields, must not be {@code null}.
	 * @param keyPrefix A prefix for all keys representing the parameters.
	 * @param <C> Type of the configuration.
	 * @param <P> Type of the configuration parameter.
	 */
	@SuppressWarnings("unchecked")
	public static <C,P> ConfigParameter<C,P>[] parameters(C configuration, String keyPrefix) {
		if (configuration == null) {
			throw new NullPointerException("configuration is null");
		}
		return ConfigSupport.fetchParameters(configuration, keyPrefix);
	}

	/**
	 * Creates a module from a configuration and does not require any annotations.
	 * In most cases, this will be used if the same module should be used twice but with different
	 * key prefixes and another usage.
	 * For example, it enables using smtp module configuration for warn and error logs and for a customer facing
	 * newsletter and keep both separate.
	 * If no customization is needed and {@link Parameter} and optionally also {@code Module} and
	 * {@code Description} annotations are used, {@link #configure(Object[])} should be used with
	 * {@code configuration} as the single argument.
	 * @param configuration An instance containing the fields in {@code params}, must not be {@code null}.
	 * @param name The name of the module as in {@link Module#name()}.
	 * @param keyPrefix A common prefix for all keys in this module as in {@link Module#keyPrefix()}.
	 * @param description A description; an alternative to the {@link Description} annotation.
	 * @param params The configuration parameters, must all reference fields on {@code configuration}.
	 *               If {@code params} is empty, the result of {@link #parameters(Object, String)} is used.
	 * @param <C> Type of the configuration.
	 */
	public static <C> Configurator module(
			C configuration, String name, String keyPrefix, String description, ConfigParameter... params) {
		if (configuration == null) {
			throw new NullPointerException("configuration is null");
		}
		if (params.length == 0) {
			params = parameters(configuration, keyPrefix);
		}
		return InstanceConfigurator.control(configuration, name, keyPrefix, description, params);
	}

	/**
	 * Creates a {@link Configurator} for configurations that have parameters annotated with {@link Parameter} and
	 * that have unique keys.
	 * This is also the preferred way to create a {@code Configurator} for a single module that uses
	 * {@link Parameter} and optionally also {@code Module} and {@code Description} annotations.
	 * @param configurations The configuration instances.
	 * @param <C> A common type for all configurations.
	 *              This is only used for consistency here so {@code C} denotes a configuration type.
	 */
	@SuppressWarnings("unchecked")
	public static <C> Configurator configure(C... configurations) {
		if (configurations.length == 0) {
			throw new ConfigurationException("configurations are empty");
		}
		// C is erased to Object and configure could have been called when manage is meant.
		// Force code corrections by feedback.
		Class cc = configurations[0].getClass();
		if (cc == InstanceConfigurator.class || cc == MultiConfigurator.class) {
			throw new ConfigurationException("Wrong function, call manage instead of configure");
		}
		// create all configurators
		Configurator[] configurators = new Configurator[configurations.length];
		for (int i = 0; i < configurations.length; i++) {
			configurators[i] = InstanceConfigurator.control(configurations[i]);
		}
		return manage(configurators);
	}

	/**
	 * Creates a {@link Configurator} wrapping multiple other configurators.
	 * @param configurators The configurators to be wrapped.
	 */
	@SuppressWarnings("unchecked")
	public static Configurator manage(Configurator... configurators) {
		if (configurators.length == 0) {
			throw new ConfigurationException("configurators are empty");
		}
		if (configurators.length == 1) {
			return configurators[0];
		}
		return MultiConfigurator.configure(configurators);
	}

	/**
	 * Prints a help text for all modules and parameters available in the specified {@link Configurator}.
	 * It also prints environment variable names and command line argument keys (-key=value)
	 * and the current and default values.
	 * @param configurator The {@link Configurator}.
	 * @param envVarPrefix The common prefix for environment variables. It must be the same as in
	 * {@link #getEnv(java.util.Map, String[], String)} and {@link #setFromEnv(Configurator, String)}.
	 * @param out The target output stream. This should be {@code System.err} in most cases.
	 */
	public static void printHelpFor(Configurator configurator, String envVarPrefix, OutputStream out) {
		HelpPrinter help = new HelpPrinter(out, envVarPrefix);
		configurator.walk(help);
	}

	/**
	 * Splits argument pairs in form {@code "-" argkey "=" value} and stores the parameter key and the parsed value
	 * in {@code dst}.
	 * A {@link ConfigParameter#key()} is converted to an {@code argkey} by changing all chars that are not ANSI
	 * letters or digits to dashes ("{@code -}"), prefixing all capital letters with dashes and converting the result
	 * to lower case.
	 * @param dst The {@link java.util.Map} where the key-value pairs are stored. May be {@code null}.
	 * @param dstUnused The {@link java.util.Collection} where all non matching arguments are stored.
	 *                     May be {@code null}.
	 * @param keys All valid keys in their regular format.
	 * @param args All arguments that should be parsed for keys.
	 * @return The number of pairs that were or would have been stored in {@code dst}.
	 */
	public static int getArgs(Map<String, String> dst, Collection<String> dstUnused, String[] keys, String...args) {
		String[] collisions = ConfigSupport.collisions(arg, keys);
		if (collisions.length > 0) {
			throw new ConfigurationException("collisions for command line argument keys: " +
					Arrays.toString(collisions)
			);
		}
		return ConfigSupport.parseValues(dst, dstUnused, keys, ARG_PREFIX, arg, args);
	}

	/**
	 * Stores all keys and their respective values in the environment in {@code dst}.
	 * A {@link ConfigParameter#key()} is converted to an environment variable name by prefixing it with
	 * {@code envVarPrefix}, changing all chars that are not ANSI letters or digits to underscores ("{@code _}"),
	 * prefixing all capital letters with underscores and converting the result to upper case.
	 * @param dst The {@link java.util.Map} where the key-value pairs are stored. May be {@code null}.
	 * @param keys All valid keys in their regular format.
	 * @param envVarPrefix The common prefix for environment variables.
	 * @return The number of pairs that were or would have been stored in {@code dst}.
	 */
	public static int getEnv(Map<String, String> dst, String[] keys, String envVarPrefix) {
		String[] collisions = ConfigSupport.collisions(env, keys);
		if (collisions.length > 0) {
			throw new ConfigurationException("collisions for environment keys: " +
					Arrays.toString(collisions)
			);
		}
		return ConfigSupport.values(dst, keys, envVarPrefix, env, System.getenv());
	}

	/**
	 * Sets configuration options from command line arguments and returns the arguments that could not be
	 * recognized. See {@link #getArgs(java.util.Map, java.util.Collection, String[], String...)} for details.
	 * @param configurator The configurator managing the configuration options.
	 * @param args The command line arguments.
	 * @return Unrecognized arguments.
	 */
	public static String[] setFromArgs(Configurator configurator, String[] args) {
		ArrayList<String> unused = new ArrayList<String>(args.length / 2);
		HashMap<String, String> config = new HashMap<String, String>(args.length, 1.0f);
		getArgs(config, unused, configurator.keys(), args);
		configurator.set(config);
		return unused.toArray(new String[unused.size()]);
	}

	/**
	 * Sets configuration options from environment variables. See {@link #getEnv(java.util.Map, String[],
	 * String)} for details.
	 * @param configurator The configurator managing the configuration options.
	 * @param envVarPrefix The common prefix for environment variables.
	 */
	public static void setFromEnv(Configurator configurator, String envVarPrefix) {
		Map<String, String> src = System.getenv();
		HashMap<String, String> config = new HashMap<String, String>(src.size(), 1.0f);
		getEnv(config, configurator.keys(), envVarPrefix);
		configurator.set(config);
	}
}
