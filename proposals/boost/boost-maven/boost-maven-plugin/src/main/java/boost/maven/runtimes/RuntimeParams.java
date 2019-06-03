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
package boost.maven.runtimes;

import java.util.List;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.twdata.maven.mojoexecutor.MojoExecutor.ExecutionEnvironment;

import boost.common.boosters.AbstractBoosterConfig;

public class RuntimeParams {

    List<AbstractBoosterConfig> boosterConfigs;
    ExecutionEnvironment env;
    MavenProject project;
    Log log;
    RepositorySystem repoSystem;
    RepositorySystemSession repoSession;
    List<RemoteRepository> remoteRepos;
    Plugin mavenDepPlugin;
    String projectBuildDir;

    public RuntimeParams(List<AbstractBoosterConfig> boosterConfigs, ExecutionEnvironment env, MavenProject project,
            Log log, RepositorySystem repoSystem, RepositorySystemSession repoSession,
            List<RemoteRepository> remoteRepos, Plugin mavenDepPlugin) {
        this.log = log;
        this.boosterConfigs = boosterConfigs;
        this.env = env;
        this.project = project;
        this.projectBuildDir = project.getBuild().getDirectory();
        this.repoSystem = repoSystem;
        this.repoSession = repoSession;
        this.remoteRepos = remoteRepos;
        this.mavenDepPlugin = mavenDepPlugin;
    }

    public Log getLog() {
        return this.log;
    }

    public List<AbstractBoosterConfig> getBoosterConfigs() {
        return this.boosterConfigs;
    }

    public ExecutionEnvironment getEnv() {
        return this.env;
    }

    public MavenProject getProject() {
        return this.project;
    }

    public String getProjectBuildDir() {
        return this.projectBuildDir;
    }

    public RepositorySystem getRepoSystem() {
        return this.repoSystem;
    }

    public RepositorySystemSession getRepoSession() {
        return this.repoSession;
    }

    public List<RemoteRepository> getRemoteRepos() {
        return this.remoteRepos;
    }

    public Plugin getMavenDepPlugin() {
        return this.mavenDepPlugin;
    }
}
