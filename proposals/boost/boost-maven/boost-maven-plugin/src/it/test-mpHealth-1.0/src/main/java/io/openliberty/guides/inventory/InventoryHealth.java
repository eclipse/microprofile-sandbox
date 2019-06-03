// tag::comment[]
/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial implementation
 *******************************************************************************/
// end::comment[]
// tag::InventoryHealth[]
package io.openliberty.guides.inventory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import io.openliberty.guides.inventory.client.SystemClient;
import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@Health
@ApplicationScoped
public class InventoryHealth implements HealthCheck {
    @Inject
    InventoryConfig config;

    private InventoryUtils invUtils = new InventoryUtils();

    public boolean isHealthy() {
        if (config.isInMaintenance()) {
            return false;
        }
        try {
            String url = invUtils.buildUrl("http", "localhost",
                    // Integer.parseInt(System.getProperty("default.http.port")),
                    9080, "/system/properties");
            Client client = ClientBuilder.newClient();
            Response response = client.target(url).request(MediaType.APPLICATION_JSON).get();
            if (response.getStatus() != 200) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public HealthCheckResponse call() {
        if (!isHealthy()) {
            return HealthCheckResponse.named(InventoryResource.class.getSimpleName())
                    .withData("services", "not available").down().build();
        }
        return HealthCheckResponse.named(InventoryResource.class.getSimpleName()).withData("services", "available").up()
                .build();
    }

}
// end::InventoryHealth[]
