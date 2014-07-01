package org.jatronizer.configurator;

/**
 * Explores the managed configure modules and their parameters.
 * Passing an instance of {@code ConfigurationVisitor} will, for each module,
 * call {@link #visitModule(String, String, Configurator)} and then
 * call {@link #visitParameter(ConfigParameter, String)} per configuration parameter.
 *
 * A {@code ConfigurationVisitor} can be used to e.g. dynamically create help texts including
 * the currently set values.
 */
public interface ConfigVisitor {

	/**
	 * Is called per configuration module.
	 */
	void visitModule(String name, String tag, String description, Configurator configurator);

	/**
	 * Is called per configuration parameter.
	 */
	void visitParameter(ConfigParameter parameter, String currentValue);
}
