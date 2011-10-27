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

package org.apache.airavata.test.suite.workflowtracking.tests.messagebox;

import java.rmi.RemoteException;

import org.apache.airavata.workflow.tracking.client.Callback;
import org.apache.airavata.workflow.tracking.client.LeadNotificationManager;
import org.apache.airavata.workflow.tracking.client.NotificationType;
import org.apache.airavata.workflow.tracking.client.Subscription;
import org.apache.airavata.test.suite.workflowtracking.tests.ThreadMessagePassingCallback;
import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubscriberThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(SubscriberThread.class);
    private ThreadMessagePassingCallback callback;

    private int subCount = 0;
    private Subscription subscription;
    private String brokerURL;
    private String topic;
    private String messageboxUrl;
    private String msgBoxId;

    public SubscriberThread(String messageboxUrl, String brokerURL, String topic,
            ThreadMessagePassingCallback callback, String msgBoxId) {
        super();
        this.msgBoxId = msgBoxId;
        this.callback = callback;
        this.brokerURL = brokerURL;
        this.topic = topic;
        this.messageboxUrl = messageboxUrl;
    }

    @Override
    public void run() {
        try {
            subscription = LeadNotificationManager.createMessageBoxSubscription(messageboxUrl, brokerURL, topic, null,
                    new Callback() {

                        public void deliverMessage(String topic, NotificationType type, XmlObject messageObj) {

                            subCount++;
                            System.out.println("Subscription received " + subCount + "th notification of type:" + type);
                            assert (type == NotificationType.WorkflowInitialized);
                            System.out.println("subcount=" + subCount + " =="
                                    + MultipleSubscriptionForMessageBoxTest.NOTIFICATIONS_PUBLISHED);
                            if (subCount == MultipleSubscriptionForMessageBoxTest.NOTIFICATIONS_PUBLISHED) {
                                System.out.println("subscription destroyed");
                                try {
                                    subscription.destroy();
                                } catch (RemoteException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                callback.done();
                            }
                        }
                    }, true);
        } catch (Exception e) {
            logger.error("exception in suscriber thread :" + e);
            e.printStackTrace();
        }
        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                logger.error("exception in suscriber thread sleep: " + e);
                e.printStackTrace();
            }
        }
    }

}
