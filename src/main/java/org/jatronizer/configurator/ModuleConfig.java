package org.jatronizer.configurator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ModuleConfig annotates a class containing configurable fields.
 * All fields belong to one module, multiple modules can be configured
 * independently.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModuleConfig {

	/**
	 * The prefix used for all <code>Config</code> keys this class contains.
	 * The prefix should end in a separator if one is used.
	 */
	String prefix() default "";

	/**
	 * A description of the module context.
	 */
	String desc() default "";
}
