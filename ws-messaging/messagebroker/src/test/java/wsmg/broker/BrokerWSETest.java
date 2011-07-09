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

package wsmg.broker;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.TestCase;

import org.apache.airavata.wsmg.client.ConsumerNotificationHandler;
import org.apache.airavata.wsmg.client.WseClientAPI;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.junit.Test;

public class BrokerWSETest extends TestCase implements ConsumerNotificationHandler {

    static Properties configs = new Properties();

    BlockingQueue<SOAPEnvelope> queue = new LinkedBlockingQueue<SOAPEnvelope>();

    public void handleNotification(SOAPEnvelope msgEnvelope) {
        queue.add(msgEnvelope);
        System.out.println("Received " + msgEnvelope);
    }

    BlockingQueue<SOAPEnvelope> getMsgQueue() {
        return queue;
    }

    @Override
    protected void setUp() throws Exception {
        // TestUtilServer.start(null, null);
    }

    @Test
    public void testRoundTrip() throws InterruptedException {

        try {

            String brokerEPR = "http://127.0.0.1:8080/axis2/services/EventingService";
            long value = System.currentTimeMillis();
            String msg = String.format("<msg> current time is : %d </msg>", value);

            WseClientAPI clientApi = new WseClientAPI(null, 1000000);

            int consumerPort = 6767;

            String[] consumerEPRs = clientApi.startConsumerService(consumerPort, this);

            assertTrue(consumerEPRs.length > 0);

            String topic = "WseRoundTripTestTopic";

            String subscriptionID = clientApi.subscribe(brokerEPR, consumerEPRs[0], topic);
            clientApi.subscribe(brokerEPR, consumerEPRs[0], topic, "/foo/bar");

            clientApi.publish(brokerEPR, topic, msg);

            clientApi.publish(brokerEPR, topic, "<foo><bar>Test</bar></foo>");

            Thread.sleep(2000);

            try {
                clientApi.unSubscribe(brokerEPR, subscriptionID, null);
            } catch (AxisFault e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            clientApi.shutdownConsumerService();

        } catch (AxisFault e) {
            e.printStackTrace();
            try {
                System.in.read();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            // fail("unexpected exception occured");
        }
        System.out.println("Broker roundtrip done");

    }

    @Override
    protected void tearDown() throws Exception {
        // TestUtilServer.stop();
    }

}
