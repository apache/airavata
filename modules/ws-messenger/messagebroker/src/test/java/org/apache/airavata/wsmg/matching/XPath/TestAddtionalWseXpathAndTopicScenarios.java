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

package org.apache.airavata.wsmg.matching.XPath;

import java.net.URL;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import org.apache.airavata.wsmg.client.ConsumerNotificationHandler;
import org.apache.airavata.wsmg.client.WseMsgBrokerClient;
import org.apache.airavata.wsmg.util.ConfigKeys;
import org.apache.airavata.wsmg.util.TestUtilServer;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

    @Override
    @Before
    public void setUp() throws Exception {
        URL configURL = ClassLoader.getSystemResource(ConfigKeys.CONFIG_FILE_NAME);
        configs.load(configURL.openStream());

        TestUtilServer.start(null, null);
    }

    @Override
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

            String brokerEpr = "http://localhost:" + TestUtilServer.TESTING_PORT + "/axis2/services/EventingService";

            WseMsgBrokerClient topicOnlyReceiverApi = new WseMsgBrokerClient();
            topicOnlyReceiverApi.init(brokerEpr);
            NotificationReciever topicOnlyMsgReceiver = new NotificationReciever("Topic Only");
            String[] topicConsumerEPRs = topicOnlyReceiverApi.startConsumerService(consumerPort, topicOnlyMsgReceiver);
            assertTrue("invalid consumer eprs returned", topicConsumerEPRs.length > 0);
            String topicOnlySubId = topicOnlyReceiverApi.subscribe(topicConsumerEPRs[0], topic, null);
            System.out.println("Topic only subscription ID: " + topicOnlySubId);

            WseMsgBrokerClient xpathAndTopicReceiverApi = new WseMsgBrokerClient();
            xpathAndTopicReceiverApi.init(brokerEpr);
            NotificationReciever topicAndXpathMsgReceiver = new NotificationReciever("Topic And Xpath");
            String[] topicAndXpathConsumerEPRs = xpathAndTopicReceiverApi.startConsumerService(consumerPort + 1,
                    topicAndXpathMsgReceiver);
            assertTrue("invalid consumer eprs returned", topicAndXpathConsumerEPRs.length > 0);
            String topicAndXpathSubId = xpathAndTopicReceiverApi.subscribe(topicAndXpathConsumerEPRs[0], topic,
                    xpathExpression);
            System.out.println("Xpath and Topic subscription ID: " + topicAndXpathSubId);

            WseMsgBrokerClient senderApi = new WseMsgBrokerClient();
            senderApi.init(brokerEpr);

            try {

                senderApi.publish(topic, AXIOMUtil.stringToOM(matchingMsg));
                senderApi.publish(topic, AXIOMUtil.stringToOM(unmatchingMsg));

                Thread.sleep(5000);

                assertTrue("topic only reciever should get all messages" + topicOnlyMsgReceiver.getMsgQueue().size(),
                        topicOnlyMsgReceiver.getMsgQueue().size() == 2);

                assertTrue("xpath and topic reciever should only get one message"
                        + topicAndXpathMsgReceiver.getMsgQueue().size(),
                        topicAndXpathMsgReceiver.getMsgQueue().size() == 1);
            } catch (XMLStreamException x) {
                fail("Error while creating OMElement");
            } catch (InterruptedException e) {
                fail("interrupted while waiting for message");
            }

            topicOnlyReceiverApi.unSubscribe(topicOnlySubId);
            topicOnlyReceiverApi.shutdownConsumerService();

            xpathAndTopicReceiverApi.unSubscribe(topicAndXpathSubId);
            xpathAndTopicReceiverApi.shutdownConsumerService();

        } catch (AxisFault e) {
            e.printStackTrace();
            fail("unexpected exception occured");
        }

    }
}
