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
package boost.runtimes.wildfly;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;
import org.twdata.maven.mojoexecutor.MojoExecutor.ExecutionEnvironment;

import boost.common.BoostException;
import boost.common.boosters.AbstractBoosterConfig;
import boost.common.config.BoosterConfigurator;
import boost.common.config.ConfigConstants;
import boost.common.runtimes.RuntimeI;
import boost.maven.runtimes.RuntimeParams;
import boost.maven.utils.BoostLogger;

public class WildflyRuntime implements RuntimeI {

	private final List<AbstractBoosterConfig> boosterConfigs;
    private final ExecutionEnvironment env;
    private final MavenProject project;
    
    private final String projectBuildDir;
    private final String wildflyInstallDir;

    private final String runtimeGroupId = "org.wildfly";
    private final String runtimeArtifactId = "wildfly-dist";
    private final String runtimeVersion = "17.0.0.Final";

    private String wildflyMavenPluginGroupId = "org.wildfly.plugins";
    private String wildflyMavenPluginArtifactId = "wildfly-maven-plugin";
    private String wildflyMavenPluginVersion = "2.0.1.Final";
    
    protected String mavenWarPluginGroupId = "org.apache.maven.plugins";
    protected String mavenWarPluginArtifactId = "maven-war-plugin";
    private String mavenWarPluginVersion = "3.2.3";

    private final Plugin mavenDepPlugin;
    
    public WildflyRuntime() {
    	this.boosterConfigs = null;
        this.env = null;
        this.project = null;
        this.projectBuildDir = null;
        this.wildflyInstallDir = null;
        this.mavenDepPlugin = null;
    }

    public WildflyRuntime(RuntimeParams runtimeParams) {
    	this.boosterConfigs = runtimeParams.getBoosterConfigs();
        this.env = runtimeParams.getEnv();
        this.project = env.getMavenProject();
        this.projectBuildDir = runtimeParams.getProjectBuildDir();
        this.wildflyInstallDir = projectBuildDir + "/wildfly-" + runtimeVersion;
        this.mavenDepPlugin = runtimeParams.getMavenDepPlugin();
    }

    private Plugin getPlugin() throws MojoExecutionException {
        return plugin(groupId(wildflyMavenPluginGroupId), artifactId(wildflyMavenPluginArtifactId),
                version(wildflyMavenPluginVersion));
    }

    @Override
    public void doPackage() throws BoostException {
        try {
            packageWildfly(boosterConfigs);
        } catch (Exception e) {
            throw new BoostException("Error packaging Wildfly server", e);
        }
    }

    private void packageWildfly(List<AbstractBoosterConfig> boosterConfigs) throws MojoExecutionException {
    	installWildfly();

    	addDependenciesToWar(boosterConfigs);

        generateServerConfig(boosterConfigs);

        // Create the runnable jar
        createUberJar();
    }

    /**
     * Load all dependencies as Wildfly modules
     * 
     * @throws MojoExecutionException
     *
     */
    private void addDependenciesToWar(List<AbstractBoosterConfig> boosterConfigs) throws MojoExecutionException {
        List<String> dependenciesToCopy = BoosterConfigurator.getDependenciesToCopy(boosterConfigs,
                BoostLogger.getInstance());

        WildflyCli cli = new WildflyCli(wildflyInstallDir, BoostLogger.getInstance());
        
        String outputDir = projectBuildDir + "/boost";
        for (String dep : dependenciesToCopy) {

            String[] dependencyInfo = dep.split(":");
            String groupId = dependencyInfo[0];
            String artifactId = dependencyInfo[1];
            String version = dependencyInfo[2];

            // Copy the dependency
            executeMojo(mavenDepPlugin, goal("copy"),
                    configuration(element(name("outputDirectory"), outputDir),
                                  element(name("artifactItems"),
                                  element(name("artifactItem"), element(name("groupId"), groupId),
                                  element(name("artifactId"), artifactId),
                                  element(name("version"), version)))),
                    env);
            
            BoostLogger.getInstance().info(groupId + "." + artifactId);
            
            try {
            	String jar = artifactId + "-" + version + ".jar";
				cli.run("module add --name=boost." + artifactId + " --resources=" + outputDir + "/" + jar);
			} catch (IOException e) {
				throw new MojoExecutionException(e.getMessage());
			}
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
            generateWildflyServerConfig(boosterConfigs);

        } catch (Exception e) {
            throw new MojoExecutionException("Unable to generate server configuration for the Liberty server.", e);
        }
    }

