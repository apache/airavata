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
//import com.rabbitmq.client.*;
//import org.apache.airavata.common.exception.AiravataException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//
//public class RabbitMQProducer {
//    public static final int DEFAULT_PRE_FETCH = 64;
//
//    private static Logger log = LoggerFactory.getLogger(RabbitMQProducer.class);
//
//    private Connection connection;
//
//    private Channel channel;
//
//    private QueueingConsumer consumer;
//
//    private String consumerTag;
//
//    private String exchangeName;
//
//    private int prefetchCount = DEFAULT_PRE_FETCH;
//
//    private boolean isReQueueOnFail = false;
//
//    private String url;
//
//    private String getExchangeType = "topic";
//
//
//    public RabbitMQProducer(String url, String exchangeName,String getExchangeType) {
//        this.exchangeName = exchangeName;
//        this.url = url;
//        this.getExchangeType = getExchangeType;
//    }
//
//    public RabbitMQProducer(String url, String exchangeName) {
//        this.exchangeName = exchangeName;
//        this.url = url;
//    }
//
//    public void setPrefetchCount(int prefetchCount) {
//        this.prefetchCount = prefetchCount;
//    }
//
//    public void setReQueueOnFail(boolean isReQueueOnFail) {
//        this.isReQueueOnFail = isReQueueOnFail;
//    }
//
//    private void reset() {
//        consumerTag = null;
//    }
//
//    private void reInitIfNecessary() throws Exception {
//        if (consumerTag == null || consumer == null) {
//            close();
//            open();
//        }
//    }
//
//    public void close() {
//        log.info("Closing channel to exchange {}", exchangeName);
//        try {
//            if (channel != null && channel.isOpen()) {
//                if (consumerTag != null) {
//                    channel.basicCancel(consumerTag);
//                }
//                channel.close();
//            }
//        } catch (Exception e) {
//            log.debug("error closing channel and/or cancelling consumer", e);
//        }
//        try {
//            log.info("closing connection to rabbitmq: " + connection);
//            connection.close();
//        } catch (Exception e) {
//            log.debug("error closing connection", e);
//        }
//        consumer = null;
//        consumerTag = null;
//        channel = null;
//        connection = null;
//    }
//
//    public void open() throws AiravataException {
//        try {
//            connection = createConnection();
//            channel = connection.createChannel();
//            if (prefetchCount > 0) {
//                log.info("setting basic.qos / prefetch count to " + prefetchCount + " for " + exchangeName);
//                channel.basicQos(prefetchCount);
//            }
//            if(exchangeName!=null) {
//                channel.exchangeDeclare(exchangeName, getExchangeType, false);
//            }
//            } catch (Exception e) {
//            reset();
//            String msg = "could not open channel for exchange " + exchangeName;
//            log.error(msg);
//            throw new AiravataException(msg, e);
//        }
//    }
//
//    public void send(byte []message, String routingKey) throws Exception {
//        try {
//            channel.basicPublish(exchangeName, routingKey, null, message);
//        } catch (IOException e) {
//            String msg = "Failed to publish message to exchange: " + exchangeName;
//            log.error(msg, e);
//            throw new Exception(msg, e);
//        }
//    }
//
//    public void sendToWorkerQueue(byte []message, String routingKey) throws Exception {
//        try {
//            channel.basicPublish( "", routingKey,
//                    MessageProperties.PERSISTENT_TEXT_PLAIN,
//                    message);
//        } catch (IOException e) {
//            String msg = "Failed to publish message to exchange: " + exchangeName;
//            log.error(msg, e);
//            throw new Exception(msg, e);
//        }
//    }
//
//    private Connection createConnection() throws IOException {
//        try {
//            ConnectionFactory connectionFactory = new ConnectionFactory();
//            connectionFactory.setUri(url);
//            connectionFactory.setAutomaticRecoveryEnabled(true);
//            Connection connection = connectionFactory.newConnection();
//            connection.addShutdownListener(new ShutdownListener() {
//                public void shutdownCompleted(ShutdownSignalException cause) {
//                }
//            });
//            log.info("connected to rabbitmq: " + connection + " for " + exchangeName);
//            return connection;
//        } catch (Exception e) {
//            log.info("connection failed to rabbitmq: " + connection + " for " + exchangeName);
//            return null;
//        }
//    }
//
//    public void ackMessage(Long msgId) throws Exception {
//        try {
//            channel.basicAck(msgId, false);
//        } catch (ShutdownSignalException sse) {
//            reset();
//            String msg = "shutdown signal received while attempting to ack message";
//            log.error(msg, sse);
//            throw new Exception(msg, sse);
//        } catch (Exception e) {
//            String s = "could not ack for msgId: " + msgId;
//            log.error(s, e);
//            throw new Exception(s, e);
//        }
//    }
//
//    public void failMessage(Long msgId) throws Exception {
//        if (isReQueueOnFail) {
//            failWithRedelivery(msgId);
//        } else {
//            deadLetter(msgId);
//        }
//    }
//
//    public void failWithRedelivery(Long msgId) throws Exception {
//        try {
//            channel.basicReject(msgId, true);
//        } catch (ShutdownSignalException sse) {
//            reset();
//            String msg = "shutdown signal received while attempting to fail with redelivery";
//            log.error(msg, sse);
//            throw new Exception(msg, sse);
//        } catch (Exception e) {
//            String msg = "could not fail with redelivery for msgId: " + msgId;
//            log.error(msg, e);
//            throw new Exception(msg, e);
//        }
//    }
//
//    public void deadLetter(Long msgId) throws Exception {
//        try {
//            channel.basicReject(msgId, false);
//        } catch (ShutdownSignalException sse) {
//            reset();
//            String msg = "shutdown signal received while attempting to fail with no redelivery";
//            log.error(msg, sse);
//            throw new Exception(msg, sse);
//        } catch (Exception e) {
//            String msg = "could not fail with dead-lettering (when configured) for msgId: " + msgId;
//            log.error(msg, e);
//            throw new Exception(msg, e);
//        }
//    }
//}
