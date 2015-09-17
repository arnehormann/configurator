package org.jatronizer.configurator;

/**
 * Signals that a parameter value can not be read or written or that the conversion
 * between the parameter type and {@code String} failed.
 */
public class IllegalValueException extends RuntimeException {
	public IllegalValueException(String msg) {
		super(msg);
	}
	public IllegalValueException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
