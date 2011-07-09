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

import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import org.apache.airavata.wsmg.client.ConsumerNotificationHandler;
import org.apache.airavata.wsmg.client.WsntClientAPI;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import wsmg.util.ConfigKeys;

public class WsntClientAPIConsumerTest extends TestCase {

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
    public final void testConsumerService() {

        ConsumerNotificationHandler handler = new ConsumerNotificationHandler() {

            public void handleNotification(SOAPEnvelope msgEnvelope) {
                try {
                    System.out.println(msgEnvelope.toStringWithConsume());
                } catch (XMLStreamException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };

        try {
            WsntClientAPI clientApi = new WsntClientAPI();

            int consumerPort = new Integer(configs.getProperty(ConfigKeys.CONSUMER_PORT));

            clientApi.startConsumerService(consumerPort, handler);
            clientApi.shutdownConsumerService();

        } catch (AxisFault e) {
            e.printStackTrace();
            fail("unable to start consumer service: " + e.toString());
        }

    }

}
