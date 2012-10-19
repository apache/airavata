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

package org.apache.airavata.workflow.tracking.impl.subscription;

import java.rmi.RemoteException;

import org.apache.airavata.workflow.tracking.WorkflowTrackingException;
import org.apache.airavata.workflow.tracking.client.Callback;
import org.apache.airavata.workflow.tracking.client.NotificationType;
import org.apache.airavata.workflow.tracking.client.Subscription;
import org.apache.airavata.workflow.tracking.util.MessageUtil;
import org.apache.airavata.wsmg.client.MsgBrokerClientException;
import org.apache.airavata.wsmg.client.NotificationHandler;
import org.apache.airavata.wsmg.client.WseMsgBrokerClient;
import org.apache.airavata.wsmg.client.msgbox.MessagePuller;
import org.apache.airavata.wsmg.client.msgbox.MsgboxHandler;
import org.apache.airavata.wsmg.commons.MsgBoxQNameConstants;
import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MessageBoxNotificationHandler implements NotificationHandler {

    private static final Log logger = LogFactory.getLog(MessageBoxNotificationHandler.class);

    private String messageBoxUrl;

    private String brokerURL;

    private String subscriptionId;

    private MessagePuller messagePuller;

    private Callback callback;

    private String topic;

    public MessageBoxNotificationHandler(String messageBoxUrl, String brokerURL) {
        if (messageBoxUrl == null || "".equals(messageBoxUrl)) {
            logger.error("Invalid messagebox Location :" + messageBoxUrl);
            throw new WorkflowTrackingException("BrokerLocation should be not null messaboxUrl:" + messageBoxUrl);
        }

        if (brokerURL == null || "".equals(brokerURL)) {
            logger.error("Invalid broker Location :" + brokerURL);
            throw new WorkflowTrackingException("BrokerLocation should be not null brokerurl:" + brokerURL);
        }
        this.messageBoxUrl = messageBoxUrl;
        this.brokerURL = brokerURL;

    }

    public void handleNotification(String message) {
        XmlObject messageObj = null;

        try {
            messageObj = XmlObject.Factory.parse(message);
            XmlCursor xc = messageObj.newCursor();
            xc.toNextToken();

            xc.dispose();
        } catch (XmlException e) {
            logger.error("error parsing message content: " + message, e);
            e.printStackTrace();
        }
        NotificationType type = MessageUtil.getType(messageObj);
        this.callback.deliverMessage(this.topic, type, messageObj);

    }

    public void destroy(EndpointReference msgBoxEpr) throws RemoteException {
        if (this.messagePuller != null) {
            messagePuller.stopPulling();

            if (logger.isDebugEnabled())
                logger.info("\n\nStopping the Messagebox for topic" + this.topic);
        }

        try {
            WseMsgBrokerClient client = new WseMsgBrokerClient();
            client.init(this.brokerURL.toString());
            client.unSubscribe(this.subscriptionId);

            MsgboxHandler msgboxHandler = new MsgboxHandler();

            logger.info("Unsubscribing the messagebox that was destroyed," + " SubscriptionID:" + this.subscriptionId);

            msgboxHandler.deleteMsgBox(msgBoxEpr, 12000L);

        } catch (MsgBrokerClientException e) {

            logger.error("unable to unsubscribe", e);
            e.printStackTrace();
        }

    }

    public Subscription renewMessageboxSubscription(String epr, String subscriptionId, String topic, String xpath,
            boolean subscribePermanatly) throws MsgBrokerClientException {

        this.subscriptionId = subscriptionId;
        this.topic = topic;
        WseMsgBrokerClient wseClient = new WseMsgBrokerClient();
        EndpointReference endpointReference = null;
        try {
            endpointReference = EndpointReferenceHelper.fromString(epr);
        } catch (AxisFault f) {
            throw new MsgBrokerClientException("unable to convert end point reference", f);
        }
        subscriptionId = subscribeToBroker(endpointReference.getAddress(), topic, xpath, wseClient, subscribePermanatly);
        Subscription subscription = new Subscription(this, subscriptionId, topic, callback, this.brokerURL);
        subscription.setMessageBoxEpr(endpointReference);
        return subscription;
    }

    public Subscription renewMessageboxSubscription(EndpointReference endpointReference, String subscriptionId,
            String topic, String xpath, boolean subscribePermanatly) throws MsgBrokerClientException {

        this.subscriptionId = subscriptionId;
        this.topic = topic;
        WseMsgBrokerClient wseClient = new WseMsgBrokerClient();
        logger.info("\n\nCreate Subscription for topic" + topic + " [Messagebox]\n\n");

        subscriptionId = subscribeToBroker(endpointReference.getAddress(), topic, xpath, wseClient, subscribePermanatly);
        Subscription subscription = new Subscription(this, subscriptionId, topic, callback, this.brokerURL);
        subscription.setMessageBoxEpr(endpointReference);
        return subscription;
    }

    public Subscription startListeningToPreviousMessageBox(EndpointReference msgBoxAddr, String subscriptionId,
            String topic, String xpath, Callback callback, boolean subscribePermanatly) throws MsgBrokerClientException {
        this.callback = callback;
        this.subscriptionId = subscriptionId;
        this.topic = topic;
        WseMsgBrokerClient wseClient = new WseMsgBrokerClient();
        MsgboxHandler msgboxHandler = new MsgboxHandler();

        messagePuller = msgboxHandler.startPullingFromExistingMsgBox(msgBoxAddr, this, 500L, 1000L);
        if (logger.isDebugEnabled())
            logger.info("\n\nCreate Subscription for topic" + topic + " [Messagebox]\n\n");
        String msgBoxEventSink = msgBoxAddr.getAddress();

        String formattedEventSink = null;

        if (msgBoxEventSink.contains("clientid")) {
            formattedEventSink = msgBoxEventSink;
        } else {
            if (msgBoxAddr.getAllReferenceParameters() == null)
                throw new MsgBrokerClientException("Invalid Message Box EPR, no reference parameters found");
            String msgBoxId = msgBoxAddr.getAllReferenceParameters().get(MsgBoxQNameConstants.MSG_BOXID_QNAME)
                    .getText();
            if (msgBoxId == null)
                throw new MsgBrokerClientException("Invalid Message Box EPR, reference parameter MsgBoxAddr is missing");
            String format = msgBoxEventSink.endsWith("/") ? "%sclientid/%s" : "%s/clientid/%s";

            formattedEventSink = String.format(format, msgBoxEventSink, msgBoxId);

        }

        subscriptionId = subscribeToBroker(formattedEventSink, topic, xpath, wseClient, subscribePermanatly);
        Subscription subscription = new Subscription(this, subscriptionId, topic, callback, this.brokerURL);
        subscription.setMessageBoxEpr(msgBoxAddr);
        return subscription;

    }

    private String subscribeToBroker(String eventSink, String topic, String xpath, WseMsgBrokerClient wseClient,
            boolean subscribePermanatly) throws MsgBrokerClientException {
        String subId = null;

        wseClient.init(brokerURL);
        if (subscribePermanatly) {

            subId = wseClient.subscribe(new EndpointReference(eventSink), topic, xpath, -1);
        } else {
            subId = wseClient.subscribe(eventSink, topic, xpath);
        }
        return subId;
    }

    private String subToBrokerWithMsgBoxSink(EndpointReference msgBoxEpr, String topic, String xpath,
            WseMsgBrokerClient wseClient, boolean subscribePermanatly) throws MsgBrokerClientException {
        String subId;
        wseClient.init(brokerURL);

        if (subscribePermanatly) {

            subId = wseClient.subscribeMsgBox(msgBoxEpr, topic, xpath, -1);
        } else {
            subId = wseClient.subscribeMsgBox(msgBoxEpr, topic, xpath,
                    WsmgCommonConstants.DEFAULT_SUBSCRIPTION_EXPIRATION_TIME);
        }
        return subId;
    }

    public Subscription createSubscription(String topic, String xpath, Callback callback, boolean subscribePermananly)
            throws Exception {
        this.topic = topic;
        this.callback = callback;

        WseMsgBrokerClient wseClient = new WseMsgBrokerClient();
        MsgboxHandler msgboxHandler = new MsgboxHandler();
        EndpointReference msgBoxAddr = msgboxHandler.createPullMsgBox(this.messageBoxUrl, 12000l);

        String messageBoxAddress = msgBoxAddr.getAddress();
        if (logger.isDebugEnabled())
            logger.debug("\n\nCreated Messagebox at address :" + messageBoxAddress);

        subscriptionId = subToBrokerWithMsgBoxSink(msgBoxAddr, topic, xpath, wseClient, subscribePermananly);
        messagePuller = msgboxHandler.startPullingEventsFromMsgBox(msgBoxAddr, this, 1500L, 30000l);
        if (logger.isDebugEnabled())
            logger.debug("\n\nCreate Subscription for topic" + topic + " [Messagebox]\n\n");

        Subscription subscription = new Subscription(this, subscriptionId, topic, callback, this.brokerURL);
        subscription.setMessageBoxEpr(msgBoxAddr);
        subscription.setBrokerURL(this.brokerURL);
        return subscription;
    }

    public Subscription createMsgBoxSubscription(String topic2, String xpath, Callback callback2,
            boolean subscribePermanatly) throws MsgBrokerClientException {

        this.topic = topic2;
        this.callback = callback2;

        WseMsgBrokerClient wseClient = new WseMsgBrokerClient();
        MsgboxHandler msgboxHandler = new MsgboxHandler();
        EndpointReference msgBoxAddr = msgboxHandler.createPullMsgBox(this.messageBoxUrl, 12000l);
        if (logger.isDebugEnabled())
            logger.info("\n\nCreated Messagebox at address :" + msgBoxAddr.getAddress());

        subscriptionId = subToBrokerWithMsgBoxSink(msgBoxAddr, topic, xpath, wseClient, subscribePermanatly);
        messagePuller = msgboxHandler.startPullingEventsFromMsgBox(msgBoxAddr, this, 500L, 30000l);
        if (logger.isDebugEnabled())
            logger.info("\n\nCreate Subscription for topic" + topic + " [Messagebox]\n\n");
        Subscription subscription = new Subscription(this, subscriptionId, topic, callback, this.brokerURL);
        subscription.setMessageBoxEpr(msgBoxAddr);
        subscription.setBrokerURL(this.brokerURL);
        return subscription;

    }

}
