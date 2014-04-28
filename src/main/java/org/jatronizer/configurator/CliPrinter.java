package org.jatronizer.configurator;

public class CliPrinter implements CliConfiguration.ModuleVisitor, CliConfiguration.ParameterVisitor {

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
		if ("1".equals(System.getenv("CLICOLOR"))) {
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

	public void visit(String name, String prefix, String description) {
		if (!"".equals(name)) {
			System.err.print("Module " + name + ":");
			if (!"".equals(description)) {
				System.err.print(" " + description);
			}
			System.err.println();
		}
	}

	public void visit(String key, String argKey, String envKey, String defaultValue, String value, String description,
	                  String...enumNameDescPairs) {
		String format = "[" + TEXT_KEY + "] / [" + TEXT_ARG_KEY + "] / [" + TEXT_ENV_KEY + "]\n";
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
		System.err.printf(format, key, argKey, envKey, description, value, defaultValue);
		if (enumNameDescPairs.length > 0) {
			System.err.printf("\tavailable values:\n");
			for (int i = 0; i < enumNameDescPairs.length; i+= 2) {
				System.err.printf("\t  %s\t%s\n", enumNameDescPairs[i], enumNameDescPairs[i+1]);
			}
		}
	}
}
