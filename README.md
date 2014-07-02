# configurator
This library vastly simplifies requirement [3 of the 12 factor apps: Configuration](http://12factor.net/config).
Use POJOs for configuration and have all configuration options in one place.
Autogenerate documentation in various formats (some assembly required).


It's [MIT-licensed](https://raw.github.com/jatronizer/configurator/master/LICENSE).

Current status: [![Build Status](https://travis-ci.org/jatronizer/configurator.png?branch=master)](https://travis-ci.org/jatronizer/configurator) *(master branch)*

## Motivation
When I read the 12factor model, I was especially impressed by the configuration part - put everything in environment variables.

A batch file starting the system can also be the configuration.

It can be copied and adapted to easily start multiple instances or use different backing services.

Users can store selected values in their profile.

Brilliant.

Then I inherited a codebase. It requires some configuration files in different formats (ini-like + Xml) and lots and lots of parameters. Some are from external libraries. Some values are overwritten. Some even more than once. It is a nightmare to find out what part influences the system in what way.

I desperately want to make it 12factor.

But what does it take to allow this *in* the system?

How do I avoid the unholy mess I inherited?
How do I discover which configuration options are available and what they do?

To discover the available parameters, there's documentation - which sucks.

Documentation is meant for human consumption.

It is extremely important and absolutely necessary, but...
* it may be inaccurate,
* it may be incomplete,
* it is not automatically validated on deployment and
* I have to write it first.

That's a shame, because a lot of information is already present and I don't see why I should duplicate that.

The JVM knows the default value of a field. It knows the type and name. Why not just use those?

It only has to know what variables are used as externally accessible configuration options.

This library was created to concentrate all configuration options in one single Object.
It can do so for multiple modules with multiple instances per module.
It fuses the documentation to the code and allows generation of documentation in different formats.
Default values and descriptions are used from the code and never have to be kept in sync.
If desired, values can be read or even changed after startup - e.g. by exporting it in a web api.

## Usage
Create a class with fields for all configuration options you need in a module.

Supported field types are all primitive types, `String` and every kind of `enum`. A little assembly is required for anything else - namely a two-way converter between the type and a String representation.

If you want to, annotate the class with `@Module` to provide a description of what it is used for.

Annotate all configurable fields with `@Parameter`. Assign an optional `key` to reference the field. If you omit it, the field name is used. You can also assign the `converter` mentioned above. And you can `tag` the parameter if you want to group it in the documentation.

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
		// see HelpPrinter for the implementation.
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
