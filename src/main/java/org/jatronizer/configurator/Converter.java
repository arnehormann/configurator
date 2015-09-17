package org.jatronizer.configurator;

/**
 * Converts between {@code String} and the specified type.
 * It must throw an Exception if a conversion is not possible (e.g. out of range: "256" to {@code Byte}).
 * Each Converter uses its own format and must support conversions in both directions,
 * so unless {@code value == null}, for each Converter c
 * {@code value.equals(c.toString(c.fromString(value)))} must be true.
 * Implementations of {@code Converter} must provide a default constructor (public, no arguments).
 * @param <P> The type converted to or from String.
 */
public interface Converter<P> {

	/**
	 * Converts a String to P.
	 * @param value the value in String form.
	 * @return the value in its native form.
	 */
	P fromString(String value);

	/**
	 * Converts a P to String.
	 * @param value the value in its native form.
	 * @return the value in String form.
	 */
	String toString(P value);
}
