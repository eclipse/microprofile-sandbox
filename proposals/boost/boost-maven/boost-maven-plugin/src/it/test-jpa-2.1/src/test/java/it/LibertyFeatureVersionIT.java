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

    private static String SERVER_XML = "target/liberty/wlp/usr/servers/BoostServer/server.xml";

    private static final String JPA_21_FEATURE = "<feature>jpa-2.1</feature>";
    private static final String JDBC_42_FEATURE = "<feature>jdbc-4.2</feature>";

    @BeforeClass
    public static void init() {
        String runtime = System.getProperty("boostRuntime");
        org.junit.Assume.assumeTrue("ol".equals(runtime) || "wlp".equals(runtime));
    }

    @Test
    public void testFeatureVersion() throws Exception {
        assertTrue("The " + JPA_21_FEATURE + " feature was not found in the server configuration",
                countTextFoundInFile(SERVER_XML, JPA_21_FEATURE) == 1);
        assertTrue("The " + JDBC_42_FEATURE + " feature was not found in the server configuration",
                countTextFoundInFile(SERVER_XML, JDBC_42_FEATURE) == 1);
    }

    private int countTextFoundInFile(String filePath, String text) throws Exception {
        File targetFile = new File(filePath);
        assertTrue(targetFile.getCanonicalFile() + "does not exist.", targetFile.exists());

        // Check contents of file for jpa feature
        int found = 0;
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(text)) {
                    found++;
                }
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }

        return found;
    }
}
