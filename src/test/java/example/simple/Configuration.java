package example.simple;

import org.jatronizer.configurator.MainConfigurator;
import org.jatronizer.configurator.HelpPrinter;
import org.jatronizer.configurator.Description;
import org.jatronizer.configurator.Parameter;

public class Configuration {
	private static enum Colors {
		@Description("the grass is always growing on the other side")
		green,
		@Description("I'm blue")
		blue
	}

	@Parameter(key = "smtp/login")
	@Description("login for the smtp account")
	private String user = "root";

	@Parameter(key = "smtp/pass")
	@Description("password for the smtp account")
	private String password;

	@Parameter(key = "smtp/host")
	private String host = "localhost";

	@Parameter(key = "smtp/port")
	private int port = 587;

	@Parameter
	private Colors color = Colors.blue;

	@Parameter
	private boolean debug = false;

	public static final Configuration INSTANCE = new Configuration();

	public static void main(String[] args) {
		MainConfigurator conf = new MainConfigurator("myapp-", args, INSTANCE);
		conf.walk(new HelpPrinter(), new HelpPrinter());
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
