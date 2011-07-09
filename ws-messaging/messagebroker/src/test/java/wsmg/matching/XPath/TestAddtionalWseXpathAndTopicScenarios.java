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

package wsmg.matching.XPath;

import java.net.URL;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.TestCase;

import org.apache.airavata.wsmg.client.ConsumerNotificationHandler;
import org.apache.airavata.wsmg.client.WseClientAPI;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import wsmg.util.ConfigKeys;

public class TestAddtionalWseXpathAndTopicScenarios extends TestCase {

    static Properties configs = new Properties();

    class NotificationReciever implements ConsumerNotificationHandler {

        private BlockingQueue<SOAPEnvelope> queue = new LinkedBlockingQueue<SOAPEnvelope>();

        private String id;

        public NotificationReciever(String id) {
            this.id = id;
        }

        public void handleNotification(SOAPEnvelope msgEnvelope) {

            queue.add(msgEnvelope);
            System.out.println(String.format("[reciever id: %s] %s", id, msgEnvelope));
        }

        public BlockingQueue<SOAPEnvelope> getMsgQueue() {
            return queue;
        }

    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        URL configURL = ClassLoader.getSystemResource(ConfigKeys.CONFIG_FILE_NAME);
        configs.load(configURL.openStream());
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public final void testXpathAndTopicOnlyRoundTrip() {

        try {

            String topic = "RoundTripTestXpathAndTopicWse";

            String xpathExpression = "/c/b/a[text()=1]";

            String msgFormat = "<c><b><a>%d</a></b></c>";

            long value = 1;
            String matchingMsg = String.format(msgFormat, value);
            String unmatchingMsg = String.format(msgFormat, value + 1);

            int consumerPort = new Integer(configs.getProperty(ConfigKeys.CONSUMER_PORT));

            EndpointReference brokerEpr = new EndpointReference(
                    configs.getProperty(ConfigKeys.BROKER_EVENTING_SERVICE_EPR));

            WseClientAPI topicOnlyReceiverApi = new WseClientAPI(brokerEpr);
            NotificationReciever topicOnlyMsgReceiver = new NotificationReciever("Topic Only");

            String[] topicConsumerEPRs = topicOnlyReceiverApi.startConsumerService(consumerPort, topicOnlyMsgReceiver);

            assertTrue("invalid consumer eprs returned", topicConsumerEPRs.length > 0);

            String topicOnlySubId = topicOnlyReceiverApi.subscribe(brokerEpr.getAddress(), topicConsumerEPRs[0], topic);

            WseClientAPI xpathAndTopicReceiverApi = new WseClientAPI(brokerEpr);
            NotificationReciever topicAndXpathMsgReceiver = new NotificationReciever("Topic And Xpath");
            String[] topicAndXpathConsumerEPRs = xpathAndTopicReceiverApi.startConsumerService(consumerPort + 1,
                    topicAndXpathMsgReceiver);

            assertTrue("invalid consumer eprs returned", topicAndXpathConsumerEPRs.length > 0);

            String topicAndXpathSubId = xpathAndTopicReceiverApi.subscribe(brokerEpr.getAddress(),
                    topicAndXpathConsumerEPRs[0], topic, xpathExpression);

            WseClientAPI senderApi = new WseClientAPI(brokerEpr);
            senderApi.publish(brokerEpr.getAddress(), topic, matchingMsg);
            senderApi.publish(brokerEpr.getAddress(), topic, unmatchingMsg);

            try {

                Thread.sleep(5000);

                assertTrue("topic only reciever should get all messages" + topicOnlyMsgReceiver.getMsgQueue().size(),
                        topicOnlyMsgReceiver.getMsgQueue().size() == 2);

                assertTrue("xpath and topic reciever should only get one message"
                        + topicAndXpathMsgReceiver.getMsgQueue().size(),
                        topicAndXpathMsgReceiver.getMsgQueue().size() == 1);

            } catch (InterruptedException e) {
                fail("interrupted while waiting for message");
            }

            topicOnlyReceiverApi.unSubscribe(brokerEpr.getAddress(), topicOnlySubId, null);
            topicOnlyReceiverApi.shutdownConsumerService();

            xpathAndTopicReceiverApi.unSubscribe(brokerEpr.getAddress(), topicAndXpathSubId, null);
            xpathAndTopicReceiverApi.shutdownConsumerService();

        } catch (AxisFault e) {
            e.printStackTrace();
            fail("unexpected exception occured");
        }

    }
}
