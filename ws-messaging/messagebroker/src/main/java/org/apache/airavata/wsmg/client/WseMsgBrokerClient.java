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

import org.apache.airavata.wsmg.broker.wseventing.WSEProtocolSupport;
import org.apache.airavata.wsmg.commons.CommonRoutines;
import org.apache.airavata.wsmg.commons.NotificationProducer;
import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.airavata.wsmg.commons.WsmgNameSpaceConstants;
import org.apache.airavata.wsmg.util.WsmgUtil;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;

public class WseMsgBrokerClient implements MessageBrokerClient {

    private EndpointReference brokerEndpointRef = null;

    protected long timeoutInMilliSeconds = WsmgCommonConstants.DEFAULT_CLIENT_SOCKET_TIME_OUT_MILLIES;

    final static String WIDGET_NS_PREFIX = WsmgNameSpaceConstants.WIDGET_NS.getPrefix() + ":";

    protected NotificationProducer notificationProducer = new NotificationProducer();
    protected ConsumerServerHandler consumerServerHandler = new ConsumerServerHandler();

    public EndpointReference createEndpointReference(String brokerURL, String topic) {

        return WSEProtocolSupport.Client.createEndpointReference(brokerURL, topic);
    }

    public void init(String brokerLocation) {

        brokerEndpointRef = new EndpointReference(WsmgUtil.formatURLString(brokerLocation));

    }

    public void publish(String topic, String plainText) throws MsgBrokerClientException {

        OMFactory factory = OMAbstractFactory.getOMFactory();

        OMElement wrappedMsg = factory.createOMElement(WsmgCommonConstants.WSMG_PLAIN_TEXT_WRAPPER,
                WsmgNameSpaceConstants.WSMG_NS);

        wrappedMsg.setText(plainText);
        publish(topic, wrappedMsg);
    }

    public void publish(String topic, OMElement message) throws MsgBrokerClientException {

        OMFactory factory = OMAbstractFactory.getOMFactory();

        OMElement topicExpressionEl = null;

        if (topic != null) {
            topicExpressionEl = factory.createOMElement("Topic", WsmgNameSpaceConstants.WSNT_NS);
            topicExpressionEl.addAttribute("Dialect", WsmgCommonConstants.TOPIC_EXPRESSION_SIMPLE_DIALECT, null);
            topicExpressionEl.declareNamespace(WsmgNameSpaceConstants.WIDGET_NS);
            topicExpressionEl.setText(WIDGET_NS_PREFIX + topic);

        }

        try {
            notificationProducer.deliverMessage(message, "wse", brokerEndpointRef, getTimeoutInMilliSeconds(),
                    topicExpressionEl);
        } catch (AxisFault e) {
            throw new MsgBrokerClientException("unable to publish msg", e);
        }
    }

    public String subscribe(String eventSinkLocation, String topicExpression, String xpathExpression)
            throws MsgBrokerClientException {
        return subscribe(new EndpointReference(eventSinkLocation), topicExpression, xpathExpression);
    }

    public String subscribe(EndpointReference eventSinkLocation, String topicExpression, String xpathExpression)
            throws MsgBrokerClientException {

        return subscribe(eventSinkLocation, topicExpression, xpathExpression,
                WsmgCommonConstants.DEFAULT_SUBSCRIPTION_EXPIRATION_TIME);
    }

    public String subscribe(EndpointReference eventSinkLocation, String topicExpression, String xpathExpression,
            long expireTime) throws MsgBrokerClientException {

        String subscriptionId = null;

        try {
            OMElement subscriptionMsg = WSEProtocolSupport.Client.createSubscription(eventSinkLocation,
                    topicExpression, xpathExpression, expireTime);
            ServiceClient serviceClient = configureServiceClientForSubscription(subscriptionMsg);
            OMElement responseMessage = serviceClient.sendReceive(subscriptionMsg);
            serviceClient.cleanupTransport();

            if (responseMessage == null) {
                throw new MsgBrokerClientException("no response recieved for subscription message");
            }

            OMElement responseSubscriptionsManagerElement = responseMessage.getFirstChildWithName(new QName(
                    WsmgNameSpaceConstants.WSE_NS.getNamespaceURI(), "SubscriptionManager"));

            subscriptionId = WSEProtocolSupport.Client.decodeSubscribeResponse(responseSubscriptionsManagerElement);

        } catch (AxisFault e) {
            throw new MsgBrokerClientException("unable to send the subscription", e);
        }

        return subscriptionId;
    }

    public String subscribeMsgBox(EndpointReference msgBoxEpr, String topicExpression, String xpathExpression,
            long expireTime) throws MsgBrokerClientException {

        String msgBoxEventSink = msgBoxEpr.getAddress();

        String formattedEventSink = null;

        if (msgBoxEpr.getAddress().contains("clientid")) {
            formattedEventSink = msgBoxEventSink;
        } else {
            if (msgBoxEpr.getAllReferenceParameters() == null)
                throw new MsgBrokerClientException("Invalid Message Box EPR, no reference parameters found");
            String msgBoxId = msgBoxEpr.getAllReferenceParameters()
                    .get(new QName("http://org.apache.airavata/xgws/msgbox/2004/", "MsgBoxAddr")).getText();
            if (msgBoxId == null)
                throw new MsgBrokerClientException("Invalid Message Box EPR, reference parameter MsgBoxAddr is missing");
            String format = msgBoxEventSink.endsWith("/") ? "%sclientid/%s" : "%s/clientid/%s";

            formattedEventSink = String.format(format, msgBoxEventSink, msgBoxId);

        }

        return subscribe(new EndpointReference(formattedEventSink), topicExpression, xpathExpression, expireTime);

    }

    public boolean unSubscribe(String subscriptionId) throws MsgBrokerClientException {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement message = WSEProtocolSupport.Client.createUnsubscribeMsg();

        OMElement identifierEl = factory.createOMElement("Identifier", message.getNamespace());

        identifierEl.setText(subscriptionId);
        String soapAction = message.getNamespace().getNamespaceURI() + "/" + message.getLocalName();

        Options opts = CommonRoutines.getOptions(soapAction, getTimeoutInMilliSeconds(), brokerEndpointRef);

        try {
            ServiceClient client = new ServiceClient();
            client.setOptions(opts);
            CommonRoutines.setHeaders(soapAction, brokerEndpointRef.getAddress(), client, identifierEl);

            client.sendReceive(message);
            client.cleanupTransport();

        } catch (AxisFault e) {
            throw new MsgBrokerClientException("unableto send subscribe msg", e);
        }

        return true;
    }

    private ServiceClient configureServiceClientForSubscription(OMElement message) throws AxisFault {

        String soapAction = message.getNamespace().getNamespaceURI() + "/" + message.getLocalName();

        Options opts = CommonRoutines.getOptions(soapAction, getTimeoutInMilliSeconds(), brokerEndpointRef);

        ServiceClient client = new ServiceClient();
        client.setOptions(opts);
        CommonRoutines.setHeaders(soapAction, brokerEndpointRef.getAddress(), client);
        return client;
    }

    public long getTimeoutInMilliSeconds() {
        return timeoutInMilliSeconds;
    }

    public void setTimeoutInMilliSeconds(long timeout) {
        timeoutInMilliSeconds = timeout;
    }

    public String[] startConsumerService(int port, ConsumerNotificationHandler handler) throws MsgBrokerClientException {

        consumerServerHandler.createConsumerServer(port, handler);
        return consumerServerHandler.getConsumerServiceEndpointReference();

    }

    public void shutdownConsumerService() {
        consumerServerHandler.shutdownConsumerService();
    }

}
