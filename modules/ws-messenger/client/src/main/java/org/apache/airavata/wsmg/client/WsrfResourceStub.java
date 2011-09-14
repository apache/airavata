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

package org.apache.airavata.wsmg.client;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.airavata.wsmg.client.util.DcDate;
import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.airavata.wsmg.commons.WsmgNameSpaceConstants;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code
 * Templates
 */
public class WsrfResourceStub {
    private final static Logger logger = LoggerFactory.getLogger(WsrfResourceStub.class);
    private final static OMFactory factory = OMAbstractFactory.getOMFactory();
    private final static SOAPFactory soapfactory = OMAbstractFactory.getSOAP11Factory();
    protected Options opts;

    private EndpointReference resourceEndpointReference;

    private long timeoutInMilliSeconds;

    protected WsrfResourceStub(EndpointReference resourceEpr, long timeout) {
        this.resourceEndpointReference = resourceEpr;
        logger.info("resourceEprInWsrfResourceStub Constructor" + resourceEpr.toString());

        timeoutInMilliSeconds = timeout;

        opts = new Options();
        opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        opts.setTo(resourceEpr);
        opts.setTimeOutInMilliSeconds(timeout);

    }

    public EndpointReference getResourceEpr() {
        return resourceEndpointReference;
    }

    public long getTimeoutInMilliSeconds() {
        return timeoutInMilliSeconds;
    }

    public void setTimeoutInMilliSeconds(long timeout) {
        timeoutInMilliSeconds = timeout;
    }

    public void destroy() throws AxisFault {
        String uuid = UUIDGenerator.getUUID();
        opts.setMessageId(uuid);
        OMElement message = factory.createOMElement("Destroy", WsmgNameSpaceConstants.WSRL_NS);
        opts.setAction(message.getNamespace().getNamespaceURI() + "/" + message.getLocalName());
        opts.setTimeOutInMilliSeconds(getTimeoutInMilliSeconds());

        ServiceClient client = new ServiceClient();

        if (client.getAxisConfiguration().getModule(WsmgCommonConstants.AXIS_MODULE_NAME_ADDRESSING) != null) {

            client.engageModule(WsmgCommonConstants.AXIS_MODULE_NAME_ADDRESSING);
        } else {
            SOAPHeaderBlock msgId = soapfactory.createSOAPHeaderBlock("MessageID", WsmgNameSpaceConstants.WSA_NS);
            msgId.setText(uuid);

            SOAPHeaderBlock to = soapfactory.createSOAPHeaderBlock("To", WsmgNameSpaceConstants.WSA_NS);
            to.setText(this.resourceEndpointReference.getAddress());

            SOAPHeaderBlock action = soapfactory.createSOAPHeaderBlock("Action", WsmgNameSpaceConstants.WSA_NS);
            action.setText(message.getNamespace().getNamespaceURI() + "/" + message.getLocalName());

            client.addHeader(action);
            client.addHeader(msgId);
            client.addHeader(to);
        }
        client.setOptions(opts);

        client.sendRobust(message);
    }

    public void setTerminationTime(Calendar cal) throws AxisFault {
        String uuid = UUIDGenerator.getUUID();
        opts.setMessageId(uuid);
        OMElement message = factory.createOMElement("SetTerminationTime", WsmgNameSpaceConstants.WSRL_NS);
        opts.setAction(message.getNamespace().getNamespaceURI() + "/" + message.getLocalName());

        opts.setTimeOutInMilliSeconds(getTimeoutInMilliSeconds());

        OMElement child = factory.createOMElement("RequestedTerminationTime", message.getNamespace(), message);

        if (cal == null) {
            OMNamespace XSI_NS = factory.createOMNamespace("http://www.w3.org/2001/XMLSchema", "xsd");
            child.addAttribute("nill", "true", XSI_NS);
            // (XmlConstants.XSI_NS, "nil", "true");
        } else {

            DcDate dcDate = new DcDate(cal);
            child.setText(dcDate.toString());
        }

        ServiceClient client = new ServiceClient();

        if (client.getAxisConfiguration().getModule(WsmgCommonConstants.AXIS_MODULE_NAME_ADDRESSING) != null) {
            client.engageModule(WsmgCommonConstants.AXIS_MODULE_NAME_ADDRESSING);
        } else {
            SOAPHeaderBlock msgId = soapfactory.createSOAPHeaderBlock("MessageID", WsmgNameSpaceConstants.WSA_NS);
            msgId.setText(uuid);

            SOAPHeaderBlock to = soapfactory.createSOAPHeaderBlock("To", WsmgNameSpaceConstants.WSA_NS);
            to.setText(this.resourceEndpointReference.getAddress());

            SOAPHeaderBlock action = soapfactory.createSOAPHeaderBlock("Action", WsmgNameSpaceConstants.WSA_NS);
            action.setText(message.getNamespace().getNamespaceURI() + "/" + message.getLocalName());

            client.addHeader(action);
            client.addHeader(msgId);
            client.addHeader(to);
        }
        client.setOptions(opts);
        client.sendRobust(message);

    }

