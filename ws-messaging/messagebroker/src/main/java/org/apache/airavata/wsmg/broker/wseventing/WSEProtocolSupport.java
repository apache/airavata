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

import java.util.Calendar;
import java.util.Date;

import javax.xml.namespace.QName;

import org.apache.airavata.wsmg.broker.context.ContextParameters;
import org.apache.airavata.wsmg.broker.context.ProcessingContext;
import org.apache.airavata.wsmg.broker.subscription.SubscriptionState;
import org.apache.airavata.wsmg.client.MsgBrokerClientException;
import org.apache.airavata.wsmg.commons.CommonRoutines;
import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.airavata.wsmg.commons.WsmgNameSpaceConstants;
import org.apache.airavata.wsmg.messenger.OutGoingQueue;
import org.apache.airavata.wsmg.util.WsmgUtil;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.log4j.Logger;

public class WSEProtocolSupport {

    Logger log = Logger.getLogger(WSEProtocolSupport.class);

    public SubscriptionState createSubscriptionState(ProcessingContext ctx, OutGoingQueue outgoingQueue)
            throws AxisFault {

        boolean neverExpire = false; // is true if expiration time is less than
        String topicLocalString = "";
        String xpathString = "";
        EndpointReference consumerReference = ctx.getContextParameter(ContextParameters.NOTIFY_TO_EPR);

        if (consumerReference == null) {
            throw new AxisFault("Only Push delivery Mode (NotifyTo) is supported in WSE");
        }

        String expireTimeString = ctx.getContextParameter(ContextParameters.SUBSCRIBER_EXPIRES);

        if (expireTimeString == null) {
            neverExpire = true;

        } else {

            long expireTime = Long.valueOf(expireTimeString);
            if (expireTime < 0) {
                neverExpire = true;
            }
        }

        OMElement filterEl = ctx.getContextParameter(ContextParameters.FILTER_ELEMENT);

        if (filterEl == null) {

            topicLocalString = ctx.getContextParameter(ContextParameters.TOPIC_FROM_URL);

            if (topicLocalString == null) {
                topicLocalString = WsmgCommonConstants.WILDCARD_TOPIC;
            }

            log.debug("got topicLocalString=" + topicLocalString);
            // topicLocalString = "wseTopic";
            // // a special topic, used in WSNT and JMS. Do not use a
            // wildcard topic here since wildcard string varies by system
        } else {

            String filterDialectAttrib = filterEl.getAttributeValue(new QName(null, "Dialect"));

            if (filterDialectAttrib.compareTo(WsmgCommonConstants.TOPIC_EXPRESSION_SIMPLE_DIALECT) == 0) {
                topicLocalString = WsmgUtil.getTopicLocalString(filterEl.getText()); // get what ever inside this
                                                                                     // element

                if (topicLocalString == null) {
                    throw new AxisFault("topic is not given in the subscription");
                }

            } else if (filterDialectAttrib.compareTo(WsmgCommonConstants.XPATH_DIALECT) == 0) {

                // use topicFromUrl if
                // was provided
                topicLocalString = ctx.getContextParameter(ContextParameters.TOPIC_FROM_URL);

                xpathString = filterEl.getText();

                log.debug("got topicLocalString=" + topicLocalString + " xpathString=" + xpathString);

                // TODO: Add XPath canonicalization here in the parsing. To
                // generate a
                // canonicalized XPath string
                // Possibly use Query query =
                // XPQuery.parseQuery(xpathExpression, index);
                if (xpathString == null) {
                    throw new AxisFault("xpath expression is not given");
                }
            } else if (filterDialectAttrib.compareTo(WsmgCommonConstants.TOPIC_AND_XPATH_DIALECT) == 0) {
                OMElement topicEl = filterEl.getFirstChildWithName(new QName(WsmgNameSpaceConstants.WSNT_NS
                        .getNamespaceURI(), "TopicExpression"));
                if (topicEl != null) {
                    topicLocalString = WsmgUtil.getTopicLocalString(topicEl.getText());
                }
                OMElement xpathEl = filterEl.getFirstChildWithName(new QName(WsmgNameSpaceConstants.WSNT_NS
                        .getNamespaceURI(), "MessageContent"));
                if (xpathEl != null) {
                    xpathString = xpathEl.getText();
                    if (xpathString == null && topicLocalString == null) {
                        throw new AxisFault("Both topic string and " + "XPath String are null!");
                    }
                }
            } else {
                throw new AxisFault("Unkown dialect: ");
                // topicLocalString = "wseTopic"; //a special topic, used in
                // WSNT and JMS
            }

        }

        if (topicLocalString == null || topicLocalString.length() == 0) {
            topicLocalString = WsmgCommonConstants.WILDCARD_TOPIC;
        }

        // Create SubscriptionState Object
        SubscriptionState state = new SubscriptionState(consumerReference, true, false, topicLocalString, xpathString,
                "wse", outgoingQueue);

        state.setNeverExpire(neverExpire); // default false

        return state;
    }

