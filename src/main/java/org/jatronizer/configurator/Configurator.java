package org.jatronizer.configurator;

import java.util.Map;
import java.util.Properties;

public interface Configurator {

	/**
	 * Reports whether the configurator manages a parameter with the specified key.
	 */
	boolean hasKey(String key);

	/**
	 * Retrieves the keys of all available configure parameters on the managed configure.
	 */
	String[] keys();

	/**
	 * Retrieves the parameter with the specified {@code key}.
	 * Returns {@code null} if no parameter with that key exists.
	 */
	ConfigParameter parameter(String key);

	/**
	 * Retrieves the current value of the configure option with the specified key in {@code String} form.
	 * If the key is unknown, {@code null} is returned.
	 */
	String value(String key);

	/**
	 * Sets a configure parameter and reports the number of values that were set.
	 * This operation is synchronized on the configure module {@code key} belongs to.
	 * @return number of values set, {@code 1} if the parameter for {@code key} exists and {@code value} can be
	 * converted to its type, else {@code 0}.
	 */
	int set(String key, String value);

	/**
	 * Sets multiple configure parameters and reports the number of values that were set.
	 * This operation is synchronized per configure module.
	 * @return the number of parameters that were set, even if they were set to the same value it had before.
	 */
	int set(Map<String, String> configuration);

	/**
	 * Sets multiple configure parameters and reports the number of values that were set.
	 * Each key and each value stored in configure must be a {@code String}.
	 * This operation is synchronized per configure module.
	 * @return the number of parameters that were set, even if they were set to the same value it had before.
	 */
	int set(Properties configuration);

	/**
	 * Iterates over all managed configure modules and all their parameters.
	 * {@code walk} calls {@code visitModule} and {@code visitParameter} on {@code v}.
	 * Use {@code walk} to e.g. create a dynamic help text with currently set values at runtime.
	 */
	void walk(ConfigurationVisitor v);
}