    public List<OMElement> getResourceProperty(QName qn) throws AxisFault { // List<XmlElement>

        OMElement messageEl = factory.createOMElement("GetResourceProperty", WsmgNameSpaceConstants.WSRP_NS);
        String uuid = UUIDGenerator.getUUID();

        opts.setTimeOutInMilliSeconds(getTimeoutInMilliSeconds());
        opts.setMessageId(uuid);
        opts.setAction(messageEl.getNamespace().getNamespaceURI() + "/" + messageEl.getLocalName());

        QName textQName = new QName(qn.getNamespaceURI(), qn.getLocalPart(), qn.getPrefix());

        factory.createOMText(messageEl, textQName);

        if (qn.getPrefix() != null) {
            OMNamespace ns = factory.createOMNamespace(qn.getNamespaceURI(), qn.getPrefix());
            messageEl.declareNamespace(ns);
        }

        ServiceClient client = new ServiceClient();

        if (client.getAxisConfiguration().getModule(WsmgCommonConstants.AXIS_MODULE_NAME_ADDRESSING) != null) {

            client.engageModule(WsmgCommonConstants.AXIS_MODULE_NAME_ADDRESSING);
        } else {

            SOAPHeaderBlock msgId = soapfactory.createSOAPHeaderBlock("MessageID", WsmgNameSpaceConstants.WSA_NS);
            msgId.setText(uuid);

            SOAPHeaderBlock to = soapfactory.createSOAPHeaderBlock("To", WsmgNameSpaceConstants.WSA_NS);
            to.setText(this.resourceEndpointReference.getAddress());

            SOAPHeaderBlock action = soapfactory.createSOAPHeaderBlock("Action", WsmgNameSpaceConstants.WSA_NS);
            action.setText(messageEl.getNamespace().getNamespaceURI() + "/" + messageEl.getLocalName());

            client.addHeader(action);
            client.addHeader(msgId);
            client.addHeader(to);
        }
        client.setOptions(opts);
        OMElement responseMessage = client.sendReceive(messageEl);
        client.cleanupTransport();

        List<OMElement> list = elementsAsList(responseMessage);
        return list;
    }

    public List<OMElement> getMultipleResourceProperties(QName[] qnamez) throws AxisFault { // TODO

        OMElement messageEl = factory.createOMElement("GetMultipleResourceProperties", WsmgNameSpaceConstants.WSRP_NS);
        String uuid = UUIDGenerator.getUUID();
        opts.setMessageId(uuid);
        opts.setAction(messageEl.getNamespace().getNamespaceURI() + "/" + messageEl.getLocalName());
        opts.setTimeOutInMilliSeconds(getTimeoutInMilliSeconds());

        // message.addChild(new QNameElText(message, WidgetService.WIDGET_NS,
        // "TerminationTime"));
        // message.declareNamespace(WidgetService.WIDGET_NS);
        // wsrp:ResourceProperty

        for (QName qn : qnamez) {

            OMNamespace ns = factory.createOMNamespace(qn.getNamespaceURI(), qn.getPrefix());

            OMElement child = factory.createOMElement("ResourceProperty", WsmgNameSpaceConstants.WSRP_NS);

            QName textQName = new QName(qn.getNamespaceURI(), qn.getLocalPart(), qn.getPrefix());

            factory.createOMText(child, textQName);

            if (qn.getPrefix() != null) {
                messageEl.declareNamespace(ns);
            }

        }

        ServiceClient client = new ServiceClient();

        if (client.getAxisConfiguration().getModule(WsmgCommonConstants.AXIS_MODULE_NAME_ADDRESSING) != null) {

            client.engageModule(WsmgCommonConstants.AXIS_MODULE_NAME_ADDRESSING);
        } else {
            SOAPHeaderBlock msgId = soapfactory.createSOAPHeaderBlock("MessageID", WsmgNameSpaceConstants.WSA_NS);
            msgId.setText(uuid);

            SOAPHeaderBlock to = soapfactory.createSOAPHeaderBlock("To", WsmgNameSpaceConstants.WSA_NS);
            to.setText(this.resourceEndpointReference.getAddress());

            SOAPHeaderBlock action = soapfactory.createSOAPHeaderBlock("Action", WsmgNameSpaceConstants.WSA_NS);
            action.setText(messageEl.getNamespace().getNamespaceURI() + "/" + messageEl.getLocalName());

            client.addHeader(action);
            client.addHeader(msgId);
            client.addHeader(to);
        }
        client.setOptions(opts);
        OMElement responseMessage = client.sendReceive(messageEl);
        client.cleanupTransport();

        List<OMElement> list = elementsAsList(responseMessage);
        return list;
    }

