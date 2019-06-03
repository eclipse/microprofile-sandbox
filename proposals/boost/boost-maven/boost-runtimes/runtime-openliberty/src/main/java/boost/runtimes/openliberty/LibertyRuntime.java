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
package boost.runtimes.openliberty;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;
import org.twdata.maven.mojoexecutor.MojoExecutor.ExecutionEnvironment;

import boost.common.BoostException;
import boost.common.boosters.AbstractBoosterConfig;
import boost.common.config.BoostProperties;
import boost.common.config.BoosterConfigurator;
import boost.common.config.ConfigConstants;
import boost.common.runtimes.RuntimeI;
import boost.maven.runtimes.RuntimeParams;
import boost.maven.utils.BoostLogger;
import boost.maven.utils.MavenProjectUtil;
import boost.runtimes.openliberty.boosters.LibertyBoosterI;

public class LibertyRuntime implements RuntimeI {
    private final List<AbstractBoosterConfig> boosterConfigs;
    private final ExecutionEnvironment env;
    private final MavenProject project;
    private final Plugin mavenDepPlugin;

    private final String serverName = "BoostServer";
    private final String projectBuildDir;
    private final String libertyServerPath;

    private final String runtimeGroupId = "io.openliberty";
    private final String runtimeArtifactId = "openliberty-runtime";
    private final String runtimeVersion = "19.0.0.3";

    private String libertyMavenPluginGroupId = "net.wasdev.wlp.maven.plugins";
    private String libertyMavenPluginArtifactId = "liberty-maven-plugin";
    private String libertyMavenPluginVersion = "2.6.3";

    public LibertyRuntime() {
        this.boosterConfigs = null;
        this.env = null;
        this.project = null;
        this.projectBuildDir = null;
        this.libertyServerPath = null;
        this.mavenDepPlugin = null;
    }

    public LibertyRuntime(RuntimeParams runtimeParams) {
        this.boosterConfigs = runtimeParams.getBoosterConfigs();
        this.env = runtimeParams.getEnv();
        this.project = runtimeParams.getProject();
        this.projectBuildDir = project.getBuild().getDirectory();
        this.libertyServerPath = projectBuildDir + "/liberty/wlp/usr/servers/" + serverName;
        this.mavenDepPlugin = runtimeParams.getMavenDepPlugin();
    }

    private Plugin getPlugin() throws MojoExecutionException {
        return plugin(groupId(libertyMavenPluginGroupId), artifactId(libertyMavenPluginArtifactId),
                version(libertyMavenPluginVersion));
    }

    @Override
    public void doPackage() throws BoostException {
        String javaCompilerTargetVersion = MavenProjectUtil.getJavaCompilerTargetVersion(project);
        System.setProperty(BoostProperties.INTERNAL_COMPILER_TARGET, javaCompilerTargetVersion);
        try {
            packageLiberty(boosterConfigs);
        } catch (MojoExecutionException e) {
            throw new BoostException("Error packaging Liberty server", e);
        }
    }

    private void packageLiberty(List<AbstractBoosterConfig> boosterConfigs) throws MojoExecutionException {
        createLibertyServer();

        // targeting a liberty install
        copyBoosterDependencies(boosterConfigs);

        generateServerConfig(boosterConfigs);

        installMissingFeatures();
        // we install the app now, after server.xml is configured. This is
        // so that we can specify a custom config-root in server.xml ("/").
        // If we installed the app prior to server.xml configuration, then
        // the LMP would write out a webapp stanza into config dropins that
        // would include a config-root setting set to the app name.
        if (project.getPackaging().equals("war")) {
            installApp(ConfigConstants.INSTALL_PACKAGE_ALL);
        } else {
            // This is temporary. When packing type is "jar", if we
            // set installAppPackages=all, the LMP will try to install
            // the project jar and fail. Once this is fixed, we can always
            // set installAppPackages=all.
            installApp(ConfigConstants.INSTALL_PACKAGE_DEP);
        }

        // Create the Liberty runnable jar
        createUberJar();
    }

