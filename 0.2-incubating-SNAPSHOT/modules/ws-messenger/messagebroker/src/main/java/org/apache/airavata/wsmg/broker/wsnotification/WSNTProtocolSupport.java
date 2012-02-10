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

import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.airavata.wsmg.broker.context.ContextParameters;
import org.apache.airavata.wsmg.broker.context.ProcessingContext;
import org.apache.airavata.wsmg.broker.subscription.SubscriptionState;
import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.airavata.wsmg.commons.NameSpaceConstants;
import org.apache.airavata.wsmg.messenger.OutGoingQueue;
import org.apache.airavata.wsmg.util.BrokerUtil;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WSNTProtocolSupport {

    private static final Logger log = LoggerFactory.getLogger(WSNTProtocolSupport.class);

    public SubscriptionState createSubscriptionState(ProcessingContext ctx, OutGoingQueue outgoingQueue)
            throws AxisFault {

        EndpointReference consumerReference = ctx.getContextParameter(ContextParameters.NOTIFY_TO_EPR);

        if (consumerReference == null) {
            throw new AxisFault("Only Push delivery Mode (NotifyTo) is supported");
        }

        boolean neverExpire = false; // is true if expiration time is less than
        boolean useNotify = true; // notify by event notifications
        boolean wsrmEnabled = false;
        String topicLocalString = "";
        String xpathString = "";

        String expireTimeString = ctx.getContextParameter(ContextParameters.SUBSCRIBER_EXPIRES);

        if (expireTimeString == null) {
            neverExpire = true;
        } else {
            long expireTime = Long.valueOf(expireTimeString);
            if (expireTime < 0) {
                neverExpire = true;
            }
        }

        OMElement useNotifyEl = ctx.getContextParameter(ContextParameters.USE_NOTIFY_ELEMENT);
        if (useNotifyEl != null) {
            String s = useNotifyEl.getText();
            useNotify = Boolean.valueOf(s);
        }

        // get policy if exist
        OMElement element = ctx.getContextParameter(ContextParameters.SUB_POLICY);
        if (element != null) {
            wsrmEnabled = true;
        }

        OMElement topicExpressionEl = ctx.getContextParameter(ContextParameters.TOPIC_EXPRESSION_ELEMENT);

        if (topicExpressionEl != null) {
            topicLocalString = BrokerUtil.getTopicLocalString(topicExpressionEl.getText());
        }

        OMElement xpathEl = ctx.getContextParameter(ContextParameters.XPATH_ELEMENT);

        if (xpathEl != null) {
            xpathString = BrokerUtil.getXPathString(xpathEl);
        }
        if (xpathString == null && topicLocalString == null) {
            throw new AxisFault("Both topic string and XPath String are null!");

        }

        if (topicLocalString == null || topicLocalString.length() == 0) {
            topicLocalString = WsmgCommonConstants.WILDCARD_TOPIC;
        }

        // Create SubscriptionState Object
        SubscriptionState state = new SubscriptionState(consumerReference, useNotify, wsrmEnabled, topicLocalString,
                xpathString, "wsnt", outgoingQueue);

        state.setNeverExpire(neverExpire); // default false

        return state;
    }

    public void createSubscribeResponse(ProcessingContext ctx, String subId) throws AxisFault {

        OMFactory factory = OMAbstractFactory.getOMFactory();

        ctx.addResponseMsgNameSpaces(NameSpaceConstants.WSNT_NS);
        OMElement responseMessage = factory.createOMElement("SubscribeResponse", NameSpaceConstants.WSNT_NS);

        OMElement identifier = factory.createOMElement(WsmgCommonConstants.SUBSCRIPTION_ID,
                responseMessage.getNamespace());
        identifier.setText(subId);
        EndpointReference serviceLocationEndpointReference = new EndpointReference(ctx.getMessageContext()
                .getAxisService().getEndpointURL());
        serviceLocationEndpointReference.addReferenceParameter(identifier);

        OMElement subscriptionReference = null;
        try {
            subscriptionReference = EndpointReferenceHelper.toOM(factory, serviceLocationEndpointReference, new QName(
                    "SubscriptionReference"), NameSpaceConstants.WSA_NS.getNamespaceURI());

            responseMessage.addChild(subscriptionReference);
            subscriptionReference.setNamespace(responseMessage.getNamespace());

        } catch (AxisFault e) {
            log.error("unable to resolve EPR from OM", e);
            throw e;
        }

        ctx.setRespMessage(responseMessage);
    }

    public static class Client {

        public static OMElement createSubscriptionMsg(EndpointReference eventSinkLocation, String topicExpression,
                String xpathExpression) throws AxisFault {
            OMFactory factory = OMAbstractFactory.getOMFactory();

            OMElement message = factory.createOMElement("SubscribeRequest", NameSpaceConstants.WSNT_NS);

            if (topicExpression != null) {
                OMElement topicExpEl = factory.createOMElement("TopicExpression", NameSpaceConstants.WSNT_NS, message);

                topicExpEl.addAttribute("Dialect", WsmgCommonConstants.TOPIC_EXPRESSION_SIMPLE_DIALECT,
                        NameSpaceConstants.WSNT_NS);
                topicExpEl.declareNamespace(NameSpaceConstants.WIDGET_NS);
                topicExpEl.setText(NameSpaceConstants.WIDGET_NS.getPrefix() + ":" + topicExpression);
            }

            if (xpathExpression != null) {
                OMElement xpathExpEl = factory.createOMElement("Selector", NameSpaceConstants.WSNT_NS, message);
                xpathExpEl.addAttribute("Dialect", WsmgCommonConstants.XPATH_DIALECT, null);
                xpathExpEl.setText(xpathExpression);
            }

            OMElement useNotifyEl = factory.createOMElement("UseNotify", message.getNamespace(), message);
            useNotifyEl.setText("true");// check wether we still need this

            OMElement eprCrEl = EndpointReferenceHelper.toOM(factory, eventSinkLocation,
                    new QName("ConsumerReference"), NameSpaceConstants.WSA_NS.getNamespaceURI());

            message.addChild(eprCrEl);
            eprCrEl.setNamespace(message.getNamespace());

            return message;
        }

        public static String decodeSubscriptionResponse(OMElement subscriptionReference) throws AxisFault {

            String subscriptionId = null;

            EndpointReference subscriptionReferenceEPR = EndpointReferenceHelper.fromOM(subscriptionReference);

            Map<QName, OMElement> referenceParams = subscriptionReferenceEPR.getAllReferenceParameters();

            if (referenceParams != null) {
                QName identifierQName = new QName(NameSpaceConstants.WSNT_NS.getNamespaceURI(),
                        WsmgCommonConstants.SUBSCRIPTION_ID);

                OMElement identifierEl = referenceParams.get(identifierQName);
                subscriptionId = (identifierEl != null) ? identifierEl.getText() : null;

            }

            return subscriptionId;
        }

        public static OMElement createUnsubscribeMsg() {
            OMFactory factory = OMAbstractFactory.getOMFactory();
            OMElement message = factory.createOMElement("UnsubsribeRequest", NameSpaceConstants.WSNT_NS);

            return message;
        }

        public static OMElement encodeNotification(String topic, OMElement message, EndpointReference producerReference)
                throws AxisFault {
            OMFactory factory = OMAbstractFactory.getOMFactory();

            OMElement topicExpEl = factory.createOMElement("Topic", NameSpaceConstants.WSNT_NS);
            topicExpEl.addAttribute("Dialect", WsmgCommonConstants.TOPIC_EXPRESSION_SIMPLE_DIALECT, null);
            topicExpEl.declareNamespace(NameSpaceConstants.WIDGET_NS);
            topicExpEl.setText(NameSpaceConstants.WIDGET_NS.getPrefix() + ":" + topic);

            OMElement messageToNotify = factory.createOMElement("Notify", NameSpaceConstants.WSNT_NS);
            messageToNotify.declareNamespace(NameSpaceConstants.WSNT_NS);
            messageToNotify.declareNamespace(NameSpaceConstants.WSA_NS);
            OMElement notificationMesssageEl = factory.createOMElement("NotificationMessage",
                    messageToNotify.getNamespace(), messageToNotify);

            notificationMesssageEl.addChild(topicExpEl);

            notificationMesssageEl.addChild(EndpointReferenceHelper.toOM(factory, producerReference, new QName(
                    notificationMesssageEl.getNamespace().getNamespaceURI(), "ProducerReference",
                    notificationMesssageEl.getNamespace().getPrefix()), NameSpaceConstants.WSA_NS.getNamespaceURI()));

            OMElement messageEl = factory.createOMElement("Message", notificationMesssageEl.getNamespace(),
                    notificationMesssageEl);

            messageEl.addChild(message);
            return messageToNotify;
        }

    }

}
