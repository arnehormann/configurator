package org.jatronizer.configurator;

import java.io.IOException;
import java.io.OutputStream;

import static org.jatronizer.configurator.ConfigSupport.KeyFormat.arg;
import static org.jatronizer.configurator.ConfigSupport.KeyFormat.env;

/**
 * HelpPrinter provides a default format to display a help text on the command line.
 * An instance can be passed to {@link org.jatronizer.configurator.MultiConfigurator#walk(org.jatronizer.configurator.ConfigVisitor)}.
 */
final class HelpPrinter implements ConfigVisitor {

	private final OutputStream out;
	private final String envVarPrefix;

	/**
	 * Create a new HelpPrinter printing to {@link System#err}.
	 * @param envVarPrefix Prefix used when looking up keys in environment variables.
	 */
	public HelpPrinter(String envVarPrefix) {
		this(null, envVarPrefix);
	}

	/**
	 * Create a new HelpPrinter printing to {@code out}.
	 * @param out Help text is printed here; when {@code out} is {@code null}, {@link System#err} is used.
	 * @param envVarPrefix Prefix used when looking up keys in environment variables.
	 */
	public HelpPrinter(OutputStream out, String envVarPrefix) {
		if (out == null) {
			out = System.err;
		}
		if (envVarPrefix == null) {
			envVarPrefix = "";
		}
		this.out = out;
		this.envVarPrefix = envVarPrefix;
	}

	private void forceWrite(String text) {
		try {
			out.write(text.getBytes());
		} catch (IOException e) {
			throw new ConfigException(e);
		}
	}

	public void visitModule(String name, String tag, String description, Configurator configurator) {
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
		String text = "arg[" + arg.from(ConfigManager.ARG_PREFIX + key) + "] / env[" + env.from(envVarPrefix + key) + "]\n";
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
