/**
 *
 * @author Bruno Baptista.
 * <a href="https://twitter.com/brunobat_">https://twitter.com/brunobat_</a>
 *
 */

package org.acme.legume.health;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Liveness
@ApplicationScoped
public class DemoLive implements HealthCheck {

    @Inject
    @ConfigProperty(name = "some.live.property")
    private String liveProperty;

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.named("quarkus-demo-live")
                .up()
                .withData("config property", liveProperty)
                .build();
    }
}
