/**
 *
 * @author Bruno Baptista.
 * <a href="https://twitter.com/brunobat_">https://twitter.com/brunobat_</a>
 *
 */

package org.acme.legume.health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

@Readiness
@ApplicationScoped
public class DemoReady implements HealthCheck {

    @Inject
    private EntityManager entityManager;

    @Override
    public HealthCheckResponse call() {
        try {
            final int connectionData = testConnection();
            if (connectionData == 1) {
                return HealthCheckResponse.named("quarkus-demo-ready")
                        .up()
                        .build();
            } else {
                return HealthCheckResponse.named("quarkus-demo-ready")
                        .withData("Connection data not 1", connectionData)
                        .down()
                        .build();
            }
        } catch (Exception e) {
            return HealthCheckResponse.named("quarkus-demo-ready")
                    .withData("Exception connecting to db", e.getMessage())
                    .down()
                    .build();
        }
    }

    private int testConnection() {
        return (int) entityManager.createNativeQuery("SELECT 1").getSingleResult();
    }
}
