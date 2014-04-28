package org.jatronizer.configurator;

import java.lang.reflect.Method;

/**
 * DefaultConverter provides constants for all non-array primitive types and
 * can provide the fitting converter for a primitive type given its class.
 */
public final class DefaultConverter implements Converter<Object> {

	/**
	 * returns the default converter for all non-array primitive data types, their respective wrappers,
	 * String and enum types.
	 * If c is null or Void.class, it returns the NULL_CONVERTER.
	 * If no fitting converter exists, it returns null.
	 */
	public static <T> Converter<T> getFor(Class<T> c) {
		if (c == null || c == Void.class) {
			return (Converter<T>) NULL_CONVERTER;
		}
		if (c == boolean.class || c == Boolean.class) {
			return (Converter<T>) BOOLEAN_CONVERTER;
		}
		if (c == char.class || c == Character.class) {
			return (Converter<T>) CHAR_CONVERTER;
		}
		if (c == byte.class || c == Byte.class) {
			return (Converter<T>) BYTE_CONVERTER;
		}
		if (c == short.class || c == Short.class) {
			return (Converter<T>) SHORT_CONVERTER;
		}
		if (c == int.class || c == Integer.class) {
			return (Converter<T>) INT_CONVERTER;
		}
		if (c == long.class || c == Long.class) {
			return (Converter<T>) LONG_CONVERTER;
		}
		if (c == float.class || c == Float.class) {
			return (Converter<T>) FLOAT_CONVERTER;
		}
		if (c == double.class || c == Double.class) {
			return (Converter<T>) DOUBLE_CONVERTER;
		}
		if (c == String.class) {
			return (Converter<T>) STRING_CONVERTER;
		}
		if (c.isEnum()) {
			return EnumConverter.create(c);
		}
		return null;
	}

	/**
	 * converts between the Strings "true" or "false" and Boolean
	 */
	public static final Converter<Boolean> BOOLEAN_CONVERTER = new Converter<Boolean>() {
		public Boolean valueOf(String value) {return Boolean.valueOf(value);}
		public String toString(Boolean value) {return Boolean.toString(value);}
		public String toString() {return "BooleanConverter";}
	};

	/**
	 * converts single character String to Character and back
	 */
	public static final Converter<Character> CHAR_CONVERTER = new Converter<Character>() {
		public Character valueOf(String value) {
			if (value.length() != 1) {
				throw new RuntimeException("must be a single character");
			}
			return value.charAt(0);
		}
		public String toString(Character value) {return Character.toString(value);}
		public String toString() {return "CharConverter";}
	};

	/**
	 * converts a numeric String to Byte and back
	 */
	public static final Converter<Byte> BYTE_CONVERTER = new Converter<Byte>() {
		public Byte valueOf(String value) {return Byte.valueOf(value);}
		public String toString(Byte value) {return Byte.toString(value);}
		public String toString() {return "ByteConverter";}
	};

	/**
	 * converts a numeric String to Short and back
	 */
	public static final Converter<Short> SHORT_CONVERTER = new Converter<Short>() {
		public Short valueOf(String value) {return Short.valueOf(value);}
		public String toString(Short value) {return Short.toString(value);}
		public String toString() {return "ShortConverter";}
	};

	/**
	 * converts a numeric String to Integer and back
	 */
	public static final Converter<Integer> INT_CONVERTER = new Converter<Integer>() {
		public Integer valueOf(String value) {return Integer.valueOf(value);}
		public String toString(Integer value) {return Integer.toString(value);}
		public String toString() {return "IntConverter";}
	};

	/**
	 * converts a numeric String to Long and back
	 */
	public static final Converter<Long> LONG_CONVERTER = new Converter<Long>() {
		public Long valueOf(String value) {return Long.valueOf(value);}
		public String toString(Long value) {return Long.toString(value);}
		public String toString() {return "LongConverter";}
	};

	/**
	 * converts a numeric String to Float and back
	 */
	public static final Converter<Float> FLOAT_CONVERTER = new Converter<Float>() {
		public Float valueOf(String value) {return Float.valueOf(value);}
		public String toString(Float value) {return Float.toString(value);}
		public String toString() {return "FloatConverter";}
	};

	/**
	 * converts a numeric String to Double and back
	 */
	public static final Converter<Double> DOUBLE_CONVERTER = new Converter<Double>() {
		public Double valueOf(String value) {return Double.valueOf(value);}
		public String toString(Double value) {return Double.toString(value);}
		public String toString() {return "DoubleConverter";}
	};

	public static final Converter<String> STRING_CONVERTER = new Converter<String>() {
		public String valueOf(String value) {return value;}
		public String toString(String value) {return value;}
		public String toString() {return "StringConverter";}
	};

	private static class EnumConverter<T> implements Converter<T> {

		public static <T> EnumConverter<T> create(Class<T> c) {
			if (!c.isEnum()) {
				throw new ConfigurationException("Class " + c.getCanonicalName() + " is not an enum");
			}
			Method valueOf = null;
			Method name = null;
			try {
				valueOf = c.getMethod("valueOf", String.class);
				name = c.getMethod("name");
			} catch (NoSuchMethodException e) {
				// famous last words: never going to happen
				throw new ConfigurationException("Method name and/or valueOf could not be accessed");
			}
			return new EnumConverter<T>(valueOf, name);
		}

		private final Method valueOf;
		private final Method name;

		private EnumConverter(Method valueOf, Method name) {
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

		public String toString() {return "EnumConverter";}
	}

	/**
	 * converts anything to null.
	 */
	public static final Converter<Object> NULL_CONVERTER = new DefaultConverter();

	// DefaultConverter has to be a Converter itself to create the default instances

	public Object valueOf(String value) {return null;}
	public String toString(Object value) {return null;}
	public String toString() {return "NullConverter";}
}
