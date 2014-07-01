package org.jatronizer.configurator;

/**
 * A {@code ConfigException} is thrown when an Object can not be used as a configuration.
 */
public final class ConfigException extends RuntimeException {
	public ConfigException(String msg) {
		super(msg);
	}
	public ConfigException(Throwable cause) {
		super(cause);
	}
	public ConfigException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
