package org.jatronizer.configurator;

import static org.jatronizer.configurator.ConfigSupport.KeyFormat.*;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Creates managed configurations.
 */
public final class ConfigManager {

	// Static class without instances, constructor is hidden
	private ConfigManager() {}

	// Command line arguments start with a dash
	static final String ARG_PREFIX = "-";

	/**
	 * Retrieves the default converter for all primitive types, their boxed forms, Strings and enums.
	 * {@code converterFor} does not handle arrays.
	 * If {@code type} is {@code null} or {@code Void.class}, it returns a converter returning {@code null}.
	 * @param type Class of the converted type (e.g. int.class, Boolean.class, MyEnum.class).
	 * @param <T> The converted type.
	 * @return The fitting Converter or {@code null}.
	 */
	public static <T> Converter<T> converter(Class<T> type) {
		return Converters.converterFor(type);
	}

	/**
	 * Creates a {@code ConfigParameter}.
	 * This function can be used as an alternative for fields in classes not annotated with {@link Parameter},
	 * e.g. for code from external libraries not under your control or code that must not have a dependency on this
	 * library.
	 * It is simpler to use {@code @Parameter} annotations and call {@link #parameters} instead.
	 * @param configuration An instance with the parameter field, must not be {@code null}.
	 * @param field The parameter field, must not be {@code null}.
	 * @param key The key representing this parameter as in {@link Parameter#key}; name of the field if {@code null}.
	 * @param tag An optional tag or a space separated list of tags as in {@link Parameter#tag}.
	 * @param converterClass The converter between String and the field type, {@code null} for the default.
	 *        The converter corresponds to {@link Parameter#converter}.
	 * @param <C> Type of the configuration.
	 * @param <P> Type of the configuration parameter.
	 * @return The configuration parameter specified by the arguments.
	 */
	public static <C,P> ConfigParameter<C,P> parameter(
			C configuration, Field field, String key, String tag, Class<P> converterClass) {
		if (configuration == null) {
			throw new NullPointerException("configuration is null");
		}
		if (field == null) {
			throw new NullPointerException("field is null");
		}
		return ConfigParameterField.create(configuration, field, key, tag, converterClass);
	}

	/**
	 * Fetches all fields annotated with {@link Parameter} from {@code configuration}.
	 * @param configuration An instance with {@code Parameter} annotated fields, must not be {@code null}.
	 * @param keyPrefix A prefix for all keys representing the parameters.
	 * @param <C> Type of the configuration.
	 * @param <P> Type of the configuration parameter.
	 * @return The configuration parameters contained in the specified configuration.
	 */
	@SuppressWarnings("unchecked")
	public static <C,P> ConfigParameter<C,P>[] parameters(C configuration, String keyPrefix) {
		if (configuration == null) {
			throw new NullPointerException("configuration is null");
		}
		return ConfigSupport.fetchParameters(configuration, keyPrefix);
	}

	/**
	 * Creates a module from a configuration that does not use the {@link Module} annotation.
	 * In most cases, this will be used if the same module should be used twice but with different
	 * key prefixes and another usage.
	 * If no customization is needed and {@link Parameter} and optionally also {@code Module} and
	 * {@code Description} annotations are used, {@link #configure(Object[])} should be used with
	 * {@code configuration} as the single argument.
	 * @param configuration An instance containing the fields in {@code params}, must not be {@code null}.
	 * @param name The name of the module as in {@link Module#name}.
	 * @param keyPrefix A common prefix for all keys in this module as in {@link Module#keyPrefix}.
	 * @param tag A tag as in {@link Module#tag}.
	 * @param description A description, an alternative to the {@link Description} annotation.
	 * @param params The configuration parameters, must all reference fields on {@code configuration}.
	 *               If {@code params} is empty, the result of {@link #parameters(Object, String)} is used.
	 * @param <C> Type of the configuration.
	 * @return Configurator for the module.
	 */
	public static <C> Configurator module(
			C configuration, String name, String keyPrefix, String tag, String description, ConfigParameter... params) {
		if (configuration == null) {
			throw new NullPointerException("configuration is null");
		}
		if (params.length == 0) {
			params = parameters(configuration, keyPrefix);
		}
		return InstanceConfigurator.control(configuration, name, keyPrefix, tag, description, params);
	}

