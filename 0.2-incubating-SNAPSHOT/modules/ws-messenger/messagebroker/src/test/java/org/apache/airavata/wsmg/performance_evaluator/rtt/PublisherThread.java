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

import java.util.concurrent.CountDownLatch;

import org.apache.airavata.wsmg.client.*;

public class PublisherThread extends Thread {
    private String brokerURL;
    private String topic;
    private final CountDownLatch startSignal;
    private final CountDownLatch doneSignal;
    private long totPublishTime = 0l;
    long avgPublishTime = 0l;

    private String payload = "";
    String msg = "";
    private MessageBrokerClient client = null;
    int trackId = 0;
    int threadId = 0;

    public PublisherThread(String protocolIn, String brokerURLIn, String topicIn, String payloadIn,
            CountDownLatch startSignalIn, CountDownLatch doneSignalIn, int threadIdIn) {
        this.payload = payloadIn;
        this.brokerURL = brokerURLIn;
        this.topic = topicIn;
        this.startSignal = startSignalIn;
        this.doneSignal = doneSignalIn;
        this.threadId = threadIdIn;
        if ("wse".equalsIgnoreCase(protocolIn)) {

            WseMsgBrokerClient wseMsgBrokerClient = new WseMsgBrokerClient();
            wseMsgBrokerClient.setTimeoutInMilliSeconds(0);
            wseMsgBrokerClient.init(brokerURL);
            client = wseMsgBrokerClient;

        } else {

            WsntMsgBrokerClient wsntMsgBrokerClient = new WsntMsgBrokerClient();
            wsntMsgBrokerClient.setTimeoutInMilliSeconds(0);
            wsntMsgBrokerClient.init(brokerURL);
            client = wsntMsgBrokerClient;
        }

    }

    public void run() {

        try {
            trackId = 1;
            startSignal.await();
            System.out.println("Publishing started for topic :" + this.topic);
            for (int i = 0; i < PerformanceTest.NOTIFICATIONS_PUBLISHED_PER_TOPIC; i++) {
                msg = "<perf:performancetest xmlns:perf=\"http://lead.extreme.indiana.edu/namespaces/performance\"><perf:time>"
                        + System.currentTimeMillis()
                        + "</perf:time><perf:trackInfo><perf:threadId>"
                        + threadId
                        + "</perf:threadId><perf:trackId>"
                        + trackId
                        + "</perf:trackId></perf:trackInfo>"
                        + "<perf:payload>" + payload + "</perf:payload></perf:performancetest>";
                long publishStartTime = System.currentTimeMillis();
                client.publish(topic, msg);
                totPublishTime += System.currentTimeMillis() - publishStartTime;
                trackId++;
            }

            avgPublishTime = totPublishTime / PerformanceTest.NOTIFICATIONS_PUBLISHED_PER_TOPIC;
            System.out.println("Publishing ended for topic :" + this.topic);
            doneSignal.countDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    synchronized long getAvgPubTime() {
        return this.avgPublishTime;
    }
}
