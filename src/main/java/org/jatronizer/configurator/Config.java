package org.jatronizer.configurator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Config annotates a field that is used for externally changeable configuration.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Config {

	/**
	 * A key defines the point of access. If it is not set, the field name is used.
	 */
	String key() default "";

	/**
	 * A description of the field and its valid values.
	 */
	String desc() default "";

	/**
	 * A class converting the field content from and to a String.
	 * The class must have a default constructor.
	 */
	Class<? extends Converter> convert() default DefaultConverter.class;
}