    /**
     * Get all booster dependencies and invoke the maven-dependency-plugin to copy
     * them to the Liberty server.
     * 
     * @throws MojoExecutionException
     *
     */
    private void copyBoosterDependencies(List<AbstractBoosterConfig> boosterConfigs) throws MojoExecutionException {
        List<String> dependenciesToCopy = BoosterConfigurator.getDependenciesToCopy(boosterConfigs,
                BoostLogger.getInstance());

        for (String dep : dependenciesToCopy) {

            String[] dependencyInfo = dep.split(":");

            executeMojo(mavenDepPlugin, goal("copy"),
                    configuration(element(name("outputDirectory"), libertyServerPath + "/resources"),
                            element(name("artifactItems"),
                                    element(name("artifactItem"), element(name("groupId"), dependencyInfo[0]),
                                            element(name("artifactId"), dependencyInfo[1]),
                                            element(name("version"), dependencyInfo[2])))),
                    env);
        }
    }

    /**
     * Generate config for the Liberty server based on the Maven project.
     * 
     * @throws MojoExecutionException
     */
    private void generateServerConfig(List<AbstractBoosterConfig> boosterConfigs) throws MojoExecutionException {

        try {
            // Generate server config
            generateLibertyServerConfig(boosterConfigs);

        } catch (Exception e) {
            throw new MojoExecutionException("Unable to generate server configuration for the Liberty server.", e);
        }
    }

    private List<String> getWarNames() {
        List<String> warNames = new ArrayList<String>();

        for (Artifact artifact : project.getArtifacts()) {
            if (artifact.getType().equals("war")) {
                warNames.add(artifact.getArtifactId() + "-" + artifact.getVersion());
            }
        }

        if (project.getPackaging().equals(ConfigConstants.WAR_PKG_TYPE)) {
            if (project.getVersion() == null) {
                warNames.add(project.getArtifactId());
            } else {
                warNames.add(project.getArtifactId() + "-" + project.getVersion());
            }
        }

        return warNames;
    }

    /**
     * Configure the Liberty runtime
     * 
     * @param boosterConfigurators
     * @throws Exception
     */
    private void generateLibertyServerConfig(List<AbstractBoosterConfig> boosterConfigurators) throws Exception {

        List<String> warNames = getWarNames();
        LibertyServerConfigGenerator libertyConfig = new LibertyServerConfigGenerator(libertyServerPath,
                BoostLogger.getInstance());

        // Add default http endpoint configuration
        Properties boostConfigProperties = BoostProperties.getConfiguredBoostProperties(BoostLogger.getInstance());

        String host = (String) boostConfigProperties.getOrDefault(BoostProperties.ENDPOINT_HOST, "*");
        libertyConfig.addHostname(host);

        String httpPort = (String) boostConfigProperties.getOrDefault(BoostProperties.ENDPOINT_HTTP_PORT, "9080");
        libertyConfig.addHttpPort(httpPort);

        String httpsPort = (String) boostConfigProperties.getOrDefault(BoostProperties.ENDPOINT_HTTPS_PORT, "9443");
        libertyConfig.addHttpsPort(httpsPort);

        // Add war configuration if necessary
        if (!warNames.isEmpty()) {
            for (String warName : warNames) {
                libertyConfig.addApplication(warName);
            }
        } else {
            throw new Exception(
                    "No war files were found. The project must have a war packaging type or specify war dependencies.");
        }

        // Loop through configuration objects and add config and
        // the corresponding Liberty feature
        for (AbstractBoosterConfig configurator : boosterConfigurators) {
            if (configurator instanceof LibertyBoosterI) {
                ((LibertyBoosterI) configurator).addServerConfig(libertyConfig);
                libertyConfig.addFeature(((LibertyBoosterI) configurator).getFeature());
            }
        }

        libertyConfig.writeToServer();
    }

