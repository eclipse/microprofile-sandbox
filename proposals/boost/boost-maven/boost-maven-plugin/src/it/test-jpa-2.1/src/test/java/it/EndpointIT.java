/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package it;

import static org.junit.Assert.assertTrue;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.BeforeClass;
import org.junit.Test;

public class EndpointIT {
    private static String URL;

    @BeforeClass
    public static void init() {
        String port = System.getProperty("boost.http.port");
        URL = "http://localhost:" + port;
    }

    @Test
    public void testDeployment() {
        testEndpoint("Hello JPA World");
        testEndpoint("Created Thing 2");
        testEndpoint("Created Thing 3");
    }

    private void testEndpoint(String expectedOutput) {
        Response response = sendRequest(URL, "GET");
        int responseCode = response.getStatus();
        assertTrue("Incorrect response code: " + responseCode, responseCode == 200);

        String responseString = response.readEntity(String.class);
        response.close();
        assertTrue("Incorrect response, response is " + responseString, responseString.contains(expectedOutput));
    }

    private Response sendRequest(String url, String requestType) {
        Client client = ClientBuilder.newClient();
        System.out.println("Testing " + url);
        WebTarget target = client.target(url);
        Invocation.Builder invoBuild = target.request();
        Response response = invoBuild.build(requestType).invoke();
        return response;
    }
}
