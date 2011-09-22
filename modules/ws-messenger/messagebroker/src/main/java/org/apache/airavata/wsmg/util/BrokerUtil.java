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

package org.apache.airavata.wsmg.util;

import javax.xml.namespace.QName;

import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;

public class BrokerUtil {

    /**
     * Compares String {@code x} with String {@code y}. The result is {@code true} if and only
     * if both arguments are {@code null} or String {@code x} has the same sequence of
     * characters as String {@code y}.
     * 
     * @param x
     * @param y
     * @return {@code true} if the String {@code x} and String {@code y} are
     *          equivalent, {@code false} otherwise
     */
    public static boolean sameStringValue(String x, String y) {
        return (x == null && y == null) || (x != null && y != null && x.equals(y));
    }

    public static String getTopicLocalString(String filterText) {

        if (filterText == null)
            throw new IllegalArgumentException("filter text can't be null");

        String localName = null;

        int pos = filterText.indexOf(':');

        if (pos != -1) {
            localName = filterText.substring(pos + 1);

        } else {

            localName = filterText;
        }

        return localName;
    }

    /**
     * 
     * @return localString
     * @throws AxisFault
     */
    public static String getXPathString(OMElement xpathEl) throws AxisFault {

        if (xpathEl == null) {
            throw new IllegalArgumentException("xpath element can't be null");
        }

        OMAttribute dialectAttribute = xpathEl.getAttribute(new QName("Dialect"));

        if (dialectAttribute == null) {
            dialectAttribute = xpathEl.getAttribute(new QName("DIALECT"));

        }
        if (dialectAttribute == null) {
            throw new AxisFault("dialect is required for subscribe");
        }
        String dialectString = dialectAttribute.getAttributeValue();
        if (!dialectString.equals(WsmgCommonConstants.XPATH_DIALECT)) {
            // System.out.println("***Unkown dialect: " + dialectString);
            throw new AxisFault("Unkown dialect: " + dialectString);
        }
        String xpathLocalString = xpathEl.getText();
        return xpathLocalString;
    }

    public static String getTopicFromRequestPath(String topicPath) {
        if (topicPath == null)
            return null;
        if (topicPath.length() == 0)
            return null;
        if (topicPath.startsWith("/")) {
            topicPath = topicPath.substring(1);
            if (topicPath.length() == 0)
                return null;
        }

        String ret = null;

        int index = topicPath.indexOf(WsmgCommonConstants.TOPIC_PREFIX);
        if (index >= 0) {

            ret = topicPath.substring(index + WsmgCommonConstants.TOPIC_PREFIX.length());

            if (ret.length() == 0) {
                ret = null;
            }

        }

        return ret;
    }

}
