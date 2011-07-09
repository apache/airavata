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

import java.io.StringReader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.airavata.wsmg.commons.NotificationProducer;
import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.airavata.wsmg.commons.WsmgNameSpaceConstants;
import org.apache.airavata.wsmg.config.WSMGParameter;
import org.apache.airavata.wsmg.util.WsmgUtil;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;

public class WseClientAPI extends CommonClientProcessing implements WsmgClientAPI {
    protected NotificationProducer notificationProducer = new NotificationProducer();

    protected final static String WIDGET_NS_PREFIX = WsmgNameSpaceConstants.WIDGET_NS.getPrefix() + ":";

    protected EndpointReference brokerEpr;
    Thread autoResubscriptionThread = null;

    public WseClientAPI() {
        this(null, 2000L);
    }

    public WseClientAPI(EndpointReference epr) {
        this(epr, 20000L);

    }

    public WseClientAPI(EndpointReference epr, long timeout) {
        super(timeout);
        this.brokerEpr = epr;

    }

    public int unSubscribe(String subscriptionManagerLocation, String subId, String replyTo) throws AxisFault {
        OMElement message = factory.createOMElement("Unsubscribe", WsmgNameSpaceConstants.WSE_NS);
        return super.unSubscribe(subscriptionManagerLocation, subId, message, replyTo);
    }

    public static EndpointReference createEndpointReference(String brokerURL, String topic) {
        if (brokerURL == null) {
            throw new IllegalArgumentException("Broker URL is null.");
        }
        if (topic == null) {
            throw new IllegalArgumentException("Topic is null.");
        }

        String sinkLocation = brokerURL.endsWith("/") ? brokerURL + "topic/" + topic : brokerURL + "/topic/" + topic;

        EndpointReference eventSinkReference = new EndpointReference(sinkLocation);
        return eventSinkReference;
    }

    public String subscribe(String brokerLocation, String eventSinkLocation, String topicExpression,
            String xpathExpression, String eventSinkEndpointReferenceNS, String eventSinkEndpointReference)
            throws AxisFault {
        return subscribe(brokerLocation, eventSinkLocation, topicExpression, xpathExpression,
                eventSinkEndpointReferenceNS, eventSinkEndpointReference, WSMGParameter.expirationTime);
    }

    public String subscribe_NeverExpire(String brokerLocation, String eventSinkLocation, String topicExpression,
            String xpathExpression, String eventSinkEndpointReferenceNS, String eventSinkEndpointReference)
            throws AxisFault {
        return subscribe(brokerLocation, eventSinkLocation, topicExpression, xpathExpression,
                eventSinkEndpointReferenceNS, eventSinkEndpointReference, -1);
    }

    public String subscribe_NeverExpire(String brokerLocation, String eventSinkLocation, String topicExpression,
            String xpathExpression) throws AxisFault {
        return subscribe(brokerLocation, eventSinkLocation, topicExpression, xpathExpression, null, null, -1);
    }

