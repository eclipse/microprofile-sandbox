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
package io.openliberty.boost.common.utils;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class DOMUtils {
    // Thanks to:
    // https://stackoverflow.com/questions/10689900/get-xml-only-immediate-children-elements-by-name
    public static List<Element> getDirectChildrenByTag(Element el, String sTagName) {
        List<Element> retVal = new ArrayList<Element>();
        NodeList descendants = el.getElementsByTagName(sTagName);
        for (int i = 0; i < descendants.getLength(); i++) {
            if (descendants.item(i).getParentNode().equals(el)) {
                retVal.add((Element) descendants.item(i));
            }
        }
        return retVal;
    }

}
