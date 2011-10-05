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

package org.apache.airavata.workflow.tracking.client;

import java.rmi.RemoteException;

import org.apache.airavata.workflow.tracking.impl.subscription.MessageBoxNotificationHandler;
import org.apache.airavata.wsmg.client.ConsumerServer;
import org.apache.airavata.wsmg.client.MsgBrokerClientException;
import org.apache.airavata.wsmg.client.WseMsgBrokerClient;
import org.apache.axis2.addressing.EndpointReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Subscription {

    private static final Logger logger = LoggerFactory.getLogger(Subscription.class);
    
    private String subscriptionID;

    private String topic;

    private Callback callback;

    private ConsumerServer xs;

    private MessageBoxNotificationHandler messageBoxNotificationHandler;

    private EndpointReference messageBoxEPR;

    private String brokerURL;

    public Subscription(MessageBoxNotificationHandler messageBoxNotificationHandler, String subscriptionID,
            String topic, Callback callback, String brokerURL) {
        this.messageBoxNotificationHandler = messageBoxNotificationHandler;
        this.subscriptionID = subscriptionID;
        this.topic = topic;
        this.callback = callback;
        this.brokerURL = brokerURL;
    }

    public Subscription(ConsumerServer xs, String subscriptionID, String topic, Callback callback, String brokerURL) {
        super();
        this.xs = xs;
        this.subscriptionID = subscriptionID;
        this.topic = topic;
        this.callback = callback;
        this.brokerURL = brokerURL;
    }

    public Callback getCallback() {
        return callback;
    }

    public String getTopic() {
        return topic;
    }

    public void destroy() throws RemoteException {
        if (this.xs != null) {
            xs.stop();
            WseMsgBrokerClient client = new WseMsgBrokerClient();
            client.init(this.brokerURL);
            try {
                client.unSubscribe(this.subscriptionID);
            } catch (MsgBrokerClientException e) {
                logger.error("axisFault occured on unsubscribing subscription ID :" + this.subscriptionID, e);
                e.printStackTrace();
            }
        } else if (this.messageBoxNotificationHandler != null) {
            this.messageBoxNotificationHandler.destroy(messageBoxEPR);
        }
    }

    public EndpointReference getMessageBoxEPR() {
        return messageBoxEPR;
    }

    public void setMessageBoxEpr(EndpointReference messageBoxEPR) {
        this.messageBoxEPR = messageBoxEPR;
    }

    public String getSubscriptionID() {
        return subscriptionID;
    }

    public void setSubscriptionID(String subscriptionID) {
        this.subscriptionID = subscriptionID;
    }

    public String getBrokerURL() {
        return brokerURL;
    }

    public void setBrokerURL(String brokerURL) {
        this.brokerURL = brokerURL;
    }

    public String getBrokerPublishEPR() {
        return LeadNotificationManager.getBrokerPublishEPR(this.brokerURL, this.topic);
    }

    public String getConsumerEPR() {

        String ret = null;

        if (null != xs) {

            String[] eprs = xs.getConsumerServiceEPRs();
            if (eprs.length > 0)
                ret = eprs[0];

        }
        return ret;
    }

}
