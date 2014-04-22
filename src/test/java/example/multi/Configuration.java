package example.multi;

import org.jatronizer.configurator.CliConfiguration;
import org.jatronizer.configurator.Config;
import org.jatronizer.configurator.ModuleConfig;

public class Configuration {
	@ModuleConfig(prefix="smtp/warn/", desc="smtp account for error and warn log messages")
	public static class WarnConfig {
		@Config(key = "login", desc = "login for smtp account")
		private String user = "root";

		@Config(key = "pass", desc = "password for smtp account")
		private String password;

		@Config(key = "host", desc = "smpt server host")
		private String host = "localhost";

		@Config(key = "port", desc = "smtp server port")
		private int port = 587;
	}

	@ModuleConfig(prefix="smtp/news/", desc="smtp account for the newsletter")
	public static class NewsConfig {
		@Config(key = "login", desc = "login for smtp account")
		private String user = "root";

		@Config(key = "pass", desc = "password for smtp account")
		private String password;

		@Config(key = "host", desc = "smpt server host")
		private String host = "localhost";

		@Config(key = "port", desc = "smtp server port")
		private int port = 587;
	}

	public static void main(String[] args) {
		// override to show the effect
		args = new String[]{
				"-smtp-warn-host=warnhost",
				"-smtp-news-host=newshost",
				"-smdp-warn-port=566" // unknown argument
		};
		WarnConfig warn = new WarnConfig();
		NewsConfig news = new NewsConfig();
		CliConfiguration conf = new CliConfiguration("myapp-", args, warn, news);
		conf.printHelp(System.out);
		String[] unknownArgs = conf.unknownArgs();
		if (unknownArgs.length > 0) {
			System.out.println("Unknown arguments:");
			for (String a : unknownArgs) {
				System.out.println("* " + a);
			}
		}
	}
}
