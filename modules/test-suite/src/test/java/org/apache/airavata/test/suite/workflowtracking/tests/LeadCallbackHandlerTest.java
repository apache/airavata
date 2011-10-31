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

import java.io.StringReader;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.airavata.commons.WorkFlowUtils;
import org.apache.airavata.test.suite.workflowtracking.tests.util.CommonUtils;
import org.apache.airavata.test.suite.workflowtracking.tests.util.TestConfigKeys;
import org.apache.airavata.workflow.tracking.client.Callback;
import org.apache.airavata.workflow.tracking.client.LeadNotificationManager;
import org.apache.airavata.workflow.tracking.client.NotificationType;
import org.apache.airavata.workflow.tracking.client.Subscription;
import org.apache.airavata.wsmg.client.WseMsgBrokerClient;
import org.apache.axiom.om.OMElement;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.tool.XSTCTester.TestCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class LeadCallbackHandlerTest extends TestCase {

    Properties configs = new Properties();
    String BROKER_URL;
    String MESSAGEBOX_URL;
    int consumerPort;
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
        AXIS_REPO = configs.getProperty(TestConfigKeys.AXIS2_REPO);
    }

    @After
    public void tearDown() throws Exception {
    }

    boolean wait = false;
    int repetition = 3;
    private Subscription subscription;

    class TestMsgCallback implements Callback {

        public TestMsgCallback(int reps) {
            repititions = reps;
        }

        BlockingQueue<Boolean> msgQueue = new LinkedBlockingQueue<Boolean>();
        int count = 0;
        int repititions;

        public void deliverMessage(String topic, NotificationType notificationType, XmlObject messageObj) {

            count++;
            System.out.println("Subscription  received " + count + "th notification of type:");
            if (repititions <= count) {
                msgQueue.add(new Boolean(true));
            }

        }

        public BlockingQueue<Boolean> getQueue() {
            return msgQueue;
        }

    }

    @Test
    public void testRoundTrip() throws Exception {
        wait = true;

        TestMsgCallback c1 = new TestMsgCallback(repetition);
        TestMsgCallback c2 = new TestMsgCallback(repetition);

        subscription = LeadNotificationManager.createSubscription(BROKER_URL, "topic", c1, consumerPort);
        Thread.sleep(100);
        Subscription subscription2 = LeadNotificationManager.createSubscription(BROKER_URL, "topic", c2,
                consumerPort + 1);

        WseMsgBrokerClient client = new WseMsgBrokerClient();
        client.init(BROKER_URL);
        client.setTimeoutInMilliSeconds(20000L);

        OMElement msg = WorkFlowUtils.reader2OMElement(new StringReader(CommonUtils.WORKFLOW_INITIALIZED_NOTIFICATION));

        for (int i = 0; i < repetition; i++) {
            client.publish("topic", msg);
            Thread.sleep(100);
        }

        Boolean b1 = c1.getQueue().take();
        Boolean b2 = c2.getQueue().take();

        System.out.println(b1);
        System.out.println(b2);

        subscription.destroy();
        subscription2.destroy();

    }

    @Test
    public void testRoundTripWithDifferentTopics() throws Exception {
        wait = true;

        TestMsgCallback c1 = new TestMsgCallback(repetition);
        TestMsgCallback c2 = new TestMsgCallback(repetition);

        subscription = LeadNotificationManager.createSubscription(BROKER_URL, "topic10", c1, consumerPort);
        Subscription subscription2 = LeadNotificationManager.createSubscription(BROKER_URL, "topic20", c2,
                consumerPort + 1);

        WseMsgBrokerClient client = new WseMsgBrokerClient();
        client.init(BROKER_URL);

        OMElement msg = WorkFlowUtils.reader2OMElement(new StringReader(CommonUtils.WORKFLOW_INITIALIZED_NOTIFICATION));

        for (int i = 0; i < repetition; i++) {
            client.publish("topic10", msg);
            Thread.sleep(100);
        }

        for (int i = 0; i < repetition; i++) {
            client.publish("topic20", msg);
            Thread.sleep(100);
        }

        Boolean b1 = c1.getQueue().take();
        Boolean b2 = c2.getQueue().take();

        System.out.println(b1);
        System.out.println(b2);

        subscription2.destroy();
        subscription.destroy();
    }
}
