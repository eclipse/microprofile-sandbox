# Boost Maven Plugin Tutorial

This is a basic walkthrough of how to use the Boost Maven Plugin to package an existing Spring Boot application into a Liberty executable jar and to build a docker image from it. 


### Create a Spring Boot project

To get started, you will first need a basic Spring Boot application. You can clone the following project from Spring's Getting Started Guide:

`git clone git@github.com:spring-guides/gs-spring-boot.git`

### Add the Boost plugin configuration 

Move into the completed directory of the Spring Boot project.

`cd gs-spring-boot/complete`

Open the pom.xml and add the following plugin definition after the spring-boot-maven-plugin:

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

### Package your application

Run the Maven package command as you normally would when building a Spring Boot Application:

`mvn clean package`


### Run the produced jar file

The jar file produced is a Liberty executable server with the thinned Spring Boot application deployed to it. To run the application, simply run the jar:

`java -jar target/gs-spring-boot-0.1.0.jar`

You can optionally run your application using the provided boost goals: 

`mvn boost:start`, `mvn boost:stop`, `mvn boost:run`, and `mvn boost:debug`. 

Go to `http://localhost:8080` and you should see the following displayed in the browser:

`Greetings from Spring Boot!`

### Create a Docker image from your Spring Boot application

With Boost, you can easily create a Spring Boot application Docker image that uses Liberty from your Spring Boot application. Note that
you must first have Docker (17.05 or higher) installed and running.

Edit the Boost plugin definition in the pom.xml to the following:

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

Now, run `mvn clean install` to build your docker image.
 
##### Note: 
The `docker-build` goal creates a Liberty specific Dockerfile in the root folder of the Spring Boot project in the absence of a Dockerfile and builds the docker image.
 

### Run the Docker image of your Spring Boot application

Run `docker images` and you should see the newly created docker image called `gs-spring-boot`.

To run the container, issue the following:

`docker run -p 9080:9080 gs-spring-boot`

Go to `http://localhost:9080` and you should again see the following displayed in the browser: 

`Greetings from Spring Boot!`



