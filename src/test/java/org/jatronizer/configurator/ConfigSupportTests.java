package org.jatronizer.configurator;

import static org.jatronizer.configurator.ConfigSupport.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class ConfigSupportTests {

	private static class Negator implements Converter<Double> {
		public Double fromString(String value) {
			return -Double.valueOf(value);
		}

		public String toString(Double value) {
			return "" + (-value);
		}
	}

	private static class NoParameters {
		private boolean bo;
		private byte by;
		private long lo = -1;
		private double d = Double.NaN;
		private String value;
	}

	private static class SomeParameters {
		private byte by;
		@Parameter
		private long lo = -1;
		private boolean bo;
		@Parameter(key = "yo", converter = Negator.class)
		private double d = Double.NaN;
		@Parameter(key = "mo")
		private String value;
	}

	private static class ExplicitKeyConflict {
		@Parameter(key = "a")
		private String a;
		@Parameter(key = "a")
		private String b;
	}

	private static class ImplicitKeyConflict {
		private String a;
		@Parameter(key = "a")
		private String b;
	}

	@Test
	public void fetchParametersErrors() {
		ConfigParameter[] ps;
		// test null config
		try {
			ps = fetchParameters(null, "");
			fail("expected a NullPointerException when no config is given");
		} catch (NullPointerException e) {
		}
		try {
			ps = fetchParameters(new NoParameters(), "");
			fail("expected a ConfigurationException when config has no @Parameter fields");
		} catch (ConfigException e) {
		}
		// TODO finish this
		// no failure with empty key prefix
		//ps = fetchParameters(new SomeParameters(), null);
		/*
		assertTrue("parameters must be in alphabetical order by key",
				"double".equals(ps[0].key()) &&

		*/
	}

	/*
	ConfigParameter[] fetchParameters(Object config, String keyPrefix)
	enum KeyFormat implements KeyFormatter
	String description(AnnotatedElement elem)
	String[] collisions(KeyFormatter format, String...keys)
	int values(
			Map<String, String> dst,
			String[] keys, String keyPrefix, KeyFormatter format,
			Map<String, String> src)
	int parseValues(
			Map<String, String> dst, Collection<String> dstUnused,
			String[] keys, String keyPrefix, KeyFormatter format,
			String...src)
	*/
}
