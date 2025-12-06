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
package org.apache.airavata.messaging.core.impl;

import com.rabbitmq.client.*;
import java.io.IOException;
import org.apache.airavata.api.thrift.util.ThriftUtils;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessageHandler;
import org.apache.airavata.model.dbevent.DBEventMessage;
import org.apache.airavata.model.messaging.event.Message;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class MessageConsumer extends DefaultConsumer {

    private static final Logger logger = LogManager.getLogger(MessageConsumer.class);

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

        Message message = new Message();

        try {
            logger.info("handleDelivery() -> Handling message delivery. Consumer Tag : " + consumerTag);
            ThriftUtils.createThriftFromBytes(body, message);

            DBEventMessage dBEventMessage = new DBEventMessage();
            ThriftUtils.createThriftFromBytes(message.getEvent(), dBEventMessage);

            MessageContext messageContext = new MessageContext(
                    dBEventMessage,
                    message.getMessageType(),
                    message.getMessageId(),
                    "gatewayId",
                    envelope.getDeliveryTag());
            handler.onMessage(messageContext);
            // sendAck(deliveryTag);

        } catch (Exception e) {
            logger.error("handleDelivery() -> Error handling delivery. Consumer Tag : " + consumerTag, e);
        }
    }
}
