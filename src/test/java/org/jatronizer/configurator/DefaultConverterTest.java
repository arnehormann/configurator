package org.jatronizer.configurator;

import org.junit.Test;

import static org.jatronizer.configurator.Common.*;

import static org.jatronizer.configurator.DefaultConverters.*;
import static org.junit.Assert.*;

public class DefaultConverterTest {

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

		public void run(Object[] args) {
			if (args.length != 2) {
				throw new RuntimeException("must be 2 arguments");
			}
			try {
				T t = (T) args[0];
				String s = (String) args[1];
				String name = t.getClass().getSimpleName();
				assertEquals("convert from " + name + " to String", s, conv.toString(t));
				assertEquals("convert from String to " + name, t, conv.fromString(s));
			} catch (Exception e) {
				throw new RuntimeException();
			}
		}
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
				assertTrue("converter constant is returned for " + name, c == getFor(type));
			}
		}
		assertTrue("getFor handles enums",
				getFor(TestValues.class).getClass() == DefaultConverters.EnumConverter.class);
	}

	@Test
	public void testBoolean() {
		Converter<Boolean> conv = BOOLEAN_CONVERTER;
		eachRow(new Object[][]{
				{true, "true"},
				{false, "false"}
		}, checkWith(conv));
		Call fromString = call(conv, "fromString", String.class);
		Call toString = call(conv, "toString", Boolean.class);
		eachRow(new Object[][]{
				{fromString, null},
				{fromString, "True"},
				{toString, null},
				{toString, ""},
		}, FAIL_ON_CALLS);
	}

	@Test
	public void testCharacter() {
		Converter<Character> conv = CHAR_CONVERTER;
		eachRow(new Object[][]{
				{'\u0000', "\u0000"},
				{'\u007f', "\u007f"},
				{'\u00ff', "\u00ff"},
				{'\u7fff', "\u7fff"},
				{'\uffff', "\uffff"}
		}, checkWith(conv));
		Call fromString = call(conv, "fromString", String.class);
		Call toString = call(conv, "toString", Character.class);
		eachRow(new Object[][]{
				{fromString, null},
				{fromString, "-1"},
				{fromString, "256"},
				{toString, null}
		}, FAIL_ON_CALLS);
	}

	@Test
	public void testByte() {
		Converter<Byte> conv = BYTE_CONVERTER;
		eachRow(new Object[][]{
				{(byte) 0, "0"},
				{(byte) 127, "127"},
				{(byte) -128, "-128"},
				{(byte) -1, "-1"}
		}, checkWith(conv));
		Call fromString = call(conv, "fromString", String.class);
		Call toString = call(conv, "toString", Byte.class);
		eachRow(new Object[][]{
				{fromString, null},
				{fromString, "-129"},
				{fromString, "128"},
				{toString, null}
		}, FAIL_ON_CALLS);
	}

	@Test
	public void testShort() {
		Converter<Short> conv = SHORT_CONVERTER;
		eachRow(new Object[][]{
				{(short) 0, "0"},
				{(short) 32767, "32767"},
				{(short) -32768, "-32768"},
				{(short) -1, "-1"}
		}, checkWith(conv));
		Call fromString = call(conv, "fromString", String.class);
		Call toString = call(conv, "toString", Short.class);
		eachRow(new Object[][]{
				{fromString, null},
				{fromString, "32768"},
				{fromString, "-32769"},
				{toString, null}
		}, FAIL_ON_CALLS);
	}

	@Test
	public void testInt() {
		Converter<Integer> conv = INT_CONVERTER;
		eachRow(new Object[][]{
				{0, "0"},
				{2147483647, "2147483647"},
				{-2147483648, "-2147483648"},
				{-1, "-1"}
		}, checkWith(conv));
		Call fromString = call(conv, "fromString", String.class);
		Call toString = call(conv, "toString", Integer.class);
		eachRow(new Object[][]{
				{fromString, null},
				{fromString, "2147483648"},
				{fromString, "-2147483649"},
				{toString, null}
		}, FAIL_ON_CALLS);
	}

	@Test
	public void testLong() {
		Converter<Long> conv = LONG_CONVERTER;
		eachRow(new Object[][]{
				{0L, "0"},
				{9223372036854775807L, "9223372036854775807"},
				{-9223372036854775808L, "-9223372036854775808"},
				{-1L, "-1"}
		}, checkWith(conv));
		Call fromString = call(conv, "fromString", String.class);
		Call toString = call(conv, "toString", Long.class);
		eachRow(new Object[][]{
				{fromString, null},
				{fromString, "9223372036854775808"},
				{fromString, "-9223372036854775809"},
				{toString, null}
		}, FAIL_ON_CALLS);
	}
}
