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

import org.apache.airavata.wsmg.client.WseClientAPI;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import wsmg.util.ConfigKeys;

public class WseClientAPISubscriptionsTest extends TestCase {

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

    // basic subscription test
    @Test
    public final void testSubscribe() {

        try {
            WseClientAPI clientApi = new WseClientAPI();

            String brokerEPR = configs.getProperty(ConfigKeys.BROKER_EVENTING_SERVICE_EPR);
            String consumerEPR = configs.getProperty(ConfigKeys.CONSUMER_EPR);
            String topic = "SimpleTopicWSE";

            String subscriptionID = clientApi.subscribe(brokerEPR, consumerEPR, topic);

            assertNotNull(subscriptionID);

        } catch (AxisFault e) {
            e.printStackTrace();
            fail("unable to subscribe: " + e.toString());
        }

    }

    @Test
    public final void testSubscribeWithURLBasedTopic() {

        try {
            WseClientAPI clientApi = new WseClientAPI();

            String brokerEPR = configs.getProperty(ConfigKeys.BROKER_EVENTING_SERVICE_EPR);
            String consumerEPR = configs.getProperty(ConfigKeys.CONSUMER_EPR);
            String topic = "MytopicFromURLWSE";

            EndpointReference brokerEprWithTopic = WseClientAPI.createEndpointReference(brokerEPR, topic);

            String subscriptionID = clientApi.subscribe(brokerEprWithTopic.getAddress(), consumerEPR, null);

            assertNotNull(subscriptionID);

        } catch (AxisFault e) {
            e.printStackTrace();
            fail("unable to subscribe: " + e.toString());
        }

    }

    // check for duplicate subscription
    @Test
    public final void testSubscribeSameSubcription() {

        try {
            WseClientAPI clientApi = new WseClientAPI();

            String brokerEPR = configs.getProperty(ConfigKeys.BROKER_EVENTING_SERVICE_EPR);
            String consumerEPR = configs.getProperty(ConfigKeys.CONSUMER_EPR);
            String topic = "DuplicateSubscriptionTestWSE";

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
            WseClientAPI clientApi = new WseClientAPI();

            String brokerEPR = configs.getProperty(ConfigKeys.BROKER_EVENTING_SERVICE_EPR);
            String consumerEPR = configs.getProperty(ConfigKeys.CONSUMER_EPR);
            String topicExpression = null;
            String xpathExpression = "/test/wse";

            clientApi.subscribe(brokerEPR, consumerEPR, topicExpression, xpathExpression);
            String subscriptionID = clientApi.subscribe(brokerEPR, consumerEPR, topicExpression);

            assertNotNull(subscriptionID);

        } catch (AxisFault e) {
            e.printStackTrace();
            fail("unable to subscribe: " + e.toString());
        }

    }

    // check for Xpath subscription
    @Test
    public final void testSubscribeTopicAndXpath() {

        try {
            WseClientAPI clientApi = new WseClientAPI();

            String brokerEPR = configs.getProperty(ConfigKeys.BROKER_EVENTING_SERVICE_EPR);
            String consumerEPR = configs.getProperty(ConfigKeys.CONSUMER_EPR);
            String topicExpression = "testTopicAndXpathWSE";
            String xpathExpression = "/test/wse1";

            String subscriptionID = clientApi.subscribe(brokerEPR, consumerEPR, topicExpression, xpathExpression);

            assertNotNull(subscriptionID);

        } catch (AxisFault e) {
            e.printStackTrace();
            fail("unable to subscribe: " + e.toString());
        }

    }

    @Test
    public final void testSubscribeURLTopicAndXpath() {

        try {
            WseClientAPI clientApi = new WseClientAPI();

            String brokerEPR = configs.getProperty(ConfigKeys.BROKER_EVENTING_SERVICE_EPR);
            String consumerEPR = configs.getProperty(ConfigKeys.CONSUMER_EPR);
            String topicExpression = "testTopicURLAndXpathWSE";
            String xpathExpression = "/wse/xpath";

            WseClientAPI.createEndpointReference(brokerEPR, topicExpression);

            String subscriptionID = clientApi.subscribe(brokerEPR, consumerEPR, null, xpathExpression);

            assertNotNull(subscriptionID);

        } catch (AxisFault e) {
            e.printStackTrace();
            fail("unable to subscribe: " + e.toString());
        }

    }

    @Test
    public final void testUnsubscribeSimpleTopic() {
        try {
            WseClientAPI clientApi = new WseClientAPI();

            String brokerEPR = configs.getProperty(ConfigKeys.BROKER_EVENTING_SERVICE_EPR);
            String consumerEPR = configs.getProperty(ConfigKeys.CONSUMER_EPR);
            String topicExpression = "testUnsubscribeTopicWSE";
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
            WseClientAPI clientApi = new WseClientAPI();

            String brokerEPR = configs.getProperty(ConfigKeys.BROKER_EVENTING_SERVICE_EPR);
            String consumerEPR = configs.getProperty(ConfigKeys.CONSUMER_EPR);
            String topicExpression = null;
            String xpathExpression = "/www/xpath/wse";

            String subscriptionID = clientApi.subscribe(brokerEPR, consumerEPR, topicExpression, xpathExpression);

            assertNotNull(subscriptionID);

            try {
                clientApi.unSubscribe(brokerEPR, subscriptionID, null);
            } catch (Exception e) {
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
            WseClientAPI clientApi = new WseClientAPI();

            String brokerEPR = configs.getProperty(ConfigKeys.BROKER_EVENTING_SERVICE_EPR);
            String consumerEPR = configs.getProperty(ConfigKeys.CONSUMER_EPR);
            String topicExpression = "unsubscribeSimpleAndTopicTestWSE";
            String xpathExpression = "/wse/test/xpath";

            String subscriptionID = clientApi.subscribe(brokerEPR, consumerEPR, topicExpression, xpathExpression);

            assertNotNull(subscriptionID);

            try {
                clientApi.unSubscribe(brokerEPR, subscriptionID, null);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } catch (AxisFault e) {
            e.printStackTrace();
            fail("unable to subscribe: " + e.toString());
        }

    }

}
