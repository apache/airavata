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
package org.apache.airavata.messaging.core.impl;

import com.rabbitmq.client.*;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessageHandler;
import org.apache.airavata.model.dbevent.DBEventMessage;
import org.apache.airavata.model.dbevent.DBEventMessageContext;
import org.apache.airavata.model.messaging.event.ExperimentSubmitEvent;
import org.apache.airavata.model.messaging.event.Message;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import java.io.IOException;

public class MessageConsumer extends QueueingConsumer {

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
    public void handleDelivery(String consumerTag,
                               Envelope envelope,
                               AMQP.BasicProperties properties,
                               byte[] body) throws IOException {

        Message message = new Message();

        try {
            logger.info("handleDelivery() -> Handling message delivery. Consumer Tag : " + consumerTag);
            ThriftUtils.createThriftFromBytes(body, message);

            DBEventMessage dBEventMessage = new DBEventMessage();
            ThriftUtils.createThriftFromBytes(message.getEvent(), dBEventMessage);

            MessageContext messageContext = new MessageContext((TBase) dBEventMessage, message.getMessageType(), message.getMessageId(), "gatewayId", envelope.getDeliveryTag());
            handler.onMessage(messageContext);
            //sendAck(deliveryTag);

        } catch (TException e) {
            logger.error("handleDelivery() -> Error handling delivery. Consumer Tag : " + consumerTag, e);
        }

    }


    private void sendAck(long deliveryTag){
        logger.info("sendAck() -> Sending ack. Delivery Tag : " + deliveryTag);
        try {
            if (channel.isOpen()){
                channel.basicAck(deliveryTag,false);
            }else {
                channel = connection.createChannel();
                channel.basicQos(20);
                channel.basicAck(deliveryTag, false);
            }
        } catch (IOException e) {
            logger.error("sendAck() -> Error sending ack. Delivery Tag : " + deliveryTag, e);
        }
    }
}
