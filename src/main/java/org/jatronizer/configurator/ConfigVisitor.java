package org.jatronizer.configurator;

/**
 * Explores configuration modules.
 * Passing an instance of {@code ConfigurationVisitor} will, for each module,
 * call {@link #visitModule(String, String, String, Configurator)} and then
 * call {@link #visitParameter(ConfigParameter, String)} per configuration parameter.
 *
 * A {@code ConfigurationVisitor} can be used to e.g. dynamically create help texts including
 * the currently set values.
 */
public interface ConfigVisitor {

	/**
	 * Is called per configuration module.
	 * @param name the name of the module
	 * @param tag the tag
	 * @param description a descriptive text
	 * @param configurator the configuration
	 */
	void visitModule(String name, String tag, String description, Configurator configurator);

	/**
	 * Is called per configuration parameter.
	 * @param parameter the current parameter
	 * @param currentValue the value of the current parameter
	 */
	void visitParameter(ConfigParameter parameter, String currentValue);
}
