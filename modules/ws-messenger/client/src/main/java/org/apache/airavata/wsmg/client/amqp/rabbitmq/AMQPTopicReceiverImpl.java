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

package org.apache.airavata.wsmg.client.amqp.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.airavata.wsmg.client.amqp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * AMQPTopicReceiver class provides functionality to receive messages based on a pattern.
 * These patterns are also called Topics.
 */
public class AMQPTopicReceiverImpl extends AMQPRoutingAwareClient implements AMQPTopicReceiver {
    private static final Logger log = LoggerFactory.getLogger(AMQPTopicReceiverImpl.class);
    
    private AMQPCallback callback = null;

    public AMQPTopicReceiverImpl(Properties properties, AMQPCallback callback) {
        super(properties);
        
        this.callback = callback;
    }

    public void Subscribe(AMQPRoutingKey topic) throws AMQPException {
        if (callback != null) {
            try {
                Connection connection = connectionFactory.newConnection();

                Channel channel = connection.createChannel();
                channel.exchangeDeclare(AMQPUtil.EXCHANGE_NAME_TOPIC, AMQPUtil.EXCHANGE_TYPE_TOPIC);

                String queueName = channel.queueDeclare().getQueue();
                channel.queueBind(queueName, AMQPUtil.EXCHANGE_NAME_TOPIC, topic.getNativeKey());

                QueueingConsumer consumer = new QueueingConsumer(channel);
                channel.basicConsume(queueName, true, consumer);

                while (true) {
                    QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                    String message = new String(delivery.getBody());

                    callback.onMessage(message);
                }
            } catch (Exception e) {
                throw new AMQPException(e);
            }
        }
    }
}
