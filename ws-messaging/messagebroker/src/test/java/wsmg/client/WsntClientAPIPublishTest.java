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
import org.apache.airavata.wsmg.client.WsntClientAPI;
import org.apache.axis2.AxisFault;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import wsmg.util.ConfigKeys;

public class WsntClientAPIPublishTest extends TestCase {

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

    // basic pubilsh test
    @Test
    public final void testPublishWithBrokerURL() {

        try {
            WsntClientAPI clientApi = new WsntClientAPI();

            String brokerEPR = configs.getProperty(ConfigKeys.BROKER_NOTIFICATIONS_SERVICE_EPR);
            String topic = "MYSimpleTOPIC";

            String message = "<msg> test message </msg>";
            // should throw an exception
            clientApi.publish(brokerEPR, topic, message);

        } catch (AxisFault e) {
            e.printStackTrace();
            fail("unable to publish: " + e.toString());
        }

    }

    // basic publish test
    @Test(expected = AxisFault.class)
    public final void testPublishInvalidBrokerLocation() {

        WseClientAPI clientApi = new WseClientAPI();

        String brokerEPR = "http://invalid/url/";
        String topic = "MYSimpleTOPIC";

        String message = "<msg> test message </msg>";
        // should throw an exception
        try {
            clientApi.publish(brokerEPR, topic, message);
            fail("didn't throw an exception");
        } catch (AxisFault e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
