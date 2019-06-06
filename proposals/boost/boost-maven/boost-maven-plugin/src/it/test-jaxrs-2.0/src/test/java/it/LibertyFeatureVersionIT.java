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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.junit.BeforeClass;
import org.junit.Test;

public class LibertyFeatureVersionIT {

    private static final String JAXRS_20_FEATURE = "<feature>jaxrs-2.0</feature>";
    private static String SERVER_XML = "target/liberty/wlp/usr/servers/BoostServer/server.xml";

    @BeforeClass
    public static void init() {
        String runtime = System.getProperty("boostRuntime");
        org.junit.Assume.assumeTrue("ol".equals(runtime) || "wlp".equals(runtime));
    }

    @Test
    public void testLibertyFeatureVersion() throws Exception {
        File targetFile = new File(SERVER_XML);
        assertTrue(targetFile.getCanonicalFile() + "does not exist.", targetFile.exists());

        // Check contents of file for jaxrs feature
        boolean found = false;
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(SERVER_XML));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(JAXRS_20_FEATURE)) {
                    found = true;
                    break;
                }
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }

        assertTrue("The " + JAXRS_20_FEATURE + " feature was not found in the server configuration", found);
    }
}
