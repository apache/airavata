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
package org.apache.airavata.messaging.consumer;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.airavata.messaging.service.MessageContext;
import org.apache.airavata.messaging.service.MessageHandler;
import org.apache.airavata.model.dbevent.proto.DBEventMessage;
import org.apache.airavata.model.messaging.event.proto.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageListener;

public class MessageConsumer implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);

    private final MessageHandler handler;

    public MessageConsumer(MessageHandler messageHandler) {
        this.handler = messageHandler;
    }

    @Override
    public void onMessage(org.springframework.amqp.core.Message amqpMessage) {
        try {
            byte[] body = amqpMessage.getBody();
            long deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag();
            String consumerTag = amqpMessage.getMessageProperties().getConsumerTag();

            logger.info("handleDelivery() -> Handling message delivery. Consumer Tag : " + consumerTag);
            Message message = Message.parseFrom(body);

            DBEventMessage dBEventMessage = DBEventMessage.parseFrom(message.getEvent());

            MessageContext messageContext = new MessageContext(
                    dBEventMessage, message.getMessageType(), message.getMessageId(), "gatewayId", deliveryTag);
            handler.onMessage(messageContext);

        } catch (InvalidProtocolBufferException e) {
            logger.error("handleDelivery() -> Error handling delivery", e);
        }
    }
}
