package org.jatronizer.configurator;

/**
 * A Converter converts between String and the specified type.
 * It must throw an Exception if a conversion is not possible (e.g. out of range: "256" to Byte).
 * Each Converter uses its own format and must support conversions in both directions,
 * so unless {@code value == null}, this must be true:
 * {@code value.equals(converter.toString(converter.valueOf(value)))}
 * Implementations must provide a public constructor without arguments.
 */
public interface Converter<T> {

	/**
	 * create T from value.
	 */
	T valueOf(String value);

	/**
	 * convert a value into its {@code String} form.
	 */
	String toString(T value);
}
