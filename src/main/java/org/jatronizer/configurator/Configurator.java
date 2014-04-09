package org.jatronizer.configurator;

import java.lang.reflect.Field;
import java.util.*;

/**
 * A Configurator processes a configuration.
 *
 * It helps to keep the whole configuration of a system and the documentation of available configuration
 * options in one place and provides tools to simplify generating specific help texts and examples.
 * It also closely couples code and documentation and keeps them in sync.
 *
 * The configuration takes the form of an Object of any type with annotated fields.
 * The field name, type and additional optional information from the annotation are used to populate the fields
 * from specified sources. Dynamic online configuration changes are possible, too.
 *
 * Configuration options are set in the order of arrival - later ones overwrite earlier ones.
 * This can be used to e.g. initialize a configuration with default values, then overwrite those with
 * a configuration file, then environment variables where given and last from command line arguments.
 * Any combination can be used - this package provides some helpers in Parser, the interface for configuration
 * options is <code>Map&lt;String, String&gt;</code> or a tuple of String (ChangeEvent) for single value changes.
 *
 * To use this class in a multi-threaded context, all read accesses of configuration fields should be synchronized
 * on the configuration. Write accesses are always synchronized.
 *
 * Example configuration:
 *   <code><pre>
 *   // First, you should use an interface to avoid dependencies on the configuration type
 *   // from independent submodules (the interface lives in their own package).
 *   // This only illustrates good practices and could be left out.
 *   public interface SftpConfig {
 *       String host();
 *       int port();
 *   }
 *
 *   public class Cfg implements SftpConfig {
 *
 *       // changes the configuration key to sftp/host (would be field name "host" otherwise)
 *       // and adds a description.
 *       // The field itself provides type, name (when key is not specified) and the default value.
 *       @Config(key = "sftp/host", desc = "sftp server host")
 *       private String host = "localhost";
 *
 *       @Config(key = "sftp/port", desc = "sftp server port")
 *       private int port = 22;
 *
 *       // no information in debug, so the key is "debug", the type is boolean and default value is false.
 *       @Config
 *       private boolean debug = false;
 *
 *       // add synchronization to enable online updates.
 *       // Don't provide a setter, apply all configuration changes through the Configurator.
 *       public synchronized String host() {
 *          return host;
 *       }
 *
 *       public synchronized int port() {
 *          return port;
 *       }
 *
 *       public synchronized boolean debug() {
 *           return debug;
 *       }
 *   }
 *   </pre></code>
 *
 * Example usage:
 *   <code><pre>
 *   Cfg cfg = new Cfg();
 *   final String envPrefix = "myapp_";
 *   Configurator<Cfg> cbuilder = Configurator.control(cfg);
 *   String[] keys = cbuilder.keys();
 *   Map<String, String> cliArgs = Parser.mapKeys(
 *      Parser.asArgKeys("-", keys),
 *      Parser.control("=", args)
 *   );
 *   Map<String, String> cliEnv = Parser.mapKeys(
 *      Parser.asEnvKeys(envPrefix, keys),
 *      System.getenv()
 *   );
 *   cbuilder.load(cliEnv).load(cliArgs).config();
 *   // cfg is now initialized with environment variables
 *   // and then with command line arguments.
 *   if (cfg.debug()) {
 *      // this code will be reached if an environment variable
 *      // MYAPP_DEBUG=true
 *      // or a command line argument
 *      // -debug=true
 *      // were passed on execution.
 *      System.err.println("debug was activated");
 *   }
 *   </pre></code>
 *
 * @param <C> the type of the configuration instance
 */
public final class Configurator<C> {

