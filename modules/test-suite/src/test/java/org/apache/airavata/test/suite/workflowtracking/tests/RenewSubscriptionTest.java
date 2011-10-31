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

package org.apache.airavata.test.suite.workflowtracking.tests;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.Properties;

import org.apache.airavata.test.suite.workflowtracking.tests.util.TestConfigKeys;
import org.apache.airavata.workflow.tracking.client.Callback;
import org.apache.airavata.workflow.tracking.client.LeadNotificationManager;
import org.apache.airavata.workflow.tracking.client.NotificationType;
import org.apache.airavata.workflow.tracking.client.Subscription;
import org.apache.airavata.wsmg.client.MsgBrokerClientException;
import org.apache.airavata.wsmg.client.WseMsgBrokerClient;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.tool.XSTCTester.TestCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class RenewSubscriptionTest extends TestCase implements Callback {

    static Properties configs = new Properties();
    String BROKER_URL;
    String MESSAGEBOX_URL;
    int consumerPort;
    public static final String TOPIC = "testTopic";
    private static final String MESSAGE_BOX_ID = "929799u028887273u9899400999999";
    public static int count = 0;
    public int messages = 10;
    public static Object mutex = new Object();
    Subscription sub;
    String AXIS_REPO;

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
        // AXIS_REPO=configs.getProperty(TestConfigKeys.AXIS2_REPO);

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testRenewSubscriptionTest() throws Exception {

        String brokerPublishEPR = null;

        sub = LeadNotificationManager
                .createMessageBoxSubscription(MESSAGEBOX_URL, BROKER_URL, TOPIC, null, this, false);

        brokerPublishEPR = sub.getBrokerURL();
        System.out.println(brokerPublishEPR);

        WseMsgBrokerClient client = new WseMsgBrokerClient();
        client.init(brokerPublishEPR);

        OMElement msg = OMAbstractFactory.getOMFactory().createOMElement("testMessage", null);
        msg.setText("some message");
        client.publish(TOPIC, msg);

        Thread.sleep(10000);
        for (int i = 0; i < messages; ++i) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                sub = LeadNotificationManager.renewMessageboxSubscription(BROKER_URL, sub.getMessageBoxEPR(),
                        sub.getSubscriptionID(), TOPIC, null, false);
            } catch (AxisFault e) {
                e.printStackTrace();
            }
        }

        Thread.sleep(10000);

        for (int i = 0; i < messages; ++i) {
            client.publish(TOPIC, msg);

            Thread.sleep(100);

        }

        while (true) {

            Thread.sleep(1000);

        }
    }

    public void deliverMessage(String topic, NotificationType notificationType, XmlObject messageObj) {
        System.out.println(messageObj.toString());
        System.out.println("A message received by handler correctly");
        synchronized (mutex) {
            count++;
        }
        if (count == messages + 1) {
            try {
                sub.destroy();
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }
            System.out.println("Destroyed");
            WseMsgBrokerClient client = new WseMsgBrokerClient();
            client.init(BROKER_URL);
            try {
                client.publish(TOPIC, "some message");
            } catch (MsgBrokerClientException e) {
                e.printStackTrace();
            }
        }

        System.out.println(count + " <=" + (messages + 1));

    }

}
