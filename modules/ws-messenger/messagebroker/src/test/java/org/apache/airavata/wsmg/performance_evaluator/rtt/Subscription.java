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

package org.apache.airavata.wsmg.performance_evaluator.rtt;

import org.apache.airavata.wsmg.client.ConsumerNotificationHandler;
import org.apache.airavata.wsmg.client.MessageBrokerClient;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Subscription {

    private String subscriptionID;

    private String topic;
    private static final Log logger = LogFactory.getLog(Subscription.class);
    private ConsumerNotificationHandler handler;
    private MessageBrokerClient client;
    private EndpointReference messageBoxEPR;
    private String xpath;
    private String brokerURL;

    private String protocol;

    public Subscription(MessageBrokerClient clientIn, String subscriptionID, String topic,
            ConsumerNotificationHandler callback, String brokerURL, String protocolIn) {
        super();
        this.subscriptionID = subscriptionID;
        this.topic = topic;
        this.handler = callback;
        this.brokerURL = brokerURL;
        this.client = clientIn;
        this.protocol = protocolIn;
    }

    public Subscription(MessageBrokerClient clientIn, String subscriptionID, String topic, String xpath,
            ConsumerNotificationHandler callback, String brokerURL, String protocolIn) {
        super();
        this.client = clientIn;
        this.subscriptionID = subscriptionID;
        this.topic = topic;
        this.handler = callback;
        this.brokerURL = brokerURL;
        this.xpath = xpath;
        this.protocol = protocolIn;
    }

    public ConsumerNotificationHandler getCallback() {
        return handler;
    }

    public String getTopic() {
        return topic;
    }

    // public void destroy() throws RemoteException {
    // client.shutdownConsumerService();
    // }

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

    // public String getConsumerEPR() throws UnknownHostException {
    // cli
    // }

}
