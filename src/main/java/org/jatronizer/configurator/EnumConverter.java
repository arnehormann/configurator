package org.jatronizer.configurator;

import java.lang.reflect.Method;

/**
 * Converts between the names of enum values and their values.
 * @param <T> enum type.
 */
public class EnumConverter<T> implements Converter<T> {

	/**
	 * Creates a converter for the specified enum type.
	 * @param c Class of the enum.
	 */
	public static <T> EnumConverter<T> create(Class<T> c) {
		if (!c.isEnum()) {
			throw new ConfigurationException("Class " + c.getCanonicalName() + " is not an enum");
		}
		Method valueOf = null;
		Method name = null;
		try {
			valueOf = c.getMethod("valueOf", String.class);
			name = c.getMethod("name");
			ParameterField.forceAccessible(valueOf, name);
		} catch (Exception e) {
			// famous last words: never going to happen
			throw new ConfigurationException("Method name and/or valueOf could not be accessed", e);
		}
		return new EnumConverter<T>("EnumConverter(" + c.getCanonicalName() +")", valueOf, name);
	}

	private final String string;
	private final Method valueOf;
	private final Method name;

	EnumConverter(String string, Method valueOf, Method name) {
		this.string = string;
		this.valueOf = valueOf;
		this.name = name;
	}

	public T valueOf(String value) {
		try {
			return (T) valueOf.invoke(null, value);
		} catch (Exception e) {
			throw new IllegalValueException("Call failed on " + valueOf.toString(), e);
		}
	}

	public String toString(T value) {
		try {
			return (String) name.invoke(value);
		} catch (Exception e) {
			throw new IllegalValueException("Call failed on " + name.toString(), e);
		}
	}

	public String toString() {return string;}
}
