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
package org.apache.airavata.messaging.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import org.apache.airavata.config.JacksonConfig;
import org.apache.airavata.messaging.MessageContext;
import org.apache.airavata.messaging.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageConsumer extends DefaultConsumer {

    private static final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);

    private static ObjectMapper objectMapper() {
        return JacksonConfig.getGlobalMapper();
    }

    private MessageHandler handler;
    private Channel channel;
    private Connection connection;

    public MessageConsumer(MessageHandler messageHandler, Connection connection, Channel channel) {
        super(channel);
        this.handler = messageHandler;
        this.connection = connection;
        this.channel = channel;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
            throws IOException {

        try {
            logger.info("handleDelivery() -> Handling message delivery. Consumer Tag : " + consumerTag);

            // Deserialize JSON bytes to MessageContext.Wrapper using Jackson (RabbitMQ uses JSON, never Thrift)
            // All RabbitMQ messages in airavata-api use Jackson JSON serialization
            MessageContext.Wrapper jsonWrapper = objectMapper().readValue(body, MessageContext.Wrapper.class);
            MessageContext messageContext = jsonWrapper.toMessageContext();

            handler.onMessage(messageContext);
            // sendAck(deliveryTag);

        } catch (Exception e) {
            logger.error("handleDelivery() -> Error handling delivery. Consumer Tag : " + consumerTag, e);
        }
    }
}
