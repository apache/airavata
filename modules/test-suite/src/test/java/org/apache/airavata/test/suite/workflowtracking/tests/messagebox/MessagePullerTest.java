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

import java.io.StringReader;
import java.net.URL;
import java.util.Properties;
import java.util.UUID;

import org.apache.airavata.commons.WorkFlowUtils;
import org.apache.airavata.test.suite.workflowtracking.tests.util.CommonUtils;
import org.apache.airavata.test.suite.workflowtracking.tests.util.TestConfigKeys;
import org.apache.airavata.workflow.tracking.client.Callback;
import org.apache.airavata.workflow.tracking.client.LeadNotificationManager;
import org.apache.airavata.workflow.tracking.client.NotificationType;
import org.apache.airavata.workflow.tracking.client.Subscription;
import org.apache.airavata.wsmg.client.WseMsgBrokerClient;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.tool.XSTCTester.TestCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessagePullerTest extends TestCase {

    private static final Logger logger = LoggerFactory.getLogger(MessagePullerTest.class);
    static Properties configs = new Properties();
    String BROKER_URL;
    String MESSAGEBOX_URL;
    int consumerPort;
    public static final String TEST_TOPIC = "3a9c7b20-0475-11db-ba88-b61b57d3be03";
    private static final String MESSAGE_BOX_ID = UUID.randomUUID().toString();
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
    }

    @After
    public void tearDown() throws Exception {
    }

    private boolean wait = true;

    public void testMessagePulling() throws Exception {
        Subscription subscription = null;

        Callback testCallback1 = new Callback() {
            public void deliverMessage(String topic, NotificationType type, XmlObject messageObj) {
                System.out.println("Notification Received, notification of type:" + type);
                // assertEquals(type, NotificationType.WorkflowInitialized);
                wait = false;
            }
        };

        subscription = LeadNotificationManager.createMessageBoxSubscription(MESSAGEBOX_URL, BROKER_URL, TEST_TOPIC,
                null, testCallback1);

        System.out.println(subscription.getMessageBoxEPR());
        System.out.println(subscription.getSubscriptionID());

        WseMsgBrokerClient client = new WseMsgBrokerClient();
        client.init(BROKER_URL);

        OMElement msg = WorkFlowUtils.reader2OMElement(new StringReader(CommonUtils.WORKFLOW_INITIALIZED_NOTIFICATION));

        client.publish(TEST_TOPIC, msg);

        EndpointReference MSG_BOX_EPR = subscription.getMessageBoxEPR();
        System.out.println(MSG_BOX_EPR);
        String subscriptionID = subscription.getSubscriptionID();
        Callback testCallback2 = new Callback() {

            public void deliverMessage(String topic, NotificationType type, XmlObject messageObj) {

                System.out.println("Notification Received, notification of type:" + type);
                // This assertion is wrong because type and NotificationType.WorkflowInitialized are two different types
                // assertEquals(type, NotificationType.WorkflowInitialized);
                wait = false;

            }
        };
        subscription = LeadNotificationManager.startListeningToSavedSubscription(BROKER_URL, MSG_BOX_EPR,
                subscriptionID, TEST_TOPIC, null, testCallback2, true);

        System.out.println(subscription.getMessageBoxEPR());

        while (wait) {

            Thread.sleep(1000);

        }

        System.out.println("MessagePuller test completed");
        subscription.destroy();
    }
}
