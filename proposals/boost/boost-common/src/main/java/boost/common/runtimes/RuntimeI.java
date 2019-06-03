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
package boost.common.runtimes;

import boost.common.BoostException;

public abstract interface RuntimeI {
    
    public void doPackage() throws BoostException;
    
    public void doDebug(boolean clean) throws BoostException;
    
    public void doRun(boolean clean) throws BoostException;
    
    public void doStart(boolean clean, int verifyTimeout, int serverStartTimeout) throws BoostException;
    
    public void doStop() throws BoostException;

}
