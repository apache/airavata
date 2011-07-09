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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.airavata.wsmg.client.ConsumerNotificationHandler;
import org.apache.airavata.wsmg.client.WsntClientAPI;
import org.apache.airavata.wsmg.commons.WsmgNameSpaceConstants;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import wsmg.util.ConfigKeys;

public class WsntClientAPIRoundTripTests extends TestCase implements ConsumerNotificationHandler {

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
    public final void testRoundTrip() {

        try {

            long value = System.currentTimeMillis();
            String msg = String.format("<msg> current time is : %d </msg>", value);

            WsntClientAPI clientApi = new WsntClientAPI();

            int consumerPort = new Integer(configs.getProperty(ConfigKeys.CONSUMER_PORT));

            String brokerEPR = configs.getProperty(ConfigKeys.BROKER_NOTIFICATIONS_SERVICE_EPR);
            String[] consumerEPRs = clientApi.startConsumerService(consumerPort, this);

            assertTrue(consumerEPRs.length > 0);

            String topic = "WsntRoundTripTestTopic";

            String subscriptionID = clientApi.subscribe(brokerEPR, consumerEPRs[0], topic);

            clientApi.publish(brokerEPR, topic, msg);

            while (true) {
                try {
                    SOAPEnvelope env = getMsgQueue().take();

                    OMElement element = extractRawMessage(env);
                    String text = element.getText();

                    if (text.indexOf(new Long(value).toString()) > 0) {
                        break;
                    }

                    fail("round trip of message failed" + " - due to invalid messege content");

                } catch (InterruptedException e) {
                    fail("interrupted while waiting for message");
                }
            }

            try {
                clientApi.unSubscribe(brokerEPR, subscriptionID, null);
            } catch (AxisFault e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            clientApi.shutdownConsumerService();

        } catch (AxisFault e) {
            e.printStackTrace();
            fail("unexpected exception occured");
        }

    }

    @Test
    public final void testMessageCount() {

        try {
            // messages might be returned out of sequence;

            Integer totalMsgCount = 2;

            List<Integer> recievedSequences = new LinkedList<Integer>();
            String msgFormat = "<msg>%d</msg>";
            String topic = "WsntRoundTripMsgCountTestTopic";

            WsntClientAPI clientApi = new WsntClientAPI();

            int consumerPort = new Integer(configs.getProperty(ConfigKeys.CONSUMER_PORT));

            String brokerEPR = configs.getProperty(ConfigKeys.BROKER_NOTIFICATIONS_SERVICE_EPR);
            String[] consumerEPRs = clientApi.startConsumerService(consumerPort, this);

            assertTrue("consumer should have at least one epr", consumerEPRs.length > 0);

            String subscriptionID = clientApi.subscribe(brokerEPR, consumerEPRs[0], topic);

            for (int i = 1; i <= totalMsgCount; i++) {

                String currentMsg = String.format(msgFormat, i);
                clientApi.publish(brokerEPR, topic, currentMsg);
            }

            while (recievedSequences.size() < totalMsgCount) {
                try {
                    SOAPEnvelope env = getMsgQueue().take();

                    OMElement element = extractRawMessage(env);

                    int currentSequence = Integer.parseInt(element.getText());

                    assertFalse("duplicate message recieved", recievedSequences.contains(currentSequence));

                    recievedSequences.add(currentSequence);

                } catch (InterruptedException e) {
                    fail("interrupted while waiting for message");
                }
            }

            try {
                Thread.sleep(5000);

                assertTrue("duplicate and/or invalid messages recieved", getMsgQueue().isEmpty());

            } catch (InterruptedException e) {
                fail("test case was interrupted");
            }

            try {
                clientApi.unSubscribe(brokerEPR, subscriptionID, null);
            } catch (AxisFault e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            clientApi.shutdownConsumerService();

        } catch (AxisFault e) {
            e.printStackTrace();
            fail("unexpected exception occured");
        }

    }

    private OMElement extractRawMessage(SOAPEnvelope env) {

        QName notify = new QName(WsmgNameSpaceConstants.WSNT_NS.getNamespaceURI(), "Notify",
                WsmgNameSpaceConstants.WSNT_NS.getPrefix());

        QName notifyMsg = new QName(WsmgNameSpaceConstants.WSNT_NS.getNamespaceURI(), "NotificationMessage",
                WsmgNameSpaceConstants.WSNT_NS.getPrefix());

        QName msg = new QName(WsmgNameSpaceConstants.WSNT_NS.getNamespaceURI(), "Message",
                WsmgNameSpaceConstants.WSNT_NS.getPrefix());

        assertNotNull("envelope can't be null", env);
        assertNotNull("envelope don't have a body", env.getBody());

        Iterator ite = env.getBody().getChildrenWithName(notify);

        assertTrue(notify.getLocalPart() + " tag is not found", ite.hasNext());

        OMElement ele = (OMElement) ite.next();
        ite = ele.getChildrenWithName(notifyMsg);

        assertTrue(notifyMsg.getLocalPart() + " tag is not found", ite.hasNext());

        ele = (OMElement) ite.next();

        ite = ele.getChildrenWithName(msg);
        assertTrue(msg.getLocalPart() + " tag is not found", ite.hasNext());

        ele = (OMElement) ite.next();

        assertNotNull("raw message was not found", ele.getFirstElement());

        return ele.getFirstElement();
    }

}
