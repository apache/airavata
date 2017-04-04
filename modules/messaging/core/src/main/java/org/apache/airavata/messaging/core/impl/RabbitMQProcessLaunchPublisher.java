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
//package org.apache.airavata.messaging.core.impl;
//
//import org.apache.airavata.common.exception.AiravataException;
//import org.apache.airavata.common.exception.ApplicationSettingsException;
//import org.apache.airavata.common.utils.ServerSettings;
//import org.apache.airavata.common.utils.ThriftUtils;
//import org.apache.airavata.messaging.core.MessageContext;
//import org.apache.airavata.messaging.core.MessagingConstants;
//import org.apache.airavata.messaging.core.Publisher;
//import org.apache.airavata.model.messaging.event.*;
//import org.apache.thrift.TException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class RabbitMQProcessLaunchPublisher implements Publisher{
//    private final static Logger log = LoggerFactory.getLogger(RabbitMQProcessLaunchPublisher.class);
//    private  String launchTask;
//
//    private RabbitMQProducer rabbitMQProducer;
//
//    public RabbitMQProcessLaunchPublisher() throws Exception {
//        String brokerUrl;
//        try {
//            brokerUrl = ServerSettings.getSetting(MessagingConstants.RABBITMQ_BROKER_URL);
//            launchTask = ServerSettings.getRabbitmqProcessLaunchQueueName();
//        } catch (ApplicationSettingsException e) {
//            String message = "Failed to get read the required properties from airavata to initialize rabbitmq";
//            log.error(message, e);
//            throw new AiravataException(message, e);
//        }
//        rabbitMQProducer = new RabbitMQProducer(brokerUrl, null,null);
//        rabbitMQProducer.open();
//    }
//
//    public void publish(MessageContext msgCtx) throws AiravataException {
//        try {
//            log.info("Publishing to launch queue ...");
//            byte[] body = ThriftUtils.serializeThriftObject(msgCtx.getEvent());
//            Message message = new Message();
//            message.setEvent(body);
//            message.setMessageId(msgCtx.getMessageId());
//            message.setMessageType(msgCtx.getType());
//            message.setUpdatedTime(msgCtx.getUpdatedTime().getTime());
//            String routingKey = launchTask;
//            byte[] messageBody = ThriftUtils.serializeThriftObject(message);
//            rabbitMQProducer.sendToWorkerQueue(messageBody, routingKey);
//            log.info("Successfully published to launch queue ...");
//        } catch (TException e) {
//            String msg = "Error while deserializing the object";
//            log.error(msg, e);
//            throw new AiravataException(msg, e);
//        } catch (Exception e) {
//            String msg = "Error while sending to rabbitmq";
//            log.error(msg, e);
//            throw new AiravataException(msg, e);
//        }
//    }
//}
