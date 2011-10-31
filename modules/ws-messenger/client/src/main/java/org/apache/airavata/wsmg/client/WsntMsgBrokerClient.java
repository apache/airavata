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

import org.apache.airavata.wsmg.client.commons.NotificationProducer;
import org.apache.airavata.wsmg.client.protocol.WSNTProtocolClient;
import org.apache.airavata.wsmg.client.util.ClientUtil;
import org.apache.airavata.wsmg.commons.CommonRoutines;
import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.airavata.wsmg.commons.NameSpaceConstants;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;

public class WsntMsgBrokerClient extends CommonMsgBrokerClient implements MessageBrokerClient {

    protected long timeoutInMilliSeconds = WsmgCommonConstants.DEFAULT_CLIENT_SOCKET_TIME_OUT_MILLIES;

    protected NotificationProducer notifProducer = new NotificationProducer();
    protected ConsumerServerHandler consumerServerHandler = new ConsumerServerHandler();

    private EndpointReference brokerEndpointRef = null;

    public static EndpointReference createEndpointReference(String brokerURL, String topic) {
        // TODO : implement
        return null;
    }

    public void init(String brokerLocation) {
        brokerEndpointRef = new EndpointReference(ClientUtil.formatURLString(brokerLocation));

    }

    public void publish(String topic, String plainText) throws MsgBrokerClientException {

        OMFactory factory = OMAbstractFactory.getOMFactory();

        OMElement wrappedMsg = factory.createOMElement(WsmgCommonConstants.WSMG_PLAIN_TEXT_WRAPPER,
                NameSpaceConstants.WSMG_NS);

        wrappedMsg.setText(plainText);
        publish(topic, wrappedMsg);

    }

    public void publish(String topic, OMElement message) throws MsgBrokerClientException {

        String producerURl = ClientUtil.formatURLString(ClientUtil.getHostIP());
        EndpointReference producerReference = new EndpointReference(producerURl);

        try {

            OMElement messageToNotify = WSNTProtocolClient.encodeNotification(topic, message, producerReference);

            notifProducer.deliverMessage(messageToNotify, "wsnt", brokerEndpointRef, getTimeoutInMilliSeconds());
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

            OMElement message = WSNTProtocolClient.createSubscriptionMsg(eventSinkLocation, topicExpression,
                    xpathExpression);

            ServiceClient client = createServiceClient(message);

            OMElement responseMessage = client.sendReceive(message);
            client.cleanupTransport();

            OMElement sr = responseMessage.getFirstChildWithName(new QName(
                    NameSpaceConstants.WSNT_NS.getNamespaceURI(), "SubscriptionReference"));

            if (sr == null) {
                throw new MsgBrokerClientException("unable to subscribe, invalid response returned by broker");
            }

            subscriptionId = WSNTProtocolClient.decodeSubscriptionResponse(sr);

        } catch (AxisFault f) {
            throw new MsgBrokerClientException("unable to send the subscription msg", f);
        }

        return subscriptionId;

    }

    private ServiceClient createServiceClient(OMElement message) throws AxisFault {

        String soapAction = message.getNamespace().getNamespaceURI() + "/" + message.getLocalName();

        Options opts = CommonRoutines.getOptions(soapAction, getTimeoutInMilliSeconds(), brokerEndpointRef);

        ServiceClient client = new ServiceClient();
        CommonRoutines.setHeaders(soapAction, brokerEndpointRef.getAddress(), client);

        client.setOptions(opts);

        return client;
    }

    public boolean unSubscribe(String subscriptionId) throws MsgBrokerClientException {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement message = WSNTProtocolClient.createUnsubscribeMsg();
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
            throw new MsgBrokerClientException("unable to send subscribe msg", e);
        }

        return true;
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
