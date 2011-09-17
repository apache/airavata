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

package org.apache.airavata.wsmg.messenger.protocol;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.airavata.wsmg.broker.AdditionalMessageContent;
import org.apache.airavata.wsmg.broker.ConsumerInfo;
import org.apache.airavata.wsmg.commons.CommonRoutines;
import org.apache.airavata.wsmg.commons.WsmgNameSpaceConstants;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.ElementHelper;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Axis2Protocol implements DeliveryProtocol {

    private static final Logger logger = LoggerFactory.getLogger(Axis2Protocol.class);

    SOAPFactory soapfactory = OMAbstractFactory.getSOAP11Factory();

    ServiceClient nonThreadLocalServiceClient = null;

    long tcpConnectionTimeout = 0;

    public void setTimeout(long timeout) {
        this.tcpConnectionTimeout = timeout;
    }

    public void deliver(ConsumerInfo consumerInfo, OMElement message, AdditionalMessageContent additionalMessageContent)
            throws SendingException {
        EndpointReference consumerReference = new EndpointReference(consumerInfo.getConsumerEprStr());

        /*
         * Extract information
         */
        String actionString = null;
        List<OMElement> soapHeaders = new LinkedList<OMElement>();
        if (consumerInfo.getType().compareTo("wsnt") == 0) {
            actionString = WsmgNameSpaceConstants.WSNT_NS.getNamespaceURI() + "/Notify";
        } else { // wse
            actionString = additionalMessageContent.getAction();
            String topicElString = additionalMessageContent.getTopicElement();
            if (topicElString != null) {
                OMElement topicEl = null;
                try {
                    topicEl = CommonRoutines.reader2OMElement(new StringReader(topicElString));
                    soapHeaders.add(topicEl);
                } catch (XMLStreamException e) {
                    logger.error("exception at topicEl xmlStreamException", e);
                }
            }
        }

        try {

            ServiceClient client = configureServiceClient(actionString, consumerReference,
                    additionalMessageContent.getMessageID(), soapHeaders);

            client.sendRobust(message);
            client.cleanupTransport();

        } catch (AxisFault ex) {
            throw new SendingException(ex.getCause());
        }
    }

    private ServiceClient getServiceClient() throws AxisFault {

        ServiceClient ret = nonThreadLocalServiceClient;
        if (ret == null) {
            ret = new ServiceClient();

            nonThreadLocalServiceClient = ret;
        }
        ret.removeHeaders();
        return ret;
    }

    private ServiceClient configureServiceClient(String action, EndpointReference consumerLocation, String msgId,
            List<OMElement> soapHeaders) throws AxisFault {

        // not engaging addressing modules

        ServiceClient client = getServiceClient();

        SOAPHeaderBlock msgIdEl = soapfactory.createSOAPHeaderBlock("MessageID", WsmgNameSpaceConstants.WSA_NS);
        msgIdEl.setText(msgId);
        SOAPHeaderBlock actionEl = soapfactory.createSOAPHeaderBlock("Action", WsmgNameSpaceConstants.WSA_NS);
        actionEl.setText(action);

        SOAPHeaderBlock to = soapfactory.createSOAPHeaderBlock("To", WsmgNameSpaceConstants.WSA_NS);
        to.setText(consumerLocation.getAddress());

        client.addHeader(actionEl);
        client.addHeader(msgIdEl);
        client.addHeader(to);

        for (OMElement omHeader : soapHeaders) {

            try {
                SOAPHeaderBlock headerBlock = ElementHelper.toSOAPHeaderBlock(omHeader, soapfactory);

                client.addHeader(headerBlock);
            } catch (Exception e) {
                throw AxisFault.makeFault(e);
            }

        }

        Options opts = new Options();
        opts.setTimeOutInMilliSeconds(tcpConnectionTimeout);
        opts.setMessageId(msgId);
        opts.setTo(consumerLocation);
        opts.setAction(action);
        opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);

        opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.HTTP_PROTOCOL_VERSION,
                org.apache.axis2.transport.http.HTTPConstants.HEADER_PROTOCOL_10);
        client.setOptions(opts);

        return client;
    }
}
