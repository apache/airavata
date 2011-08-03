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

package org.apache.airavata.workflow.tracking.samples.simple_listener;

import java.rmi.RemoteException;

import org.apache.airavata.workflow.tracking.client.LeadNotificationManager;
import org.apache.airavata.workflow.tracking.client.Subscription;

public class SimpleListener {

    /**
     * @param args
     * @throws RemoteException
     */
    public static void main(String[] args) throws RemoteException {

        String brokerLocation = args.length > 0 ? args[0] : "http://localhost:8080/axis2/services/EventingService";
        // "rainier.extreme.indiana.edu:12346";
        String topic = "pickTheTOpicThatWorkflowPublishTheEventsFrom";

        System.out.println(LeadNotificationManager.getBrokerPublishEPR(brokerLocation, topic));
        Subscription subscription = null;
        // Create a sbscription
        try {
            subscription = LeadNotificationManager.createSubscription(brokerLocation, topic,
                    new org.apache.airavata.workflow.tracking.samples.simple_listener.CallbackHandler(), 2222);
        } catch (Exception e) {
            // Falied to create subscription
            System.out.println("Failed to create Subscription");
            e.printStackTrace();
            // do what you want to do instead of rethrowing. e.g. like retrying
            throw new RuntimeException(e);
        }

        System.out.println(subscription.getBrokerPublishEPR());
        subscription.destroy();
        System.out.println("Subscription cleared");
        System.exit(0);
    }

}
