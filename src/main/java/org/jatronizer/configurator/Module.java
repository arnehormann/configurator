package org.jatronizer.configurator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class grouping multiple configure parameters.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Module {

	/**
	 * Returns the name of this module.
	 * Callers of this method should use {@code getClass().getSimpleName()} as the name if this is {@code ""}.
	 */
	String name() default "";

	/**
	 * Returns an optional tag for this module.
	 * A tag should not contain spaces. Spaces should be used to separate multiple tags.
	 * Tags can be used to group modules, e.g. to hide some modules in introductory documentation.
	 */
	String tag() default "";

	/**
	 * Returns the key prefix used for all configure parameters this class contains.
	 * To make modules distinguishable, {@code keyPrefix} should end in a separator (e.g. "{@code /}").
	 */
	String keyPrefix() default "";
}
