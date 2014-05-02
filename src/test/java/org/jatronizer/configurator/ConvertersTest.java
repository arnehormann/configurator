package org.jatronizer.configurator;

import org.junit.Test;

import static org.jatronizer.configurator.Common.*;

import static org.jatronizer.configurator.Converters.*;
import static org.junit.Assert.*;

public class ConvertersTest {

	private static enum TestValues {
		a,
		b,
		B
	}

	public static <T> ConversionCheck<T> checkWith(Converter<T> conv) {
		return new ConversionCheck<T>(conv);
	}

	private static class ConversionCheck<T> implements Action {
		private final Converter<T> conv;
		private ConversionCheck(Converter<T> conv) {
			this.conv = conv;
		}

		@SuppressWarnings("unchecked")
		public void run(Object[] args) {
			if (args.length != 2) {
				throw new RuntimeException("must be 2 arguments");
			}
			try {
				Object value = args[0];
				if (value == null) {
					assertEquals("convert roundtrip for null", value, conv.fromString(conv.toString((T) value)));
					return;
				}
				String string = (String) args[1];
				String name = value.getClass().getCanonicalName();
				assertEquals("convert from " + name + " to String", string, conv.toString((T) value));
				assertEquals("convert from String to " + name, value, conv.fromString(string));
				assertEquals("conversion roundtrip for " + name, value, conv.fromString(conv.toString((T) value)));
			} catch (Exception e) {
				throw new RuntimeException();
			}
		}
	}

	private static Object[][] ofValues(Class type, Object... values) {
		Object[][] map = new Object[values.length][2];
		for (int i = 0; i < values.length; i++) {
			final Object value = values[i];
			// map value to its native String representation with toString()
			map[i][0] = value;
			if (value != null) {
				if (type != null) {
					// ensure the types are all consistent
					assertEquals("wrong type for " + i + "th argument", type, values[i].getClass());
				}
				map[i][1] = "" + value;
			} else {
				map[i][1] = null;
			}
		}
		return map;
	}

	private static void failEach(Call call, Object... args) {
		if (args == null) {
			// with varargs, this happens when null is the only argument
			args = new Object[]{null};
		}
		for (Object arg : args) {
			try {
				call.with(arg);
				fail("Expected an Exception on calling " + call + " with '" + arg + "'");
			} catch (Exception e) {
			}
		}
	}

	private static String incrementLastDigit(Object o) {
		char[] chars = o.toString().toCharArray();
		int end = chars.length - 1;
		char last = chars[end];
		if (last < '0' || '9' <= last) {
			throw new RuntimeException("could not increment last digit");
		}
		chars[end]++;
		return new String(chars);
	}

	@Test
	public void getForTypes() {
		Object[][] idTable = {
			{NULL_CONVERTER, null, Void.class},
			{BOOLEAN_CONVERTER, Boolean.class, boolean.class},
			{CHAR_CONVERTER, Character.class, char.class},
			{BYTE_CONVERTER, Byte.class, byte.class},
			{SHORT_CONVERTER, Short.class, short.class},
			{INT_CONVERTER, Integer.class, int.class},
			{LONG_CONVERTER, Long.class, long.class},
			{FLOAT_CONVERTER, Float.class, float.class},
			{DOUBLE_CONVERTER, Double.class, double.class},
			{STRING_CONVERTER, String.class}
		};
		for (Object[] entries : idTable) {
			Converter c = (Converter) entries[0];
			for (int i = 1; i < entries.length; i++) {
				Class type = (Class) entries[i];
				String name = type == null ? "null" : type.getSimpleName();
				assertTrue("converter constant is returned for " + name, c == converterFor(type));
			}
		}
		assertTrue("converterFor handles enums",
				converterFor(TestValues.class).getClass() == Converters.EnumConverter.class);
	}

	@Test
	public void testBoolean() {
		Class type = Boolean.class;
		Converter conv = converterFor(type);
		each(ofValues(type,
				true,
				false
		), checkWith(conv));
		failEach(call(conv, "fromString", String.class),
				null,
				"True"
		);
		failEach(call(conv, "toString", type),
				null,
				""
		);
	}

	@Test
	public void testChar() {
		Class type = Character.class;
		Converter conv = converterFor(type);
		each(ofValues(type,
				Character.MIN_VALUE,
				' ',
				'a',
				'§',
				Character.MAX_VALUE
		), checkWith(conv));
		failEach(call(conv, "fromString", String.class),
				null,
				"",
				"aa"
		);
		failEach(call(conv, "toString", type),
				(Character) null
		);
	}

