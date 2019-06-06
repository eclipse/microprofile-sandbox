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
package boost.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

import boost.common.BoostException;

import org.apache.maven.plugins.annotations.*;

/**
 * Packages an existing application into a Liberty executable jar so that the
 * application can be run from the command line using java -jar. (This is for
 * the 'jar' packaging type).
 *
 */
@Mojo(name = "package", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class PackageMojo extends AbstractMojo {
        
    @Override
    public void execute() throws MojoExecutionException {
        super.execute();
        try {
            this.getRuntimeInstance().doPackage();
        } catch (BoostException e) {
            throw new MojoExecutionException("Error performing server package", e);
        }
    }
}