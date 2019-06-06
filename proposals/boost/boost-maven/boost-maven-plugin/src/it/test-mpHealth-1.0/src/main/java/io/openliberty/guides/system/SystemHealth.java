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
// tag::SystemHealth[]
package io.openliberty.guides.system;

import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@Health
@ApplicationScoped
public class SystemHealth implements HealthCheck {
    @Override
    public HealthCheckResponse call() {
        if (!System.getProperty("wlp.server.name").equals("BoostServer")) {
            return HealthCheckResponse.named(SystemResource.class.getSimpleName())
                    .withData("default server", "not available").down().build();
        }
        return HealthCheckResponse.named(SystemResource.class.getSimpleName()).withData("default server", "available")
                .up().build();
    }
}
// end::SystemHealth[]
