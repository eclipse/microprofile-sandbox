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

package boost.common;

import net.wasdev.wlp.common.plugins.util.PluginExecutionException;

public class BoostException extends PluginExecutionException {

    private static final long serialVersionUID = 1L;

    public BoostException(String message) {
        super(message);
    }

    public BoostException(String message, Throwable e) {
        super(message, e);
    }

    public BoostException(Throwable e) {
        super(e);
    }

}
