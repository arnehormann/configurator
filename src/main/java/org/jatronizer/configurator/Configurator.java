package org.jatronizer.configurator;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.*;

/**
 * A Configurator manages the configuration of an application or a subsystem.
 *
 * It helps to keep the whole configuration of a system and the description of available configuration
 * options in one place and provides tools to simplify generating specific help texts and examples.
 * It also closely couples code and description and keeps them in sync.
 *
 * The configuration takes the form of an Object of any type with <code>@Parameter</code> annotated fields.
 * The field name, type and additional optional information from the annotation are used to populate the fields
 * from specified sources. Configuration changes at runtime are possible, too.
 *
 * Configuration parameters are set in the order of arrival - later ones overwrite earlier ones.
 * This can be used to e.g. initialize a system with default values, then overwrite those with
 * a configuration file, then environment variables where given and last from command line arguments.
 *
 * To use this class in a multi-threaded context, all read accesses of configuration fields should be synchronized
 * on the configuration Object.
 *
 * @param <C> the type of the configuration instance
 */
public final class Configurator<C> {

	/**
	 * Retrieves the description of a Class, one of its fields or an enum value.
	 */
	public static String description(AnnotatedElement elem) {
		Description d = elem.getAnnotation(Description.class);
		if (d == null) {
			return null;
		}
		return d.value();
	}

	/**
	 * Creates a configuration manager.
	 * The configuration is an object with fields annotated with <code>Parameter</code>.
	 * @param configuration an instance of a configuration.
	 *               It is used by the builder to determine available fields and their types
	 *               and to get their default values (the one set on the instance passed here).
	 *               The Configurator assumes ownership - you should not write to any of the fields
	 *               yourself. Read access to fields have to be synchronized on the configuration object
	 *               when concurrent accesses are possible.
	 *               <code>Module</code> and <code>Description</code> annotations are processed.
	 */
	public static <C> Configurator<C> control(C configuration) {
		Class cc = configuration.getClass();
		Module module = (Module) cc.getAnnotation(Module.class);
		String name = "";
		String prefix = "";
		if (module != null) {
			name = module.name();
			prefix = module.prefix();
		}
		ParameterField[] params = fetchParameters(configuration, prefix);
		return new Configurator<C>(configuration, params, module != null, name, prefix, description(cc));
	}

	/**
	 * Creates a configuration manager.
	 * The configuration is an object with fields annotated with <code>Parameter</code>.
	 * @param configuration an instance of a configuration.
	 *               It is used by the builder to determine available fields and their types
	 *               and to get their default values (the one set on the instance passed here).
	 *               The Configurator assumes ownership - you should not write to any of the fields
	 *               yourself. Read access to fields have to be synchronized on the configuration object
	 *               when concurrent accesses are possible.
	 * @param name mocule name
	 * @param prefix prefix to add to all keys contained in this module
	 * @param description answer to the question "What is this module used for?"
	 */
	public static <C> Configurator<C> control(C configuration, String name, String prefix, String description) {
		ParameterField[] params = fetchParameters(configuration, prefix);
		return new Configurator<C>(configuration, params, true, name, prefix, description);
	}

	private static ParameterField[] fetchParameters(Object config, String prefix) {
		Class cc = config.getClass();
		Field[] fields = cc.getDeclaredFields();
		ArrayList<ParameterField> conf = new ArrayList<ParameterField>(fields.length);
		for (Field f : fields) {
			Parameter p = f.getAnnotation(Parameter.class);
			if (p != null) {
				conf.add(ParameterField.create(config, f, p, prefix));
			}
		}
		if (conf.isEmpty()) {
			throw new ConfigurationException(
					"configuration " +
					cc +
					" has no fields annotated with " +
					Parameter.class.getSimpleName()
			);
		}
		ParameterField[] parameters = conf.toArray(new ParameterField[conf.size()]);
		Arrays.sort(parameters);
		return parameters;
	}

	private final C config;
	private final String[] keys;
	private final ParameterField[] parameters;
	private final String name;
	private final String prefix;
	private final String description;
	private final boolean isModule;

