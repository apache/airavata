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

package org.apache.airavata.wsmg.commons;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;

public abstract class WsmgNameSpaceConstants {

    public final static String XHTML_NS = null;

    public final static String RESOURCE_ID = "ResourceID";

    public final static OMFactory factory = OMAbstractFactory.getOMFactory();

    public final static OMNamespace WSMG_NS = factory.createOMNamespace(
            "http://www.collab-ogce.org/ogce/index.php/Messaging", "org.apache.airavata.wsmg");

    public final static OMNamespace WIDGET_NS = factory.createOMNamespace("http://widgets.com", "widget");

    public final static OMNamespace NS_2004_08 = factory.createOMNamespace(
            "http://schemas.xmlsoap.org/ws/2004/08/addressing", "wa48");

    public final static OMNamespace WSRL_NS = factory.createOMNamespace(
            "http://www.ibm.com/xmlns/stdwip/web-services/WS-ResourceLifetime", "wsrl");

    public final static OMNamespace WSRP_NS = factory.createOMNamespace(
            "http://www.ibm.com/xmlns/stdwip/web-services/WS-ResourceProperties", "wsrp");
    // "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceProperties-1.2-draft-01.xsd");

    public final static OMNamespace WSNT_NS = factory.createOMNamespace(
            "http://www.ibm.com/xmlns/stdwip/web-services/WS-BaseNotification", "wsnt");
    // "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BaseNotification-1.2-draft-01.xsd");

    public final static OMNamespace WSBR_NS = factory.createOMNamespace(
            "http://www.ibm.com/xmlns/stdwip/web-services/WS-BrokeredNotification", "wsbn");

    public final static OMNamespace WSA_NS = factory.createOMNamespace("http://www.w3.org/2005/08/addressing", "wsa");
    // "http://schemas.xmlsoap.org/ws/2003/03/addressing");
    // "http://schemas.xmlsoap.org/ws/2004/03/addressing");

    public final static OMNamespace WSE_NS = factory.createOMNamespace(
            "http://schemas.xmlsoap.org/ws/2004/08/eventing", "wse");

    public final static OMNamespace SOAP_NS = factory.createOMNamespace("http://schemas.xmlsoap.org/soap/envelope/",
            "s");

    public final static OMNamespace WSE = factory.createOMNamespace("http://schemas.xmlsoap.org/ws/2004/08/eventing",
            "wse");

    public final static OMNamespace WSA_2004_NS = factory.createOMNamespace(
            "http://schemas.xmlsoap.org/ws/2004/08/addressing", "wsa");

}
