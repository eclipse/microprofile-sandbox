/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package boost.runtimes.tomee;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import java.util.List;
import java.util.Properties;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.twdata.maven.mojoexecutor.MojoExecutor.ExecutionEnvironment;

import boost.common.BoostException;
import boost.common.boosters.AbstractBoosterConfig;
import boost.common.config.BoostProperties;
import boost.common.config.BoosterConfigurator;
import boost.common.runtimes.RuntimeI;
import boost.maven.runtimes.RuntimeParams;
import boost.maven.utils.BoostLogger;

public class TomeeRuntime implements RuntimeI {
    private final List<AbstractBoosterConfig> boosterConfigs;
    private final ExecutionEnvironment env;

    private final String tomeeMavenPluginGroupId = "org.apache.tomee.maven";
    private final String tomeeMavenPluginArtifactId = "tomee-maven-plugin";
    private final String installDir;
    private final String configDir;

    private final Plugin mavenDepPlugin;

    public TomeeRuntime() {
        this.boosterConfigs = null;
        this.env = null;

        this.installDir = null;
        this.configDir = null;

        this.mavenDepPlugin = null;
    }

    public TomeeRuntime(RuntimeParams params) {
        this.boosterConfigs = params.getBoosterConfigs();
        this.env = params.getEnv();

        this.installDir = params.getProjectBuildDir() + "/apache-tomee/";
        this.configDir = installDir + "conf";
        this.mavenDepPlugin = params.getMavenDepPlugin();
    }

    private Plugin getPlugin() throws MojoExecutionException {
        return plugin(groupId(tomeeMavenPluginGroupId), artifactId(tomeeMavenPluginArtifactId), version("8.0.0-M2"));
    }

    public void doPackage() throws BoostException {
        try {
            createTomeeServer();
            configureTomeeServer(boosterConfigs);
            copyTomeeJarDependencies(boosterConfigs);
            createUberJar();
        } catch (Exception e) {
            throw new BoostException("Error packaging TomEE server", e);
        }
    }

    /**
     * Invoke the liberty-maven-plugin to run the create-server goal
     */
    private void createTomeeServer() throws MojoExecutionException {
        executeMojo(getPlugin(), goal("build"), configuration(element(name("context"), "ROOT"),
                element(name("tomeeVersion"), "8.0.0-M2"), element(name("tomeeClassifier"), "plus")), env);
    }

    /**
     * Configure the TomEE runtime
     */
    private void configureTomeeServer(List<AbstractBoosterConfig> boosterConfigurators) throws Exception {
        try {
            TomeeServerConfigGenerator tomeeConfig = new TomeeServerConfigGenerator(configDir,
                    BoostLogger.getInstance());
            tomeeConfig.addJarsDirToSharedLoader();

            // Configure HTTP endpoint
            Properties boostConfigProperties = BoostProperties.getConfiguredBoostProperties(BoostLogger.getInstance());

            String hostname = (String) boostConfigProperties.getOrDefault(BoostProperties.ENDPOINT_HOST, "localhost");
            tomeeConfig.addHostname(hostname);

            String httpPort = (String) boostConfigProperties.getOrDefault(BoostProperties.ENDPOINT_HTTP_PORT, "8080");
            tomeeConfig.addHttpPort(httpPort);

            // Loop through configuration objects and add config
            for (AbstractBoosterConfig configurator : boosterConfigurators) {
                tomeeConfig.addServerConfig(configurator);
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Unable to generate server configuration for the Tomee server.", e);
        }
    }

    /**
     * Get all booster dependencies and invoke the maven-dependency-plugin to copy
     * them to the Liberty server.
     * 
     * @throws MojoExecutionException
     *
     */
    private void copyTomeeJarDependencies(List<AbstractBoosterConfig> boosterConfigs) throws MojoExecutionException {
        List<String> tomeeDependencyJarsToCopy = BoosterConfigurator.getDependenciesToCopy(boosterConfigs,
                BoostLogger.getInstance());
        for (String dep : tomeeDependencyJarsToCopy) {
            String[] dependencyInfo = dep.split(":");

            executeMojo(mavenDepPlugin, goal("copy"),
                    configuration(element(name("outputDirectory"), installDir + "boost"),
                            element(name("artifactItems"),
                                    element(name("artifactItem"), element(name("groupId"), dependencyInfo[0]),
                                            element(name("artifactId"), dependencyInfo[1]),
                                            element(name("version"), dependencyInfo[2])))),
                    env);
        }
    }

    /**
     * Invoke the tomee-maven-plugin to create an executable jar
     */
    private void createUberJar() throws MojoExecutionException {
        executeMojo(getPlugin(), goal("exec"),
                configuration(element(name("classifier"), "exec"), element(name("tomeeAlreadyInstalled"), "true"),
                        element(name("classpaths"), "[]"), element(name("context"), "ROOT"),
                        element(name("tomeeVersion"), "8.0.0-M2"), element(name("tomeeClassifier"), "plus"),
                        element(name("catalinaBase"), installDir), element(name("config"), configDir)),
                env);
    }

    public void doDebug(boolean clean) throws BoostException {
        // TODO No debug in TomEE yet
    }

    public void doRun(boolean clean) throws BoostException {
        try {
            executeMojo(getPlugin(), goal("run"),
                    configuration(element(name("tomeeAlreadyInstalled"), "true"), element(name("context"), "ROOT"),
                            element(name("tomeeVersion"), "8.0.0-M2"), element(name("tomeeClassifier"), "plus")),
                    env);
        } catch (MojoExecutionException e) {
            throw new BoostException("Error running TomEE server", e);
        }
    }

    public void doStart(boolean clean, int verifyTimeout, int serverStartTimeout) throws BoostException {
        try {
            executeMojo(getPlugin(), goal("start"),
                    configuration(element(name("tomeeAlreadyInstalled"), "true"), element(name("context"), "ROOT"),
                            element(name("tomeeVersion"), "8.0.0-M2"), element(name("tomeeClassifier"), "plus")),
                    env);
        } catch (MojoExecutionException e) {
            throw new BoostException("Error starting TomEE server", e);
        }
    }

    public void doStop() throws BoostException {
        try {
            executeMojo(getPlugin(), goal("stop"),
                    configuration(element(name("tomeeAlreadyInstalled"), "true"), element(name("context"), "ROOT"),
                            element(name("tomeeVersion"), "8.0.0-M2"), element(name("tomeeClassifier"), "plus")),
                    env);
        } catch (MojoExecutionException e) {
            throw new BoostException("Error stopping TomEE server", e);
        }
    }

}
