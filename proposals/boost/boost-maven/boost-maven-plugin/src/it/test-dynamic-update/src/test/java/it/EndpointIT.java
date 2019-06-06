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
package it;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

public class EndpointIT {
    private static String URL;
    private static Process process;

    private static String HelloResource;
    private static String osname;
    private static boolean isWindows;
    private static String originalStatement = "        return \"Hello World From Your Friends at Liberty Boost EE! - pass 1\";";
    private static String updatedStatement = "        return \"Hello World From Your Friends at Liberty Boost EE! Updated after compile\";";

    @BeforeClass
    public static void init() {

        // This test will only work with liberty's
        // loose application functionality
        String runtime = System.getProperty("boostRuntime");
        org.junit.Assume.assumeTrue("ol".equals(runtime) || "wlp".equals(runtime));

        String port = System.getProperty("boost.http.port");
        URL = "http://localhost:" + port + "/api/hello";

        if (System.getProperty("os.name").contains("Windows")) {
            isWindows = true;
        } else {
            isWindows = false;
        }
        setupHelloResources();

    }

    @Test
    public void testServlet() throws Exception {
        HttpClient client = new HttpClient();

        GetMethod method = new GetMethod(URL);

        try {
            int statusCode = client.executeMethod(method);

            assertEquals("HTTP GET failed", HttpStatus.SC_OK, statusCode);

            String response = method.getResponseBodyAsString(10000);

            assertTrue("Unexpected response body",
                    response.contains("Hello World From Your Friends at Liberty Boost EE!"));

            updateApplicationSource(originalStatement, updatedStatement, false);

            statusCode = client.executeMethod(method);

            assertEquals("HTTP GET failed", HttpStatus.SC_OK, statusCode);

            response = method.getResponseBodyAsString(10000);

            assertTrue("Unexpected response body 1st request = " + response,
                    response.contains("Hello World From Your Friends at Liberty Boost EE! - pass 1"));

            method.releaseConnection();

        } finally {
            // Set the source back.
            updateApplicationSource(originalStatement, updatedStatement, false);

            method.releaseConnection();
        }
    }

    /*
     * Execute mvn compile Note the command will be different in Windows,
     * Linux/MAC and Cygwin. Thus all the
     */
    private void mavenCompile() throws IOException {

        boolean isCYGWIN = false;
        String sep = File.separator;
        // for windows/CYGWIN enbvironments need the path to maven
        Map<String, String> env = System.getenv();
        String path = env.get("M2");
        System.out.println("Path from M2 = " + path);
        // if M2 is not set try MAVEN_HOME.
        if (path == null) {
            path = env.get("MAVEN_HOME");
            System.out.println("Path from MAVEN_HOME = " + path);
            if (path != null) {
                if (path.indexOf(".") >= 0) {
                    path = path.substring(0, path.indexOf("."));
                    System.out.println("Path from MAVEN_HOME after strip of periods" + path);
                }
            }

        }
        if (path != null) {
            String seperator = "\\";
            path = path.replaceAll(Pattern.quote(seperator), "\\\\");
            if (isWindows && (path.contains("/"))) {
                // in CYGWIN
                System.out.println("In CYGWIN");
                isCYGWIN = true;
                path = path.trim() + "/mvn";
                path = "\'\"" + path + "\"" + " compile\'";
                System.out.println("CYGWIN Path = " + path);
            } else {
                // plain Windows or linux
                System.out.println("In plain windows");
                path = path + sep + "mvn";
            }
        } else {
            // default will try just using mvn. This
            // may not work and will cause the test
            // to fail if it doesn't... then again it may (works fine on Mac)
            path = "mvn";
        }
        System.out.println("MVN Command path = " + path);
        String[] commandToRun = null;
        if (isWindows && !isCYGWIN) {
            commandToRun = new String[4];
            commandToRun[0] = "cmd.exe";
            commandToRun[1] = "/C";
            commandToRun[2] = path;
            commandToRun[3] = "compile";
            runCommand(commandToRun);
        } else if (isCYGWIN) {
            // CYGWIN doesn't like the String[] parm for Runtime.getRuntime.exec
            // So have to make something special....
            System.out.println("running CYGWIN Command");
            String command = "c:\\cygwin\\bin\\bash.exe -c " + path;
            runCommand(command);
        } else {
            commandToRun = new String[2];
            commandToRun[0] = path;
            commandToRun[1] = "compile";
            runCommand(commandToRun);
        }

    }

    /*
     * get file name path setup correctly for environment. This file will be
     * edited on the fly by the testcase This is to test loosApplication
     * function.
     */
    private static void setupHelloResources() {

        String sep = File.separator;
        HelloResource = "src" + sep + "main" + sep + "java" + sep + "com" + sep + "example" + sep
                + "HelloResource.java";

    }

    /*
     * Update source that will be recompiled to test loose application support
     * 
     * @param String newStatement - the statement the application should output
     * 
     * @param String oldStatement - the statement the application is currently
     * outputting
     * 
     * @param boolean - validate that the old statement was in the file before
     * editing.
     * 
     * @throws Exception throws IOException on file errors, throw Exception if
     * validate is true and the old statement is not found in the file.
     */
    private void updateApplicationSource(String newStatement, String oldStatement, boolean validate) throws Exception {
        BufferedWriter bw;
        FileReader fr;
        FileWriter fw;
        BufferedReader br;
        String s;
        boolean updatedApplication = false;
        ArrayList<String> fileContents = new ArrayList<String>();

        try {
            fr = new FileReader(HelloResource);
            br = new BufferedReader(fr);

            while ((s = br.readLine()) != null) {
                if (s.trim().equals(oldStatement.trim())) {
                    fileContents.add(newStatement);
                    updatedApplication = true;
                } else
                    fileContents.add(s);
            }
            br.close();

            if (!updatedApplication && validate)
                throw new Exception("Statement not found in application " + oldStatement);

            fw = new FileWriter(HelloResource);
            bw = new BufferedWriter(fw);

            for (String statement : fileContents) {
                bw.write(statement + '\n');
            }
            bw.flush();
            bw.close();
        } catch (FileNotFoundException e) {
            System.out.println("File was not found! " + e.toString());
        } catch (IOException e) {
            System.out.println("No file found!" + e.toString());
        }

    }

    /*
     * run a command using a String array
     */
    private void runCommand(String[] command) throws IOException {

        StringBuilder commandString = new StringBuilder();
        for (String commandPart : command) {
            commandString.append(commandPart);
            commandString.append(" ");
        }
        System.out.println("executing command " + commandString.toString());
        process = Runtime.getRuntime().exec(command);
        handleCommandOutput(process);

    }

    /*
     * run a command using a string
     */
    private void runCommand(String command) throws IOException {

        System.out.println("Executing command: " + command);
        process = Runtime.getRuntime().exec(command);
        handleCommandOutput(process);

    }

    /*
     * Handle output from the command issued.
     */
    private void handleCommandOutput(Process process) throws IOException {

        BufferedReader ereader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
            builder.append(System.getProperty("line.separator"));
        }
        System.out.println("command result =" + builder.toString());

        line = null;
        builder = new StringBuilder();
        while ((line = ereader.readLine()) != null) {
            builder.append(line);
            builder.append(System.getProperty("line.separator"));
        }

        String result = builder.toString();
        System.out.println("error result =" + builder.toString());

    }
}
