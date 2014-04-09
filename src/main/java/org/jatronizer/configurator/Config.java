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
	 * A key defines the point of access
	 * @return
	 */
	String key() default "";
	String desc() default "";
	Class<? extends Converter> convert() default DefaultConverter.class;
}