    private String subscribe(String brokerLocation, String eventSinkLocation, String topicExpression,
            String xpathExpression, String eventSinkEndpointReferenceNS, String eventSinkEndpointReference,
            long expireTime) throws AxisFault {

        String brokerLocationFormattedString = WsmgUtil.formatURLString(brokerLocation);

        String eventSinkLocationFormattedString = WsmgUtil.formatURLString(eventSinkLocation);
        OMElement filterEl = null;

        if (eventSinkLocationFormattedString == null) {
            throw new IllegalStateException("eventSink location required.");
        }

        EndpointReference brokerLocationEPR = new EndpointReference(brokerLocationFormattedString);

        WseCreateSubscription eventSourceStub = new WseCreateSubscription(brokerLocationEPR, getTimeOutInMilliSeconds());

        EndpointReference eventSinkLocationEPR = new EndpointReference(eventSinkLocationFormattedString);

        if (eventSinkEndpointReferenceNS != null) {
            OMNamespace eventSinkEPR_NS = factory.createOMNamespace(eventSinkEndpointReferenceNS, "ncex");
            OMElement resourceId = factory.createOMElement("resourceId", eventSinkEPR_NS);
            resourceId.setText(eventSinkEndpointReference);
            eventSinkLocationEPR.addReferenceParameter(resourceId);
        }

        boolean hasTopicExpression = (topicExpression != null && topicExpression.length() != 0);
        boolean hasXPathExpression = (xpathExpression != null && xpathExpression.length() != 0);

        if (hasTopicExpression || hasXPathExpression) {
            filterEl = factory.createOMElement("Filter", WsmgNameSpaceConstants.WSE_NS);

            if (!hasXPathExpression) {
                filterEl.addAttribute("Dialect", WsmgCommonConstants.TOPIC_EXPRESSION_SIMPLE_DIALECT, null);
                filterEl.declareNamespace(WsmgNameSpaceConstants.WIDGET_NS);
                filterEl.setText(WsmgNameSpaceConstants.WIDGET_NS.getPrefix() + ":" + topicExpression);
            } else if (!hasTopicExpression) { // Xpath only
                filterEl.addAttribute("Dialect", WsmgCommonConstants.XPATH_DIALECT, null);
                filterEl.setText(xpathExpression);
            } else { // both topic and XPath
                filterEl.addAttribute("Dialect", WsmgCommonConstants.TOPIC_AND_XPATH_DIALECT, null);
                OMElement topicExpressionEl = factory
                        .createOMElement("TopicExpression", WsmgNameSpaceConstants.WSNT_NS);
                topicExpressionEl.addAttribute("Dialect", WsmgCommonConstants.TOPIC_EXPRESSION_SIMPLE_DIALECT, null);
                topicExpressionEl.declareNamespace(WsmgNameSpaceConstants.WIDGET_NS);
                topicExpressionEl.setText(WsmgNameSpaceConstants.WIDGET_NS.getPrefix() + ":" + topicExpression);
                filterEl.addChild(topicExpressionEl);
                OMElement xpathEl = factory.createOMElement("MessageContent", WsmgNameSpaceConstants.WSNT_NS);
                xpathEl.addAttribute("Dialect", WsmgCommonConstants.XPATH_DIALECT, null);
                xpathEl.setText(xpathExpression);
                filterEl.addChild(xpathEl);
            }
        }

        OMElement subscriptionManager = eventSourceStub.subscribe(eventSinkLocationEPR, filterEl, true, expireTime);
        OMElement referencePropertiesEl = subscriptionManager.getFirstChildWithName(new QName(
                WsmgNameSpaceConstants.WSA_NS.getNamespaceURI(), "ReferenceProperties"));

        if (referencePropertiesEl == null) {
            referencePropertiesEl = subscriptionManager.getFirstChildWithName(new QName(WsmgNameSpaceConstants.WSA_NS
                    .getNamespaceURI(), "ReferenceParameters"));
        }

        OMElement identifierEl = referencePropertiesEl.getFirstChildWithName(new QName(WsmgNameSpaceConstants.WSE_NS
                .getNamespaceURI(), WsmgCommonConstants.SUBSCRIPTION_ID));

        if (identifierEl == null) {
            throw new RuntimeException("invalid response message, subscription id was not sent by broker");
        }

        return identifierEl.getText();
    }

    public void enableAutoResubscription() {
        if (autoResubscriptionThread != null) {
            return;
        }

    }

    public void publish(String message) throws AxisFault {
        if (this.brokerEpr != null) {
            this.publish(this.brokerEpr, message);
        } else {
            throw new RuntimeException("The Endpoint Reference is not set: Use the "
                    + "*WseClientAPI(EndpointReference epr)* constructor if this" + " method is called for publishing");
        }
    }

    public void publishPlainText(String plainText) throws AxisFault {

        if (this.brokerEpr != null) {

            OMElement message = factory.createOMElement(WsmgCommonConstants.WSMG_PLAIN_TEXT_WRAPPER,
                    WsmgNameSpaceConstants.WSMG_NS);

            message.setText(plainText);

            this.publish(this.brokerEpr, message);

        } else {
            throw new RuntimeException("The Endpoint Reference is not set: Use the "
                    + "*WseClientAPI(EndpointReference epr)* constructor if this" + " method is called for publishing");
        }

    }

