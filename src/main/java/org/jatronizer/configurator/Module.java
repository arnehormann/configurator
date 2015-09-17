package org.jatronizer.configurator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a configuration class that has fields storing parameters.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Module {

	/**
	 * Returns the name of this module.
	 * If it is {@code ""}, callers of this method should use {@code getClass().getSimpleName()} instead.
	 * @return the module name.
	 */
	String name() default "";

	/**
	 * Returns an optional tag for this module.
	 * A tag should not contain spaces. Spaces should be used to separate multiple tags.
	 * Tags can be used to group modules, e.g. to hide some modules in introductory documentation.
	 * @return the module tag.
	 */
	String tag() default "";

	/**
	 * Returns the key prefix used for all configuration parameters this class contains.
	 * To make modules distinguishable, {@code keyPrefix} should end in a separator (e.g. "{@code /}").
	 * @return the specific key prefix of a module instance.
	 */
	String keyPrefix() default "";
}
