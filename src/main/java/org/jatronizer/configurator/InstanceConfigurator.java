package org.jatronizer.configurator;

import java.util.*;

/**
 * An InstanceConfigurator manages the configure of an application or a subsystem.
 *
 * It assists keeping the whole configure of a system and the description of available configure
 * options in one place and provides tools to simplify generating specific help texts and examples.
 * It also encourages close coupling of code and its description and keeping them in sync.
 *
 * The managed configure is an Object of any type with fields annotated with {@code @Parameter}.
 * The field name, type and additional optional information from the annotation are used to populate the fields
 * from specified sources. ConfigManager changes at runtime are possible, too.
 *
 * ConfigManager parameters are set in the order of arrival - later ones overwrite earlier ones.
 * This can be used to e.g. initialize a system with default values, then overwrite those with
 * a configure file, then environment variables where given and last from command line arguments.
 *
 * To use this class in a multi-threaded context, all read accesses of configure fields should be synchronized
 * on the configure Object.
 *
 * @param <C> the type of the configure instance
 */
final class InstanceConfigurator<C> implements Configurator<C> {

	/**
	 * Creates a configure manager.
	 * The configure is an object with fields annotated with {@link Parameter}.
	 * @param configuration an instance of a configure.
	 *               It is used by the builder to determine available fields and their types
	 *               and to get their default values (the one set on the instance passed here).
	 *               The InstanceConfigurator assumes ownership - you should not write to any of the fields
	 *               yourself. Read access to fields have to be synchronized on the configure object
	 *               when concurrent accesses are possible.
	 *               {@link Module} and {@link Description} annotations are processed.
	 */
	public static <C> InstanceConfigurator<C> control(C configuration) {
		Class cc = configuration.getClass();
		Module module = (Module) cc.getAnnotation(Module.class);
		String name = "";
		String keyPrefix = "";
		if (module != null) {
			name = module.name();
			keyPrefix = module.keyPrefix();
		}
		ConfigParameter[] params = ConfigSupport.fetchParameters(configuration, keyPrefix);
		return new InstanceConfigurator<C>(
				configuration, params, module != null,
				name, keyPrefix, ConfigSupport.description(cc)
		);
	}

	/**
	 * Creates a configure manager.
	 * @param configuration An instance of a configure.
	 *               The InstanceConfigurator assumes ownership - you should not write to any of the fields
	 *               yourself. Read access to fields have to be synchronized on the configure object
	 *               when concurrent accesses are possible.
	 * @param name ConfigManager module name.
	 * @param keyPrefix Prefix to add to all keys contained in this module.
	 * @param description Answer to the question "What is this module used for?".
	 * @param params Managed configure parameters, must all represent fields on {@code configure}.
	 */
	public static <C> InstanceConfigurator<C> control(
			C configuration, String name, String keyPrefix, String description, ConfigParameter[] params) {
		// to synchronize access on an instance, check that all params share the same declaring class
		Class configType = configuration.getClass();
		for (ConfigParameter param : params) {
			if (param.outerType() != configType) {
				throw new ConfigurationException("All configure parameters must belong to " + configType);
			}
		}
		return new InstanceConfigurator<C>(configuration, params, true, name, keyPrefix, description);
	}

	private final C config;
	private final String[] keys;
	private final ConfigParameter[] parameters;
	private final String name;
	private final String prefix;
	private final String description;
	private final boolean isModule;

	private InstanceConfigurator(
			C config, ConfigParameter[] parameters,
			boolean isModule, String name, String prefix, String desc) {
		this.config = config;
		String[] keys = new String[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			keys[i] = parameters[i].key();
		}
		this.parameters = parameters;
		this.keys = keys;
		this.isModule = isModule;
		this.prefix = prefix == null ? "" : prefix;
		this.description = desc == null ? "" : desc;
		this.name = name == null || "".equals(name)
				? config.getClass().getSimpleName()
				: name;
	}

	private ConfigParameter field(String key) {
		int i = Arrays.binarySearch(keys, key);
		if (i < 0) {
			return null;
		}
		return parameters[i];
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasKey(String key) {
		return Arrays.binarySearch(keys, key) > 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] keys() {
		String[] keys = new String[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			keys[i] = parameters[i].key();
		}
		return keys;
	}

	/**
	 * {@inheritDoc}
	 */
	public ConfigParameter parameter(String key) {
		return field(key);
	}

	/**
	 * {@inheritDoc}
	 */
	public String value(String key) {
		ConfigParameter p = field(key);
		if (p == null) {
			return null;
		}
		return p.get(config);
	}

	/**
	 * {@inheritDoc}
	 */
	public int set(String key, String value) {
		ConfigParameter p = field(key);
		if (p == null) {
			return 0;
		}
		p.set(config, value);
		return 1;
	}

	/**
	 * {@inheritDoc}
	 */
	public int set(Map<String, String> configuration) {
		int numSet = 0;
		synchronized (config) {
			for (Map.Entry<String, String> e : configuration.entrySet()) {
				numSet += set(e.getKey(), e.getValue());
			}
		}
		return numSet;
	}

	/**
	 * {@inheritDoc}
	 */
	public int set(Properties configuration) {
		int numSet = 0;
		synchronized (config) {
			for (Map.Entry<Object, Object> e : configuration.entrySet()) {
				numSet += set((String) e.getKey(), (String) e.getValue());
			}
		}
		return numSet;
	}

	/**
	 * {@inheritDoc}
	 */
	public void walk(ConfigurationVisitor v) {
		v.visitModule(name, description, this);
		for (ConfigParameter field : parameters) {
			v.visitParameter(field, field.get(config));
		}
	}
}
