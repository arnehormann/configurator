package org.jatronizer.configurator;

/**
 * Manages a configuration parameter.
 * @param <C> Type containing the field represented by the {@code ConfigParameter}.
 * @param <P> Type of the configuration parameter.
 */
public interface ConfigParameter<C,P> extends Converter<P> {
	/**
	 * Retrieves the key used to address the configuration parameter.
	 * @return the key
	 */
	String key();

	/**
	 * Retrieves the default value of the configuration parameter in {@code String} form.
	 * @return the default value
	 */
	String defaultValue();

	/**
	 * Retrieves the description of this configuration parameter.
	 * If it is not annotated with {@link Description}, {@code ""} is returned.
	 * @return the description
	 */
	String description();

	/**
	 * Retrieves the tag of this configuration parameter.
	 * @return the tag
	 */
	String tag();

	/**
	 * Retrieves the type of the configuration parameter.
	 * @return the type
	 */
	Class<P> type();

	/**
	 * Retrieves the type containing the field represented by the {@code ConfigParameter}.
	 * @return the outer type
	 */
	Class<C> outerType();

	/**
	 * Retrieves all valid values a parameter can take.
	 * An option is one of a handful of specific and discrete values, e.g. the values of an {@code enum}.
	 * Each option should have a specific description retrievable with {@code description(option)}.
	 * If there are no options, an empty array is returned.
	 * @return the valid options
	 */
	String[] options();

	/**
	 * Retrieves the description of the specified option.
	 * If the specified option does not exist, {@code null} is returned.
	 * If it has no description, {@code ""} is returned.
	 * @param option the option for which a description should be retrieved
	 * @return the description
	 */
	String description(String option);

	/**
	 * Retrieves the {@code String} form of the configuration parameter on the specified configuration.
	 * @param configuration the configuration from which the value is retrieved
	 * @return the value of this parameter in a configuration
	 */
	String get(C configuration);

	/**
	 * Sets the configuration parameter on the specified configuration to a value given in {@code String} format.
	 * @param configuration the configuration on which this parameter is set
	 * @param value the new value
	 */
	void set(C configuration, String value);
}
