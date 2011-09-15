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

package org.apache.airavata.wsmg.broker;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.airavata.wsmg.broker.context.ContextParameters;
import org.apache.airavata.wsmg.broker.context.ProcessingContext;
import org.apache.airavata.wsmg.commons.OutGoingMessage;
import org.apache.airavata.wsmg.commons.WsmgNameSpaceConstants;
import org.apache.airavata.wsmg.config.WSMGParameter;
import org.apache.airavata.wsmg.config.WsmgConfigurationContext;
import org.apache.airavata.wsmg.matching.AbstractMessageMatcher;
import org.apache.airavata.wsmg.messenger.OutGoingQueue;
import org.apache.airavata.wsmg.util.RunTimeStatistics;
import org.apache.airavata.wsmg.util.BrokerUtil;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;

public class NotificationProcessor {

    org.apache.log4j.Logger logger = Logger.getLogger(NotificationProcessor.class);

    WsmgConfigurationContext wsmgConfigContext = null;

    protected long messageCounter = 0;
    protected long messageId = 0;

    OMFactory factory = OMAbstractFactory.getOMFactory();

    OutGoingQueue outgoingQueue = null;

    public NotificationProcessor(WsmgConfigurationContext config) {
        init(config);
    }

    private void init(WsmgConfigurationContext config) {
        this.wsmgConfigContext = config;
        outgoingQueue = config.getOutgoingQueue();

        // Ramith : JMS support is removed.
        assert (!WSMGParameter.useIncomingQueue);
        /*
         * PublisherThread publisherThread = new PublisherThread(wsmgConfigContext); new
         * Thread(publisherThread).start();
         */

    }

    private synchronized long getNextTrackId() {
        messageCounter++;
        return messageCounter;
    }

    private synchronized long getNextMsgId() {
        messageId++;
        return messageId;
    }

    public void processMsg(ProcessingContext ctx, OMNamespace protocolNs) throws OMException, AxisFault {

        String trackId = "trackId_A_" + getNextTrackId();
        if (WSMGParameter.showTrackId) {
            logger.debug(trackId + ": received.");
        }

        AdditionalMessageContent additionalMessageContent = new AdditionalMessageContent(ctx.getMessageContext()
                .getSoapAction(), ctx.getMessageContext().getMessageID());
        additionalMessageContent.setTrackId(trackId);

        if (WsmgNameSpaceConstants.WSNT_NS.equals(protocolNs)) {

            onWSNTMsg(ctx, additionalMessageContent);
            setResponseMsg(ctx, trackId, protocolNs);
        } else { // WSE Notifications No specific namespace

            onWSEMsg(ctx, trackId, additionalMessageContent);
            setResponseMsg(ctx, trackId, protocolNs);
        }

    }

    /**
     * @param ctx
     * @param topicElString
     * @param trackId
     * @param additionalMessageContent
     * @throws OMException
     * @throws XMLStreamException
     */
    private void onWSEMsg(ProcessingContext ctx, String trackId, AdditionalMessageContent additionalMessageContent)
            throws OMException, AxisFault {

        String topicElString = null;
        String topicLocalString = null;

        QName qName = new QName(WsmgNameSpaceConstants.WSNT_NS.getNamespaceURI(), "Topic");

        OMElement topicEl = ctx.getMessageContext().getEnvelope().getHeader().getFirstChildWithName(qName);

        if (topicEl == null) {

            topicLocalString = ctx.getContextParameter(ContextParameters.TOPIC_FROM_URL);

            if (topicLocalString != null) {

                topicElString = "<wsnt:Topic "
                        + "Dialect=\"http://www.ibm.com/xmlns/stdwip/web-services/WS-Topics/TopicExpression/simple\" "
                        + "xmlns:ns2=\"http://tutorial.globus.org/auction\" "
                        + "xmlns:wsnt=\"http://www.ibm.com/xmlns/stdwip/web-services/WS-BaseNotification\">" + "ns2:"
                        + topicLocalString + "</wsnt:Topic>";
                // / }
                additionalMessageContent.setTopicElement(topicElString);
            } else {

                topicLocalString = "wseTopic";
                topicElString = "<wsnt:Topic "
                        + "Dialect=\"http://www.ibm.com/xmlns/stdwip/web-services/WS-Topics/TopicExpression/simple\" "
                        + "xmlns:ns2=\"http://tutorial.globus.org/auction\" "
                        + "xmlns:wsnt=\"http://www.ibm.com/xmlns/stdwip/web-services/WS-BaseNotification\">"
                        + "ns2:wseTopic</wsnt:Topic>";
                // / }
                additionalMessageContent.setTopicElement(topicElString);
            }
        } else {

            topicLocalString = BrokerUtil.getTopicLocalString(topicEl.getText());
            try {
                topicElString = topicEl.toStringWithConsume();
            } catch (XMLStreamException e) {
                // TODO Auto-generated catch block Add throw
                logger.fatal("exceptions occured at WSE " + "eventing notification creating", e);
            }
            additionalMessageContent.setTopicElement(topicElString);
        }

        OMElement messageEl = ctx.getSoapBody().getFirstElement();
        if (messageEl == null) {
            throw new AxisFault("no message found");
        }

        String message = null;
        try {
            message = messageEl.toStringWithConsume();
        } catch (XMLStreamException e) {
            logger.error("unable to serialize the message", e);
            throw new AxisFault("unable to serialize the message", e);
        }

        matchAndSave(message, topicLocalString, additionalMessageContent);
    }