	private Configurator(C config, ParameterField[] parameters,
	                     boolean isModule, String name, String prefix, String doc) {
		this.config = config;
		String[] keys = new String[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			keys[i] = parameters[i].key;
		}
		this.parameters = parameters;
		this.keys = keys;
		this.isModule = isModule;
		this.prefix = prefix == null ? "" : prefix;
		this.description = doc == null ? "" : doc;
		this.name = name == null || "".equals(name)
				? config.getClass().getSimpleName()
				: name;
	}

	/**
	 * Retrieves the configuration managed with this <code>Configurator</code>.
	 */
	public C config() {
		return config;
	}

	/**
	 * Reports whether the controlled configuration is annotated with <code>Module</code>.
	 * If it is, the methods <code>name</code>, <code>prefix</code> and <code>description</code>
	 * retrieve its respective values.
	 */
	public boolean isModule() {
		return isModule;
	}

	/**
	 * Retrieves the name of the configuration module.
	 */
	public String name() {
		return name;
	}

	/**
	 * Retrieves the prefix of the configuration module.
	 */
	public String prefix() {
		return prefix;
	}

	/**
	 * Retrieves the description of the configuration module.
	 */
	public String description() {
		return description;
	}

	/**
	 * Retrieves the keys of all available configuration parameters for this module.
	 */
	public String[] keys() {
		String[] keys = new String[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			keys[i] = parameters[i].key;
		}
		return keys;
	}

	private ParameterField parameter(String key) {
		int i = Arrays.binarySearch(keys, key);
		if (i < 0) {
			return null;
		}
		return parameters[i];
	}

	/**
	 * Retrieves the description of the configuration parameter with the specified key.
	 * If the key is unknown, <code>null</code> is returned.
	 */
	public String description(String key) {
		ParameterField p = parameter(key);
		if (p == null) {
			return null;
		}
		return p.description;
	}

	/**
	 * Retrieves the default value of the configuration option with the specified key in String form.
	 * If the key is unknown, <code>null</code> is returned.
	 */
	public String defaultValue(String key) {
		ParameterField p = parameter(key);
		if (p == null) {
			return null;
		}
		return p.defaultValue;
	}

	/**
	 * Retrieves the available values for an enum field.
	 * If the key is unknown, <code>null</code> is returned.
	 * If the key does not belong to an enum, an empty array is returned.
	 */
	public String[] options(String key) {
		ParameterField p = parameter(key);
		if (p == null) {
			return null;
		}
		return p.enumNames();
	}

	/**
	 * Retrieves the description of an option - an enum value - for a parameter.
	 * If the key or option is unknown, <code>null</code> is returned.
	 * If the option has no Description annotation, "" is returned.
	 */
	public String description(String key, String option) {
		ParameterField p = parameter(key);
		if (p == null) {
			return null;
		}
		Field field = p.enumValue(option);
		if (field == null) {
			return null;
		}
		return description(field);
	}

	/**
	 * Retrieves the current value of the configuration option with the specified key in String form.
	 * If the key is unknown, <code>null</code> is returned.
	 */
	public String value(String key) {
		ParameterField p = parameter(key);
		if (p == null) {
			return null;
		}
		return p.get(config);
	}

	/**
	 * Sets a configuration parameter and reports whether the key exists.
	 */
	public boolean set(String key, String value) {
		ParameterField p = parameter(key);
		if (p == null) {
			return false;
		}
		p.set(config, value);
		return true;
	}

	/**
	 * Sets multiple configuration parameters and reports whether all keys existed.
	 */
	public boolean set(Map<String, String> configuration) {
		boolean allExist = true;
		for (Map.Entry<String, String> e : configuration.entrySet()) {
			allExist &= set(e.getKey(), e.getValue());
		}
		return allExist;
	}

	/**
	 * Sets multiple configuration parameters and reports whether all keys existed.
	 * Each key and each value stored in configuration must be a String.
	 */
	public boolean set(Properties configuration) {
		boolean allExist = true;
		for (Map.Entry<Object, Object> e : configuration.entrySet()) {
			allExist &= set((String) e.getKey(), (String) e.getValue());
		}
		return allExist;
	}
}