	/**
	 * creates a reflective configuration builder.
	 * The configuration is an object with fields annotated with Config.
	 * It returns null the value of an annotated key could not be accessed or
	 * if there are no fields annotated with Config.
	 * @param configuration an instance of a configuration.
	 *               It is used by the builder to determine available fields and their types
	 *               and to get their default values (the one set on the instance passed here).
	 *               The Configurator assumes ownership - you should not write to any of the annotated fields
	 *               yourself.
	 * @return a builder usable to load environment variables, arguments or other files.
	 */
	public static <C> Configurator<C> control(C configuration) {
		Field[] fields = configuration.getClass().getDeclaredFields();
		ArrayList<Parameter> conf = new ArrayList<Parameter>(fields.length);
		for (Field f : fields) {
			Config c = f.getAnnotation(Config.class);
			if (c != null) {
				conf.add(Parameter.create(configuration, f, c));
			}
		}
		if (conf.isEmpty()) {
			return null;
		}
		Parameter[] parameters = conf.toArray(new Parameter[conf.size()]);
		Arrays.sort(parameters);
		return new Configurator<C>(configuration, parameters);
	}

	private final C config;
	private final Parameter[] parameters;

	private Configurator(C config, Parameter[] parameters) {
		this.config = config;
		this.parameters = parameters;
	}

	private int indexOf(String key) {
		int min = 0;
		int max = parameters.length;
		int i = (max + min) / 2;
		while (min <= i && i < max) {
			int cmp = key.compareTo(parameters[i].key);
			if (cmp == 0) {
				return i;
			}
			if (cmp < 0) {
				max = i;
			} else {
				min = i + 1;
			}
			i = (max + min) / 2;
		}
		return -1;
	}

	/**
	 * retrieves the configuration managed with this Configurator.
	 */
	public C config() {
		return config;
	}

	/**
	 * retrieves the keys of all available configuration options.
	 */
	public String[] keys() {
		String[] keys = new String[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			keys[i] = parameters[i].key;
		}
		return keys;
	}

	/**
	 * retrieves the description of the configuration option with the specified key.
	 * The result may be an empty String but is never null.
	 */
	public String description(String key) {
		int i = indexOf(key);
		if (i < 0) {
			return null;
		}
		return parameters[i].desc;
	}

	/**
	 * retrieves the default value of the configuration option with the specified key in String form.
	 * The result may be null.
	 */
	public String defaultValue(String key) {
		int i = indexOf(key);
		if (i < 0) {
			return null;
		}
		return parameters[i].defval;
	}

	/**
	 * retrieves the current value of the configuration option with the specified key in String form.
	 * The result may be null.
	 */
	public String value(String key) {
		int i = indexOf(key);
		if (i < 0) {
			return null;
		}
		return parameters[i].get(config);
	}

	/**
	 * set configuration options.
	 * The values are converted from String form to the format used by the configuration.
	 * This may be used dynamically, e.g. to set a log level at runtime.
	 * To support dynamic usage, the configuration fields must be read with synchronized
	 * get-methods and must not be accessed directly. Their values also must not be cached.
	 */
	public Configurator<C> process(ChangeEvent...ces) {
		for (ChangeEvent ce : ces) {
			for (Parameter p : parameters) {
				if (p.key.equals(ce.key)) {
					p.set(config, ce.value);
				}
			}
		}
		return this;
	}

	/**
	 * set all configuration options with the same keys as in data to the value stored in data.
	 * The values from data are converted from String form to the format used by the configuration.
	 */
	public Configurator<C> process(Map<String, String> data) {
		for (Map.Entry<String, String> e : data.entrySet()) {
			int i = indexOf(e.getKey());
			if (i >= 0) {
				parameters[i].set(config, e.getValue());
			}
		}
		return this;
	}

	/**
	 * set all configuration options with the same keys as in data to the value stored in data.
	 * The values from data are converted from String form to the format used by the configuration.
	 * Each key and value stored in data must be a String.
	 */
	public Configurator<C> process(Properties data) {
		for (Map.Entry<Object, Object> e : data.entrySet()) {
			int i = indexOf((String) e.getKey());
			if (i >= 0) {
				parameters[i].set(config, (String) e.getValue());
			}
		}
		return this;
	}
}
