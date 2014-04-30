package org.jatronizer.configurator;

/**
 * A {@code IllegalValueException} is thrown when a parameter value can not be read or written or when the conversion
 * between the parameter type and {@code String} fails.
 */
public class IllegalValueException extends RuntimeException {
	public IllegalValueException(String msg) {
		super(msg);
	}
	public IllegalValueException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
