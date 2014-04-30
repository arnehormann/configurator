package org.jatronizer.configurator;

/**
 * A {@code ConfigurationException} is thrown when an Object can not be used as a configure.
 */
public final class ConfigurationException extends RuntimeException {
	public ConfigurationException(String msg) {
		super(msg);
	}

	public ConfigurationException(Throwable cause) {
		super(cause);
	}

	public ConfigurationException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
