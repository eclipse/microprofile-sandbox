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

public class Db2VersionIT {

    private static String DB2_JAR;

    @BeforeClass
    public static void init() {
        String runtime = System.getProperty("boostRuntime");

        if ("tomee".equals(runtime)) {
            DB2_JAR = "target/apache-tomee/boost/db2jcc-db2jcc4.jar";
        } else if ("ol".equals(runtime) || "wlp".equals(runtime)) {
            DB2_JAR = "target/liberty/wlp/usr/servers/BoostServer/resources/db2jcc-db2jcc4.jar";
        }
    }

    @Test
    public void testDb2Version() throws Exception {
        File targetFile = new File(DB2_JAR);
        assertTrue(targetFile.getCanonicalFile() + "does not exist.", targetFile.exists());
    }
}
