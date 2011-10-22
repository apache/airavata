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

package org.apache.airavata.wsmg.broker.wseventing;

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.airavata.wsmg.broker.context.ContextParameters;
import org.apache.airavata.wsmg.broker.context.ProcessingContext;
import org.apache.airavata.wsmg.broker.context.ProcessingContextBuilder;
import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.airavata.wsmg.commons.NameSpaceConstants;
import org.apache.airavata.wsmg.util.BrokerUtil;
import org.apache.airavata.wsmg.util.WsEventingOperations;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.axis2.context.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WSEProcessingContextBuilder extends ProcessingContextBuilder {

    private static final Logger logger = LoggerFactory.getLogger(WSEProcessingContextBuilder.class);

    public ProcessingContext build(OMElement elem) {

        ProcessingContext processingContext = new ProcessingContext();

        if (elem != null && elem.getLocalName().equals("Subscribe")) {
            logger.debug("found subscribe element");
            onSubscription(processingContext, elem);

        }

        return processingContext;
    }

    public ProcessingContext build(SOAPEnvelope elem) {

        ProcessingContext context = null;

        SOAPBody soapBody = elem.getBody();
        if (soapBody != null) {

            context = build(soapBody.getFirstElement());

        } else {
            context = build((OMElement) null);
        }

        context.setEnvelope(elem);
        extractInfoFromHeader(context, elem.getHeader());

        return context;
    }

    public ProcessingContext build(MessageContext msgContext, WsEventingOperations operation) {

        ProcessingContext processingContext = new ProcessingContext();

        switch (operation) {
        case SUBSCRIBE: {

            Iterator<OMElement> iterator = msgContext.getEnvelope().getBody()
                    .getChildrenWithName(new QName(NameSpaceConstants.WSE_NS.getNamespaceURI(), "Subscribe"));

            if (!iterator.hasNext()) {
                throw new RuntimeException("invalid subscription message - no subscribe element");
            }

            onSubscription(processingContext, iterator.next());
        }
            break;

        }

        processingContext.setMessageConext(msgContext);
        processingContext.setEnvelope(msgContext.getEnvelope());
        extractInfoFromHeader(processingContext, msgContext.getEnvelope().getHeader());
        String topicFromUrl = BrokerUtil.getTopicFromRequestPath(msgContext.getTo().getAddress());

        processingContext.setContextParameter(ContextParameters.TOPIC_FROM_URL, topicFromUrl);

        return processingContext;
    }

    /**
     * @param processingContext
     * @param subscribeElement
     */
    private void onSubscription(ProcessingContext processingContext, OMElement subscribeElement) {

        processingContext.setContextParameter(ContextParameters.SUBSCRIBE_ELEMENT, subscribeElement);

        // -- check optional element - expires
        Iterator iterator = subscribeElement.getChildrenWithName(new QName(NameSpaceConstants.WSE_NS.getNamespaceURI(),
                "Expires"));

        if (iterator.hasNext()) {

            processingContext.setContextParameter(ContextParameters.SUBSCRIBER_EXPIRES,
                    ((OMElement) iterator.next()).getText());

        }

        iterator = subscribeElement
                .getChildrenWithName(new QName(NameSpaceConstants.WSE_NS.getNamespaceURI(), "Filter"));

        if (!iterator.hasNext()) {

            throw new RuntimeException("invalid subscription - unable to find filter dialet");

        }

        processingContext.setContextParameter(ContextParameters.FILTER_ELEMENT, iterator.next());

        iterator = subscribeElement.getChildrenWithName(new QName(NameSpaceConstants.WSE_NS.getNamespaceURI(),
                "Delivery"));

        if (!iterator.hasNext()) {
            throw new RuntimeException("invalid subscription - unable to find delivery tag");
        }

        OMElement delivery = (OMElement) iterator.next();

        iterator = delivery.getChildrenWithName(new QName(NameSpaceConstants.WSE_NS.getNamespaceURI(), "NotifyTo"));

        if (!iterator.hasNext()) {
            throw new RuntimeException("invalid subscription - unable to find NotifyTo tag");
        }

        OMElement notifyToElement = (OMElement) iterator.next();

        processingContext.setContextParameter(ContextParameters.NOTIFY_TO_ELEMENT, notifyToElement);

        try {

            processingContext.setContextParameter(ContextParameters.NOTIFY_TO_EPR,
                    EndpointReferenceHelper.fromOM(notifyToElement));

        } catch (AxisFault e) {
            throw new RuntimeException("invalid subscription - unable to parse notify to end point reference", e);
        }

    }

    private void extractInfoFromHeader(ProcessingContext context, SOAPHeader header) {

        Iterator ite = header.getChildrenWithName(new QName(NameSpaceConstants.WSE_NS.getNamespaceURI(),
                WsmgCommonConstants.SUBSCRIPTION_ID));

        if (ite.hasNext()) {
            OMElement identifier = (OMElement) ite.next();
            logger.debug("extracted identifier " + identifier.getText());

            context.setContextParameter(ContextParameters.SUB_ID, identifier.getText());

        }

    }

}
