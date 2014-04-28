package org.jatronizer.configurator;

public final class DefaultConverters {

	/**
	 * Returns the default converter for all primitive data types, their respective wrappers,
	 * String and enum types.
	 * {@code getFor} does not handle arrays.
	 * If {@code c} is {@code null} or {@code Void.class}, it returns {@code NULL_CONVERTER}.
	 * If no fitting converter exists, it returns {@code null}.
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
	 * Converts between the Strings "true" or "false" and their representations as Boolean
	 */
	public static final Converter<Boolean> BOOLEAN_CONVERTER = new Converter<Boolean>() {
		public Boolean valueOf(String value) {return Boolean.valueOf(value);}
		public String toString(Boolean value) {return Boolean.toString(value);}
		public String toString() {return "BooleanConverter";}
	};

	/**
	 * Converts between one-char Strings and Character
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
	 * Converts between a numeric String in decimal notation and Byte
	 */
	public static final Converter<Byte> BYTE_CONVERTER = new Converter<Byte>() {
		public Byte valueOf(String value) {return Byte.valueOf(value);}
		public String toString(Byte value) {return Byte.toString(value);}
		public String toString() {return "ByteConverter";}
	};

	/**
	 * Converts between a numeric String in decimal notation and Short
	 */
	public static final Converter<Short> SHORT_CONVERTER = new Converter<Short>() {
		public Short valueOf(String value) {return Short.valueOf(value);}
		public String toString(Short value) {return Short.toString(value);}
		public String toString() {return "ShortConverter";}
	};

	/**
	 * Converts between a numeric String in decimal notation and Integer
	 */
	public static final Converter<Integer> INT_CONVERTER = new Converter<Integer>() {
		public Integer valueOf(String value) {return Integer.valueOf(value);}
		public String toString(Integer value) {return Integer.toString(value);}
		public String toString() {return "IntConverter";}
	};

	/**
	 * Converts between a numeric String in decimal notation and Long
	 */
	public static final Converter<Long> LONG_CONVERTER = new Converter<Long>() {
		public Long valueOf(String value) {return Long.valueOf(value);}
		public String toString(Long value) {return Long.toString(value);}
		public String toString() {return "LongConverter";}
	};

	/**
	 * Converts between a numeric String and Float.
	 * The conversion uses {@code Float.valueOf} and {@code Float.toString}, the valid format
	 * is described there.
	 */
	public static final Converter<Float> FLOAT_CONVERTER = new Converter<Float>() {
		public Float valueOf(String value) {return Float.valueOf(value);}
		public String toString(Float value) {return Float.toString(value);}
		public String toString() {return "FloatConverter";}
	};

	/**
	 * Converts between a numeric String and Double.
	 * The conversion uses {@code Double.valueOf} and {@code Double.toString}, the valid format
	 * is described there.
	 */
	public static final Converter<Double> DOUBLE_CONVERTER = new Converter<Double>() {
		public Double valueOf(String value) {return Double.valueOf(value);}
		public String toString(Double value) {return Double.toString(value);}
		public String toString() {return "DoubleConverter";}
	};

	/**
	 * Returns the String as is.
	 */
	public static final Converter<String> STRING_CONVERTER = new Converter<String>() {
		public String valueOf(String value) {return value;}
		public String toString(String value) {return value;}
		public String toString() {return "StringConverter";}
	};

	/**
	 * Converts anything to null.
	 */
	public static final Converter<Object> NULL_CONVERTER = new NullConverter();

}
