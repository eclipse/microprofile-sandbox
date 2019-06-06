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

import static org.junit.Assert.*;

import java.io.StringReader;
import java.net.URL;
import java.util.Scanner;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HealthCheck {

    protected String dbName;

    public HealthCheck(String dbName) {
        this.dbName = dbName;
    }

    @Test
    public void testServlet() throws Exception {

        String port = System.getProperty("boost.http.port");
        String urlString = "http://localhost:" + port + "/health";

        URL url = new URL(urlString);
        Scanner sc = new Scanner(url.openStream());

        StringBuilder jsonString = new StringBuilder();
        while (sc.hasNext()) {
            jsonString.append(sc.nextLine());
        }

        JsonParser parser = new JsonParser();
        JsonObject root = parser.parse(jsonString.toString()).getAsJsonObject();
        JsonArray checks = root.getAsJsonArray("checks");
        JsonObject data = checks.get(0).getAsJsonObject().getAsJsonObject("data");

        String productName = data.get("databaseProductName").getAsString();
        String state = checks.get(0).getAsJsonObject().get("state").getAsString();

        assertEquals(productName, dbName);
        assertEquals(state, "UP");
    }
}
