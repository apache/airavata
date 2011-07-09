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

package org.apache.airavata.workflow.tracking.tests.messagebox;

import java.io.StringReader;
import java.net.URL;
import java.util.Properties;

import org.apache.airavata.commons.WorkFlowUtils;
import org.apache.airavata.workflow.tracking.tests.ThreadMessagePassingCallback;
import org.apache.airavata.workflow.tracking.tests.util.CommonUtils;
import org.apache.airavata.workflow.tracking.tests.util.TestConfigKeys;
import org.apache.airavata.wsmg.client.WseMsgBrokerClient;
import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.impl.tool.XSTCTester.TestCase;
import org.junit.*;

public class MultipleSubscriptionForMessageBoxTest extends TestCase implements ThreadMessagePassingCallback {

    static Properties configs = new Properties();
    String BROKER_URL;
    String MESSAGEBOX_URL;
    int consumerPort;
    public static final String TEST_TOPIC = "3a9c7b20-0475-11db-ba88-b61b57d3be03";
    public static final int NOTIFICATIONS_PUBLISHED = 10;
    public static final int NUMBER_OF_SUBSCRIBERS = 1;
    private org.apache.log4j.Logger logger = Logger.getLogger(MultipleSubscriptionForMessageBoxTest.class);

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

    private int succesfulclients = 0;

    @Test
    public void testMultipleSubscribers() throws Exception {

        SubscriberThread[] subscribers = new SubscriberThread[NUMBER_OF_SUBSCRIBERS];
        for (int i = 0; i < NUMBER_OF_SUBSCRIBERS; ++i) {
            subscribers[i] = new SubscriberThread(MESSAGEBOX_URL, BROKER_URL, TEST_TOPIC, this, "MytestId331234"
                    + Integer.toString(i));
            subscribers[i].start();

        }

        Thread.sleep(100);

        WseMsgBrokerClient client = new WseMsgBrokerClient();
        client.init(BROKER_URL);

        OMElement msg = WorkFlowUtils.reader2OMElement(new StringReader(CommonUtils.WORKFLOW_INITIALIZED_NOTIFICATION));

        for (int j = 0; j < NUMBER_OF_SUBSCRIBERS; j++) {
            for (int i = 0; i < NOTIFICATIONS_PUBLISHED; i++) {

                client.publish(TEST_TOPIC, msg);

                Thread.sleep(100);
            }
        }

        while (succesfulclients < NUMBER_OF_SUBSCRIBERS - 1) {
            Thread.sleep(1000);
        }
        Thread.sleep(5000);
        System.out.println("All successful");
        System.exit(0);

    }

    public void done() {
        succesfulclients++;
    }

}
