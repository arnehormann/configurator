package org.jatronizer.configurator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides finer grained control for configuration elements.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Module {

	/**
	 * Returns the name of this module.
	 * Callers of this method should use <code>getClass().getSimpleName()</code> as the name if this is "".
	 */
	String name() default "";

	/**
	 * Returns the prefix used for all <code>Config</code> keys this class contains.
	 * To make modules distinguishable, the prefix should end in a separator (e.g. "/").
	 */
	String prefix() default "";
}