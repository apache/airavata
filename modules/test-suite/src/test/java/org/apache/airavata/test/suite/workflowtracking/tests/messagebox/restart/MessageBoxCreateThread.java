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

package org.apache.airavata.test.suite.workflowtracking.tests.messagebox.restart;

import org.apache.airavata.workflow.tracking.client.Callback;
import org.apache.airavata.workflow.tracking.client.LeadNotificationManager;
import org.apache.airavata.workflow.tracking.client.NotificationType;
import org.apache.airavata.workflow.tracking.client.Subscription;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageBoxCreateThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(MessageBoxCreateThread.class);
    private String brokerLocation;
    private String messageboxLocation;
    private String topic;
    private Subscription subscription;

    public MessageBoxCreateThread(String brokerLocation, String messageboxLocation, String topic) {
        super();
        this.brokerLocation = brokerLocation;
        this.messageboxLocation = messageboxLocation;
        this.topic = topic;

    }

    @Override
    public void run() {
        Subscription subscription = null;
        try {
            subscription = LeadNotificationManager.createMessageBoxSubscription(this.messageboxLocation,
                    this.brokerLocation, this.topic, null, new Callback() {

                        public void deliverMessage(String topic, NotificationType type, XmlObject messageObj) {

                            throw new RuntimeException("This piece of code probably shouldnt have been called");

                        }
                    }, true);

            System.out.println("Created messageBox");
        } catch (Exception e1) {
            logger.error("error in message box creat thread :" + e1);
            e1.printStackTrace();
        }
        this.subscription = subscription;
    }

    public String getSubscriptionID() {
        return this.subscription.getSubscriptionID();
    }

    public EndpointReference getWsaEndpointReference() {
        return this.subscription.getMessageBoxEPR();
    }

    public String getTopic() {
        return this.subscription.getTopic();
    }

}
