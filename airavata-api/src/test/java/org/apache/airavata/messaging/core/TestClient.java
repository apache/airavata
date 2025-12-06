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
import org.apache.airavata.api.thrift.util.ThriftUtils;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.model.messaging.event.ExperimentStatusChangeEvent;
import org.apache.airavata.model.messaging.event.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestClient {
    public static final String RABBITMQ_BROKER_URL = "rabbitmq.broker.url";
    public static final String RABBITMQ_EXCHANGE_NAME = "rabbitmq.exchange.name";
    private static final Logger logger = LoggerFactory.getLogger(TestClient.class);
    private static final String experimentId = "*";

    public static void main(String[] args) {
        try {
            List<String> routingKeys = new ArrayList<>();
            routingKeys.add(experimentId);
            routingKeys.add(experimentId + ".*");
            MessagingFactory.getSubscriber(getMessageHandler(), routingKeys, Type.STATUS);
        } catch (ApplicationSettingsException e) {
            logger.error("Error reading airavata server properties", e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static MessageHandler getMessageHandler() {
        return message -> {
            if (message.getType().equals(MessageType.EXPERIMENT)) {
                try {
                    ExperimentStatusChangeEvent event = new ExperimentStatusChangeEvent();
                    var messageEvent = message.getEvent();
                    byte[] bytes = ThriftUtils.serializeThriftObject(messageEvent);
                    ThriftUtils.createThriftFromBytes(bytes, event);
                    logger.info("Message Received with message id:" + message.getMessageId() + ", message type: "
                            + message.getType() + ", state: " + event.getState().toString());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        };
    }
}
