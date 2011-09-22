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
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;

public class WseCreateSubscription {
    private final OMFactory factory = OMAbstractFactory.getOMFactory();
    private final static SOAPFactory soapfactory = OMAbstractFactory.getSOAP11Factory();
    protected EndpointReference brokerEndPointReference;

    protected long timeoutInMilliSeconds;

    public WseCreateSubscription(EndpointReference brokerLocationEPR, long timeout) {
        this.brokerEndPointReference = brokerLocationEPR;
        timeoutInMilliSeconds = timeout;
    }

    public long getTimeoutInMilliSeconds() {
        return timeoutInMilliSeconds;
    }

    public void setTimeoutInMilliSeconds(long timeout) {
        timeoutInMilliSeconds = timeout;
    }

    public OMElement subscribe(EndpointReference eventSinkReference, OMElement filterEl, boolean useNotfy)
            throws AxisFault {
        return subscribe(eventSinkReference, filterEl, useNotfy, ClientUtil.EXPIRE_TIME);
    }

    public OMElement subscribe(EndpointReference eventSinkReferenceEPR, OMElement filterEl, boolean useNotfy,
            long expireTime) throws AxisFault {

        OMElement message = createMessageEl(eventSinkReferenceEPR, filterEl, expireTime);

        ServiceClient serviceClient = createServiceClient(message);
        OMElement responseMessage = serviceClient.sendReceive(message);
        serviceClient.cleanupTransport();
        if (responseMessage == null) {
            throw AxisFault.makeFault(new RuntimeException("no response recieved for subscription message"));
        }
        OMElement responseSubscriptionsManagerElement = responseMessage.getFirstChildWithName(new QName(
                NameSpaceConstants.WSE_NS.getNamespaceURI(), "SubscriptionManager"));
        return responseSubscriptionsManagerElement;
    }

    private ServiceClient createServiceClient(OMElement message) throws AxisFault {
        Options opts = new Options();
        opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        opts.setTo(brokerEndPointReference);
        opts.setAction(message.getNamespace().getNamespaceURI() + "/" + message.getLocalName());

        opts.setTimeOutInMilliSeconds(getTimeoutInMilliSeconds());
        opts.setMessageId(UUIDGenerator.getUUID());

        ServiceClient client = new ServiceClient();

        if (client.getAxisConfiguration().getModule(WsmgCommonConstants.AXIS_MODULE_NAME_ADDRESSING) != null) {

            client.engageModule(WsmgCommonConstants.AXIS_MODULE_NAME_ADDRESSING);
        } else {
            SOAPHeaderBlock msgId = soapfactory.createSOAPHeaderBlock("MessageID", NameSpaceConstants.WSA_NS);
            msgId.setText(UUIDGenerator.getUUID());

            SOAPHeaderBlock to = soapfactory.createSOAPHeaderBlock("To", NameSpaceConstants.WSA_NS);
            to.setText(opts.getTo().getAddress());

            SOAPHeaderBlock action = soapfactory.createSOAPHeaderBlock("Action", NameSpaceConstants.WSA_NS);
            action.setText(message.getNamespace().getNamespaceURI() + "/" + message.getLocalName());

            client.addHeader(action);
            client.addHeader(msgId);
            client.addHeader(to);
        }
        client.setOptions(opts);

        return client;
    }

    private OMElement createMessageEl(EndpointReference eventSinkReferenceEPR, OMElement filterEl, long expireTime)
            throws AxisFault {

        OMElement message = factory.createOMElement("Subscribe", NameSpaceConstants.WSE_NS);

        OMElement delivery = factory.createOMElement("Delivery", NameSpaceConstants.WSE_NS);

        OMElement expires = factory.createOMElement("Expires", NameSpaceConstants.WSE_NS);
        expires.setText(Long.toString(expireTime));
        message.addChild(expires);

        OMElement notifyTo = EndpointReferenceHelper.toOM(factory, eventSinkReferenceEPR, new QName(
                NameSpaceConstants.WSE_NS.getNamespaceURI(), "NotifyTo"), NameSpaceConstants.WSA_NS
                .getNamespaceURI());

        delivery.addChild(notifyTo);
        message.addChild(delivery);

        if (filterEl != null) {
            message.addChild(filterEl);
        }

        message.declareNamespace(NameSpaceConstants.WSA_NS);

        return message;
    }

}
