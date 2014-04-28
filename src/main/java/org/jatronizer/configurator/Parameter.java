package org.jatronizer.configurator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks fields used for externally changeable configuration.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Parameter {

	/**
	 * Returns the internal key used to access this value.
	 * Callers of this method should use the field name if the value is {@code ""}.
	 */
	String key() default "";

	/**
	 * Returns a class used to convert between the parameter type and its {@code String} representation.
	 * The class must have a public constructor taking no arguments.
	 */
	Class<? extends Converter> convert() default NullConverter.class;
}
