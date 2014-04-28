package org.jatronizer.configurator;

/**
 * A {@code NullConverter} converts anything to {@code null}.
 */
public class NullConverter implements Converter<Object> {
	public Object valueOf(String value) {return null;}
	public String toString(Object value) {return null;}
	public String toString() {return "NullConverter";}
}
