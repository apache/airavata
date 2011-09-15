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

package org.apache.airavata.wsmg.broker.wsnotification;

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.airavata.wsmg.broker.context.ContextParameters;
import org.apache.airavata.wsmg.broker.context.ProcessingContext;
import org.apache.airavata.wsmg.broker.context.ProcessingContextBuilder;
import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.airavata.wsmg.commons.WsmgNameSpaceConstants;
import org.apache.airavata.wsmg.util.WsNotificationOperations;
import org.apache.airavata.wsmg.util.BrokerUtil;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.axis2.context.MessageContext;
import org.apache.log4j.Logger;

public class WSNotificationProcessingContextBuilder extends ProcessingContextBuilder {

    private Logger logger = Logger.getLogger(WSNotificationProcessingContextBuilder.class);

    public ProcessingContext build(OMElement elem) {

        ProcessingContext processingContext = new ProcessingContext();

        if (elem == null
                || (!elem.getNamespace().getNamespaceURI().equals(WsmgNameSpaceConstants.WSNT_NS.getNamespaceURI()))) {

            logger.warn("invalid message payload recieved: " + elem);

            return processingContext;
        }

        String localName = elem.getLocalName();

        if (localName.equals("SubscribeRequest")) {
            onSubscription(processingContext, elem);
        }
        return processingContext;
    }

    public void onSubscription(ProcessingContext context, OMElement subscribeElement) {
        context.setContextParameter(ContextParameters.SUBSCRIBE_ELEMENT, subscribeElement);

        OMElement consumerReference = subscribeElement.getFirstChildWithName(new QName(WsmgNameSpaceConstants.WSNT_NS
                .getNamespaceURI(), "ConsumerReference"));
        if (consumerReference == null) {
            logger.warn("unable to find consumer reference" + " in subscribe message: " + subscribeElement);
            return;
        }
        context.setContextParameter(ContextParameters.NOTIFY_TO_ELEMENT, consumerReference);

        try {
            EndpointReference consumerEpr = EndpointReferenceHelper.fromOM(consumerReference);
            context.setContextParameter(ContextParameters.NOTIFY_TO_EPR, consumerEpr);

        } catch (AxisFault e) {
            logger.warn("invalid epr", e);
            return;
        }

        OMElement topicExpression = subscribeElement.getFirstChildWithName(new QName(WsmgNameSpaceConstants.WSNT_NS
                .getNamespaceURI(), "TopicExpression"));

        if (topicExpression != null) { // topic can be null

            context.setContextParameter(ContextParameters.TOPIC_EXPRESSION_ELEMENT, topicExpression);

        }

        OMElement useNotify = subscribeElement.getFirstChildWithName(new QName(WsmgNameSpaceConstants.WSNT_NS
                .getNamespaceURI(), "UseNotify"));

        if (useNotify != null) {
            context.setContextParameter(ContextParameters.USE_NOTIFY_ELEMENT, useNotify);
        }

        OMElement selector = subscribeElement.getFirstChildWithName(new QName(WsmgNameSpaceConstants.WSNT_NS
                .getNamespaceURI(), "Selector"));

        if (selector != null) {
            context.setContextParameter(ContextParameters.XPATH_ELEMENT, selector);
        }

        OMElement subscriptionPolicy = subscribeElement.getFirstChildWithName(new QName(WsmgNameSpaceConstants.WSNT_NS
                .getNamespaceURI(), WsmgCommonConstants.SUBSCRIPTION_POLICY));

        if (subscriptionPolicy != null) {
            context.setContextParameter(ContextParameters.SUB_POLICY, subscriptionPolicy);
        }

    }

    public ProcessingContext build(MessageContext msgContext, WsNotificationOperations operation) {
        ProcessingContext context = new ProcessingContext();

        SOAPEnvelope soapEnvelope = msgContext.getEnvelope();

        if (soapEnvelope == null) {
            throw new RuntimeException("invalid message context - envelope is not found");
        }

        SOAPBody soapBody = soapEnvelope.getBody();

        if (soapBody == null) {
            throw new RuntimeException("invalid message context - soap envelope is not found");
        }

        SOAPHeader soapHeader = soapEnvelope.getHeader();

        if (soapHeader == null) {
            throw new RuntimeException("invalid message context - soap header is not found");
        }

        switch (operation) {
        case SUBSCRIBE: {

            Iterator<OMElement> iterator = soapBody.getChildrenWithName(new QName(WsmgNameSpaceConstants.WSNT_NS
                    .getNamespaceURI(), "SubscribeRequest"));
            if (!iterator.hasNext()) {
                throw new RuntimeException("invalid message context - unable to find Subscribe information");
            }

            onSubscription(context, iterator.next());
        }
            break;
        }

        context.setEnvelope(soapEnvelope);
        extractInfoFromHeader(context, soapHeader);
        context.setMessageConext(msgContext);
        String topicFromUrl = BrokerUtil.getTopicFromRequestPath(msgContext.getTo().getAddress());
        context.setContextParameter(ContextParameters.TOPIC_FROM_URL, topicFromUrl);

        return context;
    }

    private void extractInfoFromHeader(ProcessingContext context, SOAPHeader header) {

        Iterator ite = header.getChildrenWithName(new QName(WsmgNameSpaceConstants.WSNT_NS.getNamespaceURI(),
                WsmgCommonConstants.SUBSCRIPTION_ID));
        if (ite.hasNext()) {
            OMElement identifier = (OMElement) ite.next();
            logger.debug("extracted identifier " + identifier.getText());

            context.setContextParameter(ContextParameters.SUB_ID, identifier.getText());

        }

    }
}
