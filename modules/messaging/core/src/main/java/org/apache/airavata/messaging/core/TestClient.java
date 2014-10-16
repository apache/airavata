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

package org.apache.airavata.messaging.core;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.messaging.core.impl.RabbitMQConsumer;
import org.apache.airavata.model.messaging.event.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


public class TestClient {
    public static final String RABBITMQ_BROKER_URL = "rabbitmq.broker.url";
    public static final String RABBITMQ_EXCHANGE_NAME = "rabbitmq.exchange.name";
    private final static Logger logger = LoggerFactory.getLogger(TestClient.class);
    private final static String experimentId = "echoExperiment_cc733586-2bf8-4ee2-8a25-6521db135e7f";

    public static void main(String[] args) {
        try {
            AiravataUtils.setExecutionAsServer();
            String brokerUrl = ServerSettings.getSetting(RABBITMQ_BROKER_URL);
            String exchangeName = ServerSettings.getSetting(RABBITMQ_EXCHANGE_NAME);
            RabbitMQConsumer consumer = new RabbitMQConsumer(brokerUrl, exchangeName);
            consumer.listen(new MessageHandler() {
                @Override
                public Map<String, String> getProperties() {
                    Map<String, String> props = new HashMap<String, String>();
                    props.put(MessagingConstants.RABBIT_ROUTING_KEY, experimentId);
                    return props;
                }

                @Override
                public void onMessage(MessageContext message) {
                    System.out.println(" Message Received with message id '" + message.getMessageId()
                            + "' and with message type '" + message.getType());
                    System.out.println("message received: " + message);
                }
            });
        } catch (ApplicationSettingsException e) {
            logger.error("Error reading airavata server properties", e);
        }catch (Exception e) {
            e.printStackTrace();
        }

    }
}