    // Liberty Maven Plugin executions

    /**
     * Invoke the liberty-maven-plugin to run the create-server goal
     */
    private void createLibertyServer() throws MojoExecutionException {
        executeMojo(getPlugin(), goal("create-server"),
                configuration(element(name("serverName"), serverName), getRuntimeArtifactElement()), env);
    }

    /**
     * Invoke the liberty-maven-plugin to run the install-feature goal.
     *
     * This will install any missing features defined in the server.xml or
     * configDropins.
     *
     */
    private void installMissingFeatures() throws MojoExecutionException {
        executeMojo(getPlugin(), goal("install-feature"), configuration(element(name("serverName"), serverName),
                element(name("features"), element(name("acceptLicense"), "false"))), env);
    }

    /**
     * Invoke the liberty-maven-plugin to run the install-app goal.
     */
    private void installApp(String installAppPackagesVal) throws MojoExecutionException {
        Element installAppPackages = element(name("installAppPackages"), installAppPackagesVal);
        Element serverNameElement = element(name("serverName"), serverName);

        Xpp3Dom configuration = configuration(installAppPackages, serverNameElement, getRuntimeArtifactElement());
        configuration.addChild(element(name("appsDirectory"), "apps").toDom());
        configuration.addChild(element(name("looseApplication"), "true").toDom());

        executeMojo(getPlugin(), goal("install-apps"), configuration, env);
    }

    private Element getRuntimeArtifactElement() {
        return element(name("assemblyArtifact"), element(name("groupId"), runtimeGroupId),
                element(name("artifactId"), runtimeArtifactId), element(name("version"), runtimeVersion),
                element(name("type"), "zip"));
    }

    /**
     * Invoke the liberty-maven-plugin to package the server into a runnable Liberty
     * JAR
     */
    private void createUberJar() throws MojoExecutionException {
        executeMojo(getPlugin(), goal("package-server"),
                configuration(element(name("isInstall"), "false"), element(name("include"), "minify,runnable"),
                        element(name("outputDirectory"), "target/liberty-alt-output-dir"),
                        element(name("packageFile"), ""), element(name("serverName"), serverName)),
                env);
    }

    @Override
    public void doDebug(boolean clean) throws BoostException {
        try {
            executeMojo(getPlugin(), goal("debug"), configuration(element(name("serverName"), serverName),
                    element(name("clean"), String.valueOf(clean)), getRuntimeArtifactElement()), env);
        } catch (MojoExecutionException e) {
            throw new BoostException("Error debugging Liberty server", e);
        }
    }

    @Override
    public void doRun(boolean clean) throws BoostException {
        try {
            executeMojo(getPlugin(), goal("run"), configuration(element(name("serverName"), serverName),
                    element(name("clean"), String.valueOf(clean)), getRuntimeArtifactElement()), env);
        } catch (MojoExecutionException e) {
            throw new BoostException("Error running Liberty server", e);
        }

    }

    @Override
    public void doStart(boolean clean, int verifyTimeout, int serverStartTimeout) throws BoostException {
        try {
            executeMojo(getPlugin(), goal("start"),
                    configuration(element(name("serverName"), serverName),
                            element(name("verifyTimeout"), String.valueOf(verifyTimeout)),
                            element(name("serverStartTimeout"), String.valueOf(serverStartTimeout)),
                            element(name("clean"), String.valueOf(clean)), getRuntimeArtifactElement()),
                    env);
        } catch (MojoExecutionException e) {
            throw new BoostException("Error starting Liberty server", e);
        }
    }

    @Override
    public void doStop() throws BoostException {
        try {
            executeMojo(getPlugin(), goal("stop"),
                    configuration(element(name("serverName"), serverName), getRuntimeArtifactElement()), env);
        } catch (MojoExecutionException e) {
            throw new BoostException("Error stopping Liberty server", e);
        }
    }

}
