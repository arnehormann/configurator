package example.program;

import org.jatronizer.configurator.*;

public class MultiConfiguration {

	public static class MailConfig {
		@Parameter(key = "login")
		@Description("login used for the smtp account")
		private String user = "root";

		@Parameter(key = "pass")
		@Description("password used for the smtp account")
		private String password;

		@Parameter(key = "host")
		private String host = "localhost";

		@Parameter(key = "port")
		private int port = 587;
	}

	public static void main(String[] args) {
		// prefix for all keys - also makes environment variables start with MYAPP_
		final String APP_PREFIX = "myapp/";

		// configuration for warnings by mail 
		MailConfig warn = new MailConfig();
		// use port 25 instead of 587 for warn mails
		warn.port = 25;

		// configuration for newsletters
		MailConfig news = new MailConfig();

		// the configurator manages the warn and news modules
		Configurator conf = ConfigManager.manage(
				ConfigManager.module(
						warn, "warnmails", "smtp/warn/", "",
						"SMTP Account for log messages with level WARN and up"
				),
				ConfigManager.module(
						news, "mailinglist", "smtp/news/", "",
						"SMTP Account for newsletter"
				)
		);

		// use these instead of command line arguments to show the effect
		args = new String[]{
				"-smtp-warn-host=warnhost",
				"-smtp-news-host=newshost",
				"-smdp-warn-port=566" // smdp instead of smtp -> unknown argument
		};

		// fill the configuration instance from environment variables
		ConfigManager.setFromEnv(conf, APP_PREFIX);

		// fill the configuration instance from environment variables
		String[] unknownArgs = ConfigManager.setFromArgs(conf, args);

		// print help text in default format:
		// see ConfigManager$HelpPrinter for the implementation.
		// This could also be used to generate PDFs or a JSON API documentation.
		ConfigManager.printHelpFor(conf, APP_PREFIX, System.err);

		// print list of unknown arguments
		System.out.println();
		if (unknownArgs.length > 0) {
			System.err.println("Unknown arguments:");
			for (String a : unknownArgs) {
				System.err.println("* " + a);
			}
		}
	}
}
