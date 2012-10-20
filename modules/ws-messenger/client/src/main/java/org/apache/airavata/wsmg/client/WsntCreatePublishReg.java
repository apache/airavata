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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code
 * Templates
 */
public class WsntCreatePublishReg extends WidgetStub {
    private static final Logger logger = LoggerFactory.getLogger(WseCreateSubscription.class);
    private OMFactory factory = OMAbstractFactory.getOMFactory();
    private final static SOAPFactory soapfactory = OMAbstractFactory.getSOAP11Factory();

    // private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;

    public WsntCreatePublishReg(EndpointReference widgetEpr, long timeout) {
        super(widgetEpr, timeout);
    }

    public WsrfResourceStub registerPublisher(EndpointReference omEndpointReference) throws AxisFault {

        // invoker.setMessageId(null);
        OMElement message = factory.createOMElement(new QName(NameSpaceConstants.WSBR_NS.getNamespaceURI(),
                "RegisterPublisher"));
        // builder.newFragment(WsmgConstants.WSBR_NS,
        // "RegisterPublisher");

        // EndpointReference publisherRef;

        OMElement publisherRef = null;

        publisherRef = EndpointReferenceHelper.toOM(factory, omEndpointReference,
                new QName(NameSpaceConstants.WSBR_NS.getNamespaceURI(), "PublisherReference"),
                NameSpaceConstants.WSA_NS.getNamespaceURI());

        message.addChild(publisherRef);

        ServiceClient client = createServiceClient(message);

        OMElement responseMessage = client.sendReceive(message);
        client.cleanup();
        client.cleanupTransport();

        OMElement publisherRegistrationRef = responseMessage.getFirstChildWithName(new QName(NameSpaceConstants.WSBR_NS
                .getNamespaceURI(), "PublisherRegistrationReference"));

        EndpointReference epr = EndpointReferenceHelper.fromOM(publisherRegistrationRef);
        return new WsrfResourceStub(epr, getTimeoutInMilliSeconds());
    }

    private ServiceClient createServiceClient(OMElement message) throws AxisFault {
        String uuid = UUIDGenerator.getUUID();
        opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        opts.setMessageId(uuid);
        opts.setAction(message.getNamespace().getNamespaceURI() + "/" + message.getLocalName());
        opts.setTimeOutInMilliSeconds(getTimeoutInMilliSeconds());

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
        client.setOptions(opts);
        return client;
    }

}
