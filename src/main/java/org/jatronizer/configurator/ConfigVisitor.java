package org.jatronizer.configurator;

/**
 * Explores configurations.
 * Passing an instance of {@code ConfigurationVisitor} will call
 * {@link #visitConfiguration(String, String, String, Configurator)} for each configuration and then call
 * {@link #visitParameter(ConfigParameter, String)} per configuration parameter.
 *
 * A {@code ConfigurationVisitor} can e.g. dynamically create help texts including
 * the currently set values.
 */
public interface ConfigVisitor {

	/**
	 * Is called per parameter containing other parameters.
	 * @param name the name of the parameter.
	 * @param tags one or more space-separated tags.
	 * @param description a descriptive text.
	 * @param configurator the configuration.
	 */
	void visitConfiguration(String name, String tags, String description, Configurator configurator);

	/**
	 * Is called per configuration parameter.
	 * @param parameter the current parameter.
	 * @param currentValue the value of the current parameter.
	 */
	void visitParameter(ConfigParameter parameter, String currentValue);
}