    public void createSubscribeResponse(ProcessingContext ctx, String subId) throws AxisFault {

        OMFactory factory = OMAbstractFactory.getOMFactory();

        ctx.addResponseMsgNameSpaces(WsmgNameSpaceConstants.WSE_NS);

        OMElement responseMessage = factory.createOMElement("SubscribeResponse", WsmgNameSpaceConstants.WSE_NS);

        OMElement identifier = factory.createOMElement(WsmgCommonConstants.SUBSCRIPTION_ID,
                responseMessage.getNamespace());
        identifier.setText(subId);
        EndpointReference serviceLocationEndpointReference = new EndpointReference(ctx.getMessageContext()
                .getAxisService().getEndpointURL());

        serviceLocationEndpointReference.addReferenceParameter(identifier);

        OMElement expiresEl = factory.createOMElement("Expires", responseMessage.getNamespace(), responseMessage);

        Date expiration = getFutureExpirationDate();
        String dateString = CommonRoutines.getXsdDateTime(expiration);
        expiresEl.setText(dateString);

        OMElement subscriptionManagerEpr = null;
        try {

            subscriptionManagerEpr = EndpointReferenceHelper.toOM(factory, serviceLocationEndpointReference, new QName(
                    WsmgNameSpaceConstants.WSE_NS.getNamespaceURI(), "SubscriptionManager"),
                    WsmgNameSpaceConstants.WSA_NS.getNamespaceURI());

            responseMessage.addChild(subscriptionManagerEpr);
            subscriptionManagerEpr.setNamespace(responseMessage.getNamespace());
        } catch (AxisFault e) {
            // TODO Auto-generated catch block
            log.fatal("unable to resolve EPR from OM", e);
            throw e;
        }

        ctx.setRespMessage(responseMessage);
    }

    private Date getFutureExpirationDate() {
        // Get a Calendar for current locale and time zone
        Calendar cal = Calendar.getInstance();
        // currentDate.setDate(currentDate.getDate()+1);
        // Get a Date object that represents 30 days from now
        Date currentDate = new Date(); // Current date
        cal.setTime(currentDate); // Set it in the Calendar object
        cal.add(Calendar.DATE, 30); // Add 30 days
        Date expiration = cal.getTime(); // Retrieve the resulting date
        return expiration;
    }

    public static class Client {

        public static EndpointReference createEndpointReference(String brokerURL, String topic) {
            if (brokerURL == null) {
                throw new IllegalArgumentException("Broker URL is null.");
            }
            if (topic == null) {
                throw new IllegalArgumentException("Topic is null.");
            }

            String sinkLocation = brokerURL.endsWith("/") ? brokerURL + "topic/" + topic : brokerURL + "/topic/"
                    + topic;

            EndpointReference eventSinkReference = new EndpointReference(sinkLocation);
            return eventSinkReference;
        }

