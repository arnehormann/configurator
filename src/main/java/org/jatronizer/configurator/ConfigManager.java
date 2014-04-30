package org.jatronizer.configurator;

import static org.jatronizer.configurator.ConfigSupport.KeyFormat.*;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class ConfigManager {

	// Can not be instanciated
	private ConfigManager() {}

	private static final String ARG_PREFIX = "-";

	/**
	 * HelpPrinter provides a default format to display a help text on the command line.
	 * An instance can be passed to {@code MultiConfigurator.walk}.
	 */
	private static class HelpPrinter implements ConfigurationVisitor {

		private final OutputStream out;
		private final String envVarPrefix;

		/**
		 * Create a new HelpPrinter printing to {@link java.lang.System#err}.
		 * @param envVarPrefix Prefix used when looking up keys in environment variables.
		 */
		public HelpPrinter(String envVarPrefix) {
			this(null, envVarPrefix);
		}

		/**
		 * Create a new HelpPrinter printing to {@code out}.
		 * @param out Help text is printed here; when {@code out} is {@code null}, {@link java.lang.System#err} is used.
		 * @param envVarPrefix Prefix used when looking up keys in environment variables.
		 */
		public HelpPrinter(OutputStream out, String envVarPrefix) {
			this.out = out == null ? System.err : out;
			this.envVarPrefix = envVarPrefix == null ? "" : envVarPrefix;
		}

		private void forceWrite(String text) {
			try {
				out.write(text.getBytes());
			} catch (IOException e) {
				throw new ConfigurationException(e);
			}
		}

		public void visitModule(String name, String description, Configurator configurator) {
			if ("".equals(name)) {
				return;
			}
			String text = "Module " + name + ":";
			if (!"".equals(description)) {
				text += " " + description;
			}
			text += "\n";
			forceWrite(text);
		}

		public void visitParameter(ConfigParameter parameter, String value) {
			final String EMPTY = "                                ";
			String key = parameter.key();
			String text = "arg[" + arg.from(ARG_PREFIX + key) + "] / env[" + env.from(envVarPrefix + key) + "]\n";
			String description = parameter.description();
			if (!"".equals(description)) {
				text += "\t" + description + "\n";
			}
			String defaultValue = parameter.defaultValue();
			if (value == null) {
				if (defaultValue != null) {
					text += "\tdefault: \"" + defaultValue + "\"\n";
				}
			} else {
				if (defaultValue == null) {
					text += "\tvalue: '" + value + "'\n";
				} else if (value.equals(defaultValue)) {
					text += "\tvalue: '" + value + "' (default)\n";
				} else {
					text += "\tvalue: '" + value + "', default: '" + defaultValue + "'\n";
				}
			}
			String[] options = parameter.options();
			if (options.length > 0) {
				text += "\tavailable values:\n";
				int longest = 0;
				for (String option : options) {
					if (option.length() > longest) {
						longest = option.length();
					}
				}
				for (String option : options) {
					text += "\t  ";
					int paddingNeeded = longest - option.length();
					while (paddingNeeded > EMPTY.length()) {
						text += EMPTY;
						paddingNeeded -= EMPTY.length();
					}
					text += EMPTY.substring(0, longest) + option;
					String pdesc = parameter.description(option);
					if (!"".equals(pdesc)) {
						text += "  " + pdesc;
					}
					text += "\n";
				}
			}
			forceWrite(text);
		}
	}

	public static <C,P> ConfigParameter<C,P> parameter(
			C configuration, Field field, String key, Class<P> converterClass, String keyPrefix) {
		if (configuration == null) {
			throw new ConfigurationException("configuration is null");
		}
		if (field == null) {
			throw new ConfigurationException("field is null");
		}
		if (configuration.getClass() != field.getDeclaringClass()) {
			throw new ConfigurationException("field is not declared on " + configuration.getClass());
		}
		return ConfigParameterField.create(configuration, field, key, converterClass, keyPrefix);
	}

	@SuppressWarnings("unchecked")
	public static <C,P> ConfigParameter<C,P>[] parameters(C configuration, String keyPrefix) {
		if (configuration == null) {
			throw new ConfigurationException("configuration is null");
		}
		return ConfigSupport.fetchParameters(configuration, keyPrefix);
	}

	public static <C> Configurator<C> module(
			C configuration, String name, String keyPrefix, String description, ConfigParameter... params) {
		if (configuration == null) {
			throw new ConfigurationException("configuration is null");
		}
		if (params.length == 0) {
			params = parameters(configuration, keyPrefix);
		}
		return InstanceConfigurator.control(configuration, name, keyPrefix, description, params);
	}

	@SuppressWarnings("unchecked")
	public static <C> Configurator<C> configure(C... configurations) {
		if (configurations.length == 0) {
			throw new ConfigurationException("configurations are empty");
		}
		// C is erased to Object and configure could have been called when manage is meant.
		// Force code corrections by feedback.
		Class cc = configurations[0].getClass();
		if (cc == InstanceConfigurator.class || cc == MultiConfigurator.class) {
			throw new ConfigurationException("Wrong function, call manage instead of configure");
		}
		// create all configurators
		Configurator<C>[] configurators = new Configurator[configurations.length];
		for (int i = 0; i < configurations.length; i++) {
			configurators[i] = InstanceConfigurator.control(configurations[i]);
		}
		return manage(configurators);
	}

	@SuppressWarnings("unchecked")
	public static <C> Configurator<C> manage(Configurator<C>... configurators) {
		switch (configurators.length) {
			case 0:
				throw new ConfigurationException("configurators are empty");
			case 1:
				return configurators[0];
		}
		return MultiConfigurator.configure(configurators);
	}

	public static void printHelpFor(Configurator configurator, String envVarPrefix, OutputStream out) {
		HelpPrinter help = new HelpPrinter(out, envVarPrefix);
		configurator.walk(help);
	}

	public static int getArgs(Map<String, String> dst, Collection<String> dstUnused, String[] keys, String...args) {
		return ConfigSupport.setParsedValues(dst, dstUnused, keys, ARG_PREFIX, arg, args);
	}

	public static int getEnv(Map<String, String> dst, String[] keys, String envVarPrefix) {
		return ConfigSupport.setValues(dst, keys, envVarPrefix, env, System.getenv());
	}

	public static String[] setFromArgs(Configurator configurator, String[] args) {
		ArrayList<String> unused = new ArrayList<String>(args.length / 2);
		HashMap<String, String> config = new HashMap<String, String>(args.length, 1.0f);
		getArgs(config, unused, configurator.keys(), args);
		configurator.set(config);
		return unused.toArray(new String[unused.size()]);
	}

	public static void setFromEnv(Configurator configurator, String envVarPrefix) {
		Map<String, String> src = System.getenv();
		HashMap<String, String> config = new HashMap<String, String>(src.size(), 1.0f);
		getEnv(config, configurator.keys(), envVarPrefix);
		configurator.set(config);
	}
}
