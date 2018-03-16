package org.eclipse.microprofile.streams.example.chat;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Set;

public class Main {

  public static void main(String... args) throws IOException {

    Weld weld = new Weld();
    weld.property(Weld.SCAN_CLASSPATH_ENTRIES_SYSTEM_PROPERTY, true);
    weld.setBeanDiscoveryMode(BeanDiscoveryMode.ANNOTATED);
    weld.setClassLoader(Main.class.getClassLoader());
    weld.enableDiscovery();
    WeldContainer container = weld.initialize();

    URI baseUri = UriBuilder.fromUri("http://localhost/").port(8080).build();
    ResourceConfig config = ResourceConfig.forApplication(new Application() {
      @Override
      public Set<Class<?>> getClasses() {
        return Set.of(
            ChatRoomResource.class
        );
      }
    }).register(MyExceptionMapper.class);
    HttpServer server = GrizzlyHttpServerFactory.createHttpServer(baseUri, config);

    System.out.println("Weld container is running. Push enter to stop.");
    new BufferedReader(new InputStreamReader(System.in)).readLine();

    System.out.println("Shutting down weld container.");
    server.shutdownNow();
    container.shutdown();

    System.exit(0);
  }

}
