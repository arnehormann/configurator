# configurator
Use POJOs for configuration and have all configuration options in one place.
This library vastly simplifies requirement [3 of the 12 factor apps: Configuration](http://12factor.net/config).

It's [MIT-licensed](https://raw.github.com/jatronizer/configurator/master/LICENSE).

Current status: [![Build Status](https://travis-ci.org/jatronizer/configurator.png?branch=master)](https://travis-ci
.org/jatronizer/configurator) *(master branch)*

## Motivation
The configuration part in the 12factor model is excellent, it is extremely configurable and fast.

A batch file starting the system can also be the configuration.
It can be copied and adapted to easily start multiple instances or use different backing services.
Users can store selected values in their profile.

But how does it look *in* the system? How do you know which configuration options are available?

Probably by reading the documentation - which sucks.

It is not part of the code and requires effort and processes to always keep both in sync.
It may omit parameters. It may not be detailed or belong to a different version.

This library was created to concentrate all configuration options a module has in one single Object.
It fuses the documentation to the code and allows to create it synthetically.
Default values and descriptions are used from the code and never have to be kept in sync.
If desired, values can be changed on the fly - e.g. by a REST api.

## Usage
Create a class with fields for all configuration options you need in a module.

Annotate all configurable fields with `@Parameter`.
  * Assign an optional `key` String to reference the field. If you omit it, the field name is used.
  * Assign a `converter` to control the conversion between String and the field type.

Optionally annotate the field with `@Description` and provide a description of what the parameter does.
This documentation is contained in the compiled class file and always matches the version of the source file.

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

## Example 1 - multiple modules
Here's an example using the same module in one configuration:
```java
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
```
Running it produces the following output:
```
Module warnmails: SMTP Account for log messages with level WARN and up
arg[-smtp-warn-host] / env[MYAPP_SMTP_WARN_HOST]
	value: 'warnhost', default: 'localhost'
arg[-smtp-warn-login] / env[MYAPP_SMTP_WARN_LOGIN]
	login used for the smtp account
	value: 'root' (default)
arg[-smtp-warn-pass] / env[MYAPP_SMTP_WARN_PASS]
	password used for the smtp account
arg[-smtp-warn-port] / env[MYAPP_SMTP_WARN_PORT]
	value: '25' (default)
Module mailinglist: SMTP Account for newsletter
arg[-smtp-news-host] / env[MYAPP_SMTP_NEWS_HOST]
	value: 'newshost', default: 'localhost'
arg[-smtp-news-login] / env[MYAPP_SMTP_NEWS_LOGIN]
	login used for the smtp account
	value: 'root' (default)
arg[-smtp-news-pass] / env[MYAPP_SMTP_NEWS_PASS]
	password used for the smtp account
arg[-smtp-news-port] / env[MYAPP_SMTP_NEWS_PORT]
	value: '587' (default)
Unknown arguments:
* -smdp-warn-port=566
```

## Example 2 - a single module with an enum
```java
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
				System.err.println("* " + a);
			}
		}
	}
}
```
Running it produces the following output:
```
Module SingleConfiguration:
arg[-color] / env[MYAPP_COLOR]
	value: 'blue' (default)
	available values:
	       blue  I'm blue
	       green  the grass is always growing on the other side
arg[-debug] / env[MYAPP_DEBUG]
	value: 'false' (default)
arg[-smtp-host] / env[MYAPP_SMTP_HOST]
	value: 'localhost' (default)
arg[-smtp-login] / env[MYAPP_SMTP_LOGIN]
	login for the smtp account
	value: 'root' (default)
arg[-smtp-pass] / env[MYAPP_SMTP_PASS]
	password for the smtp account
arg[-smtp-port] / env[MYAPP_SMTP_PORT]
	value: '587' (default)
```
