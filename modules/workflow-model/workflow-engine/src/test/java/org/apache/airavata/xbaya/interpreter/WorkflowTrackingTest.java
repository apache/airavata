/**
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
 */
///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
//*/
//package org.apache.airavata.xbaya.interpreter;
//
//
//import junit.framework.Assert;
//import org.apache.airavata.wsmg.client.ConsumerNotificationHandler;
//import org.apache.airavata.wsmg.client.WseMsgBrokerClient;
//import org.apache.airavata.xbaya.interpreter.utils.ConfigKeys;
//import org.apache.airavata.xbaya.interpreter.utils.TestUtilServer;
//import org.apache.axiom.soap.SOAPEnvelope;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.rules.MethodRule;
//import org.junit.rules.TestWatchman;
//import org.junit.runners.model.FrameworkMethod;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.Properties;
//
//public class WorkflowTrackingTest implements ConsumerNotificationHandler{
//    final static Logger logger = LoggerFactory.getLogger(ConsumerNotificationHandler.class);
//
//    @Rule
//    public MethodRule watchman = new TestWatchman() {
//        public void starting(FrameworkMethod method) {
//            logger.info("{} being run...", method.getName());
//        }
//    };
//
//    public void handleNotification(SOAPEnvelope msgEnvelope) {
//        logger.info("Received " + msgEnvelope);
//        String message = "<?xml version='1.0' encoding='utf-8'?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Header><wsa:Action xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">http://org.apache.airavata/WseNotification</wsa:Action><wsa:MessageID xmlns:wsa=\"http://www.w3.org/2005/08/addressing\" /><wsa:To xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">http://140.182.151.20:61436/axis2/services/ConsumerService/</wsa:To><wsnt:Topic xmlns:wsnt=\"http://www.ibm.com/xmlns/stdwip/web-services/WS-BaseNotification\" xmlns:ns2=\"http://tutorial.globus.org/auction\" Dialect=\"http://www.ibm.com/xmlns/stdwip/web-services/WS-Topics/TopicExpression/simple\">ns2:abc</wsnt:Topic></soapenv:Header><soapenv:Body><wor:workflowInvoked xmlns:wor=\"http://airavata.apache.org/schemas/wft/2011/08\" infoModelVersion=\"2.6\"><wor:notificationSource wor:serviceID=\"abc\" /><wor:timestamp>2011-12-20T14:47:33.736-05:00</wor:timestamp><wor:description>Workflow Started</wor:description><wor:annotation /><wor:initiator wor:serviceID=\"abc\" /></wor:workflowInvoked></soapenv:Body></soapenv:Envelope>";
//        Assert.assertEquals(message,msgEnvelope);
//    }
//
//    private static Properties getDefaults() {
//            Properties defaults = new Properties();
//            defaults.setProperty(ConfigKeys.MSGBOX_SERVICE_URL,
//                    "http://localhost:8080/axis2/services/MsgBoxService");
//            defaults.setProperty(ConfigKeys.MSGBROKER_SERVICE_URL,
//                    "http://localhost:8080/axis2/services/EventingService");
//            defaults.setProperty(ConfigKeys.TOPIC, "abc");
//            return defaults;
//        }
//
//    protected void setUp() throws Exception {
//        TestUtilServer.start(null, null);
//    }
//
//    protected void tearDown() throws Exception {
//            TestUtilServer.stop();
//    }
//
//    @Test
//    public void WorkflowTrackingtest() throws Exception{
//        logger.info("Running WorkflowTrackingTest...");
//        setUp();
//        Properties configurations = new Properties(getDefaults());
//        WseMsgBrokerClient brokerClient = new WseMsgBrokerClient();
//        String brokerLocation = "http://localhost:" + TestUtilServer.TESTING_PORT + "/axis2/services/EventingService";
//        brokerClient.init(brokerLocation);
//        String topic = configurations.getProperty(ConfigKeys.TOPIC);
//        NotificationSender sender = null;
//        sender = new NotificationSender(brokerLocation, topic);
//
//        int consumerPort = TestUtilServer.getAvailablePort();
//
//            String[] consumerEPRs = brokerClient.startConsumerService(consumerPort, this);
//        // subscribing to the above created messsage box with configured topic
//        String subscriptionID = brokerClient.subscribe(consumerEPRs[0], topic, null);
//
//        sender.workflowStarted("Workflow Started");
//
//        //Here we simply assume the workflow invocation is the invoke of the subscribe operation of EventingService and result
//        // Is considered as the subscriptionID got from subscribe operation
//        try {
//            Thread.sleep(10000L);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        brokerClient.unSubscribe(subscriptionID);
//        Assert.assertEquals(brokerLocation,"http://localhost:" + TestUtilServer.TESTING_PORT + "/axis2/services/EventingService");
//        tearDown();
//    }
//}