package example.program;

import org.jatronizer.configurator.ConfigManager;
import org.jatronizer.configurator.Configurator;
import org.jatronizer.configurator.Description;
import org.jatronizer.configurator.Parameter;

public class SmtpConfiguration {
	public enum SmtpPort {
		@Description("smtp port for mailing between servers")
		smtp(25),

		@Description("smtp submit port for clients sending mail")
		submit(587)
		;

		public final int port;

		SmtpPort(int port) {
			this.port = port;
		}

		public String toString() {
			return super.toString() + ": port " + port;
		}
	}

	// configuration is only used in our own code, it can be public.
	// private is possible too - unless the SecurityManager forbids it.
	// final and static are disallowed
	@Parameter
	private final String address;

	// enums are possible
	@Parameter
	private final SmtpPort port;

	public SmtpConfiguration(String address, SmtpPort port) {
		this.address = address;
		this.port = port;
	}

	public static final String ENV_PREFIX = "mymail/";
	public static final SmtpConfiguration CONFIG =
			new SmtpConfiguration("localhost", SmtpPort.smtp);

	public static void main(String[] args) {
		// get managed configuration
		Configurator conf = ConfigManager.configure(CONFIG);

		// set configuration values from environment variables
		ConfigManager.setFromEnv(conf, ENV_PREFIX);
		// set configuration values from command line arguments
		String[] badArgs = ConfigManager.setFromArgs(conf, args);

		// use the configuration
		System.out.println("Using: " + CONFIG.address + ":" + CONFIG.port.port);

		// print help text
		ConfigManager.printHelpFor(conf, ENV_PREFIX, System.err);

		// print unknown command line arguments
		if (badArgs.length > 0) {
			System.err.println("Arguments that are unknown or have invalid values:");
			for (String a : badArgs) {
				System.err.println("  * " + a);
			}
		}
	}
}
