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
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import boost.common.BoostException;

/**
 * Starts the executable archive application as a background process.
 * 
 */
@Mojo(name = "start", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class StartMojo extends AbstractMojo {

    /**
     * Time in seconds to wait while verifying that the server has started.
     */
    @Parameter(property = "verifyTimeout", defaultValue = "30")
    private int verifyTimeout = 30;

    /**
     * Time in seconds to wait while verifying that the server has started.
     */
    @Parameter(property = "serverStartTimeout", defaultValue = "30")
    private int serverStartTimeout = 30;

    /**
     * Clean all cached information on server start up.
     */
    @Parameter(property = "clean", defaultValue = "false")
    private boolean clean;

    @Override
    public void execute() throws MojoExecutionException {
        super.execute();

        try {
            this.getRuntimeInstance().doStart(clean, verifyTimeout, serverStartTimeout);
        } catch (BoostException e) {
            throw new MojoExecutionException("Error starting server", e);
        }
    }

}
