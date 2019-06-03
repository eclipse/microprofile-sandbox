# Boost Maven Plugin Prototype

### What is Boost?

This is a prototype Maven plugin to package a Spring Boot application with Liberty.

When added to your pom.xml, the plugin will

1. Install Open Liberty.
2. Create an Open Liberty server.
3. Thin the Spring Boot project application.
4. Install the application to the server.
5. Install and add appropriate Liberty features to the server configuration.
6. Package the server and application into a runnable jar.
7. Create a Spring Boot application Docker image that uses Liberty.


### Build Liberty Boost Plugin

1. `git clone git@github.com:OpenLiberty/boost-common.git`
2. `cd boost-common`
3. `mvn clean install`
4. `git clone git@github.com:OpenLiberty/boost-maven.git`
5. `cd boost-maven`
6. `mvn clean install`  (To run integration tests, add the -Pit parameter)

### Use the Liberty Boost plugin in your Spring Boot Maven project 

#### Try it!

Kick the tires of Boost with zero configuration:

* Produce a Liberty uber jar for your Spring Boot app:
    * `mvn clean package io.openliberty.boost:boost-maven-plugin:0.1:package`
    * `java -jar target/<application name>.jar`

* Create a Liberty based Docker image for your Spring Boot app:
    * `mvn clean package io.openliberty.boost:boost-maven-plugin:0.1:docker-build`
    * `docker run -p 9080:9080 <application name>`


#### Quick start - uber jar

1. Add the following to your project pom.xml
    ```xml
      <plugin>
            <groupId>io.openliberty.boost</groupId>
            <artifactId>boost-maven-plugin</artifactId>
            <version>0.1</version>
            <executions>
              <execution>
                    <phase>package</phase>
                    <goals>
                          <goal>package</goal>
                    </goals>
              </execution>
           </executions>
      </plugin>
    ```
1. Run `mvn clean package`
1. Run the produced jar file: `java -jar <application_name>.jar`

#### Quick start - docker

1. Add the following to your project pom.xml
    ```xml
      <plugin>
            <groupId>io.openliberty.boost</groupId>
            <artifactId>boost-maven-plugin</artifactId>
            <version>0.1</version>
            <executions>
              <execution>
                    <goals>
                          <goal>docker-build</goal>
                    </goals>
              </execution>
           </executions>
      </plugin>
    ```
1. Run `mvn clean install`
1. Run the produced Docker image: `docker run -p 9080:9080 <application_name>`

#### Tutorial

For a more detailed tutorial, see [here](Tutorial.md).

### Building and Developing Boost

See [here](https://github.com/OpenLiberty/boost/wiki/Home) 
