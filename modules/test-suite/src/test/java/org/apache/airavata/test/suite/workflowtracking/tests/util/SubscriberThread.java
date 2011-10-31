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

package org.apache.airavata.test.suite.workflowtracking.tests.util;

import java.rmi.RemoteException;

import org.apache.airavata.test.suite.workflowtracking.tests.MultipleSubscriptionTest;
import org.apache.airavata.test.suite.workflowtracking.tests.ThreadMessagePassingCallback;
import org.apache.airavata.workflow.tracking.client.Callback;
import org.apache.airavata.workflow.tracking.client.LeadNotificationManager;
import org.apache.airavata.workflow.tracking.client.NotificationType;
import org.apache.airavata.workflow.tracking.client.Subscription;
import org.apache.xmlbeans.XmlObject;

public class SubscriberThread extends Thread {

    private ThreadMessagePassingCallback callback;

    public static int count = 0;

    private int subCount = 0;

    private Subscription subscription;
    private String brokerURL;
    private String topic;
    private int consumerServerPort;

    public SubscriberThread(ThreadMessagePassingCallback callback, String brokerURL, String topic,
            int consumerServerPort) {
        this.callback = callback;
        this.brokerURL = brokerURL;
        this.topic = topic;
        this.consumerServerPort = consumerServerPort;
    }

    @Override
    public void run() {
        try {
            subscription = LeadNotificationManager.createSubscription(brokerURL, topic, new Callback() {

                public void deliverMessage(String topic, NotificationType type, XmlObject messageObj) {

                    subCount++;
                    count++;
                    System.out.println("Subscription received " + subCount + "th notification of type:" + type
                            + " Total is :" + count);
                    assert (type == NotificationType.WorkflowInitialized);

                    if (subCount == MultipleSubscriptionTest.NOTIFICATIONS_PUBLISHED) {
                        try {
                            subscription.destroy();
                        } catch (RemoteException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        callback.done();
                    }
                }
            }, consumerServerPort);
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
