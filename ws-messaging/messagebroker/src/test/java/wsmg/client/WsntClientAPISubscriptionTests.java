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

package wsmg.client;

import java.net.URL;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.airavata.wsmg.client.WsntClientAPI;
import org.apache.axis2.AxisFault;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import wsmg.util.ConfigKeys;

public class WsntClientAPISubscriptionTests extends TestCase {

    static Properties configs = new Properties();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {

        URL configURL = ClassLoader.getSystemResource(ConfigKeys.CONFIG_FILE_NAME);
        configs.load(configURL.openStream());

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public final void testSimpleSubscribe() {

        WsntClientAPI clientApi = new WsntClientAPI();

        String brokerLocation = configs.getProperty(ConfigKeys.BROKER_NOTIFICATIONS_SERVICE_EPR);
        String consumerLocation = configs.getProperty(ConfigKeys.CONSUMER_EPR);
        String topicExpression = "SimpleTopicWSNT";

        try {
            clientApi.setTimeOutInMilliSeconds(0);
            String subscriptionID = clientApi.subscribe(brokerLocation, consumerLocation, topicExpression);

            assertNotNull("subscription id can't be null", subscriptionID);
            assertTrue(subscriptionID.trim().length() > 0);

        } catch (AxisFault e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("test case failed - unable to subscribe");
        }

    }

    // check for duplicate subscription
    @Test
    public final void testSubscribeSameSubcription() {

        try {
            WsntClientAPI clientApi = new WsntClientAPI();

            String brokerEPR = configs.getProperty(ConfigKeys.BROKER_NOTIFICATIONS_SERVICE_EPR);
            String consumerEPR = configs.getProperty(ConfigKeys.CONSUMER_EPR);
            String topic = "DuplicateWSNTSubscriptionTest";

            String subscriptionIDFirst = clientApi.subscribe(brokerEPR, consumerEPR, topic);

            assertNotNull(subscriptionIDFirst);

            String subscriptionIDSecond = clientApi.subscribe(brokerEPR, consumerEPR, topic);

            assertEquals(subscriptionIDFirst, subscriptionIDSecond);

        } catch (AxisFault e) {
            e.printStackTrace();
            fail("unable to subscribe: " + e.toString());
        }

    }

    // check for Xpath subscription
    @Test
    public final void testSubscribeXpath() {

        try {
            WsntClientAPI clientApi = new WsntClientAPI();

            String brokerEPR = configs.getProperty(ConfigKeys.BROKER_NOTIFICATIONS_SERVICE_EPR);
            String consumerEPR = configs.getProperty(ConfigKeys.CONSUMER_EPR);
            String topicExpression = null;
            String xpathExpression = "/wsnt/test";

            String subscriptionID = clientApi.subscribe(brokerEPR, consumerEPR, topicExpression, xpathExpression);

            assertNotNull(subscriptionID);

        } catch (AxisFault e) {
            e.printStackTrace();
            fail("unable to subscribe: " + e.toString());
        }

    }

    @Test
    public final void testSubscribeTopicAndXpath() {

        try {
            WsntClientAPI clientApi = new WsntClientAPI();

            String brokerEPR = configs.getProperty(ConfigKeys.BROKER_NOTIFICATIONS_SERVICE_EPR);
            String consumerEPR = configs.getProperty(ConfigKeys.CONSUMER_EPR);
            String topicExpression = "testTopicAndXpathWSNT";
            String xpathExpression = "/c/b";

            String subscriptionID = clientApi.subscribe(brokerEPR, consumerEPR, topicExpression, xpathExpression);

            assertNotNull(subscriptionID);

        } catch (AxisFault e) {
            e.printStackTrace();
            fail("unable to subscribe: " + e.toString());
        }

    }

    @Test
    public final void testUnsubscribeSimpleTopic() {
        try {
            WsntClientAPI clientApi = new WsntClientAPI();

            String brokerEPR = configs.getProperty(ConfigKeys.BROKER_NOTIFICATIONS_SERVICE_EPR);
            String consumerEPR = configs.getProperty(ConfigKeys.CONSUMER_EPR);
            String topicExpression = "testUnsubscribeTopicWSNT";
            String xpathExpression = null;

            String subscriptionID = clientApi.subscribe(brokerEPR, consumerEPR, topicExpression, xpathExpression);

            assertNotNull(subscriptionID);

            try {
                clientApi.unSubscribe(brokerEPR, subscriptionID, null);
            } catch (AxisFault e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } catch (AxisFault e) {
            e.printStackTrace();
            fail("unable to subscribe: " + e.toString());
        }

    }

    @Test
    public final void testUnsubscribeXpathTopic() {
        try {
            WsntClientAPI clientApi = new WsntClientAPI();

            String brokerEPR = configs.getProperty(ConfigKeys.BROKER_NOTIFICATIONS_SERVICE_EPR);
            String consumerEPR = configs.getProperty(ConfigKeys.CONSUMER_EPR);
            String topicExpression = null;
            String xpathExpression = "/a/wsnt/c";

            String subscriptionID = clientApi.subscribe(brokerEPR, consumerEPR, topicExpression, xpathExpression);

            assertNotNull(subscriptionID);

            try {
                clientApi.unSubscribe(brokerEPR, subscriptionID, null);
            } catch (AxisFault e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } catch (AxisFault e) {
            e.printStackTrace();
            fail("unable to subscribe: " + e.toString());
        }

    }

    @Test
    public final void testUnsubscribeSimpleAndXpathTopic() {
        try {
            WsntClientAPI clientApi = new WsntClientAPI();

            String brokerEPR = configs.getProperty(ConfigKeys.BROKER_NOTIFICATIONS_SERVICE_EPR);
            String consumerEPR = configs.getProperty(ConfigKeys.CONSUMER_EPR);
            String topicExpression = "unsubscribeSimpleAndTopicTest";
            String xpathExpression = "/a/ws/nt";

            String subscriptionID = clientApi.subscribe(brokerEPR, consumerEPR, topicExpression, xpathExpression);

            assertNotNull(subscriptionID);
            try {
                clientApi.unSubscribe(brokerEPR, subscriptionID, null);
            } catch (AxisFault e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } catch (AxisFault e) {
            e.printStackTrace();
            fail("unable to subscribe: " + e.toString());
        }

    }
}
