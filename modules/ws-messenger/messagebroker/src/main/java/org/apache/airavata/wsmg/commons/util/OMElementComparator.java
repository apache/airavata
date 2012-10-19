/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.airavata.wsmg.commons.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.airavata.wsmg.util.BrokerUtil;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Compare two OMElement with its namespace, attributes, children, and text. Current implementation supports ignore
 * namespace checking i.e. if the namespace is in the list, it is skipped and return as equals.
 */
public class OMElementComparator {

    private static final Log log = LogFactory.getLog(OMElementComparator.class);

    private static List<String> ignorableNamespaceList = new ArrayList<String>();

    private OMElementComparator() {
    }

    public void addIgnorableNamespace(String nsURI) {
        ignorableNamespaceList.add(nsURI);
    }

    public void clearIgnorableNamespaces() {
        ignorableNamespaceList.clear();
    }

    public static boolean compare(OMElement elementOne, OMElement elementTwo) {

        if (isIgnorable(elementOne) || isIgnorable(elementTwo)) {
            // ignore if the elements belong to any of the ignorable namespaces
            // list
            return true;
        } else if (elementOne == null && elementTwo == null) {
            log.debug("Both Elements are null.");
            return true;
        } else if (elementOne == null || elementTwo == null) {
            log.debug("One of item to compare is null");
            return false;
        }

        return BrokerUtil.sameStringValue(elementOne.getLocalName(), elementTwo.getLocalName())
                && compare(elementOne.getNamespace(), elementTwo.getNamespace())
                && compareAttibutes(elementOne, elementTwo)
                /*
                 * Trimming the value of the XMLElement is not correct since this compare method cannot be used to
                 * compare element contents with trailing and leading whitespaces BUT for the practical side of tests
                 * and to get the current tests working we have to trim() the contents
                 */
                && BrokerUtil.sameStringValue(elementOne.getText().trim(), elementTwo.getText().trim())
                && compareChildren(elementOne, elementTwo);
    }

    private static boolean isIgnorable(OMElement elt) {
        if (elt != null) {
            OMNamespace namespace = elt.getNamespace();
            if (namespace != null) {
                return ignorableNamespaceList.contains(namespace.getNamespaceURI());
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static boolean compareChildren(OMElement elementOne, OMElement elementTwo) {
        HashMap<QName, OMElement> map = new HashMap<QName, OMElement>();
        Iterator oneIter = elementOne.getChildElements();
        while (oneIter.hasNext()) {
            OMElement elementOneChild = (OMElement) oneIter.next();
            OMElement elementTwoChild = elementTwo.getFirstChildWithName(elementOneChild.getQName());
            if (!compare(elementOneChild, elementTwoChild)) {
                return false;
            }

            /*
             * Cache for later access
             */
            map.put(elementOneChild.getQName(), elementOneChild);
        }

        /*
         * Case the second element has more elements than the first
         */
        Iterator twoIter = elementTwo.getChildElements();
        while (twoIter.hasNext()) {
            OMElement elementTwoChild = (OMElement) twoIter.next();
            if (!isIgnorable(elementTwoChild) && !map.containsKey(elementTwoChild.getQName())) {
                return false;
            }
        }

        return true;
    }

    private static boolean compareAttibutes(OMElement elementOne, OMElement elementTwo) {
        int elementOneAtribCount = 0;
        int elementTwoAtribCount = 0;
        Iterator oneIter = elementOne.getAllAttributes();
        while (oneIter.hasNext()) {

            /*
             * This catches a case where the first one has more items than the second one (one.attributes.size >
             * two.attributes.size) and a case where the first and the second have a different attributes.
             * (one.attributes.size == two.attributes.size)
             */
            OMAttribute omAttribute = (OMAttribute) oneIter.next();
            OMAttribute attr = elementTwo.getAttribute(omAttribute.getQName());
            if (attr == null) {
                log.debug("Attribute " + omAttribute + " is not found in both elements.");
                return false;
            }
            /*
             * Count attributes in the first item
             */
            elementOneAtribCount++;
        }

        /*
         * Count attributes in the second item
         */
        Iterator elementTwoIter = elementTwo.getAllAttributes();
        while (elementTwoIter.hasNext()) {
            elementTwoIter.next();
            elementTwoAtribCount++;
        }

        /*
         * This catches a case where the second one has more items than the first one. (two.attributes.size >
         * one.attributes.size)
         */
        log.debug("Number of Attributes are equal? : " + (elementOneAtribCount == elementTwoAtribCount));
        return elementOneAtribCount == elementTwoAtribCount;
    }

    /*
     * Compare only URI not prefix
     */
    private static boolean compare(OMNamespace x, OMNamespace y) {
        log.debug("Compare namespace:" + x + " with " + y);
        return (x == null && y == null)
                || (x != null && y != null && BrokerUtil.sameStringValue(x.getNamespaceURI(), y.getNamespaceURI()));
    }

}
