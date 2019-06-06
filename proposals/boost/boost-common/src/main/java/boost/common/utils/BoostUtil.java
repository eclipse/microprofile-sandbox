/*******************************************************************************
 * Copyright (c) 2018, 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package boost.common.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import boost.common.BoostLoggerI;
import net.wasdev.wlp.common.plugins.util.OSUtil;

public class BoostUtil {

    public static boolean isNotNullOrEmpty(String s) {
        return s != null && !s.isEmpty();
    }

    public static boolean isLibertyJar(File artifact, BoostLoggerI logger) {
        boolean isLibertyJar = false;

        try (JarFile artifactJar = new JarFile(artifact)) {
            isLibertyJar = artifactJar.getEntry("wlp") != null;
        } catch (Exception e) {
            logger.debug("Exception when checking Liberty JAR", e);
        }

        return isLibertyJar;
    }

    public static void extract(File artifact, File projectDirectory, String dependencyFolder) {
        File extractDir = new File(projectDirectory.getPath() + File.separator + dependencyFolder);
        if (!extractDir.exists())
            extractDir.mkdirs();

        try {
            byte[] buffer = new byte[1024];
            ZipInputStream zis = new ZipInputStream(new FileInputStream(artifact));
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                File newFile = new File(extractDir, ze.getName());
                if (ze.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    new File(newFile.getParent()).mkdirs();
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                ze = zis.getNextEntry();
            }
            // close last ZipEntry
            zis.closeEntry();
            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String makeVariable(String propertyName) {
        return "${" + propertyName + "}";
    }

    private static String getSecurityUtilCmd(String libertyInstallPath) {
        if (OSUtil.isWindows()) {
            return libertyInstallPath + "/bin/securityUtility.bat";
        } else {
            return libertyInstallPath + "/bin/securityUtility";
        }
    }

    public static String encrypt(String libertyInstallPath, String property, String encryptionType, String encryptionKey, BoostLoggerI logger) throws IOException {
        //Won't encode the property if it contains the aes flag
        if (!isEncoded(property)) {
            Runtime rt = Runtime.getRuntime();
            List<String> commands = new ArrayList<String>();

            commands.add(getSecurityUtilCmd(libertyInstallPath));
            commands.add("encode");
            commands.add(property);

            if(encryptionType != null && !encryptionType.equals("")) {
                commands.add("--encoding=" + encryptionType);
            } else {
                commands.add("--encoding=aes");
            }

            if(encryptionKey != null && !encryptionKey.equals("")) {
                commands.add("--key=" + encryptionKey);
            }

            Process proc = rt.exec(commands.toArray(new String[0]));

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

            String s = null;

            StringBuilder out = new StringBuilder();
            while ((s = stdInput.readLine()) != null) {
                out.append(s);
            }

            StringBuilder error = new StringBuilder();
            while ((s = stdError.readLine()) != null) {
                error.append(s + "\n");
            }

            if (error.length() != 0) {
                throw new IOException("Password encryption failed: " + error);
            }

            return out.toString();
        }
        return property;
    }

    public static boolean isEncoded(String property) {
        return property.contains("{aes}") || property.contains("{hash}") || property.contains("{xor}");
    }

}
