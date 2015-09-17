package org.jatronizer.configurator;

import java.util.*;

/**
 * Manages the configuration of an application or a subsystem.
 *
 * It assists keeping the whole configuration of a system and the description of available configuration
 * options in one place and provides tools to simplify generating specific help texts and examples.
 * It also encourages tight coupling of code and its description and keeping them in sync.
 *
 * The managed configuration is an Object of any type with fields annotated with {@code @Parameter}.
 * The field name, type and additional optional information from the annotation are used to populate the fields
 * from specified sources. Configuration changes at runtime are possible, too.
 *
 * Configuration parameters are set in the order of arrival - later ones overwrite earlier ones.
 * This can be used to e.g. initialize a system with default values, then overwrite those with parameters from
 * a configuration file, then environment variables and last from command line arguments.
 *
 * To use this class in a multi-threaded context, all read accesses of configuration fields should be synchronized
 * on the configuration Object.
 *
 * @param <C> the type of the configuration instance.
 */
final class InstanceConfigurator<C> implements Configurator {

	/**
	 * Creates a configuration manager.
	 * The configuration is an object with fields annotated with {@link Parameter}.
	 * @param configuration an instance of a configuration.
	 *               It is used by the builder to determine available fields and their types
	 *               and to get their default values (the one set on the instance passed here).
	 *               The InstanceConfigurator assumes ownership - you should not write to any of the fields
	 *               yourself. Read access to fields have to be synchronized on the configuration object
	 *               when concurrent accesses are possible.
	 *               {@link Module} and {@link Description} annotations are processed.
	 */
	@SuppressWarnings("unchecked")
	public static <C> InstanceConfigurator<C> control(C configuration) {
		Class cc = configuration.getClass();
		Module module = (Module) cc.getAnnotation(Module.class);
		String name = "";
		String keyPrefix = "";
		String tag = "";
		if (module != null) {
			name = module.name();
			keyPrefix = module.keyPrefix();
			tag = module.tag();
		}
		ConfigParameter[] params = ConfigSupport.fetchParameters(configuration, keyPrefix);
		return new InstanceConfigurator<C>(
				configuration, params, name, tag, ConfigSupport.description(cc)
		);
	}

	/**
	 * Creates a configuration manager.
	 * @param configuration An instance of a configuration.
	 *               The InstanceConfigurator assumes ownership - you should not write to any of the fields
	 *               yourself. Read access to fields have to be synchronized on the configuration object
	 *               when concurrent accesses are possible.
	 * @param name Configuration module name.
	 * @param keyPrefix Prefix to add to all keys contained in this module.
	 * @param tag An optional tag or space separated list of tags for the module.
	 * @param description Answer to the question "What is this module used for?".
	 * @param params Managed configuration parameters, must all represent fields on {@code configuration}.
	 */
	public static <C> InstanceConfigurator<C> control(
			C configuration,
			String name,
			String keyPrefix,
			String tag,
			String description,
			ConfigParameter<C,?>[] params
	) {
		// to synchronize access on an instance, check that all params share the same declaring class
		Class configType = configuration.getClass();
		for (ConfigParameter param : params) {
			if (param.outerType() != configType) {
				throw new ConfigException("All configure parameters must belong to " + configType);
			}
		}
		return new InstanceConfigurator<C>(configuration, params, name, tag, description);
	}

	private final C config;
	private final String[] keys;
	private final ConfigParameter[] parameters;
	private final String name;
	private final String tag;
	private final String description;

	private InstanceConfigurator(
			C config,
			ConfigParameter[] parameters,
			String name,
			String tag,
			String desc
	) {
		this.config = config;
		String[] keys = new String[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			keys[i] = parameters[i].key();
		}
		this.parameters = parameters;
		this.keys = keys;
		this.tag = tag == null ? "" : tag;
		this.description = desc == null ? "" : desc;
		this.name = name == null || "".equals(name)
				? config.getClass().getSimpleName()
				: name;
	}

	@SuppressWarnings("unchecked")
	private ConfigParameter field(String key) {
		int i = Arrays.binarySearch(keys, key);
		if (i < 0) {
			return null;
		}
		return parameters[i];
	}

	public boolean hasKey(String key) {
		return Arrays.binarySearch(keys, key) > 0;
	}

	public String[] keys() {
		String[] keys = new String[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			keys[i] = parameters[i].key();
		}
		return keys;
	}

	public ConfigParameter parameter(String key) {
		return field(key);
	}

	@SuppressWarnings("unchecked")
	public String value(String key) {
		ConfigParameter p = field(key);
		if (p == null) {
			return null;
		}
		return p.get(config);
	}

	@SuppressWarnings("unchecked")
	public int set(String key, String value) {
		ConfigParameter p = field(key);
		if (p == null) {
			return 0;
		}
		try {
			p.set(config, value);
			return 1;
		} catch (Exception e) {
			// probably a conversion error
			return 0;
		}
	}

	public Map<String, String> set(Map<String, String> configuration) {
		ErrorMap invalid = ErrorMap.EMPTY;
		synchronized (config) {
			for (Map.Entry<String, String> e : configuration.entrySet()) {
				if (set(e.getKey(), e.getValue()) == 0) {
					invalid = invalid.fput(e.getKey(), e.getValue());
				}
			}
		}
		return invalid;
	}

	public Map<String, String> set(Properties configuration) {
		ErrorMap invalid = ErrorMap.EMPTY;
		synchronized (config) {
			for (Map.Entry<Object, Object> e : configuration.entrySet()) {
				String key = (String) e.getKey();
				String value = (String) e.getValue();
				if (set(key, value) == 0) {
					invalid = invalid.fput(key, value);
				}
			}
		}
		return invalid;
	}

	@SuppressWarnings("unchecked")
	public void walk(ConfigVisitor v) {
		v.visitModule(name, tag, description, this);
		for (ConfigParameter field : parameters) {
			v.visitParameter(field, field.get(config));
		}
	}
}
