package org.jatronizer.configurator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks fields used as configuration parameters.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Parameter {

	/**
	 * Returns the internal key used to access this value.
	 * If its value is {@code ""}, callers of this method should use the field name instead.
	 */
	String key() default "";

	/**
	 * Returns an optional tag for the parameter.
	 * A tag should not contain spaces. Spaces should be used to separate multiple tags.
	 * Tags can be used to group parameters, e.g. to hide some parameters in introductory documentation.
	 */
	String tag() default "";

	/**
	 * Returns a class used to convert between the parameter type and its {@code String} representation.
	 * The class must have a public constructor taking no arguments.
	 */
	Class<? extends Converter> converter() default Converters.NullConverter.class;
}
