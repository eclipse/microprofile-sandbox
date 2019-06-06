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
package boost.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import boost.common.BoostException;

/**
 * Stops the executable archive application started by the 'start' or 'run'
 * goals.
 */
@Mojo(name = "stop", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class StopMojo extends AbstractMojo {

    @Override
    public void execute() throws MojoExecutionException {
        super.execute();

        try {
            this.getRuntimeInstance().doStop();
        } catch (BoostException e) {
            throw new MojoExecutionException("Error stopping server", e);
        }
    }

}
