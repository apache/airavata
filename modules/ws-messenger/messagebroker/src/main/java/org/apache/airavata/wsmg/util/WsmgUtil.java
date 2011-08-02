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

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.xml.namespace.QName;

import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;

public class WsmgUtil {
    private static final String BODY = "Body";

    private static final String ACTION = "Action";

    private static final String MESSAGE_ID = "MessageID";

    private static final String XMLNS_WIDGET = "xmlns:widget";

    private static final String DIALECT = "Dialect";

    private static final String TOPIC = "Topic";

    private static final String TO = "To";

    private static final String HEADER = "Header";

    private static final String ENVELOPE = "Envelope";

    private static final String WSNT = "wsnt";

    private static final String XSI = "xsi";

    private static final String WA48 = "wa48";

    private static final String S = "S";

    private static final String HTTP_WIDGETS_COM = "http://widgets.com";

    private static final String HTTP_WWW_IBM_COM_XMLNS_STDWIP_WEB_SERVICES_WS_TOPICS_TOPIC_EXPRESSION_SIMPLE = "http://www.ibm.com/xmlns/stdwip/web-services/WS-Topics/TopicExpression/simple";

    private static final String HTTP_WWW_IBM_COM_XMLNS_STDWIP_WEB_SERVICES_WS_BASE_NOTIFICATION = "http://www.ibm.com/xmlns/stdwip/web-services/WS-BaseNotification";

    private static final String HTTP_WWW_W3_ORG_2001_XMLSCHEMA_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance";

    private static final String HTTP_SCHEMAS_XMLSOAP_ORG_WS_2004_08_ADDRESSING = "http://schemas.xmlsoap.org/ws/2004/08/addressing";

    private static final String HTTP_SCHEMAS_XMLSOAP_ORG_SOAP_ENVELOPE = "http://schemas.xmlsoap.org/soap/envelope/";

    public static String formatURLString(String url) {

        if (url == null) {
            throw new IllegalArgumentException("url can't be null");
        }

        if (url.indexOf("//") < 0) {
            url = "http://" + url; // use default http
        }
        return url;
    }

    public static boolean sameStringValue(String stringA, String stringB) {
        if (stringA == null) {
            if (stringB == null) {
                return true;
            }
            return false;

        }
        // StringA!=null
        if (stringB == null)
            return false;
        if (stringA.compareTo(stringB) == 0) {
            return true;
        }
        return false;

    }

    public static String getHostIP() {
        InetAddress localAddress = null;
        try {
            localAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException ex) {
            System.out.println("Error - unable to resolve localhost");
        }
        // Use IP address since DNS entry cannot update the laptop's entry
        // promptly
        String hostIP = localAddress.getHostAddress();
        return hostIP;
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
