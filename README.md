# Configurator
This library vastly simplifies requirement [3 of the 12 factor apps: configuration](http://12factor.net/config).

You can use POJOs (plain old java objects) for configuration and have all configuration options in one place!

It's [MIT-licensed](https://raw.github.com/jatronizer/configurator/master/LICENSE).

Current build status: [![Build Status](https://travis-ci.org/jatronizer/configurator.png?branch=master)](https://travis-ci.org/jatronizer/configurator) *(master branch)*

## Motivation
Remember being a user of a program somebody else wrote.

Did you ever want to know the configuration objects to a program? Or what each of them mean?

Did you want examples for those options? Did you want to know the currently set values?

Have you experienced rising acid levels in your stomach because someone just had to use yet another non standard format for configuration? In the worst case without dependable documentation?

Did you want to use environment variables instead of command line arguments to avoid repetition? Did you ever want a template for your configuration file?

Now think as a developer.

Did your help texts ever grow stale? Have you wished for closer coupling between your program state and the externally configurable parameters?

Did you ever want to generate help texts and man pages automatically?

Have you dreamt of not having to depend of a configuration parsing library?

Did you want to serve your configuration at runtime as JSON and make it editable?

Good news: all this and more is possible - with less code than you ever wrote for it before.

## But how?

A lot of information needed for configuration is already present in your program.

The JVM knows the value of a field. It knows the type and name. Why not just use those?

It only has to know what variables are used as externally accessible configuration options.

Configurator was created to concentrate all configuration options in one single Object.
It can do so for multiple modules with multiple instances per module.

Now the documentation is very close to the code, any kind of help text is easily created!

Default values and descriptions are used from the code and never have to be kept in sync.
If desired, values can be read or even changed after startup - e.g. by exporting it in a web api.

## Usage
Create a class with fields for all configuration options you need in a module.

Supported field types are all primitive types, `String` and every kind of `enum`. A little assembly is required for anything else, namely a two-way converter between the type and a String representation.

Annotate all configurable fields with `@Parameter`. Assign an optional `key` to reference the field. If you omit it, the field name is used. You can also assign the `converter` mentioned above. And you can `tag` the parameter if you want to group it in the documentation. If the parameter
contains other parameters, set `container=true`.

Annotate the field with `@Description` and provide a description of what the parameter does.

Create a `Configurator` with `ConfigManager.configure`.

If you dislike annotations or you want to use classes out of your control for a configuration,
you'll be happy to have the `manage`, `module` and `parameter` methods at your disposal.

Fill the configuration values from
  * command line arguments
  * environment variables
  * configuration files
  * database entries
  * web api calls
  * ...

All these scenarios are possible, support for command line arguments and environment variables is built in.

## Examples

Here's an example using the same module in one configuration:
```java
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
	private String address;

	// enums are possible
	@Parameter
	private SmtpPort port;

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
		String[] unknownArgs = ConfigManager.setFromArgs(conf, args);

		// use the configuration
		System.out.println("Using: " + CONFIG.address + ":" + CONFIG.port.port);

		// print help text
		ConfigManager.printHelpFor(conf, ENV_PREFIX, System.err);

		// print unknown command line arguments
		if (unknownArgs.length > 0) {
			System.err.println("Unknown arguments:");
			for (String a : unknownArgs) {
				System.err.println("  * " + a);
			}
		}
	}
}
```
Running it with `-port=submit` produces the following output:
```
Using: localhost:587
Module SmtpConfiguration:
-address, $MYMAIL_ADDRESS
	value: 'localhost' (is default)
-port, $MYMAIL_PORT
	value: 'submit', default: 'smtp'
	available values:
	  smtp    smtp port for mailing between servers
	  submit  smtp submit port for clients sending mail
```

You can change the output. `export MYMAIL_PORT=submit` sets the port field to submit. `-port=smtp` would overwrite it.

Setting `-port=lalala` causes an error; `lalala` is no value of the SmtpPort enum.
```
Using: localhost:25
Module SmtpConfiguration:
-address, $MYMAIL_ADDRESS
	value: 'localhost' (is default)
-port, $MYMAIL_PORT
	value: 'smtp' (is default)
	available values:
	  smtp    smtp port for mailing between servers
	  submit  smtp submit port for clients sending mail

Arguments that are unknown or have invalid values:
  * port=lalala
```

Have a look at the java/example/ folder for more!
