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

import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import org.apache.airavata.wsmg.client.ConsumerNotificationHandler;
import org.apache.airavata.wsmg.client.WseClientAPI;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import wsmg.util.ConfigKeys;

public class TestWseXpathSubscription extends TestCase implements ConsumerNotificationHandler {

    static Properties configs = new Properties();

    BlockingQueue<SOAPEnvelope> queue = new LinkedBlockingQueue<SOAPEnvelope>();

    public void handleNotification(SOAPEnvelope msgEnvelope) {

        queue.add(msgEnvelope);
        System.out.println(msgEnvelope);
    }

    BlockingQueue<SOAPEnvelope> getMsgQueue() {
        return queue;
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
    public final void testXpathOnlyRoundTrip() {

        try {

            String validMsgFormat = "<c><b><a> %d </a></b></c>";
            String invalidMsgFormat = "<a><b><c> %d </c></b></a>";

            long value = System.currentTimeMillis();
            String validMsg = String.format(validMsgFormat, value);
            String invalidMsg = String.format(invalidMsgFormat, value);

            int consumerPort = new Integer(configs.getProperty(ConfigKeys.CONSUMER_PORT));

            String brokerEPR = configs.getProperty(ConfigKeys.BROKER_EVENTING_SERVICE_EPR);

            WseClientAPI clientApi = new WseClientAPI(new EndpointReference(brokerEPR));

            String[] consumerEPRs = clientApi.startConsumerService(consumerPort, this);

            assertTrue(consumerEPRs.length > 0);

            String xpathExpression = "/c/b/a";

            String subscriptionID = clientApi.subscribe(brokerEPR, consumerEPRs[0], null, xpathExpression);

            clientApi.publish(validMsg);
            clientApi.publish(invalidMsg);

            try {
                SOAPEnvelope env = getMsgQueue().take();

                assertNotNull(env.getBody());
                assertNotNull(env.getBody().getChildrenWithLocalName("c"));

                OMElement element = (OMElement) env.getBody().getChildrenWithLocalName("c").next();

                String text = element.toStringWithConsume();

                assertTrue("round trip of message failed" + " - due to invalid messege content",
                        text.indexOf(new Long(value).toString()) > 0);

                Thread.sleep(5000);

                assertTrue("unexpected msg recieved", getMsgQueue().isEmpty());

            } catch (InterruptedException e) {
                fail("interrupted while waiting for message");
            } catch (XMLStreamException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                fail("invalid xml recieved: " + e.getMessage());
            }

            clientApi.unSubscribe(brokerEPR, subscriptionID, null);
            clientApi.shutdownConsumerService();

        } catch (AxisFault e) {
            e.printStackTrace();
            fail("unexpected exception occured");
        }

    }

    @Test
    public final void testSimpleXpathTopicRoundTrip() {

        try {

            String validMsgFormat = "<c><b><a> %d </a></b></c>";
            String invalidMsgFormat = "<a><b><c> %d </c></b></a>";

            long value = System.currentTimeMillis();
            String validMsg = String.format(validMsgFormat, value);
            String invalidMsg = String.format(invalidMsgFormat, value);

            int consumerPort = new Integer(configs.getProperty(ConfigKeys.CONSUMER_PORT));

            String brokerEPR = configs.getProperty(ConfigKeys.BROKER_EVENTING_SERVICE_EPR);

            WseClientAPI clientApi = new WseClientAPI(new EndpointReference(brokerEPR));

            String[] consumerEPRs = clientApi.startConsumerService(consumerPort, this);

            assertTrue(consumerEPRs.length > 0);

            String xpathExpression = "/c/b/a";
            String topicExpression = "XpathAndTopicTestWse";

            String subscriptionID = clientApi.subscribe(brokerEPR, consumerEPRs[0], topicExpression, xpathExpression);

            clientApi.publish(brokerEPR, topicExpression, validMsg);
            clientApi.publish(brokerEPR, topicExpression, invalidMsg);

            try {
                SOAPEnvelope env = getMsgQueue().take();

                assertNotNull(env.getBody());
                assertNotNull(env.getBody().getChildrenWithLocalName("c"));

                OMElement element = (OMElement) env.getBody().getChildrenWithLocalName("c").next();

                String text = element.toStringWithConsume();

                assertTrue("round trip of message failed" + " - due to invalid messege content",
                        text.indexOf(new Long(value).toString()) > 0);

                Thread.sleep(5000);

                assertTrue("unexpected msg recieved", getMsgQueue().isEmpty());

            } catch (InterruptedException e) {
                fail("interrupted while waiting for message");
            } catch (XMLStreamException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                fail("invalid xml recieved: " + e.getMessage());
            }

            clientApi.unSubscribe(brokerEPR, subscriptionID, null);
            clientApi.shutdownConsumerService();

        } catch (AxisFault e) {
            e.printStackTrace();
            fail("unexpected exception occured");
        }

    }
}
