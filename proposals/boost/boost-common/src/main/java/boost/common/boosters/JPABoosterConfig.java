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
package boost.common.boosters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import boost.common.BoostException;
import boost.common.BoostLoggerI;
import boost.common.boosters.AbstractBoosterConfig.BoosterCoordinates;

@BoosterCoordinates(AbstractBoosterConfig.BOOSTERS_GROUP_ID + ":jpa")
public class JPABoosterConfig extends AbstractBoosterConfig {

    public JPABoosterConfig(Map<String, String> dependencies, BoostLoggerI logger) throws BoostException {
        super(dependencies.get(getCoordinates(JPABoosterConfig.class)));
    }

    @Override
    public List<String> getDependencies() {
        return new ArrayList<String>();
    }
}
