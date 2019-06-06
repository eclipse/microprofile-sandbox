# Boost Maven Plugin Prototype

### What is Boost?

This is a prototype Maven plugin to package a fully configured Java EE or MicroProfile application with a target runtime.

When added to your pom.xml, the plugin will

1. Install the desired target runtime.
2. Create a server.
3. Install the application to the server.
4. Configure the server appropriately for the application.

### Build the Boost Maven Plugin

1. `git clone git@github.com:OpenLiberty/boost.git`
2. `boost-maven.sh`

### Use the Boost Maven Plugin

Edit your project pom.xml and place the following plugin stanza into your build:
```xml
<plugin>
	<groupId>boost</groupId>
	<artifactId>boost-maven-plugin</artifactId>
	<version>0.1.3-SNAPSHOT</version>
	<executions>
		<execution>
			<goals>
				<goal>package</goal>
			</goals>
		</execution>
	</executions>
</plugin>
 ```

 Run `mvn clean package`

### Tutorial

For more detailed instructions on using the boost plugin, see [here](https://github.com/OpenLiberty/boost/wiki/Boosted-MicroProfile-Rest-Client-sample-app).

### Building and Developing Boost

See [here](https://github.com/OpenLiberty/boost/wiki/Home) 
