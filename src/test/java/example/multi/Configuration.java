package example.multi;

import org.jatronizer.configurator.*;

public class Configuration {
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
		final String APP_PREFIX = "myapp/";
		MailConfig warn = new MailConfig();
		MailConfig news = new MailConfig();
		Configurator conf = ConfigManager.manage(
			ConfigManager.module(
					warn, "warnmails", "smtp/warn/",
					"SMTP Account for log messages with level WARN and up"
			),
			ConfigManager.module(
					news, "mailinglist", "smtp/news/",
					"SMTP Account for newsletter"
			)
		);
		// override to show the effect
		args = new String[]{
				"-smtp-warn-host=warnhost",
				"-smtp-news-host=newshost",
				"-smdp-warn-port=566" // unknown argument
		};
		ConfigManager.setFromEnv(conf, APP_PREFIX);
		String[] unknownArgs = ConfigManager.setFromArgs(conf, args);
		ConfigManager.printHelpFor(conf, APP_PREFIX, System.err);
		if (unknownArgs.length > 0) {
			System.err.println("Unknown arguments:");
			for (String a : unknownArgs) {
				System.err.println("* " + a);
			}
		}
	}
}
