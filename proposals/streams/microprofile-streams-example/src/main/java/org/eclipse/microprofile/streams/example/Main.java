package org.eclipse.microprofile.streams.example;

import org.eclipse.microprofile.streams.Ingest;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

  public static void main(String... args) throws IOException {
    Weld weld = new Weld();
    weld.property(Weld.SCAN_CLASSPATH_ENTRIES_SYSTEM_PROPERTY, true);
    weld.setBeanDiscoveryMode(BeanDiscoveryMode.ANNOTATED);
    weld.setClassLoader(Main.class.getClassLoader());
    weld.enableDiscovery();
    weld.addPackages(true, Ingest.class.getPackage());
    WeldContainer container = weld.initialize();

    System.out.println("Weld container is running. Push enter to stop.");
    new BufferedReader(new InputStreamReader(System.in)).readLine();

    System.out.println("Shutting down weld container.");
    container.shutdown();

    System.exit(0);
  }
}
