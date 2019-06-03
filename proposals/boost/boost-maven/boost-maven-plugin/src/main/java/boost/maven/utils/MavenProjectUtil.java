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
package boost.maven.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactDescriptorException;
import org.eclipse.aether.resolution.ArtifactDescriptorRequest;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;

import boost.common.boosters.AbstractBoosterConfig;

public class MavenProjectUtil {

    public static Map<String, String> getAllDependencies(MavenProject project, RepositorySystem repoSystem,
            RepositorySystemSession repoSession, List<RemoteRepository> remoteRepos, BoostLogger logger)
            throws ArtifactDescriptorException {
        logger.debug("Processing project for dependencies.");
        Map<String, String> dependencies = new HashMap<String, String>();

        for (Artifact artifact : project.getArtifacts()) {
            logger.debug("Found dependency while processing project: " + artifact.getGroupId() + ":"
                    + artifact.getArtifactId() + ":" + artifact.getVersion() + ":" + artifact.getType() + ":"
                    + artifact.getScope());

            if (artifact.getType().equals("war")) {
                logger.debug("Resolving transitive booster dependencies for war");
                org.eclipse.aether.artifact.Artifact pomArtifact = new DefaultArtifact(artifact.getGroupId(),
                        artifact.getArtifactId(), "pom", artifact.getVersion());

                List<org.eclipse.aether.artifact.Artifact> warArtifacts = getWarArtifacts(pomArtifact, repoSystem,
                        repoSession, remoteRepos);

                for (org.eclipse.aether.artifact.Artifact warArtifact : warArtifacts) {
                    // Only add booster dependencies for this war. Anything else
                    // like datasource
                    // dependencies must be explicitly defined by this project.
                    // This is to allow the
                    // current project to have full control over those optional
                    // dependencies.
                    if (warArtifact.getGroupId().equals(AbstractBoosterConfig.BOOSTERS_GROUP_ID)) {
                        logger.debug("Found booster dependency: " + warArtifact.getGroupId() + ":"
                                + warArtifact.getArtifactId() + ":" + warArtifact.getVersion());

                        dependencies.put(warArtifact.getGroupId() + ":" + warArtifact.getArtifactId(),
                                warArtifact.getVersion());
                    }
                }

            } else {
                dependencies.put(artifact.getGroupId() + ":" + artifact.getArtifactId(), artifact.getVersion());
            }
        }

        return dependencies;
    }

    public static String getJavaCompilerTargetVersion(MavenProject project) {

        // Check maven compiler properties
        Properties mavenProperties = project.getProperties();

        for (Map.Entry<Object, Object> property : mavenProperties.entrySet()) {
            String propertyKey = property.getKey().toString();
            String propertyValue = property.getValue().toString();

            if (propertyKey.equals("maven.compiler.target") || propertyKey.equals("maven.compiler.release")) {
                return propertyValue;
            }
        }

        // Check maven-compiler-plugin release value
        String release = net.wasdev.wlp.maven.plugins.utils.MavenProjectUtil.getPluginConfiguration(project,
                "org.apache.maven.plugins", "maven-compiler-plugin", "release");

        if (release != null) {
            return release;
        }

        // Check maven-compiler-plugin target value
        String target = net.wasdev.wlp.maven.plugins.utils.MavenProjectUtil.getPluginConfiguration(project,
                "org.apache.maven.plugins", "maven-compiler-plugin", "target");

        if (target != null) {
            return target;
        }

        return "";
    }

    private static List<org.eclipse.aether.artifact.Artifact> getWarArtifacts(
            org.eclipse.aether.artifact.Artifact artifact, RepositorySystem repoSystem,
            RepositorySystemSession repoSession, List<RemoteRepository> remoteRepos)
            throws ArtifactDescriptorException {

        List<org.eclipse.aether.artifact.Artifact> artifacts = new ArrayList<>();

        ArtifactDescriptorRequest descriptorRequest = new ArtifactDescriptorRequest();
        descriptorRequest.setArtifact(artifact);
        for (RemoteRepository remoteRepo : remoteRepos) {
            descriptorRequest.addRepository(remoteRepo);
        }

        ArtifactDescriptorResult descriptorResult = repoSystem.readArtifactDescriptor(repoSession, descriptorRequest);

        for (Dependency dependency : descriptorResult.getDependencies()) {
            artifacts.add(dependency.getArtifact());
        }

        return artifacts;
    }
}
