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

public abstract class NameSpaceConstants {

    public final static OMFactory factory = OMAbstractFactory.getOMFactory();  

    public final static OMNamespace WSMG_NS = factory.createOMNamespace(
            "http://org.apache.airavata/ws-messenger/wsmg/2011", "wsmg");
    
    public final static OMNamespace MSG_BOX = factory.createOMNamespace(
            "http://org.apache.airavata/ws-messenger/msgbox/2011", "msgbox");

    public final static OMNamespace WIDGET_NS = factory.createOMNamespace("http://widgets.com", "widget");
    
    /**
     * WS Addressing
     */
    public final static OMNamespace WSA_NS = factory.createOMNamespace("http://www.w3.org/2005/08/addressing", "wsa");

    /**
     * WS Notification
     * 
     * https://www.ibm.com/developerworks/library/specification/ws-notification/
     * 
     */
    public final static OMNamespace WSNT_NS = factory.createOMNamespace(
            "http://www.ibm.com/xmlns/stdwip/web-services/WS-BaseNotification", "wsnt");
    
    public final static OMNamespace WSBR_NS = factory.createOMNamespace(
            "http://www.ibm.com/xmlns/stdwip/web-services/WS-BrokeredNotification", "wsbn");    
    
    /**
     * WS Resource specification namespace
     * 
     * http://www.ibm.com/developerworks/webservices/library/specification/ws-resource/
     * 
     */
    public final static OMNamespace WSRP_NS = factory.createOMNamespace(
            "http://www.ibm.com/xmlns/stdwip/web-services/WS-ResourceProperties", "wsrp");
    
    public final static OMNamespace WSRL_NS = factory.createOMNamespace(
            "http://www.ibm.com/xmlns/stdwip/web-services/WS-ResourceLifetime", "wsrl");
    

    /**
     * Web Services Eventing (WS-Eventing)
     * 
     * http://schemas.xmlsoap.org/ws/2004/08/eventing/
     */
    public final static OMNamespace WSE_NS = factory.createOMNamespace(
            "http://schemas.xmlsoap.org/ws/2004/08/eventing", "wse");

}
