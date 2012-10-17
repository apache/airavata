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

package workflowtracking.samples.invoke;


import org.apache.airavata.wsmg.client.WseMsgBrokerClient;

import org.apache.airavata.wsmg.client.msgbox.MessagePuller;
import org.apache.axis2.addressing.EndpointReference;
import workflowtracking.samples.util.ConfigKeys;
import workflowtracking.samples.util.Listener;

import java.io.IOException;
import java.util.Properties;

import java.io.*;

public class SimpleWorkflowTracker {

    private static Properties getDefaults() {
        Properties defaults = new Properties();
        defaults.setProperty(ConfigKeys.MSGBOX_SERVICE_URL,
                "http://localhost:8080/axis2/services/MsgBoxService");
        defaults.setProperty(ConfigKeys.MSGBROKER_SERVICE_URL,
                "http://localhost:8080/axis2/services/EventingService");
        defaults.setProperty(ConfigKeys.TOPIC, "abc");
        return defaults;
    }

    public static void main(String[] args) throws IOException {

        Properties configurations = new Properties(getDefaults());
        try {
            InputStream ioStream = new FileInputStream(ConfigKeys.CONFIG_FILE_NAME);
            configurations.load(ioStream);
        } catch (IOException ioe) {

            System.out.println("unable to load configuration file, "
                    + "default settings will be used");
        }

        WseMsgBrokerClient brokerClient = new WseMsgBrokerClient();
        String brokerLocation = configurations.getProperty(ConfigKeys.MSGBROKER_SERVICE_URL);
        brokerClient.init(brokerLocation);
        String topic = configurations.getProperty(ConfigKeys.TOPIC);
        NotificationSender sender = new NotificationSender(brokerLocation, topic);
        // Creating a messagebox
        EndpointReference msgBoxEpr = brokerClient.createPullMsgBox(configurations.getProperty(ConfigKeys.MSGBOX_SERVICE_URL));

        // subscribing to the above created messsage box with configured topic
        String subscriptionID = brokerClient.subscribe(msgBoxEpr.getAddress(), topic, null);


        //Start the messagePuller to pull messages from newly created messagebox
        MessagePuller puller = brokerClient.startPullingEventsFromMsgBox(msgBoxEpr, new Listener(), 1000L, 2000L);

        sender.workflowStarted("Workflow Started");
        //Here we simply assume the workflow invocation is the invoke of the subscribe operation of EventingService and result
        // Is considered as the subscriptionID got from subscribe operation
        String workflowResult = subscriptionID;
        sender.workflowFinished(workflowResult);

        try {
            Thread.sleep(10000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        brokerClient.unSubscribe(subscriptionID);
        puller.stopPulling();
        System.out.println("Delete message box response :  "
                + brokerClient.deleteMsgBox(msgBoxEpr, 500L));

    }

}
