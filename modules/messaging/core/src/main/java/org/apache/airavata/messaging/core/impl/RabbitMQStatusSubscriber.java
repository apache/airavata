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
///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
// */
//
//package org.apache.airavata.messaging.core.impl;
//
//
//import com.rabbitmq.client.*;
//import org.apache.airavata.common.exception.AiravataException;
//import org.apache.airavata.common.exception.ApplicationSettingsException;
//import org.apache.airavata.common.utils.AiravataUtils;
//import org.apache.airavata.common.utils.ServerSettings;
//import org.apache.airavata.common.utils.ThriftUtils;
//import org.apache.airavata.messaging.core.Subscriber;
//import org.apache.airavata.messaging.core.MessageContext;
//import org.apache.airavata.messaging.core.MessageHandler;
//import org.apache.airavata.messaging.core.MessagingConstants;
//import org.apache.airavata.model.messaging.event.*;
//import org.apache.thrift.TBase;
//import org.apache.thrift.TException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.annotation.Nonnull;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class RabbitMQStatusSubscriber implements Subscriber {
//	public static final String EXCHANGE_TYPE = "topic";
//	private static Logger log = LoggerFactory.getLogger(RabbitMQStatusSubscriber.class);
//
//    private String exchangeName;
//    private String url;
//    private Connection connection;
//    private Channel channel;
//    private int prefetchCount;
//    private Map<String, QueueDetails> queueDetailsMap = new HashMap<String, QueueDetails>();
//
//    public RabbitMQStatusSubscriber() throws AiravataException {
//        try {
//            url = ServerSettings.getSetting(MessagingConstants.RABBITMQ_BROKER_URL);
//            exchangeName = ServerSettings.getSetting(MessagingConstants.RABBITMQ_STATUS_EXCHANGE_NAME);
//            prefetchCount = Integer.valueOf(ServerSettings.getSetting(MessagingConstants.PREFETCH_COUNT, String.valueOf(64)));
//            createConnection();
//        } catch (ApplicationSettingsException e) {
//            String message = "Failed to get read the required properties from airavata to initialize rabbitmq";
//            log.error(message, e);
//            throw new AiravataException(message, e);
//        }
//    }
//
//    public RabbitMQStatusSubscriber(String brokerUrl, String exchangeName) throws AiravataException {
//        this.exchangeName = exchangeName;
//        this.url = brokerUrl;
//
//        createConnection();
//    }
//
//    private void createConnection() throws AiravataException {
//        try {
//            ConnectionFactory connectionFactory = new ConnectionFactory();
//            connectionFactory.setUri(url);
//            connectionFactory.setAutomaticRecoveryEnabled(true);
//            connection = connectionFactory.newConnection();
//            connection.addShutdownListener(new ShutdownListener() {
//                public void shutdownCompleted(ShutdownSignalException cause) {
//                }
//            });
//            log.info("connected to rabbitmq: " + connection + " for " + exchangeName);
//
//            channel = connection.createChannel();
//            channel.basicQos(prefetchCount);
//            channel.exchangeDeclare(exchangeName, EXCHANGE_TYPE, false);
//
//        } catch (Exception e) {
//            String msg = "could not open channel for exchange " + exchangeName;
//            log.error(msg);
//            throw new AiravataException(msg, e);
//        }
//    }
//
//    public String listen(final MessageHandler handler) throws AiravataException {
//        try {
//            Map<String, Object> props = handler.getProperties();
//            final Object routing = props.get(MessagingConstants.RABBIT_ROUTING_KEY);
//            if (routing == null) {
//                throw new IllegalArgumentException("The routing key must be present");
//            }
//
//            List<String> keys = new ArrayList<String>();
//            if (routing instanceof List) {
//                for (Object o : (List)routing) {
//                    keys.add(o.toString());
//                }
//            } else if (routing instanceof String) {
//                keys.add((String) routing);
//            }
//
//            String queueName = (String) props.get(MessagingConstants.RABBIT_QUEUE);
//            String consumerTag = (String) props.get(MessagingConstants.RABBIT_CONSUMER_TAG);
//            if (queueName == null) {
//                if (!channel.isOpen()) {
//                    channel = connection.createChannel();
//                    channel.exchangeDeclare(exchangeName, "topic", false);
//                }
//                queueName = channel.queueDeclare().getQueue();
//            } else {
//                channel.queueDeclare(queueName, true, false, false, null);
//            }
//
//            final String id = getId(keys, queueName);
//            if (queueDetailsMap.containsKey(id)) {
//                throw new IllegalStateException("This subscriber is already defined for this Subscriber, " +
//                        "cannot define the same subscriber twice");
//            }
//
//            if (consumerTag == null) {
//                consumerTag = "default";
//            }
//
//            // bind all the routing keys
//            for (String routingKey : keys) {
//                channel.queueBind(queueName, exchangeName, routingKey);
//            }
//
//            channel.basicConsume(queueName, true, consumerTag, new DefaultConsumer(channel) {
//                @Override
//                public void handleDelivery(String consumerTag,
//                                           Envelope envelope,
//                                           AMQP.BasicProperties properties,
//                                           byte[] body) {
//                    Message message = new Message();
//
//                    try {
//                        ThriftUtils.createThriftFromBytes(body, message);
//                        TBase event = null;
//                        String gatewayId = null;
//
//                        if (message.getMessageType().equals(MessageType.EXPERIMENT)) {
//                            ExperimentStatusChangeEvent experimentStatusChangeEvent = new ExperimentStatusChangeEvent();
//                            ThriftUtils.createThriftFromBytes(message.getEvent(), experimentStatusChangeEvent);
//                            log.debug(" Message Received with message id '" + message.getMessageId()
//                                    + "' and with message type '" + message.getMessageType() + "'  with status " +
//                                    experimentStatusChangeEvent.getState());
//                            event = experimentStatusChangeEvent;
//                            gatewayId = experimentStatusChangeEvent.getGatewayId();
//                        } else if (message.getMessageType().equals(MessageType.PROCESS)) {
//	                        ProcessStatusChangeEvent processStatusChangeEvent = new ProcessStatusChangeEvent();
//	                        ThriftUtils.createThriftFromBytes(message.getEvent(), processStatusChangeEvent);
//	                        log.debug("Message Recieved with message id :" + message.getMessageId() + " and with " +
//			                        "message type " + message.getMessageType() + " with status " +
//			                        processStatusChangeEvent.getState());
//	                        event = processStatusChangeEvent;
//	                        gatewayId = processStatusChangeEvent.getProcessIdentity().getGatewayId();
//                        } else if (message.getMessageType().equals(MessageType.TASK)) {
//                            TaskStatusChangeEvent taskStatusChangeEvent = new TaskStatusChangeEvent();
//                            ThriftUtils.createThriftFromBytes(message.getEvent(), taskStatusChangeEvent);
//                            log.debug(" Message Received with message id '" + message.getMessageId()
//                                    + "' and with message type '" + message.getMessageType() + "'  with status " +
//                                    taskStatusChangeEvent.getState());
//                            event = taskStatusChangeEvent;
//                            gatewayId = taskStatusChangeEvent.getTaskIdentity().getGatewayId();
//                        }else if (message.getMessageType() == MessageType.PROCESSOUTPUT) {
//                            TaskOutputChangeEvent taskOutputChangeEvent = new TaskOutputChangeEvent();
//                            ThriftUtils.createThriftFromBytes(message.getEvent(), taskOutputChangeEvent);
//                            log.debug(" Message Received with message id '" + message.getMessageId() + "' and with message type '" + message.getMessageType());
//                            event = taskOutputChangeEvent;
//                            gatewayId = taskOutputChangeEvent.getTaskIdentity().getGatewayId();
//                        } else if (message.getMessageType().equals(MessageType.JOB)) {
//                            JobStatusChangeEvent jobStatusChangeEvent = new JobStatusChangeEvent();
//                            ThriftUtils.createThriftFromBytes(message.getEvent(), jobStatusChangeEvent);
//                            log.debug(" Message Received with message id '" + message.getMessageId()
//                                    + "' and with message type '" + message.getMessageType() + "'  with status " +
//                                    jobStatusChangeEvent.getState());
//                            event = jobStatusChangeEvent;
//                            gatewayId = jobStatusChangeEvent.getJobIdentity().getGatewayId();
//                        } else if (message.getMessageType().equals(MessageType.LAUNCHPROCESS)) {
//                            TaskSubmitEvent taskSubmitEvent = new TaskSubmitEvent();
//                            ThriftUtils.createThriftFromBytes(message.getEvent(), taskSubmitEvent);
//                            log.debug(" Message Received with message id '" + message.getMessageId()
//                                    + "' and with message type '" + message.getMessageType() + "'  for experimentId: " +
//                                    taskSubmitEvent.getExperimentId() + "and taskId: " + taskSubmitEvent.getTaskId());
//                            event = taskSubmitEvent;
//                            gatewayId = taskSubmitEvent.getGatewayId();
//                        } else if (message.getMessageType().equals(MessageType.TERMINATEPROCESS)) {
//                            TaskTerminateEvent taskTerminateEvent = new TaskTerminateEvent();
//                            ThriftUtils.createThriftFromBytes(message.getEvent(), taskTerminateEvent);
//                            log.debug(" Message Received with message id '" + message.getMessageId()
//                                    + "' and with message type '" + message.getMessageType() + "'  for experimentId: " +
//                                    taskTerminateEvent.getExperimentId() + "and taskId: " + taskTerminateEvent.getTaskId());
//                            event = taskTerminateEvent;
//                            gatewayId = null;
//                        }
//                        MessageContext messageContext = new MessageContext(event, message.getMessageType(), message.getMessageId(), gatewayId);
//                        messageContext.setUpdatedTime(AiravataUtils.getTime(message.getUpdatedTime()));
//	                    messageContext.setIsRedeliver(envelope.isRedeliver());
//                        handler.onMessage(messageContext);
//                    } catch (TException e) {
//                        String msg = "Failed to de-serialize the thrift message, from routing keys and queueName " + id;
//                        log.warn(msg, e);
//                    }
//                }
//            });
//            // save the name for deleting the queue
//            queueDetailsMap.put(id, new QueueDetails(queueName, keys));
//            return id;
//        } catch (Exception e) {
//            String msg = "could not open channel for exchange " + exchangeName;
//            log.error(msg);
//            throw new AiravataException(msg, e);
//        }
//    }
//
//    public void stopListen(final String id) throws AiravataException {
//        QueueDetails details = queueDetailsMap.get(id);
//        if (details != null) {
//            try {
//                for (String key : details.getRoutingKeys()) {
//                    channel.queueUnbind(details.getQueueName(), exchangeName, key);
//                }
//                channel.queueDelete(details.getQueueName(), true, true);
//            } catch (IOException e) {
//                String msg = "could not un-bind queue: " + details.getQueueName() + " for exchange " + exchangeName;
//                log.debug(msg);
//            }
//        }
//    }
//
//    /**
//     * Private class for holding some information about the consumers registered
//     */
//    private class QueueDetails {
//        String queueName;
//
//        List<String> routingKeys;
//
//        private QueueDetails(String queueName, List<String> routingKeys) {
//            this.queueName = queueName;
//            this.routingKeys = routingKeys;
//        }
//
//        public String getQueueName() {
//            return queueName;
//        }
//
//        public List<String> getRoutingKeys() {
//            return routingKeys;
//        }
//    }
//
//    private String getId(List<String> routingKeys, String queueName) {
//        String id = "";
//        for (String key : routingKeys) {
//            id = id + "_" + key;
//        }
//        return id + "_" + queueName;
//    }
//
//    public void close() {
//        if (connection != null) {
//            try {
//                connection.close();
//            } catch (IOException ignore) {
//            }
//        }
//    }
//}
