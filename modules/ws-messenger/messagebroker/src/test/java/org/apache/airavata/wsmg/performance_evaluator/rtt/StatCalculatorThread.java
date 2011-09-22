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

import java.util.concurrent.TimeUnit;

public class StatCalculatorThread extends Thread {

    private NotificationManager notifManager = null;
    private long lastMsgReceivedTime = 0l;
    private long timeTot = 0;
    private long avgTime = 0;
    private int numberOfMssgsReceived = 0; // to avoid concurrency
    private long timeOutMillis;
    private int expectedNoMessages = 0;

    public StatCalculatorThread(NotificationManager notificationManager, long timeOutInMillis) throws Exception {
        this.timeOutMillis = timeOutInMillis;
        this.notifManager = notificationManager;
        expectedNoMessages = PerformanceTest.NOTIFICATIONS_PUBLISHED_PER_TOPIC
                * notificationManager.getNoTopicsSubscribed();
    }

    @Override
    public void run() {
        do {

            StatContainer container = null;
            try {
                container = notifManager.getQueue().poll(timeOutMillis, TimeUnit.MILLISECONDS);

                if (container != null) {
                    timeTot += container.getRondTripTime();
                    lastMsgReceivedTime = container.getMessageReceivedTime();
                    numberOfMssgsReceived++;
                    // ******un-comment in order to log trakId and message
                    // related other information*****
                    // if (logger.isInfoEnabled()) {
                    // trackInfo = env
                    // .getBody()
                    // .getFirstElement()
                    // .getFirstChildWithName(
                    // new QName(
                    // "http://lead.extreme.indiana.edu/namespaces/performance",
                    // "trackInfo")).toStringWithConsume();
                    // logger.info(trackInfo + "   Send time :" + time
                    // + "  Received time :" + System.currentTimeMillis());
                    // }
                } else {
                    System.out.println("stat calculator thread was interrupted");
                    break;
                }
            } catch (InterruptedException e1) {
                e1.printStackTrace();
                break;
            }

        } while (expectedNoMessages > numberOfMssgsReceived);

        if (numberOfMssgsReceived > 0) {
            avgTime = timeTot / numberOfMssgsReceived;
        } else {
            System.out.println("no messages received");
        }

        System.out.println("end of stat calculator");
    }

    synchronized long getTotalTime() {
        return timeTot;
    }

    synchronized long getAverageTime() {
        return avgTime;
    }

    synchronized long getNumberOfMsgReceived() {
        return numberOfMssgsReceived;
    }

    synchronized public long getLastMsgReceivedTime() {
        return lastMsgReceivedTime;
    }
}
