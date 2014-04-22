package example.simple;

import org.jatronizer.configurator.CliConfiguration;
import org.jatronizer.configurator.Config;

public class Configuration {
	@Config(key = "smtp/login", desc = "login for smtp account")
	private String user = "root";

	@Config(key = "smtp/pass", desc = "password for smtp account")
	private String password;

	@Config(key = "smtp/host", desc = "smpt server host")
	private String host = "localhost";

	@Config(key = "smtp/port", desc = "smtp server port")
	private int port = 587;

	@Config
	private boolean debug = false;

	public static final Configuration INSTANCE = new Configuration();

	public static void main(String[] args) {
		CliConfiguration conf = new CliConfiguration("myapp-", args, INSTANCE);
		conf.printHelp(System.out);
		if (INSTANCE.debug) {
			System.out.println("Debug was set");
		}
		String[] unknownArgs = conf.unknownArgs();
		if (unknownArgs.length > 0) {
			System.out.println("Unknown arguments:");
			for (String a : unknownArgs) {
				System.out.println("* " + a);
			}
		}
	}
}
