/**
 *
 * @author Bruno Baptista.
 * <a href="https://twitter.com/brunobat_">https://twitter.com/brunobat_</a>
 *
 */

package org.acme.legume.database;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.h2.tools.Server;

import java.sql.SQLException;
import java.util.Map;

import static java.util.Collections.emptyMap;

public class H2DatabaseTestResource implements QuarkusTestResourceLifecycleManager {

    private Server tcpServer;

    @Override
    public Map<String, String> start() {

        try {
            tcpServer = Server.createTcpServer();
            tcpServer.start();
            System.out.println("[INFO] H2 database started in TCP server mode on port " + tcpServer.getPort());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return emptyMap();
    }

    @Override
    public synchronized void stop() {
        if (tcpServer != null) {
            tcpServer.stop();
            System.out.println("[INFO] H2 database was shut down");
            tcpServer = null;
        }
    }
}