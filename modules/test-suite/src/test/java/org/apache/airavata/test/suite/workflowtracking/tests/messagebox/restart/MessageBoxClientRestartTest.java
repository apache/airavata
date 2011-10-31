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

import java.io.StringReader;
import java.net.URL;
import java.util.Properties;

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
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageBoxClientRestartTest extends TestCase {

    private static final Logger logger = LoggerFactory.getLogger(MessageBoxClientRestartTest.class);
    private static final String TOPIC = "RestartclientTopic2";
    private boolean wait = true;
    static Properties configs = new Properties();
    String BROKER_URL;
    String MESSAGEBOX_URL;
    int consumerPort;
    public static final int NOTIFICATIONS_PUBLISHED = 4;
    public static final int NUMBER_OF_SUBSCRIBERS = 4;

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

    @Test
    public void testRestart() throws Exception {
        Subscription subscription = null;

        MessageBoxCreateThread thread = new MessageBoxCreateThread(BROKER_URL, MESSAGEBOX_URL, TOPIC);
        thread.start();

        Thread.sleep(100);
        thread.stop();

        Thread.sleep(5000);

        System.out.println("bringing down the Puller\n Publishing Messages");
        WseMsgBrokerClient client = new WseMsgBrokerClient();
        client.init(BROKER_URL);

        OMElement msg = WorkFlowUtils.reader2OMElement(new StringReader(CommonUtils.WORKFLOW_INITIALIZED_NOTIFICATION));

        client.publish(TOPIC, msg);

        System.out.println("Messages published");
        System.out.println("Creating another puller");
        EndpointReference epr = thread.getWsaEndpointReference();
        String subscriptionID = thread.getSubscriptionID();
        String topic = thread.getTopic();
        System.out.println(epr);
        System.out.println(subscriptionID);

        subscription = LeadNotificationManager.startListeningToSavedSubscription(BROKER_URL, epr, subscriptionID,
                topic, null, new Callback() {

                    public void deliverMessage(String topic, NotificationType type, XmlObject messageObj) {

                        System.out.println("Notification Received, notification of type:" + type);
                        System.out.println("Topic[" + topic + "]");
                        // assertEquals(type, NotificationType.WorkflowInitialized);
                        wait = false;

                    }
                }, false);

        System.out.println(subscription.getMessageBoxEPR());

        while (wait) {
            Thread.sleep(1000);

        }
    }
}