    /**
     * @param ctx
     * @param trackId
     * @throws OMException
     */
    private void setResponseMsg(ProcessingContext ctx, String trackId, OMNamespace responseNS) throws OMException {
        // set response message

        ctx.addResponseMsgNameSpaces(responseNS);

        OMAttribute trackIdAttribute = factory.createOMAttribute("trackId", null, trackId);
        OMElement messageElement = ctx.getMessageContext().getEnvelope().getBody().getFirstElement();
        OMElement responseMsgElement = factory.createOMElement(messageElement.getLocalName() + "Response", responseNS);
        responseMsgElement.addAttribute(trackIdAttribute);
        ctx.setRespMessage(responseMsgElement);

    }

    /**
     * @param ctx
     * @param topicLocalString
     * @param topicElString
     * @param producerReferenceElString
     * @param additionalMessageContent
     * @throws OMException
     * @throws XMLStreamException
     * @throws AxisFault
     */
    private void onWSNTMsg(ProcessingContext ctx, AdditionalMessageContent additionalMessageContent)
            throws OMException, AxisFault {

        String producerReferenceElString = null;
        String topicElString = null;

        boolean noElements = true;

        // TODO: set nicely with a processing context
        OMElement notifyEl = ctx.getSoapBody().getFirstElement();
        for (Iterator<OMElement> iter = notifyEl.getChildrenWithLocalName("NotificationMessage"); iter.hasNext();) {
            noElements = false;
            OMElement wrappedMessageEl = iter.next();

            String topicLocalString = null;

            OMElement topicEl = wrappedMessageEl.getFirstChildWithName(new QName(WsmgNameSpaceConstants.WSNT_NS
                    .getNamespaceURI(), "Topic"));
            if (topicEl != null) {

                topicLocalString = BrokerUtil.getTopicLocalString(topicEl.getText()); // get what ever inside this element

                try {
                    topicElString = topicEl.toStringWithConsume();
                } catch (XMLStreamException e) {
                    // TODO Add with throws block
                    logger.fatal("exception occured while " + "creating NotificationConsumer", e);

                }
                additionalMessageContent.setTopicElement(topicElString);
            }
            OMElement producerReferenceEl = wrappedMessageEl.getFirstChildWithName(new QName(
                    WsmgNameSpaceConstants.WSNT_NS.getNamespaceURI(), "ProducerReference"));

            if (producerReferenceEl != null) {
                try {
                    producerReferenceElString = producerReferenceEl.toStringWithConsume();
                } catch (XMLStreamException e) {

                    logger.fatal("exception occured while " + "creating notification consumer", e);

                }
                additionalMessageContent.setProducerReference(producerReferenceElString);
            }

            OMElement notificationMessageEl = wrappedMessageEl.getFirstChildWithName(
                    new QName(WsmgNameSpaceConstants.WSNT_NS.getNamespaceURI(), "Message")).getFirstElement();

            String message = null;
            try {
                message = notificationMessageEl.toStringWithConsume();
            } catch (XMLStreamException e) {
                logger.error("exception occured while " + "creating notification consumer", e);

                throw new AxisFault("unable to serialize the message", e);
            }

            matchAndSave(message, topicLocalString, additionalMessageContent);

        }
        if (noElements) {
            throw new AxisFault("at least one element is required");
        }
    }

    private void matchAndSave(String notificationMessage, String topicLocalString,
            AdditionalMessageContent additionalMessageContent) {

        // Ramith: Jms support is not implemented now.
        assert (!WSMGParameter.useIncomingQueue);

        List<ConsumerInfo> matchedConsumers = new LinkedList<ConsumerInfo>();

        // not use incoming queue
        // This is a fix for the bug seen in yfilter.
        try {

            for (AbstractMessageMatcher matcher : wsmgConfigContext.getMessageMatchers()) {
                matcher.populateMatches(null, additionalMessageContent, notificationMessage, topicLocalString,
                        matchedConsumers);
            }

            save(matchedConsumers, notificationMessage, additionalMessageContent);

        } catch (RuntimeException e) {
            logger.fatal("Caught RuntimeException", e);
        }

    }

    public void save(List<ConsumerInfo> consumerInfoList, String message,
            AdditionalMessageContent additionalMessageContent) {

        if (consumerInfoList.size() == 0) // No subscription
            return;

        assert (WSMGParameter.useOutGoingQueue); // we will only use out going
        // queue for the moment

        RunTimeStatistics.addNewNotificationMessageSize(message.length());
        OutGoingMessage outGoingMessage = new OutGoingMessage();
        outGoingMessage.setTextMessage(message);
        outGoingMessage.setConsumerInfoList(consumerInfoList);
        outGoingMessage.setAdditionalMessageContent(additionalMessageContent);

        outgoingQueue.storeNotification(outGoingMessage, getNextMsgId());

        if (WSMGParameter.showTrackId)
            logger.info(additionalMessageContent.getTrackId() + ": putIn Outgoing queue.");
    }

}