	/**
	 * Creates a {@link Configurator} for configurations with {@link Parameter} annotated fields.
	 * This is the preferred way to create a {@code Configurator} for a single module that uses
	 * {@link Parameter} and optionally also {@code Module} and {@code Description} annotations.
	 * All configurations must have unique keys.
	 * @param configurations The configuration instances.
	 * @param <C> Type of the configuration.
	 * @return Common Configurator for the configurations.
	 */
	@SuppressWarnings("unchecked")
	public static <C> Configurator configure(C... configurations) {
		// <C> could also be Object here. It is used for documentation and consistency.
		if (configurations.length == 0) {
			throw new ConfigException("configurations are empty");
		}
		// create all configurators
		Configurator[] configurators = new Configurator[configurations.length];
		for (int i = 0; i < configurations.length; i++) {
			configurators[i] = InstanceConfigurator.control(configurations[i]);
		}
		return manage(configurators);
	}

	/**
	 * Creates a {@link Configurator} wrapping other configurators.
	 * @param configurators The configurators to be wrapped.
	 * @return Common Configurator including the specified Configurators.
	 */
	@SuppressWarnings("unchecked")
	public static Configurator manage(Configurator... configurators) {
		if (configurators.length == 0) {
			throw new ConfigException("configurators are empty");
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
	 * Splits argument pairs in form {@code -key=value} and stores the parameter key and the parsed value.
	 * A {@link ConfigParameter#key} is converted to a command line argument key by changing all chars that are not ANSI
	 * letters or digits to dashes ({@code -}), prefixing all sequences of uppercase letters with dashes,
	 * changing sequences of dashes to a single dash and converting the result to lowercase.
	 *
	 * Example: the key "myApp" becomes "-my-app", "HTML$Valües" becomes "-html-val-es".
	 * @param dest The Map where the key-value pairs are stored. To only count valid arguments, pass {@code null}.
	 * @param destUnused The Collection where all non matching arguments are stored. May be {@code null}.
	 * @param keys All valid keys in their regular format.
	 * @param args All arguments that should be parsed for keys.
	 * @return The number of pairs that were or would have been stored in {@code dest}.
	 */
	public static int getArgs(Map<String, String> dest, Collection<String> destUnused, String[] keys, String[] args) {
		String[] collisions = ConfigSupport.collisions(arg, keys);
		if (collisions.length > 0) {
			throw new ConfigException("collisions for command line argument keys: " + Arrays.toString(collisions));
		}
		return ConfigSupport.parseValues(dest, destUnused, keys, ARG_PREFIX, arg, args);
	}

	/**
	 * Stores values of all keys read from environment variables in {@code dest}.
	 * A {@link ConfigParameter#key} is converted to an environment variable name by prefixing it with
	 * {@code envVarPrefix}, changing all chars that are not ANSI letters or digits to underscores ({@code _}),
	 * prefixing all sequences of uppercase letters with underscores, changing sequences of underscores to a single
	 * underscore and converting the result to uppercase.
	 *
	 * Example: the key "myApp" becomes "MY_APP", "HTML$Valües" becomes "HTML_VAL_ES".
	 * @param dest The Map where the key-value pairs are stored. May be {@code null}.
	 * @param keys All valid keys in their regular format.
	 * @param envVarPrefix The common prefix for environment variables.
	 * @return The number of pairs that were or would have been stored in {@code dest}.
	 */
	public static int getEnv(Map<String, String> dest, String[] keys, String envVarPrefix) {
		String[] collisions = ConfigSupport.collisions(env, keys);
		if (collisions.length > 0) {
			throw new ConfigException("collisions for environment keys: " + Arrays.toString(collisions));
		}
		return ConfigSupport.values(dest, keys, envVarPrefix, env, System.getenv());
	}

	/**
	 * Sets configuration options from command line arguments and returns the arguments that could not be
	 * recognized. See {@link #getArgs} for details.
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
	 * Sets configuration options from environment variables.
	 * See {@link #getEnv} for details.
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
