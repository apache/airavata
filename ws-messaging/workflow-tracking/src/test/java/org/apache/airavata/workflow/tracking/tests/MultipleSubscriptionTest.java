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

package org.apache.airavata.workflow.tracking.tests;

import java.net.URL;
import java.util.Properties;

import org.apache.airavata.workflow.tracking.tests.util.CommonUtils;
import org.apache.airavata.workflow.tracking.tests.util.SubscriberThread;
import org.apache.airavata.workflow.tracking.tests.util.TestConfigKeys;
import org.apache.airavata.wsmg.client.WseMsgBrokerClient;
import org.apache.xmlbeans.impl.tool.XSTCTester.TestCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public class MultipleSubscriptionTest extends TestCase implements ThreadMessagePassingCallback {

    static Properties configs = new Properties();
    String BROKER_URL;
    String MESSAGEBOX_URL;
    int consumerPort;
    public static final int NOTIFICATIONS_PUBLISHED = 4;
    public static final int NUMBER_OF_SUBSCRIBERS = 4;
    private int succesfulclients = 0;
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

    public void testMultipleSubscribers() throws Exception {

        SubscriberThread[] subscribers = new SubscriberThread[NUMBER_OF_SUBSCRIBERS];
        for (int i = 0; i < NUMBER_OF_SUBSCRIBERS; ++i) {
            subscribers[i] = new SubscriberThread(this, BROKER_URL, "topic" + i, consumerPort + i);
            subscribers[i].start();

        }

        Thread.sleep(5000);

        WseMsgBrokerClient client = new WseMsgBrokerClient();
        client.init(BROKER_URL);

        for (int j = 0; j < NUMBER_OF_SUBSCRIBERS; j++) {
            for (int i = 0; i < NOTIFICATIONS_PUBLISHED; i++) {

                client.publish("topic" + j, CommonUtils.WORKFLOW_INITIALIZED_NOTIFICATION);

                Thread.sleep(100);
            }
        }

        while (succesfulclients < NUMBER_OF_SUBSCRIBERS) {
            Thread.sleep(1000);
        }

    }

    public void done() {
        System.out.println("Done Multiple subscription test");
        succesfulclients++;
    }

}
