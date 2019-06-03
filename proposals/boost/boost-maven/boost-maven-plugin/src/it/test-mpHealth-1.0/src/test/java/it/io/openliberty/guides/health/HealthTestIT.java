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
// tag::HealthTest[]
package it.io.openliberty.guides.health;

import static org.junit.Assert.assertEquals;
import java.util.HashMap;
import javax.json.JsonArray;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public class HealthTestIT {

    private JsonArray servicesStates;
    private static HashMap<String, String> dataWhenServicesUP;
    private static HashMap<String, String> dataWhenInventoryDown;

    @BeforeClass
    public static void init() {
        // These tests is being temporarily skipped when running on TomEE
        // until the failures are addressed.
        String runtime = System.getProperty("boostRuntime");
        org.junit.Assume.assumeTrue("ol".equals(runtime) || "wlp".equals(runtime));

        dataWhenServicesUP = new HashMap<String, String>();
        dataWhenInventoryDown = new HashMap<String, String>();

        dataWhenServicesUP.put("SystemResource", "UP");
        dataWhenServicesUP.put("InventoryResource", "UP");

        dataWhenInventoryDown.put("SystemResource", "UP");
        dataWhenInventoryDown.put("InventoryResource", "DOWN");
    }

    @Test
    public void testIfServicesAreUp() {
        servicesStates = HealthTestUtil.connectToHealthEnpoint(200);
        checkStates(dataWhenServicesUP, servicesStates);
    }

    @Test
    public void testIfInventoryServiceIsDown() {
        servicesStates = HealthTestUtil.connectToHealthEnpoint(200);
        checkStates(dataWhenServicesUP, servicesStates);
        HealthTestUtil.changeInventoryProperty(HealthTestUtil.INV_MAINTENANCE_FALSE,
                HealthTestUtil.INV_MAINTENANCE_TRUE);
        servicesStates = HealthTestUtil.connectToHealthEnpoint(503);
        checkStates(dataWhenInventoryDown, servicesStates);
    }

    private void checkStates(HashMap<String, String> testData, JsonArray servStates) {
        testData.forEach((service, expectedState) -> {
            assertEquals("The state of " + service + " service is not matching.", expectedState,
                    HealthTestUtil.getActualState(service, servStates));
        });
    }

    @After
    public void teardown() {
        HealthTestUtil.cleanUp();
    }

}
// end::HealthTest[]
