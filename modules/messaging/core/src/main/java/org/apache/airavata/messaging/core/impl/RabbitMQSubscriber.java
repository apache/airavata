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

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.messaging.core.Subscriber;
import org.apache.airavata.messaging.core.RabbitMQProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;


public class RabbitMQSubscriber implements Subscriber {
    private static final Logger log = LoggerFactory.getLogger(RabbitMQSubscriber.class);

    private Connection connection;
    private Channel channel;
    private Map<String, QueueDetail> queueDetailMap = new HashMap<>();
    private RabbitMQProperties properties;

    public RabbitMQSubscriber(RabbitMQProperties properties) throws AiravataException {
        this.properties = properties;
        createConnection();
    }

    private void createConnection() throws AiravataException {
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setUri(properties.getBrokerUrl());
            connectionFactory.setAutomaticRecoveryEnabled(properties.isAutoRecoveryEnable());
            connection = connectionFactory.newConnection();
            addShutdownListener();
            log.info("connected to rabbitmq: " + connection + " for " + properties.getExchangeName());
            channel = connection.createChannel();
            channel.basicQos(properties.getPrefetchCount());
            channel.exchangeDeclare(properties.getExchangeName(),
                    properties.getExchangeType(),
                    true); // durable
        } catch (Exception e) {
            String msg = "could not open channel for exchange " + properties.getExchangeName();
            log.error(msg);
            throw new AiravataException(msg, e);
        }
    }

    @Override
    public String listen(BiFunction<Connection, Channel, Consumer> supplier,
                         String queueName,
                         List<String> routingKeys) throws AiravataException {

        try {
            if (!channel.isOpen()) {
                channel = connection.createChannel();
                channel.exchangeDeclare(properties.getExchangeName(), properties.getExchangeType(), false);
            }
            if (queueName == null) {
                queueName = channel.queueDeclare().getQueue();
            } else {
                channel.queueDeclare(queueName,
                                     true, // durable
                                     false, // exclusive
                                     false, // autoDelete
                                     null);// arguments
            }
            final String id = getId(routingKeys, queueName);
            if (queueDetailMap.containsKey(id)) {
                throw new IllegalStateException("This subscriber is already defined for this Subscriber, " +
                        "cannot define the same subscriber twice");
            }
            // bind all the routing keys
            for (String key : routingKeys) {
//                log.info("Binding key:" + key + " to queue:" + queueName);
                channel.queueBind(queueName, properties.getExchangeName(), key);
            }

            channel.basicConsume(queueName,
                    properties.isAutoAck(),
                    properties.getConsumerTag(),
                    supplier.apply(connection, channel));

            queueDetailMap.put(id, new QueueDetail(queueName, routingKeys));
            return id;
        } catch (IOException e) {
            String msg = "could not open channel for exchange " + properties.getExchangeName();
            log.error(msg);
            throw new AiravataException(msg, e);
        }
    }

    @Override
    public void stopListen(String id) throws AiravataException {
        QueueDetail details = queueDetailMap.get(id);
        if (details != null) {
            try {
                for (String key : details.getRoutingKeys()) {
                    channel.queueUnbind(details.getQueueName(), properties.getExchangeName(), key);
                }
                channel.queueDelete(details.getQueueName(), true, true);
            } catch (IOException e) {
                String msg = "could not un-bind queue: " + details.getQueueName() + " for exchange " + properties.getExchangeName();
                log.debug(msg);
            }
        }
    }

    @Override
    public void sendAck(long deliveryTag) {
        try {
            if (channel.isOpen()){
                channel.basicAck(deliveryTag,false);
            }else {
                channel = connection.createChannel();
                channel.basicQos(properties.getPrefetchCount());
                channel.basicAck(deliveryTag, false);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void addShutdownListener() {
        connection.addShutdownListener(new ShutdownListener() {
            public void shutdownCompleted(ShutdownSignalException cause) {
                log.error("RabbitMQ connection " + connection + " for " + properties.getExchangeName() + " has been shut down", cause);
            }
        });
    }


    private String getId(List<String> routingKeys, String queueName) {
        String id = "";
        for (String key : routingKeys) {
            id = id + "_" + key;
        }
        return id + "_" + queueName;
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException ignore) {
            }
        }
    }


    private class QueueDetail {
        String queueName;
        List<String> routingKeys;

        private QueueDetail(String queueName, List<String> routingKeys) {
            this.queueName = queueName;
            this.routingKeys = routingKeys;
        }

        public String getQueueName() {
            return queueName;
        }

        List<String> getRoutingKeys() {
            return routingKeys;
        }
    }
}
