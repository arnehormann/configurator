package org.jatronizer.configurator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A {@code Description} describes the usage of something.
 * In this case, something may be
 * - a Module
 * - a Parameter
 * - an Option (enum value if the parameter type is an enum)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface Description {
	String value();
}