        private static OMElement createFilter(String topicExpression, String xpathExpression) {

            boolean hasTopicExpression = (topicExpression != null && topicExpression.length() != 0);
            boolean hasXPathExpression = (xpathExpression != null && xpathExpression.length() != 0);

            OMFactory factory = OMAbstractFactory.getOMFactory();
            OMElement filterEl = null;

            if (hasTopicExpression && hasXPathExpression) {
                filterEl = factory.createOMElement("Filter", WsmgNameSpaceConstants.WSE_NS);

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
            } else if (hasTopicExpression) {
                filterEl = factory.createOMElement("Filter", WsmgNameSpaceConstants.WSE_NS);

                filterEl.addAttribute("Dialect", WsmgCommonConstants.TOPIC_EXPRESSION_SIMPLE_DIALECT, null);
                filterEl.declareNamespace(WsmgNameSpaceConstants.WIDGET_NS);
                filterEl.setText(WsmgNameSpaceConstants.WIDGET_NS.getPrefix() + ":" + topicExpression);
            } else if (hasXPathExpression) {
                filterEl = factory.createOMElement("Filter", WsmgNameSpaceConstants.WSE_NS);

                filterEl.addAttribute("Dialect", WsmgCommonConstants.XPATH_DIALECT, null);
                filterEl.setText(xpathExpression);
            }

            return filterEl;
        }

        public static OMElement createSubscription(EndpointReference eventSink, String topicExpression,
                String xpathExpression, long expireTime) throws AxisFault {

            OMFactory factory = OMAbstractFactory.getOMFactory();
            OMElement subscribeMsg = factory.createOMElement("Subscribe", WsmgNameSpaceConstants.WSE_NS);

            OMElement delivery = factory.createOMElement("Delivery", WsmgNameSpaceConstants.WSE_NS);

            OMElement expires = factory.createOMElement("Expires", WsmgNameSpaceConstants.WSE_NS);
            expires.setText(Long.toString(expireTime));
            subscribeMsg.addChild(expires);

            OMElement notifyTo = EndpointReferenceHelper.toOM(factory, eventSink, new QName(
                    WsmgNameSpaceConstants.WSE_NS.getNamespaceURI(), "NotifyTo"), WsmgNameSpaceConstants.WSA_NS
                    .getNamespaceURI());

            delivery.addChild(notifyTo);
            subscribeMsg.addChild(delivery);

            OMElement filterEl = createFilter(topicExpression, xpathExpression);

            if (filterEl != null) {
                subscribeMsg.addChild(filterEl);
            }

            subscribeMsg.declareNamespace(WsmgNameSpaceConstants.WSA_NS);

            return subscribeMsg;
        }

        public static String decodeSubscribeResponse(OMElement responseSubscriptionsManagerElement)
                throws MsgBrokerClientException {
            String subscriptionId = null;
            OMElement referencePropertiesEl = responseSubscriptionsManagerElement.getFirstChildWithName(new QName(
                    WsmgNameSpaceConstants.WSA_NS.getNamespaceURI(), "ReferenceProperties"));

            if (referencePropertiesEl == null) {
                referencePropertiesEl = responseSubscriptionsManagerElement.getFirstChildWithName(new QName(
                        WsmgNameSpaceConstants.WSA_NS.getNamespaceURI(), "ReferenceParameters"));
            }

            OMElement identifierEl = referencePropertiesEl.getFirstChildWithName(new QName(
                    WsmgNameSpaceConstants.WSE_NS.getNamespaceURI(), WsmgCommonConstants.SUBSCRIPTION_ID));

            if (identifierEl == null) {
                throw new MsgBrokerClientException("invalid response message, subscription id was not sent by broker");
            }

            subscriptionId = identifierEl.getText();
            return subscriptionId;
        }

        public static OMElement createUnsubscribeMsg() {
            OMFactory factory = OMAbstractFactory.getOMFactory();
            OMElement message = factory.createOMElement("Unsubscribe", WsmgNameSpaceConstants.WSE_NS);

            return message;
        }

    }

}