    private List<Path> getWarFiles() {
        List<Path> warFiles = new ArrayList<Path>();
        
        for (Artifact artifact : project.getArtifacts()) {
            if (artifact.getType().equals("war")) {
                warFiles.add(Paths.get(artifact.getArtifactId() + "-" + artifact.getVersion()));
            }
        }

        if (project.getPackaging().equals(ConfigConstants.WAR_PKG_TYPE)) {
	        if (project.getVersion() == null) {
	            warFiles.add(Paths.get(projectBuildDir + "/" + project.getArtifactId() + ".war"));
	        } else {
	            warFiles.add(
	                    Paths.get(projectBuildDir + "/" + project.getArtifactId() + "-" + project.getVersion() + ".war"));
	        }
        }

        return warFiles;
    }
    
    private void setContextRoot(WildlfyServerConfigGenerator config) throws Exception  {
    	
    	// Check if jboss-web.xml already exists in the app
    	String webInfPath = projectBuildDir + "/" + project.getArtifactId() + "-" + project.getVersion() + "/WEB-INF";
    	File f = new File(webInfPath + "/jboss-web.xml");
    	if(!f.exists()) { 
    	    // Create the file
    		config.createJbossWebXml(webInfPath);
    		
    		// Repackage the war
            executeMojo(getMavenWarPlugin(), goal("war"),
                    configuration(),
                    env);
    	}
    	
    }

    /**
     * Configure the Liberty runtime
     * 
     * @param boosterConfigurators
     * @throws Exception
     */
    private void generateWildflyServerConfig(List<AbstractBoosterConfig> boosterConfigurators) throws Exception {	
        WildlfyServerConfigGenerator wildflyConfig = new WildlfyServerConfigGenerator(wildflyInstallDir,
                BoostLogger.getInstance());
        
    	// Set context root of this application
        setContextRoot(wildflyConfig);
        
        // Add war configuration if necessary
        List<Path> warFiles = getWarFiles();
        if (!warFiles.isEmpty()) {
            for (Path war : warFiles) {
            	wildflyConfig.addApplication(war.toString());
            }
        } else {
            throw new Exception(
                    "No war files were found. The project must have a war packaging type or specify war dependencies.");
        }

        // Loop through configuration objects and add config
        for (AbstractBoosterConfig configurator : boosterConfigurators) {
        	configurator.addServerConfig(wildflyConfig);
        }

    }


    private void installWildfly() throws MojoExecutionException {

        executeMojo(mavenDepPlugin, goal("unpack"),
                configuration(element(name("outputDirectory"), projectBuildDir),
                        element(name("artifactItems"), getRuntimeArtifactElement())),
                env);
    }

    private Element getRuntimeArtifactElement() {
    	return element(name("artifactItem"), element(name("groupId"), runtimeGroupId),
                element(name("artifactId"), runtimeArtifactId),
                element(name("version"), runtimeVersion),
                element(name("type"), "zip"));
    }
    
    protected Plugin getMavenWarPlugin() throws MojoExecutionException {
        return plugin(groupId(mavenWarPluginGroupId), artifactId(mavenWarPluginArtifactId),
        		version(mavenWarPluginVersion));
    }

    /**
     * Invoke the liberty-maven-plugin to package the server into a runnable
     * Liberty JAR
     */
    private void createUberJar() throws MojoExecutionException {
    	// Not available
    }

    @Override
    public void doDebug(boolean clean) throws BoostException {}

    @Override
    public void doRun(boolean clean) throws BoostException {
        try {
            executeMojo(getPlugin(), goal("run"), configuration(element(name("jbossHome"), wildflyInstallDir)), env);
        } catch (MojoExecutionException e) {
            throw new BoostException("Error running Wildfly server", e);
        }

    }

    @Override
    public void doStart(boolean clean, int verifyTimeout, int serverStartTimeout) throws BoostException {
    	try {
            executeMojo(getPlugin(), goal("start"), configuration(element(name("jbossHome"), wildflyInstallDir)), env);
        } catch (MojoExecutionException e) {
            throw new BoostException("Error starting Wildfly server", e);
        }
    }

    @Override
    public void doStop() throws BoostException {
    	try {
            executeMojo(getPlugin(), goal("shutdown"), configuration(), env);
        } catch (MojoExecutionException e) {
            throw new BoostException("Error stopping Wildfly server", e);
        }
    }

}
