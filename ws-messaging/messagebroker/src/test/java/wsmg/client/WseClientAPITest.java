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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class WseClientAPITest extends TestCase {

    static class ConfigKeys {
        static String BROKER_EPR = "broker.epr";
        static String CONSUMER_EPR = "consumer.location";
        static String CONSUMER_PORT = "consumer.port";
        static String TOPIC_SIME = "topic.simple";
        static String TOPIC_XPATH = "topic.xpath";
    }

    static Properties configs = new Properties();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        URL configURL = ClassLoader.getSystemResource("unit_tests.properties");
        configs.load(configURL.openStream());

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public final void testSubscribe() {
        WseClientAPI clientApi = new WseClientAPI();
        try {
            clientApi.subscribe(configs.getProperty(ConfigKeys.BROKER_EPR),
                    configs.getProperty(ConfigKeys.CONSUMER_EPR), configs.getProperty(ConfigKeys.TOPIC_SIME));
        } catch (AxisFault e) {

            fail("unable to subscribe: " + e.toString());
        }

    }

}
