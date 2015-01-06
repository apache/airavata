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

package org.apache.airavata.workflow.tracking.samples.listener;

import java.io.StringReader;

import org.apache.airavata.commons.WorkFlowUtils;
import org.apache.airavata.workflow.tracking.client.LeadNotificationManager;
import org.apache.airavata.workflow.tracking.client.Subscription;
import org.apache.airavata.wsmg.client.WseMsgBrokerClient;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.addressing.EndpointReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Listener {

    private static OMFactory factory = OMAbstractFactory.getOMFactory();
    private static final Logger log = LoggerFactory.getLogger(Listener.class);

    public static final String finalNotification = "<end>This is the last Notification. end your subscription when you receive this</end>";

    /**
     * @param args
     */
    public static void main(String[] args) {

        String brokerLocation = args.length > 0 ? args[0] : "localhost:8080/axis2/services/EventingService";
        String topic = "pickTheTOpicThatWorkflowPublishTheEventsFrom";

        Subscription subscription = null;
        // Create a sbscription
        try {
            // create a callback
            CallbackHandler callback = new CallbackHandler();
            // create the subscription
            subscription = LeadNotificationManager.createSubscription(brokerLocation, topic, callback, 2222);
            // set the subscription in the callback so we could destroy the
            // subscription within the callback
            callback.setSubscription(subscription);
        } catch (Exception e) {
            // Falied to create subscription
            log.error("Failed to create Subscription", e);
            // do what you want to do instead of rethrowing. e.g. like retrying
            throw new RuntimeException(e);
        }

        // Subscription is created and now we listen. Now the workflow should
        // publish notification with
        // that particular topic.
        // Inthis sample we emulate it by manually publishing notifications

        // created a publisher

        WseMsgBrokerClient publisher = new WseMsgBrokerClient();
        EndpointReference endpointRef = publisher.createEndpointReference(brokerLocation, topic);
        publisher.init(endpointRef.getAddress());

        try {

            OMElement finalNotificationEl = WorkFlowUtils.reader2OMElement(new StringReader(finalNotification));

            OMElement testNotification = factory.createOMElement("Test", null);

            testNotification.setText("test event for workflow tracking sample");

            publisher.publish(null, testNotification);
            publisher.publish(null, finalNotificationEl);
        } catch (Exception e) {
            log.error("Failed to publish", e);
        }

    }

}
