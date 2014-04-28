package example.multi;

import org.jatronizer.configurator.*;

public class Configuration {
	@Module(prefix="smtp/warn/")
	@Description("smtp account for error and warn log messages")
	public static class WarnConfig {
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

	@Module(prefix="smtp/news/")
	@Description("smtp account for the newsletter")
	public static class NewsConfig {
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
		// override to show the effect
		args = new String[]{
				"-smtp-warn-host=warnhost",
				"-smtp-news-host=newshost",
				"-smdp-warn-port=566" // unknown argument
		};
		WarnConfig warn = new WarnConfig();
		NewsConfig news = new NewsConfig();
		CliConfiguration conf = new CliConfiguration("myapp-", args, warn, news);
		conf.walk(new CliPrinter(), new CliPrinter());
		String[] unknownArgs = conf.unknownArgs();
		if (unknownArgs.length > 0) {
			System.out.println("Unknown arguments:");
			for (String a : unknownArgs) {
				System.out.println("* " + a);
			}
		}
	}
}
