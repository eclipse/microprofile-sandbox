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
package io.openliberty.boost.common.utils;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigFileUtils {

    public static boolean findStringInServerXml(String serverXMLPath, String stringToFind) throws IOException {

        boolean found = false;

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(serverXMLPath));
            String line;
            while ((line = br.readLine()) != null && !found) {
                if (line.contains(stringToFind)) {
                    found = true;
                }
            }

        } catch (FileNotFoundException e) {
            fail("The file " + serverXMLPath + " does not exist");
        } finally {
            if (br != null) {
                br.close();
            }
        }

        return found;
    }

    public static String findPropertyInBootstrapProperties(String bootstrapPropertiesPath, String key)
            throws IOException {

        String foundProperty = null;

        Properties properties = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream(bootstrapPropertiesPath);

            // load a properties file
            properties.load(input);

            foundProperty = properties.getProperty(key);

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return foundProperty;
    }

}
