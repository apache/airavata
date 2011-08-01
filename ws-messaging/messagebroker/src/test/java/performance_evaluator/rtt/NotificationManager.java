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

package performance_evaluator.rtt;

import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.airavata.wsmg.client.*;
import org.apache.axiom.soap.SOAPEnvelope;

public class NotificationManager implements ConsumerNotificationHandler {

    private MessageBrokerClient client = null;
    private String[] eprs = null;
    private String brokerLocation = null;
    private String protocol = null;
    private int consumerServerPort = 0;
    private LinkedList<String> subscriptionIds;
    private int numberOfTopicSubscribed = 0;
    private int numMultiThreadSupportPerSub = 0;
    private int multipleThreadSupportIndex = 1;

    public NotificationManager(String brokerLocationIn, int consumerServerPortIn, String protocolIn,
            int numMultiThreadSupportPerSub) throws MsgBrokerClientException {

        this.brokerLocation = brokerLocationIn;
        this.consumerServerPort = consumerServerPortIn;
        this.protocol = protocolIn;
        this.numMultiThreadSupportPerSub = numMultiThreadSupportPerSub;

        if (client == null) {
            if (protocol.equalsIgnoreCase("wse")) {
                WsntMsgBrokerClient wseClient = new WsntMsgBrokerClient();
                wseClient.init(this.brokerLocation);
                wseClient.setTimeoutInMilliSeconds(200000000);
                eprs = wseClient.startConsumerService(consumerServerPort, this);
                client = wseClient;
            } else {
                WsntMsgBrokerClient wsntClient = new WsntMsgBrokerClient();
                wsntClient.init(this.brokerLocation);
                wsntClient.setTimeoutInMilliSeconds(200000000);
                eprs = wsntClient.startConsumerService(consumerServerPort, this);
                client = wsntClient;
            }
        }

        subscriptionIds = new LinkedList<String>();

    }

    public Subscription createTopicSubscription(String topic) throws Exception {

        if (multipleThreadSupportIndex > numMultiThreadSupportPerSub) {
            multipleThreadSupportIndex = 1;
        }

        String subscriptionId = client
                .subscribe(brokerLocation, eprs[0] + "user" + multipleThreadSupportIndex++, topic);
        subscriptionIds.add(subscriptionId);
        Subscription subscription = new Subscription(client, subscriptionId, topic, this, brokerLocation, protocol);
        return subscription;
    }

    public Subscription createXpathSubscription(String topicExpression, String xpathExpression) throws Exception {
        if (multipleThreadSupportIndex > numMultiThreadSupportPerSub) {
            multipleThreadSupportIndex = 1;
        }

        String subscriptionId = client.subscribe(eprs[0] + "user" + multipleThreadSupportIndex++,
                topicExpression, xpathExpression);
        subscriptionIds.add(subscriptionId);
        Subscription subscription = new Subscription(client, subscriptionId, topicExpression, xpathExpression, this,
                brokerLocation, protocol);
        return subscription;
    }

    public void cleanup() throws MsgBrokerClientException {

        WseMsgBrokerClient wseClient = null;
        WsntMsgBrokerClient wsntClient = null;

        if ("wse".equalsIgnoreCase(this.protocol)) {
            wseClient = (WseMsgBrokerClient) client;
        } else {
            wsntClient = (WsntMsgBrokerClient) client;
        }

        if (subscriptionIds != null) {
            if (wseClient != null) {
                while (!subscriptionIds.isEmpty()) {
                    String subId = subscriptionIds.remove();
                    wseClient.unSubscribe(subId);
                }
            } else {
                while (!subscriptionIds.isEmpty()) {
                    String subId = subscriptionIds.remove();
                    wsntClient.unSubscribe(subId);
                }

            }
        }

        if (client != null) {
            client.shutdownConsumerService();
        }
    }

    private BlockingQueue<StatContainer> queue = new LinkedBlockingQueue<StatContainer>();
    private int numMsgsReceived = 0;

    public void handleNotification(SOAPEnvelope msgEnvelope) {
        queue.add(new StatContainer(msgEnvelope));
        numMsgsReceived += 1;
    }

    public BlockingQueue<StatContainer> getQueue() {
        return queue;
    }

    public int getNumberOfMsgsReceived() {
        return numMsgsReceived;
    }

    public synchronized void incNoTopicsSubscribed() {
        numberOfTopicSubscribed++;
    }

    public synchronized int getNoTopicsSubscribed() {
        return numberOfTopicSubscribed;
    }
}
