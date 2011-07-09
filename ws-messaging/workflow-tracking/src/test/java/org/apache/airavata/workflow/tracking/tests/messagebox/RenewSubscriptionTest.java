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

import java.net.URL;
import java.util.Properties;

import org.apache.airavata.workflow.tracking.client.Callback;
import org.apache.airavata.workflow.tracking.client.LeadNotificationManager;
import org.apache.airavata.workflow.tracking.client.NotificationType;
import org.apache.airavata.workflow.tracking.client.Subscription;
import org.apache.airavata.workflow.tracking.tests.util.TestConfigKeys;
import org.apache.airavata.wsmg.client.WseMsgBrokerClient;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.tool.XSTCTester.TestCase;
import org.junit.*;

public class RenewSubscriptionTest extends TestCase implements Callback {

    static Properties configs = new Properties();
    String BROKER_URL;
    String MESSAGEBOX_URL;
    int consumerPort;
    public static final String TEST_TOPIC = "3a9c7b20-0475-11db-ba88-b61b57d3be03";
    public static int count = 0;
    public int messages = 10;
    public static Object mutex = new Object();
    Subscription sub;
    private static String TOPIC = "RENEW_TOPIC" + Math.random();
    private static int counter = 0;

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
    public void testRenewSubscription() throws Exception {
        Subscription sub = LeadNotificationManager.createMessageBoxSubscription(MESSAGEBOX_URL, BROKER_URL, TOPIC,
                null, this, false);

        WseMsgBrokerClient client = new WseMsgBrokerClient();
        client.init(BROKER_URL);

        OMElement msg = OMAbstractFactory.getOMFactory().createOMElement("test", null);
        msg.setText("mustwork");
        client.publish(TOPIC, msg);
        // sub.destroy();

        msg.setText("destroyed");
        client.publish(TOPIC, msg);
        Subscription sub2 = LeadNotificationManager.renewMessageboxSubscription(BROKER_URL, sub.getMessageBoxEPR(),
                sub.getSubscriptionID(), TOPIC, null, false);

        msg.setText("mustworkagain");
        client.publish(TOPIC, msg);

        System.out.println(sub2.getSubscriptionID());
        while (counter < 2) {
            Thread.sleep(1000);
        }
        Thread.sleep(10000);
    }

    public void deliverMessage(String topic, NotificationType notificationType, XmlObject messageObj) {
        System.out.println("Callbacked");
        System.out.println(messageObj.toString());
        counter++;
    }

}
