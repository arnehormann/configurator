package org.jatronizer.configurator;

/**
 * Represents a managed configure parameter.
 * @param <C> Type of the field represented by the {@code ConfigParameter} is declared in.
 * @param <P> Type of the configure parameter.
 */
public interface ConfigParameter<C,P> extends Converter<P> {
	/**
	 * Retrieves the key used to address the configure parameter.
	 */
	String key();

	/**
	 * Retrieves the default value of the configure parameter in {@code String} form.
	 */
	String defaultValue();

	/**
	 * Retrieves the description of this configure parameter.
	 * If it is not annotated with {@link Description}, {@code ""} is returned.
	 */
	String description();

	/**
	 * Retrieves the type of the configure parameter.
	 */
	Class<P> type();

	/**
	 * Retrieves the type of the field represented by the {@code ConfigParameter} is declared in.
	 */
	Class<C> outerType();

	/**
	 * Retrieves all valid values a parameter can take.
	 * An option is one of a handful of specific and discrete values, e.g. the values of an {@code enum}.
	 * Each option should have a specific description retrievable with {@code description(option)}.
	 * If there are no options, an empty array is returned.
	 */
	String[] options();

	/**
	 * Retrieves the description of the specified option.
	 * If the specified option does not exist, {@code null} is returned.
	 * If it has no description, {@code ""} is returned.
	 */
	String description(String option);

	/**
	 * Retrieves the {@code String} form of the configure parameter on the specified configure instance.
	 */
	String get(C configuration);

	/**
	 * Sets the configure parameter on the specified configure instance to a value given in {@code String}
	 * format.
	 */
	void set(C configuration, String value);
}
