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

package org.apache.airavata.workflow.tracking.tests.samples.workflow;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.Properties;

import org.apache.airavata.workflow.tracking.client.Callback;
import org.apache.airavata.workflow.tracking.client.LeadNotificationManager;
import org.apache.airavata.workflow.tracking.client.NotificationType;
import org.apache.airavata.workflow.tracking.client.Subscription;
import org.apache.airavata.workflow.tracking.tests.util.TestConfigKeys;
import org.apache.airavata.workflow.tracking.types.WorkflowTerminatedDocument;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.*;

public class WorkflowNotificationListener implements Callback {

    Subscription subscription;
    Properties configs = new Properties();
    String BROKER_URL;
    String MESSAGEBOX_URL;
    int consumerPort;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        URL configURL = ClassLoader.getSystemResource(TestConfigKeys.CONFIG_FILE_NAME);
        configs.load(configURL.openStream());
        BROKER_URL = configs.getProperty(TestConfigKeys.BROKER_EVENTING_SERVICE_EPR);
        MESSAGEBOX_URL = configs.getProperty(TestConfigKeys.MSGBOX_SERVICE_EPR);
        consumerPort = Integer.parseInt(configs.getProperty(TestConfigKeys.CONSUMER_PORT));
    }

    @After
    public void tearDown() throws Exception {
    }

    public WorkflowNotificationListener() {
    }

    /**
     * Method deliverMessage is called when a Lead Message is received on the subscribed topic.
     * 
     * @param topic
     *            the topic to which this message was sent. This can also be retrieved from the messageObj XMlObject
     *            directly after typecasting.
     * @param messageObj
     *            the XmlObject representing one of the LeadMessages, This needs to be typecast to the correct message
     *            type before being used.
     * 
     */
    public void deliverMessage(String topic, NotificationType type, XmlObject messageObj) {

        System.out.println("Received Notification Type [" + type + "] on topic [" + topic + "]\n" + messageObj
                + "\n---");

        if (type == NotificationType.WorkflowTerminated) {
            System.out.println("Workflow terminated. Unsubscribing...");
            WorkflowTerminatedDocument obj = (WorkflowTerminatedDocument) messageObj;
            try {
                obj.getWorkflowTerminated().getAnnotation()
                        .set(XmlObject.Factory.parse("<something>someval</something>"));
                System.out.println(obj.toString());
            } catch (XmlException e) {
                e.printStackTrace();
            }
            try {
                subscription.destroy();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testWokflowNotificationListener() throws Exception {

        String topic = "somerandomtopic";

        WorkflowNotificationListener subscriber = new WorkflowNotificationListener();
        boolean useMessageBox = true;
        if (!useMessageBox) {
            subscription = LeadNotificationManager.createSubscription(BROKER_URL, topic, subscriber, consumerPort);
        } else {
            subscription = LeadNotificationManager.createMessageBoxSubscription(MESSAGEBOX_URL, BROKER_URL, topic,
                    null, subscriber);
        }
        System.out.println("Subscribing to broker: " + BROKER_URL);
        System.out.println("Started listening on topic: " + subscription.getTopic());
        while (true) {
            Thread.sleep(10000);
        }

    }

}
