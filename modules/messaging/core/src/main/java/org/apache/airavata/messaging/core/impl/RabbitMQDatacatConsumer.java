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

package org.apache.airavata.messaging.core.impl;


import com.rabbitmq.client.*;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.messaging.core.Consumer;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessageHandler;
import org.apache.airavata.messaging.core.MessagingConstants;
import org.apache.airavata.model.messaging.event.*;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RabbitMQDatacatConsumer implements Consumer {
    private static Logger log = LoggerFactory.getLogger(RabbitMQConsumer.class);

    private String exchangeName;
    private String url;
    private Connection connection;
    private Channel channel;
    private Map<String, QueueDetails> queueDetailsMap = new HashMap<String, QueueDetails>();

    public RabbitMQDatacatConsumer() throws AiravataException {
        try {
            url = ServerSettings.getSetting(MessagingConstants.RABBITMQ_BROKER_URL);
            exchangeName = ServerSettings.getDatacatExchange();

            url = "amqp://localhost:5672";
            exchangeName = "datacat";

            createConnection();
        } catch (ApplicationSettingsException e) {
            String message = "Failed to get read the required properties from airavata to initialize rabbitmq";
            log.error(message, e);
            throw new AiravataException(message, e);
        }
    }

    public RabbitMQDatacatConsumer(String brokerUrl, String exchangeName) throws AiravataException {
        this.exchangeName = exchangeName;
        this.url = brokerUrl;

        createConnection();
    }

    private void createConnection() throws AiravataException {
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setUri(url);
            connection = connectionFactory.newConnection();
            connection.addShutdownListener(new ShutdownListener() {
                public void shutdownCompleted(ShutdownSignalException cause) {
                }
            });
            log.info("connected to rabbitmq: " + connection + " for " + exchangeName);

            channel = connection.createChannel();
            channel.exchangeDeclare(exchangeName, "topic", false);

        } catch (Exception e) {
            String msg = "could not open channel for exchange " + exchangeName;
            log.error(msg);
            throw new AiravataException(msg, e);
        }
    }

    public String listen(final MessageHandler handler) throws AiravataException {
        try {
            Map<String, Object> props = handler.getProperties();
            final Object routing = props.get(MessagingConstants.RABBIT_ROUTING_KEY);
            if (routing == null) {
                throw new IllegalArgumentException("The routing key must be present");
            }

            List<String> keys = new ArrayList<String>();
            if (routing instanceof List) {
                for (Object o : (List)routing) {
                    keys.add(o.toString());
                }
            } else if (routing instanceof String) {
                keys.add((String) routing);
            }

            String queueName = (String) props.get(MessagingConstants.RABBIT_QUEUE);
            String consumerTag = (String) props.get(MessagingConstants.RABBIT_CONSUMER_TAG);
            if (queueName == null) {
                if (!channel.isOpen()) {
                    channel = connection.createChannel();
                    channel.exchangeDeclare(exchangeName, "topic", false);
                }
                queueName = channel.queueDeclare().getQueue();
            } else {
                channel.queueDeclare(queueName, true, false, false, null);
            }

            final String id = getId(keys, queueName);
            if (queueDetailsMap.containsKey(id)) {
                throw new IllegalStateException("This subscriber is already defined for this Consumer, " +
                        "cannot define the same subscriber twice");
            }

            if (consumerTag == null) {
                consumerTag = "default";
            }

            // bind all the routing keys
            for (String routingKey : keys) {
                channel.queueBind(queueName, exchangeName, routingKey);
            }

            channel.basicConsume(queueName, true, consumerTag, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag,
                                           Envelope envelope,
                                           AMQP.BasicProperties properties,
                                           byte[] body) {
                    Message message = new Message();

                    try {
                        ThriftUtils.createThriftFromBytes(body, message);
                        TBase event = null;
                        String gatewayId = null;
                        if (message.getMessageType().equals(MessageType.OUTPUT_PARSED)) {
                            ExperimentOutputParsedEvent experimentOutputParsedEvent = new ExperimentOutputParsedEvent();
                            ThriftUtils.createThriftFromBytes(message.getEvent(), experimentOutputParsedEvent);
                            log.debug(" Message Received with message id '" + message.getMessageId()
                                    + "' and with message type '" + message.getMessageType());
                            event = experimentOutputParsedEvent;
                            //FIXME : transfer the gatewayID
                            gatewayId = "temp";
                        }
                        MessageContext messageContext = new MessageContext(event, message.getMessageType(), message.getMessageId(), gatewayId);
                        messageContext.setUpdatedTime(AiravataUtils.getTime(message.getUpdatedTime()));
                        handler.onMessage(messageContext);
                    } catch (TException e) {
                        String msg = "Failed to de-serialize the thrift message, from routing keys and queueName " + id;
                        log.warn(msg, e);
                    }
                }
            });
            // save the name for deleting the queue
            queueDetailsMap.put(id, new QueueDetails(queueName, keys));
            return id;
        } catch (Exception e) {
            String msg = "could not open channel for exchange " + exchangeName;
            log.error(msg);
            throw new AiravataException(msg, e);
        }
    }

    public void stopListen(final String id) throws AiravataException {
        QueueDetails details = queueDetailsMap.get(id);
        if (details != null) {
            try {
                for (String key : details.getRoutingKeys()) {
                    channel.queueUnbind(details.getQueueName(), exchangeName, key);
                }
                channel.queueDelete(details.getQueueName(), true, true);
            } catch (IOException e) {
                String msg = "could not un-bind queue: " + details.getQueueName() + " for exchange " + exchangeName;
                log.error(msg);
                throw new AiravataException(msg, e);
            }
        }
    }

    /**
     * Private class for holding some information about the consumers registered
     */
    private class QueueDetails {
        String queueName;

        List<String> routingKeys;

        private QueueDetails(String queueName, List<String> routingKeys) {
            this.queueName = queueName;
            this.routingKeys = routingKeys;
        }

        public String getQueueName() {
            return queueName;
        }

        public List<String> getRoutingKeys() {
            return routingKeys;
        }
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
}
