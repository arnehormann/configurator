package org.jatronizer.configurator;

import java.util.Map;
import java.util.Properties;

/**
 * Manages a configuration object.
 * A Configurator is usually created by {@link ConfigManager}.
 */
public interface Configurator {

	/**
	 * Reports whether the configurator manages a parameter with the specified key.
	 * @param key the key identifiying a parameter.
	 * @return {@code true} if the parameter exists, else {@code false}.
	 */
	boolean hasKey(String key);

	/**
	 * Retrieves the keys of all available configuration parameters on the managed configuration.
	 * @return keys for available parameters.
	 */
	String[] keys();

	/**
	 * Retrieves the parameter with the specified {@code key}.
	 * Returns {@code null} if no parameter with that key exists.
	 * @param key the key identifiying the parameter.
	 * @return the parameter.
	 */
	ConfigParameter parameter(String key);

	/**
	 * Retrieves the current value of the configuration option with the specified key in {@code String} form.
	 * If the key is unknown, {@code null} is returned.
	 * @param key the key identifiying the parameter.
	 * @return value of a parameter.
	 */
	String value(String key);

	/**
	 * Sets a configuration parameter and reports the number of values that were set.
	 * This operation is synchronized on the configuration module {@code key} belongs to.
	 * @param key the key identifiying the parameter.
	 * @param value the new value.
	 * @return number of values set, {@code 1} if the parameter for {@code key} exists and {@code value} can be
	 * converted to its type, else {@code 0}.
	 */
	int set(String key, String value);

	/**
	 * Sets multiple configuration parameters and reports the number of values that were set.
	 * This operation is synchronized per configuration module.
	 * @param configuration key-value combinations that should be set.
	 * @return key-value combinations that could not be set.
	 */
	Map<String, String> set(Map<String, String> configuration);

	/**
	 * Sets multiple configuration parameters and reports the number of values that were set.
	 * Each key and each value stored in configuration must be a {@code String}.
	 * This operation is synchronized per configuration module.
	 * @param configuration key-value combinations that should be set.
	 * @return key-value combinations that could not be set.
	 */
	Map<String, String> set(Properties configuration);

	/**
	 * Iterates over all managed configuration modules and all their parameters.
	 * {@code walk} calls {@code visitModule} and {@code visitParameter} on {@code v}.
	 * A visitor could use {@code walk} to generate help text and documentation or a user interface to change values
	 * dynamically.
	 * @param visitor the visitor.
	 */
	void walk(ConfigVisitor visitor);
}