	@Test
	public void testByte() {
		Class type = Byte.class;
		Byte min = Byte.MIN_VALUE;
		Byte max = Byte.MAX_VALUE;
		Converter conv = converterFor(type);
		each(ofValues(type,
				min,
				(byte) -1,
				(byte) 0,
				(byte) 1,
				max
		), checkWith(conv));
		final String minstr = min.toString();
		failEach(call(conv, "fromString", String.class),
				null,
				minstr.substring(1),
				incrementLastDigit(min)
		);
		failEach(call(conv, "toString", type),
				(Byte) null
		);
	}

	@Test
	public void testShort() {
		Class type = Short.class;
		Short min = Short.MIN_VALUE;
		Short max = Short.MAX_VALUE;
		Converter conv = converterFor(type);
		each(ofValues(type,
				min,
				(short) -1,
				(short) 0,
				(short) 1,
				max
		), checkWith(conv));
		final String minstr = min.toString();
		failEach(call(conv, "fromString", String.class),
				null,
				minstr.substring(1),
				incrementLastDigit(min)
		);
		failEach(call(conv, "toString", type),
				(Short) null
		);
	}

	@Test
	public void testInt() {
		Class type = Integer.class;
		Integer min = Integer.MIN_VALUE;
		Integer max = Integer.MAX_VALUE;
		Converter conv = converterFor(type);
		each(ofValues(type,
				min,
				-1,
				0,
				1,
				max
		), checkWith(conv));
		final String minstr = min.toString();
		failEach(call(conv, "fromString", String.class),
				null,
				minstr.substring(1),
				incrementLastDigit(min)
		);
		failEach(call(conv, "toString", type),
				(Integer) null
		);
	}

	@Test
	public void testLong() {
		Class type = Long.class;
		Long min = Long.MIN_VALUE;
		Long max = Long.MAX_VALUE;
		Converter conv = converterFor(type);
		each(ofValues(type,
				min,
				-1L,
				0L,
				1L,
				max
		), checkWith(conv));
		final String minstr = min.toString();
		failEach(call(conv, "fromString", String.class),
				null,
				minstr.substring(1),
				incrementLastDigit(min)
		);
		failEach(call(conv, "toString", type),
				(Long) null
		);
	}

	@Test
	public void testFloat() {
		Class type = Float.class;
		final Float inf = Float.POSITIVE_INFINITY;
		final Float max = Float.MAX_VALUE;
		final Float norm = Float.MIN_NORMAL;
		final Float min = Float.MIN_VALUE;
		Converter conv = converterFor(type);
		each(ofValues(type,
				-inf,
				-max,
				-1.0f,
				-norm,
				-min,
				0.0f,
				min,
				norm,
				1.0f,
				max,
				inf,
				Float.NaN // succeeds because Float and not float is compared
		), checkWith(conv));
		failEach(call(conv, "fromString", String.class),
				(String) null
		);
		failEach(call(conv, "toString", type),
				(Float) null
		);
	}

	@Test
	public void testDouble() {
		Class type = Double.class;
		final Double inf = Double.POSITIVE_INFINITY;
		final Double max = Double.MAX_VALUE;
		final Double norm = Double.MIN_NORMAL;
		final Double min = Double.MIN_VALUE;
		Converter conv = converterFor(type);
		each(ofValues(type,
				-inf,
				-max,
				-1.0,
				-norm,
				-min,
				0.0,
				min,
				norm,
				1.0,
				max,
				inf,
				Double.NaN // succeeds because Double and not double is compared
		), checkWith(conv));
		failEach(call(conv, "fromString", String.class),
				(String) null
		);
		failEach(call(conv, "toString", type),
				(Float) null
		);
	}

	@Test
	public void testString() {
		Class type = String.class;
		Converter conv = converterFor(type);
		each(ofValues(type,
				null,
				"",
				" ",
				"a",
				"aaaaaaaaaa",
				"\u0000\uffff"
		), checkWith(conv));
	}

	@Test
	public void testNull() {
		Converter conv = converterFor(null);
		each(new Object[][]{
				{null, null},
				{null, ""},
				{null, 0},
				{null, Double.NaN}
		}, checkWith(conv));
	}

	@Test
	public void testEnum() {
		Class type = TestValues.class;
		Converter conv = converterFor(type);
		each(ofValues(type,
				TestValues.a,
				TestValues.b,
				TestValues.B
		), checkWith(conv));
		failEach(call(conv, "fromString", String.class),
				(String) null
		);
		failEach(call(conv, "toString", Object.class),
				(TestValues) null
		);
	}
}
