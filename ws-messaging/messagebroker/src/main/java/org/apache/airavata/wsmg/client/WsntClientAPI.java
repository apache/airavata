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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.airavata.wsmg.commons.NotificationProducer;
import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.airavata.wsmg.commons.WsmgNameSpaceConstants;
import org.apache.airavata.wsmg.util.WsmgUtil;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.EndpointReferenceHelper;

public class WsntClientAPI extends CommonClientProcessing implements WsmgClientAPI {

    protected NotificationProducer notifProducer = new NotificationProducer();

    public WsntClientAPI() {
        this(20000L);
    }

    public WsntClientAPI(long timeout) {
        super(timeout);
    }

    public String subscribe(String producerLocation, String consumerLocation, String topicExpression,
            String xpathExpression, String consumerEndpointReferenceNSuri, String consumerEndpointReference)
            throws AxisFault {

        if (consumerLocation == null) {
            throw new IllegalStateException("consumer location required.");
        }
        consumerLocation = WsmgUtil.formatURLString(consumerLocation);
        producerLocation = WsmgUtil.formatURLString(producerLocation);
        EndpointReference producerReference = null;

        producerReference = new EndpointReference(producerLocation);

        boolean gt4 = false;
        if (gt4) {
            OMNamespace producerEPR_NS = factory.createOMNamespace("http://auction.com", "ns1");
            producerReference.addReferenceParameter(new QName(producerEPR_NS.getNamespaceURI(), "BidKey"), "6581243");
        }

        NotificationProducerStub notificationProducer = new NotificationProducerStub(producerReference,
                getTimeOutInMilliSeconds());

        EndpointReference consumerReference = new EndpointReference(consumerLocation);

        if (consumerEndpointReferenceNSuri != null) {
            consumerReference.addReferenceParameter(
                    new QName(consumerEndpointReferenceNSuri, "NotificationConsumerKey"), consumerEndpointReference);
        }

        OMElement topicExpEl = null;
        if (topicExpression != null) {
            topicExpEl = factory.createOMElement("TopicExpression", WsmgNameSpaceConstants.WSNT_NS);

            topicExpEl.addAttribute("Dialect", WsmgCommonConstants.TOPIC_EXPRESSION_SIMPLE_DIALECT,
                    WsmgNameSpaceConstants.WSNT_NS);
            topicExpEl.declareNamespace(WsmgNameSpaceConstants.WIDGET_NS);
            topicExpEl.setText(WsmgNameSpaceConstants.WIDGET_NS.getPrefix() + ":" + topicExpression);
        }

        OMElement xpathExpEl = null;
        if (xpathExpression != null) {
            xpathExpEl = factory.createOMElement("Selector", null);
            xpathExpEl.addAttribute("Dialect", WsmgCommonConstants.XPATH_DIALECT, null);
            xpathExpEl.setText(xpathExpression);
        }

        SubscriptionStub subscriptionRes = notificationProducer.subscribe(consumerReference, topicExpEl, xpathExpEl,
                true);

        EndpointReference subscriptionManagerEpr = subscriptionRes.getResourceEpr();

        String subscriptionId = null;
        Map<QName, OMElement> referenceParams = subscriptionManagerEpr.getAllReferenceParameters();

        if (referenceParams != null) {
            QName identifierQName = new QName(WsmgNameSpaceConstants.WSNT_NS.getNamespaceURI(),
                    WsmgCommonConstants.SUBSCRIPTION_ID);

            OMElement identifierEl = referenceParams.get(identifierQName);
            subscriptionId = (identifierEl != null) ? identifierEl.getText() : null;

        }
        return subscriptionId;
    }

    public int unSubscribe(String subscriptionManagerLocation, String subId, String replyTo) throws AxisFault {
        OMElement message = factory.createOMElement("UnsubsribeRequest", WsmgNameSpaceConstants.WSNT_NS);
        return super.unSubscribe(subscriptionManagerLocation, subId, message, replyTo);
    }

    public int publish(String consumerLocation, String topic, String message) throws AxisFault {
        String publisherIP = WsmgUtil.getHostIP();
        return publishTopic(consumerLocation, publisherIP, topic, message);

    }

    public int publishTopic(String consumerLocation, String publisherLocation, String topic, String message)
            throws AxisFault {
        String publisherLoc = WsmgUtil.formatURLString(publisherLocation);
        String consumerLoc = WsmgUtil.formatURLString(consumerLocation);

        boolean useNotify = true;

        EndpointReference consumerReference = new EndpointReference(consumerLoc);

        EndpointReference producerReference = null;
        if (publisherLoc == null) {
            InetAddress localAddress = null;
            try {
                localAddress = InetAddress.getLocalHost();
            } catch (UnknownHostException ex) {
                throw new RuntimeException("unable to resolve localhost");
            }
            publisherLoc = localAddress.getHostAddress();
        }

        producerReference = new EndpointReference(publisherLoc);
        OMElement topicExpEl = factory.createOMElement("Topic", WsmgNameSpaceConstants.WSNT_NS);
        topicExpEl.addAttribute("Dialect", WsmgCommonConstants.TOPIC_EXPRESSION_SIMPLE_DIALECT, null);
        topicExpEl.declareNamespace(WsmgNameSpaceConstants.WIDGET_NS);
        topicExpEl.setText(WsmgNameSpaceConstants.WIDGET_NS.getPrefix() + ":" + topic);
        OMElement embeddedMessageEl = null;
        try {
            embeddedMessageEl = org.apache.airavata.wsmg.commons.CommonRoutines.reader2OMElement(new StringReader(
                    message));
        } catch (XMLStreamException e) {
            throw new AxisFault("unable to conver message to om", e);

        }
        OMElement messageToNotify = null;

        if (useNotify) {
            messageToNotify = factory.createOMElement("Notify", WsmgNameSpaceConstants.WSNT_NS);
            messageToNotify.declareNamespace(WsmgNameSpaceConstants.WSNT_NS);
            messageToNotify.declareNamespace(WsmgNameSpaceConstants.WSA_NS);
            OMElement notificationMesssageEl = factory.createOMElement("NotificationMessage",
                    messageToNotify.getNamespace(), messageToNotify);

            notificationMesssageEl.addChild(topicExpEl);
            notificationMesssageEl
                    .addChild(EndpointReferenceHelper.toOM(factory, producerReference, new QName(notificationMesssageEl
                            .getNamespace().getNamespaceURI(), "ProducerReference", notificationMesssageEl
                            .getNamespace().getPrefix()), WsmgNameSpaceConstants.WSA_NS.getNamespaceURI()));

            OMElement messageEl = factory.createOMElement("Message", notificationMesssageEl.getNamespace(),
                    notificationMesssageEl);

            messageEl.addChild(embeddedMessageEl);
        } else {
            messageToNotify = embeddedMessageEl;
        }
        notifProducer.deliverMessage(messageToNotify, "wsnt", consumerReference, getTimeOutInMilliSeconds());
        return 0;
    }
}
