package example.program;

import org.jatronizer.configurator.ConfigManager;
import org.jatronizer.configurator.Configurator;
import org.jatronizer.configurator.Description;
import org.jatronizer.configurator.Parameter;

public class SingleConfiguration {
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

	public static final SingleConfiguration INSTANCE = new SingleConfiguration();

	public static void main(String[] args) {
		final String APP_PREFIX = "myapp/";
		Configurator conf = ConfigManager.configure(INSTANCE);
		ConfigManager.setFromEnv(conf, APP_PREFIX);
		String[] unknownArgs = ConfigManager.setFromArgs(conf, args);
		ConfigManager.printHelpFor(conf, APP_PREFIX, System.err);
		if (INSTANCE.debug) {
			System.err.println("Debug was set");
		}
		if (unknownArgs.length > 0) {
			System.err.println("Unknown arguments:");
			for (String a : unknownArgs) {
				System.err.println("  * " + a);
			}
		}
	}
}
