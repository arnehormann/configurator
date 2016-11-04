package org.jatronizer.configurator;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

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
	 *               yourself.
	 *               {@link Description} annotations are processed.
	 */
	@SuppressWarnings("unchecked")
	public static <C> InstanceConfigurator<C> control(C configuration) {
		return control(
				configuration,
				"",
				"",
				ConfigSupport.description(configuration.getClass()),
				ConfigSupport.fetchParameters(configuration, "")
		);
	}

	/**
	 * Creates a configuration manager.
	 * @param configuration An instance of a configuration.
	 *               The InstanceConfigurator assumes ownership - you should not write to any of the fields
	 *               yourself.
	 * @param name Configuration name.
	 * @param tag An optional tag or space separated list of tags.
	 * @param description Answer to the question "What is it used for?".
	 * @param params Managed configuration parameters.
	 */
	public static <C> InstanceConfigurator<C> control(
			C configuration,
			String name,
			String tag,
			String description,
			ConfigParameter<C,?>[] params
	) {
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
		return p.get();
	}

	@SuppressWarnings("unchecked")
	public int set(String key, String value) {
		ConfigParameter p = field(key);
		if (p == null) {
			return 0;
		}
		try {
			p.set(value);
			return 1;
		} catch (Exception e) {
			// probably a conversion error
			return 0;
		}
	}

	public Map<String, String> set(Map<String, String> configuration) {
		ErrorMap invalid = ErrorMap.EMPTY;
		for (Map.Entry<String, String> e : configuration.entrySet()) {
			if (set(e.getKey(), e.getValue()) == 0) {
				invalid = invalid.fput(e.getKey(), e.getValue());
			}
		}
		return invalid;
	}

	public Map<String, String> set(Properties configuration) {
		ErrorMap invalid = ErrorMap.EMPTY;
		for (Map.Entry<Object, Object> e : configuration.entrySet()) {
			String key = (String) e.getKey();
			String value = (String) e.getValue();
			if (set(key, value) == 0) {
				invalid = invalid.fput(key, value);
			}
		}
		return invalid;
	}

	@SuppressWarnings("unchecked")
	public void walk(ConfigVisitor v) {
		v.visitConfiguration(name, tag, description, this);
		for (ConfigParameter field : parameters) {
			v.visitParameter(field, field.get());
		}
	}
}
