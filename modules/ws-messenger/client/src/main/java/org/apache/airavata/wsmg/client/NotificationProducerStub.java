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

import javax.xml.namespace.QName;

import org.apache.airavata.wsmg.client.util.ClientUtil;
import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.airavata.wsmg.commons.NameSpaceConstants;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.axis2.client.ServiceClient;

/**
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
/**
 * This stub is NOT multi-thread safe!
 */
public class NotificationProducerStub extends WidgetStub {
    private static final String RMASSERTION = "RMAssertion";

    private static final String WSRM_NAMESPACE = "http://schemas.xmlsoap.org/ws/2005/02/rm";

    private static final String WSRM = "wsrm";

    private final static OMFactory factory = OMAbstractFactory.getOMFactory();
    private final static SOAPFactory soapfactory = OMAbstractFactory.getSOAP11Factory();

    public NotificationProducerStub(EndpointReference widgetEpr, long timeout) {
        super(widgetEpr, timeout);
    }

    public OMElement getCurrentMessage(OMElement topicExp) throws AxisFault {

        OMElement message = factory.createOMElement("GetCurrentMessage", NameSpaceConstants.WSNT_NS);
        message.addChild(topicExp);

        ServiceClient client = createServiceClient(message);

        OMElement responseMessage = client.sendReceive(message);
        client.cleanupTransport();

        if (responseMessage.getFirstElement() == null) {
            return null;
        }
        return (OMElement) responseMessage.getChildren().next();
    }

    private ServiceClient createServiceClient(OMElement message) throws AxisFault {

        String uuid = UUIDGenerator.getUUID();
        ServiceClient client = new ServiceClient();

        if (client.getAxisConfiguration().getModule(WsmgCommonConstants.AXIS_MODULE_NAME_ADDRESSING) != null) {
            client.engageModule(WsmgCommonConstants.AXIS_MODULE_NAME_ADDRESSING);
        } else {
            SOAPHeaderBlock msgId = soapfactory.createSOAPHeaderBlock("MessageID", NameSpaceConstants.WSA_NS);
            msgId.setText(uuid);
            SOAPHeaderBlock to = soapfactory.createSOAPHeaderBlock("To", NameSpaceConstants.WSA_NS);
            to.setText(opts.getTo().getAddress());

            SOAPHeaderBlock action = soapfactory.createSOAPHeaderBlock("Action", NameSpaceConstants.WSA_NS);
            action.setText(message.getNamespace().getNamespaceURI() + "/" + message.getLocalName());

            client.addHeader(action);
            client.addHeader(msgId);
            client.addHeader(to);
        }

        opts.setMessageId(uuid);
        opts.setAction(message.getNamespace().getNamespaceURI() + "/" + message.getLocalName());

        opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        opts.setTimeOutInMilliSeconds(getTimeoutInMilliSeconds());
        client.setOptions(opts);

        return client;
    }

    public SubscriptionStub subscribe(EndpointReference consumerReference, OMElement topicExpression, boolean useNotfy)
            throws AxisFault {
        return this.subscribe(consumerReference, topicExpression, null, useNotfy, false);

    }

    public SubscriptionStub subscribe(EndpointReference consumerReference, OMElement topicExpression,
            OMElement xpathExpression, boolean useNotfy) throws AxisFault {
        return this.subscribe(consumerReference, topicExpression, xpathExpression, useNotfy, false);

    }

    public SubscriptionStub subscribe(EndpointReference consumerReference, OMElement topicExpression, boolean useNotfy,
            boolean wsrm) throws AxisFault {
        return subscribe(consumerReference, topicExpression, null, useNotfy, wsrm);
    }

    public SubscriptionStub subscribe(EndpointReference consumerReference, OMElement topicExpression,
            OMElement xpathExpression, boolean useNotfy, boolean wsrm) throws AxisFault {

        OMElement message = factory.createOMElement("SubscribeRequest", NameSpaceConstants.WSNT_NS);

        message.declareNamespace(NameSpaceConstants.WSNT_NS);

        OMElement eprCrEl = EndpointReferenceHelper.toOM(factory, consumerReference, new QName("ConsumerReference"),
                NameSpaceConstants.WSA_NS.getNamespaceURI());

        message.addChild(eprCrEl);
        eprCrEl.setNamespace(message.getNamespace());

        if (topicExpression != null) {
            message.addChild(topicExpression);
            topicExpression.setNamespace(message.getNamespace());
        }
        if (xpathExpression != null) {
            message.addChild(xpathExpression);
            xpathExpression.setNamespace(message.getNamespace());
        }
        OMElement useNotifyEl = factory.createOMElement("UseNotify", message.getNamespace(), message);

        useNotifyEl.setText(useNotfy ? "true" : "false");

        if (wsrm) {

            setPolicyAttachment(message);
        }

        ServiceClient client = createServiceClient(message);

        OMElement responseMessage = client.sendReceive(message);
        client.cleanupTransport();

        OMElement sr = responseMessage.getFirstChildWithName(new QName(
                NameSpaceConstants.WSNT_NS.getNamespaceURI(), "SubscriptionReference"));

        if (sr == null) {
            throw new AxisFault("unable to subscribe, invalid response returned by broker");
        }

        return new SubscriptionStub(EndpointReferenceHelper.fromOM(sr), getTimeoutInMilliSeconds());
    }

    public static void verbose(String msg) {

        System.err.println(msg);
    }

    protected void setPolicyAttachment(OMElement message) {

        OMElement policy = factory.createOMElement(WsmgCommonConstants.SUBSCRIPTION_POLICY, message.getNamespace(),
                message);

        // builder.newFragment(WsmgConstants.WSNT_NS,
        // WsmgCommonConstants.SUBSCRIPTION_POLICY);
        OMElement childEl = factory.createOMElement(new QName(WSRM_NAMESPACE, WSRM));
        // XmlNamespace wsrmNamespace = builder.newNamespace(WSRM,
        // WSRM_NAMESPACE);
        policy.addChild(childEl);

    }

}
