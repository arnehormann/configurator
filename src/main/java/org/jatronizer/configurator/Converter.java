package org.jatronizer.configurator;

/**
 * A Converter converts between String and the specified type.
 * It must throw an Exception if a conversion is not possible (e.g. out of range: "255" to Byte).
 * Each Converter uses its own format and must support conversions in both directions,
 * so unless <code>value == null</code>, this must be true:
 * <code>value.equals(conv.toString(conv.valueOf(value)))</code>
 * Implementations must provide a public constructor without arguments.
 */
public interface Converter<T> {

	/**
	 * create T from a value in String form
	 */
	T valueOf(String value);

	/**
	 * convert a value into its String form
	 */
	String toString(T value);
}
