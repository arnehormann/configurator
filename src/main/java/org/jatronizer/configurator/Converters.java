package org.jatronizer.configurator;

import java.lang.reflect.Method;

final class Converters {

	/**
	 * Returns the default converter for all primitive data types, their respective wrappers,
	 * String and enum types.
	 * {@code getFor} does not handle arrays.
	 * If {@code c} is {@code null} or {@code Void.class}, it returns {@code NULL_CONVERTER}.
	 * If no fitting converter exists, it returns {@code null}.
	 */
	@SuppressWarnings("unchecked")
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
	 * Converts between the Strings "true" or "false" and their representations as Boolean
	 */
	public static final Converter<Boolean> BOOLEAN_CONVERTER = new Converter<Boolean>() {
		public Boolean fromString(String value) {
			if ("false".equals(value)) {
				return false;
			}
			if ("true".equals(value)) {
				return true;
			}
			throw new IllegalValueException("\"" + value + "\" is not a boolean");
		}
		public String toString(Boolean value) {return value.toString();}
		public String toString() {return "BooleanConverter";}
	};

	/**
	 * Converts between one-char Strings and Character
	 */
	public static final Converter<Character> CHAR_CONVERTER = new Converter<Character>() {
		public Character fromString(String value) {
			if (value.length() != 1) {
				throw new RuntimeException("value is not exactly one char long");
			}
			return value.charAt(0);
		}
		public String toString(Character value) {return Character.toString(value);}
		public String toString() {return "CharConverter";}
	};

	/**
	 * Converts between a numeric String in decimal notation and Byte
	 */
	public static final Converter<Byte> BYTE_CONVERTER = new Converter<Byte>() {
		public Byte fromString(String value) {return Byte.valueOf(value);}
		public String toString(Byte value) {return Byte.toString(value);}
		public String toString() {return "ByteConverter";}
	};

	/**
	 * Converts between a numeric String in decimal notation and Short
	 */
	public static final Converter<Short> SHORT_CONVERTER = new Converter<Short>() {
		public Short fromString(String value) {return Short.valueOf(value);}
		public String toString(Short value) {return Short.toString(value);}
		public String toString() {return "ShortConverter";}
	};

	/**
	 * Converts between a numeric String in decimal notation and Integer
	 */
	public static final Converter<Integer> INT_CONVERTER = new Converter<Integer>() {
		public Integer fromString(String value) {return Integer.valueOf(value);}
		public String toString(Integer value) {return Integer.toString(value);}
		public String toString() {return "IntConverter";}
	};

	/**
	 * Converts between a numeric String in decimal notation and Long
	 */
	public static final Converter<Long> LONG_CONVERTER = new Converter<Long>() {
		public Long fromString(String value) {return Long.valueOf(value);}
		public String toString(Long value) {return Long.toString(value);}
		public String toString() {return "LongConverter";}
	};

	/**
	 * Converts between a numeric String and Float.
	 * The conversion uses {@code Float.fromString} and {@code Float.toString}, the valid format
	 * is described there.
	 */
	public static final Converter<Float> FLOAT_CONVERTER = new Converter<Float>() {
		public Float fromString(String value) {return Float.valueOf(value);}
		public String toString(Float value) {return Float.toString(value);}
		public String toString() {return "FloatConverter";}
	};

	/**
	 * Converts between a numeric String and Double.
	 * The conversion uses {@code Double.fromString} and {@code Double.toString}, the valid format
	 * is described there.
	 */
	public static final Converter<Double> DOUBLE_CONVERTER = new Converter<Double>() {
		public Double fromString(String value) {return Double.valueOf(value);}
		public String toString(Double value) {return Double.toString(value);}
		public String toString() {return "DoubleConverter";}
	};

	/**
	 * Returns the String as is (even if it's {@code null}).
	 */
	public static final Converter<String> STRING_CONVERTER = new Converter<String>() {
		public String fromString(String value) {return value;}
		public String toString(String value) {return value;}
		public String toString() {return "StringConverter";}
	};

	/**
	 * Converts anything to {@code null}.
	 */
	public static final Converter<Object> NULL_CONVERTER = new NullConverter();

	/**
	 * A {@code NullConverter} converts anything to {@code null}.
	 */
	public static class NullConverter implements Converter<Object> {
		public Object fromString(String value) {return null;}
		public String toString(Object value) {return null;}
		public String toString() {return "NullConverter";}
	}

	/**
	 * Converts between the names of enum values and their values.
	 * @param <P> enum type.
	 */
	public static class EnumConverter<P> implements Converter<P> {

		/**
		 * Creates a converter for the specified enum type.
		 * If the enum values are not accessible, {@code create} will call
		 * {@link java.lang.reflect.Field#setAccessible(boolean);}.
		 * Throws a {@link ConfigurationException} if {@code c} is not an enum, the enum values of {@code c} could
		 * not be accessed and it could not be made accessible.
		 * @param c Type of the enum.
		 */
		public static <P> EnumConverter<P> create(Class<P> c) {
			if (!c.isEnum()) {
				throw new ConfigurationException("Class " + c.getCanonicalName() + " is not an enum");
			}
			Method valueOf = null;
			Method name = null;
			try {
				valueOf = c.getMethod("valueOf", String.class);
				name = c.getMethod("name");
				if (!valueOf.isAccessible()) {
					// NOTE making fromString and name accessible is not reverted later.
					valueOf.setAccessible(true);
					name.setAccessible(true);
				}
			} catch (Exception e) {
				// famous last words: never going to happen
				throw new ConfigurationException("Method name and/or fromString could not be accessed", e);
			}
			return new EnumConverter<P>("EnumConverter(" + c.getCanonicalName() +")", valueOf, name);
		}

		private final String string;
		private final Method valueOf;
		private final Method name;

		private EnumConverter(String string, Method valueOf, Method name) {
			this.string = string;
			this.valueOf = valueOf;
			this.name = name;
		}

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		public P fromString(String value) {
			try {
				return (P) valueOf.invoke(null, value);
			} catch (Exception e) {
				throw new IllegalValueException("Could not convert from String with " + valueOf.toString(), e);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public String toString(P value) {
			try {
				return (String) name.invoke(value);
			} catch (Exception e) {
				throw new IllegalValueException("Could not convert to String with " + name.toString(), e);
			}
		}

		public String toString() {return string;}

		public boolean equals(Object o) {
			return this == o ||
					o.getClass() == EnumConverter.class && (valueOf.equals(((EnumConverter) o).valueOf));
		}

		public int hashCode() {
			return valueOf.hashCode() ^ ~name.hashCode();
		}
	}
}