    public List<OMNode> queryResourcePropertiesByXpath(String query) throws AxisFault {
        if (query == null) {
            throw new IllegalArgumentException();
        }
        String uuid = UUIDGenerator.getUUID();
        opts.setMessageId(uuid);
        opts.setTimeOutInMilliSeconds(getTimeoutInMilliSeconds());
        OMElement messageEl = factory.createOMElement("QueryResourceProperties", WsmgNameSpaceConstants.WSRP_NS);

        opts.setAction(messageEl.getNamespace().getNamespaceURI() + "/" + messageEl.getLocalName());

        OMElement queryExpressionEl = factory.createOMElement("QueryExpression", WsmgNameSpaceConstants.WSRP_NS);

        queryExpressionEl.addAttribute("dialect", WsmgCommonConstants.XPATH_DIALECT, null);

        queryExpressionEl.setText(query);

        ServiceClient client = new ServiceClient();

        if (client.getAxisConfiguration().getModule(WsmgCommonConstants.AXIS_MODULE_NAME_ADDRESSING) != null) {
            client.engageModule(WsmgCommonConstants.AXIS_MODULE_NAME_ADDRESSING);
        } else {

            SOAPHeaderBlock msgId = soapfactory.createSOAPHeaderBlock("MessageID", WsmgNameSpaceConstants.WSA_NS);
            msgId.setText(uuid);

            SOAPHeaderBlock to = soapfactory.createSOAPHeaderBlock("To", WsmgNameSpaceConstants.WSA_NS);
            to.setText(this.resourceEndpointReference.getAddress());

            SOAPHeaderBlock action = soapfactory.createSOAPHeaderBlock("Action", WsmgNameSpaceConstants.WSA_NS);
            action.setText(messageEl.getNamespace().getNamespaceURI() + "/" + messageEl.getLocalName());

            client.addHeader(action);
            client.addHeader(msgId);
            client.addHeader(to);
        }
        client.setOptions(opts);
        OMElement responseMessage = client.sendReceive(messageEl);
        client.cleanupTransport();
        List<OMNode> list = childrenAsList(responseMessage);
        return list;
    }

    public void setResourceProperties(OMElement[] requests) throws AxisFault {
        if (requests.length == 0) {
            throw new IllegalArgumentException("at least one request is required");
        }

        OMElement messageEl = factory.createOMElement("SetResourceProperties", WsmgNameSpaceConstants.WSRP_NS);

        String uuid = UUIDGenerator.getUUID();
        opts.setMessageId(uuid);
        opts.setTimeOutInMilliSeconds(getTimeoutInMilliSeconds());
        opts.setAction(messageEl.getNamespace().getNamespaceURI() + "/" + messageEl.getLocalName());

        for (int i = 0; i < requests.length; i++) {
            messageEl.addChild(requests[i]);
        }
        // message.addChild(new QNameElText(message, WidgetService.WIDGET_NS,
        // "TerminationTime"));
        // message.declareNamespace(WidgetService.WIDGET_NS);
        // wsrp:ResourceProperty
        // message.declareNamespace(WidgetService.WSRL_NS);

        ServiceClient client = new ServiceClient();

        if (client.getAxisConfiguration().getModule(WsmgCommonConstants.AXIS_MODULE_NAME_ADDRESSING) != null) {

            client.engageModule(WsmgCommonConstants.AXIS_MODULE_NAME_ADDRESSING);
        } else {
            SOAPHeaderBlock msgId = soapfactory.createSOAPHeaderBlock("MessageID", WsmgNameSpaceConstants.WSA_NS);
            msgId.setText(uuid);

            SOAPHeaderBlock to = soapfactory.createSOAPHeaderBlock("To", WsmgNameSpaceConstants.WSA_NS);
            to.setText(this.resourceEndpointReference.getAddress());

            SOAPHeaderBlock action = soapfactory.createSOAPHeaderBlock("Action", WsmgNameSpaceConstants.WSA_NS);
            action.setText(messageEl.getNamespace().getNamespaceURI() + "/" + messageEl.getLocalName());

            client.addHeader(action);
            client.addHeader(msgId);
            client.addHeader(to);
        }
        client.setOptions(opts);
        client.sendRobust(messageEl);

    }

    private List<OMElement> elementsAsList(OMElement responseMessage) {
        List<OMElement> list = new ArrayList<OMElement>();

        for (Iterator it = responseMessage.getChildElements(); it.hasNext();) {
            OMElement current = (OMElement) it.next();
            list.add(current);
        }

        return list;
    }

    private List<OMNode> childrenAsList(OMElement responseMessage) {
        List<OMNode> list = new ArrayList<OMNode>();

        for (Iterator it = responseMessage.getChildren(); it.hasNext();) {
            OMNode child = (OMNode) it.next();
            list.add(child);
        }
        return list;
    }

    public static void verbose(String msg) {
        System.err.println(msg);

    }
}