    public void publish(EndpointReference eventSinkReference, String message) throws AxisFault {

        OMElement messageToNotify = null;
        try {
            messageToNotify = org.apache.airavata.wsmg.commons.CommonRoutines
                    .reader2OMElement(new StringReader(message));
        } catch (XMLStreamException e) {
            throw AxisFault.makeFault(e);
        }

        publish(eventSinkReference, messageToNotify);
    }

    /**
     * @param eventSinkReference
     * @param messageToNotify
     * @throws AxisFault
     */
    protected void publish(EndpointReference eventSinkReference, OMElement messageToNotify) throws AxisFault {
        EndpointReference omEventSinkReference = new EndpointReference(eventSinkReference.getAddress());

        notificationProducer.deliverMessage(messageToNotify, "wse", omEventSinkReference, getTimeOutInMilliSeconds());
    }

    public int publishTopic(String brokerLocation, String publisherLocation, String topic, String message)
            throws AxisFault {
        return publish(brokerLocation, topic, message);
    }

    public int publishPlainText(String brokerLocation, String topic, String plainText) throws AxisFault {

        OMElement wrappedMsg = factory.createOMElement(WsmgCommonConstants.WSMG_PLAIN_TEXT_WRAPPER,
                WsmgNameSpaceConstants.WSMG_NS);

        wrappedMsg.setText(plainText);

        return publish(brokerLocation, topic, wrappedMsg);

    }

    public int publish(String brokerLocation, String topic, OMElement messageToNotify) throws AxisFault {
        String brokerLocationFormattedString = WsmgUtil.formatURLString(brokerLocation);

        EndpointReference brokerLocationEPR = new EndpointReference(brokerLocationFormattedString);
        OMElement topicExpressionEl = null;
        if (topic != null) {
            topicExpressionEl = factory.createOMElement("Topic", WsmgNameSpaceConstants.WSNT_NS);
            topicExpressionEl.addAttribute("Dialect", WsmgCommonConstants.TOPIC_EXPRESSION_SIMPLE_DIALECT, null);
            topicExpressionEl.declareNamespace(WsmgNameSpaceConstants.WIDGET_NS);
            topicExpressionEl.setText(WIDGET_NS_PREFIX + topic);

        }

        notificationProducer.deliverMessage(messageToNotify, "wse", brokerLocationEPR, getTimeOutInMilliSeconds(),
                topicExpressionEl);

        return 0;
    }

    public int publish(String brokerLocation, String topic, String message) throws AxisFault {

        OMElement messageToNotify = null;
        try {
            StringReader strgReader = new StringReader(message);
            messageToNotify = org.apache.airavata.wsmg.commons.CommonRoutines.reader2OMElement(strgReader);
        } catch (XMLStreamException e) {

            throw AxisFault.makeFault(e);
        }
        return publish(brokerLocation, topic, messageToNotify);
    }

    public String subscribeMsgBoxNeverExpr(String brokerLocation, EndpointReference msgBoxEpr, String topicExpression,
            String xpathExpression) throws AxisFault {
        String msgBoxEventSink = msgBoxEpr.getAddress();

        String formattedEventSink = null;

        if (msgBoxEpr.getAddress().contains("clientid")) {
            formattedEventSink = msgBoxEventSink;
        } else {
            if (msgBoxEpr.getAllReferenceParameters() == null)
                throw new RuntimeException("Invalid Message Box EPR, no reference parameters found");
            String msgBoxId = msgBoxEpr.getAllReferenceParameters()
                    .get(new QName("http://www.extreme.indiana.edu/xgws/msgbox/2004/", "MsgBoxAddr")).getText();
            if (msgBoxId == null)
                throw new RuntimeException("Invalid Message Box EPR, reference parameter MsgBoxAddr is missing");
            String format = msgBoxEventSink.endsWith("/") ? "%sclientid/%s" : "%s/clientid/%s";

            formattedEventSink = String.format(format, msgBoxEventSink, msgBoxId);

        }
        return subscribe(brokerLocation, formattedEventSink, topicExpression, xpathExpression, null, null, -1);
    }

}
