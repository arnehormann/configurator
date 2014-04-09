package org.jatronizer.configurator;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class CliConfiguration<C> {

	private static enum AnsiMode {
		//reset(0),
		bold(1),
		//blink(5),
		//reverse(7),
		fgBlack(30),
		fgRed(31),
		fgGreen(32),
		fgYellow(33),
		fgBlue(34),
		fgMagenta(35),
		fgCyan(36),
		fgWhite(37),
		bgBlack(40),
		bgRed(41),
		bgGreen(42),
		bgYellow(43),
		bgBlue(44),
		bgMagenta(45),
		bgCyan(46),
		bgWhite(47);

		public final String code;

		private AnsiMode(int code) {
			this.code = "" + code;
		}
	}

	private static String wrap(String text, AnsiMode...mode) {
		if (mode.length == 0) {
			return text;
		}
		String result = "\u001b[" + mode[0].code;
		for (int i = 1; i < mode.length; i++) {
			result += ";" + mode[i].code;
		}
		return result + "m" + text + "\u001b[0m";
	}

	private static final String TEXT_KEY;
	private static final String TEXT_ARG_KEY;
	private static final String TEXT_ENV_KEY;
	private static final String TEXT_DESCRIPTION;
	private static final String TEXT_VALUE;
	private static final String TEXT_DEFAULT;

	static {
		// see http://en.wikipedia.org/wiki/ANSI_escape_code
		if ("xterm-256color".equals(System.getenv("TERM"))) {
			TEXT_KEY     = wrap("%1$s",AnsiMode.fgWhite,  AnsiMode.bgGreen);
			TEXT_ARG_KEY = wrap("%2$s",AnsiMode.fgWhite,  AnsiMode.bgCyan);
			TEXT_ENV_KEY = wrap("%3$s",AnsiMode.fgWhite,  AnsiMode.bgBlue);
			TEXT_DESCRIPTION  = "%4$s";
			TEXT_VALUE   = wrap("%5$s",AnsiMode.fgYellow, AnsiMode.bgBlack);
			TEXT_DEFAULT = wrap("%6$s",AnsiMode.fgCyan,   AnsiMode.bgBlack);
		} else {
			TEXT_KEY         = "%1$s";
			TEXT_ARG_KEY     = "%2$s";
			TEXT_ENV_KEY     = "%3$s";
			TEXT_DESCRIPTION = "%4$s";
			TEXT_VALUE       = "%5$s";
			TEXT_DEFAULT     = "%6$s";
		}
	}

	private static void print(
			PrintStream out,
			String key, String argkey, String envkey,
			String description,
			String value, String defaultValue) {
		String format = TEXT_KEY + " / " + TEXT_ARG_KEY + " / " + TEXT_ENV_KEY + "\n";
		if (!"".equals(description)) {
			format += "\t" + TEXT_DESCRIPTION + "\n";
		}
		if (value == null) {
			if (defaultValue != null) {
				format += "\tdefault: '" + TEXT_DEFAULT + "'\n";
			}
		} else {
			if (defaultValue == null) {
				format += "\tvalue: '" + TEXT_VALUE + "'\n";
			} else if (value.equals(defaultValue)) {
				format += "\tvalue: '" + TEXT_VALUE + "' (default)\n";
			} else {
				format += "\tvalue: '" + TEXT_VALUE + "', default: '" + TEXT_DEFAULT + "'\n";
			}
		}
		out.printf(format, key, argkey, envkey, description, value, defaultValue);
	}

	private Configurator<C> cbuilder;
	private String[] keys;
	private Map<String, String> argkeys;
	private Map<String, String> invargkeys;
	private Map<String, String> envkeys;
	private Map<String, String> invenvkeys;
	private String[] argrest;

	public CliConfiguration(C configuration, String envPrefix, String[] args) {
		cbuilder = Configurator.control(configuration);
		keys = cbuilder.keys();
		argkeys = Parser.asArgKeys("-", keys);
		envkeys = Parser.asEnvKeys(envPrefix, keys);
		Parser.Args parsedArgs = Parser.mapKeys(argkeys, Parser.parse("=", args));
		argrest = parsedArgs.rest();
		Map<String, String> argmap = parsedArgs.parsed;
		Map<String, String> envmap = Parser.mapKeys(envkeys, System.getenv()).parsed;
		cbuilder.process(envmap).process(argmap);
		// invert argument maps for reverse lookup
		invargkeys = new HashMap<String, String>(argkeys.size(), 1.0f);
		for (Map.Entry<String, String> e : argkeys.entrySet()) {
			invargkeys.put(e.getValue(), e.getKey());
		}
		invenvkeys = new HashMap<String, String>(envkeys.size(), 1.0f);
		for (Map.Entry<String, String> e : envkeys.entrySet()) {
			invenvkeys.put(e.getValue(), e.getKey());
		}
	}

	public String[] unknownArgKeys() {
		return argrest.clone();
	}

	public void printHelp(PrintStream out) {
		for (String key : keys) {
			print(
					out,
					key, invargkeys.get(key), invenvkeys.get(key),
					cbuilder.description(key),
					cbuilder.value(key),
					cbuilder.defaultValue(key)
			);
		}
	}

	public void printCurrent(PrintStream out, boolean all) {
		for (String key : keys) {
			String value = cbuilder.value(key);
			String defaultValue = cbuilder.defaultValue(key);
			if (all || (value != defaultValue || (value != null && value.equals(defaultValue)))) {
				out.printf("%s=%s\n", key, value);
			}
		}
	}
}
