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

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.airavata.wsmg.client.ConsumerNotificationHandler;
import org.apache.airavata.wsmg.client.MsgBrokerClientException;
import org.apache.airavata.wsmg.client.WseMsgBrokerClient;
import org.apache.airavata.wsmg.util.test.TestUtilServer;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestWsntMsgBrokerClient {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testRoundTrip() throws Exception {

        TestUtilServer.start(null, null);

        final LinkedBlockingQueue<SOAPEnvelope> queue = new LinkedBlockingQueue<SOAPEnvelope>();

        ConsumerNotificationHandler handler = new ConsumerNotificationHandler() {

            public void handleNotification(SOAPEnvelope msgEnvelope) throws AxisFault {
                queue.add(msgEnvelope);
                System.out.println(msgEnvelope.toString());
            }
        };

        try {

            String brokerEPR = "http://127.0.0.1:" + TestUtilServer.TESTING_PORT + "/axis2/services/EventingService";
            long value = System.currentTimeMillis();
            String msg = String.format("<msg> current time is : %d </msg>", value);

            WseMsgBrokerClient client = new WseMsgBrokerClient();
            client.init(brokerEPR);

            int consumerPort = 6767;

            String[] consumerEPRs = client.startConsumerService(consumerPort, handler);

            assertTrue(consumerEPRs.length > 0);

            String topic = "WseRoundTripTestTopic";

            String subscriptionID = client.subscribe(consumerEPRs[0], topic, null);

            System.out.println("topic sub id = " + subscriptionID);

            String subscriptionID2 = client.subscribe(consumerEPRs[0], topic, "/foo/bar");

            System.out.println("xpath sub id = " + subscriptionID2);

            client.publish(topic, msg);

            client.publish(topic, "<foo><bar>Test</bar></foo>");

            Thread.sleep(2000);

            client.unSubscribe(subscriptionID);

            client.shutdownConsumerService();

        } catch (MsgBrokerClientException e) {
            e.printStackTrace();
            try {
                System.in.read();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            // fail("unexpected exception occured");
        }

        TestUtilServer.stop();
        System.out.println("Broker roundtrip done");

    }

}
