package org.jatronizer.configurator;

/**
 * DefaultConverter provides constants for all non-array primitive types and
 * can provide the fitting converter for a primitive type given its class.
 */
public final class DefaultConverter implements Converter<Object> {

	/**
	 * returns the fitting converter for all non-array primitive data types and their wrappers.
	 * If c is null or Void.class, it returns the NULL_CONVERTER.
	 * If no fitting converter exists, it returns null.
	 */
	public static Converter getFor(Class c) {
		if (c == null || c == Void.class) {
			return NULL_CONVERTER;
		}
		if (c == boolean.class || c == Boolean.class) {
			return BOOLEAN_CONVERTER;
		}
		if (c == char.class || c == Character.class) {
			return CHAR_CONVERTER;
		}
		if (c == byte.class || c == Byte.class) {
			return BYTE_CONVERTER;
		}
		if (c == short.class || c == Short.class) {
			return SHORT_CONVERTER;
		}
		if (c == int.class || c == Integer.class) {
			return INT_CONVERTER;
		}
		if (c == long.class || c == Long.class) {
			return LONG_CONVERTER;
		}
		if (c == float.class || c == Float.class) {
			return FLOAT_CONVERTER;
		}
		if (c == double.class || c == Double.class) {
			return DOUBLE_CONVERTER;
		}
		if (c == String.class) {
			return STRING_CONVERTER;
		}
		return null;
	}

	/**
	 * converts anything to null.
	 */
	public static final Converter<Object> NULL_CONVERTER = new DefaultConverter();

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

	private DefaultConverter() {
	}

	public Object valueOf(String s) {
		return null;
	}

	public String toString(Object value) {
		return null;
	}

	public String toString() {
		return "DefaultConverter";
	}
}
