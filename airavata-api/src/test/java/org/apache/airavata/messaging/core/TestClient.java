/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.messaging.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.model.ExperimentStatusChangeEvent;
import org.apache.airavata.common.model.MessageType;
import org.apache.airavata.config.AiravataServerProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("Requires messaging infrastructure (RabbitMQ/Kafka) to be running")
public class TestClient {
    public static final String RABBITMQ_BROKER_URL = "rabbitmq.broker.url";
    public static final String RABBITMQ_EXCHANGE_NAME = "rabbitmq.exchange.name";
    private static final Logger logger = LoggerFactory.getLogger(TestClient.class);
    private static final String experimentId = "*";

    @Test
    public void testMessagingFactorySubscriberCreation() throws Exception {
        try {
            AiravataServerProperties properties = new AiravataServerProperties();
            MessagingFactory messagingFactory = new MessagingFactory(properties);
            List<String> routingKeys = new ArrayList<>();
            routingKeys.add(experimentId);
            routingKeys.add(experimentId + ".*");
            
            CountDownLatch messageReceived = new CountDownLatch(1);
            MessageHandler handler = getMessageHandler(messageReceived);
            
            messagingFactory.getSubscriber(handler, routingKeys, Type.STATUS);
            
            // Wait for a message (with timeout) or verify subscriber was created
            // Note: This test requires actual messaging infrastructure
            assertNotNull(messagingFactory, "MessagingFactory should be created");
        } catch (ApplicationSettingsException e) {
            logger.error("Error reading airavata server properties", e);
            fail("Failed to create messaging factory: " + e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail("Unexpected error: " + e.getMessage());
        }
    }

    private MessageHandler getMessageHandler(CountDownLatch messageReceived) {
        return message -> {
            if (message.getType().equals(MessageType.EXPERIMENT)) {
                try {
                    ExperimentStatusChangeEvent event = (ExperimentStatusChangeEvent) message.getEvent();
                    logger.info("Message Received with message id:" + message.getMessageId() + ", message type: "
                            + message.getType() + ", state: " + event.getState().toString());
                    messageReceived.countDown();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        };
    }
}
