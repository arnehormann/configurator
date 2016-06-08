package org.jatronizer.configurator;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import static org.jatronizer.configurator.ConfigSupport.KeyFormat.arg;
import static org.jatronizer.configurator.ConfigSupport.KeyFormat.env;

/**
 * Provides a default format to display a help text on the command line.
 * An instance can be passed to {@link MultiConfigurator#walk}.
 */
final class HelpPrinter implements ConfigVisitor {

	private final OutputStream out;
	private final String envVarPrefix;

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
			out.write(text.getBytes(Charset.forName("UTF-8")));
		} catch (IOException e) {
			throw new ConfigException(e);
		}
	}

	public void visitConfiguration(String name, String tags, String description, Configurator configurator) {
		if ("".equals(name)) {
			return;
		}
		String text = "\nParameters for " + name + ":";
		if (!"".equals(description)) {
			text += " " + description;
		}
		text += "\n";
		forceWrite(text);
	}

	public void visitParameter(ConfigParameter parameter, String value) {
		final String EMPTY = "                                ";
		String key = parameter.key();
		String text = arg.from(ConfigManager.ARG_PREFIX + key) + ", $" + env.from(envVarPrefix + key) + "\n";
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
				text += "\tvalue: '" + value + "' (is default)\n";
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
				text += "\t  " + option;
				String pdesc = parameter.description(option);
				if (pdesc == null || "".equals(pdesc)) {
					text += "\n";
					continue;
				}
				int paddingNeeded = 2 + longest - option.length();
				while (paddingNeeded > EMPTY.length()) {
					text += EMPTY;
					paddingNeeded -= EMPTY.length();
				}
				text += EMPTY.substring(0, paddingNeeded) + pdesc + "\n";
			}
		}
		forceWrite(text);
	}
}
