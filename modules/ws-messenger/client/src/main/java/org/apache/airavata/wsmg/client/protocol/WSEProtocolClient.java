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

package org.apache.airavata.wsmg.client.protocol;

import javax.xml.namespace.QName;

import org.apache.airavata.wsmg.commons.NameSpaceConstants;
import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.EndpointReferenceHelper;

public class WSEProtocolClient {

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

    private static OMElement createFilter(String topicExpression, String xpathExpression) {

        boolean hasTopicExpression = (topicExpression != null && topicExpression.length() != 0);
        boolean hasXPathExpression = (xpathExpression != null && xpathExpression.length() != 0);

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement filterEl = null;

        if (hasTopicExpression && hasXPathExpression) {
            filterEl = factory.createOMElement("Filter", NameSpaceConstants.WSE_NS);

            filterEl.addAttribute("Dialect", WsmgCommonConstants.TOPIC_AND_XPATH_DIALECT, null);
            OMElement topicExpressionEl = factory.createOMElement("TopicExpression", NameSpaceConstants.WSNT_NS);
            topicExpressionEl.addAttribute("Dialect", WsmgCommonConstants.TOPIC_EXPRESSION_SIMPLE_DIALECT, null);
            topicExpressionEl.declareNamespace(NameSpaceConstants.WIDGET_NS);
            topicExpressionEl.setText(NameSpaceConstants.WIDGET_NS.getPrefix() + ":" + topicExpression);
            filterEl.addChild(topicExpressionEl);
            OMElement xpathEl = factory.createOMElement("MessageContent", NameSpaceConstants.WSNT_NS);
            xpathEl.addAttribute("Dialect", WsmgCommonConstants.XPATH_DIALECT, null);
            xpathEl.setText(xpathExpression);
            filterEl.addChild(xpathEl);
        } else if (hasTopicExpression) {
            filterEl = factory.createOMElement("Filter", NameSpaceConstants.WSE_NS);

            filterEl.addAttribute("Dialect", WsmgCommonConstants.TOPIC_EXPRESSION_SIMPLE_DIALECT, null);
            filterEl.declareNamespace(NameSpaceConstants.WIDGET_NS);
            filterEl.setText(NameSpaceConstants.WIDGET_NS.getPrefix() + ":" + topicExpression);
        } else if (hasXPathExpression) {
            filterEl = factory.createOMElement("Filter", NameSpaceConstants.WSE_NS);

            filterEl.addAttribute("Dialect", WsmgCommonConstants.XPATH_DIALECT, null);
            filterEl.setText(xpathExpression);
        }

        return filterEl;
    }

    public static OMElement createSubscription(EndpointReference eventSink, String topicExpression,
            String xpathExpression, long expireTime) throws AxisFault {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement subscribeMsg = factory.createOMElement("Subscribe", NameSpaceConstants.WSE_NS);

        OMElement delivery = factory.createOMElement("Delivery", NameSpaceConstants.WSE_NS);

        OMElement expires = factory.createOMElement("Expires", NameSpaceConstants.WSE_NS);
        expires.setText(Long.toString(expireTime));
        subscribeMsg.addChild(expires);

        OMElement notifyTo = EndpointReferenceHelper.toOM(factory, eventSink,
                new QName(NameSpaceConstants.WSE_NS.getNamespaceURI(), "NotifyTo"),
                NameSpaceConstants.WSA_NS.getNamespaceURI());

        delivery.addChild(notifyTo);
        subscribeMsg.addChild(delivery);

        OMElement filterEl = createFilter(topicExpression, xpathExpression);

        if (filterEl != null) {
            subscribeMsg.addChild(filterEl);
        }

        subscribeMsg.declareNamespace(NameSpaceConstants.WSA_NS);

        return subscribeMsg;
    }

    public static String decodeSubscribeResponse(OMElement responseSubscriptionsManagerElement) throws AxisFault {
        String subscriptionId = null;
        OMElement referencePropertiesEl = responseSubscriptionsManagerElement.getFirstChildWithName(new QName(
                NameSpaceConstants.WSA_NS.getNamespaceURI(), "ReferenceProperties"));

        if (referencePropertiesEl == null) {
            referencePropertiesEl = responseSubscriptionsManagerElement.getFirstChildWithName(new QName(
                    NameSpaceConstants.WSA_NS.getNamespaceURI(), "ReferenceParameters"));
        }

        OMElement identifierEl = referencePropertiesEl.getFirstChildWithName(new QName(NameSpaceConstants.WSE_NS
                .getNamespaceURI(), WsmgCommonConstants.SUBSCRIPTION_ID));

        if (identifierEl == null) {
            throw new AxisFault("invalid response message, subscription id was not sent by broker");
        }

        subscriptionId = identifierEl.getText();
        return subscriptionId;
    }

    public static OMElement createUnsubscribeMsg() {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement message = factory.createOMElement("Unsubscribe", NameSpaceConstants.WSE_NS);

        return message;
    }

}