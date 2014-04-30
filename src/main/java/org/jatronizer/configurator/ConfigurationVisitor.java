package org.jatronizer.configurator;

/**
 * Explores the managed configure modules and their parameters.
 * Passing an instance of {@code ConfigurationVisitor} will, for each module,
 * call {@link #visitModule(String, String, Configurator)} and then
 * call {@link #visitParameter(ConfigParameter, String)} per configure parameter.
 *
 * A {@code ConfigurationVisitor} can be used to e.g. dynamically create help texts including
 * the currently set values.
 *
 * @param <C> Type of the configure.
 */
public interface ConfigurationVisitor<C> {

	/**
	 * Is called per configure module.
	 */
	void visitModule(String name, String description, Configurator<C> configurator);

	/**
	 * Is called per configure parameter.
	 * @param <P> Type of the current configure parameter.
	 */
	<P> void visitParameter(ConfigParameter<C,P> parameter, String currentValue);
}
