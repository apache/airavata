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

package org.apache.airavata.xbaya.messaging;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.model.messaging.event.Message;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageClient {

    private static final Logger log = LoggerFactory.getLogger(MessageClient.class);

    private Monitor monitor;

    public static final String RABBITMQ_BROKER_URL = "rabbitmq.broker.url";
    public static final String RABBITMQ_EXCHANGE_NAME = "rabbitmq.exchange.name";

    private String brokerURL;

    private String exchangeName;

    private String subscriptionID;

    private long timeout = 20000L;

    private long interval = 1000L;

    private List<TerminateListener> terminateListeners = new ArrayList<TerminateListener>();

    private static final Logger logger = LoggerFactory.getLogger(MessageClient.class);
    private Connection connection;
    private Channel channel;
    private ExecutorService executorService;

    /**
     * Constructs a MessageMonitore.
     *
     * @param monitor
     */
    public MessageClient(Monitor monitor) {
//        try {
            this.monitor = monitor;
            // We need to copy these because the configuration might change.
//            this.brokerURL = ServerSettings.getSetting(RABBITMQ_BROKER_URL);
            this.brokerURL = "amqp://127.0.0.1:5672";
//            this.exchangeName = ServerSettings.getSetting(RABBITMQ_EXCHANGE_NAME);
            this.exchangeName = "airavata_rabbitmq_exchange";
        executorService = Executors.newFixedThreadPool(25);
            init();
//        } catch (ApplicationSettingsException e) {
//            logger.error("Exception while initiating monitoring client ");
//        }
    }

    private void init() {
        try {
            connection = createConnection();
            channel = connection.createChannel();
            channel.exchangeDeclare(exchangeName, "direct", false);
        } catch (IOException e) {
            log.error("Error occur while Client initiating", e);
        }
    }

    private Connection createConnection() {
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setUri(brokerURL);
            Connection connection = connectionFactory.newConnection();
            connection.addShutdownListener(new ShutdownListener() {
                public void shutdownCompleted(ShutdownSignalException cause) {
                    log.info("Connection shutdown listener triggered -----------");
                }
            });
            log.info("connected to rabbitmq: " + connection + " for " + exchangeName);
            return connection;
        } catch (Exception e) {
            log.info("connection failed to rabbitmq: " + connection + " for " + exchangeName);
            return null;
        }
    }

    /**
     * Subscribes to the notification.
     *
     * @throws MonitorException
     */
    public synchronized void subscribe(String experimentId){
        try {
            String queueName = channel.queueDeclare().getQueue();
            System.out.println("Experiment ID is : " + experimentId);
            channel.queueBind(queueName, exchangeName, experimentId ); // send experiment Id as routing Key
            QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.basicConsume(queueName, true, consumer);
            executorService.execute(new Thread(new RabbitMQConsumer(consumer, experimentId)));
        } catch (IOException e) {
            log.error("Error while subscribe to routing key : " + experimentId, e);
        }

    }

    /**
     * Unsubscribes from the notification.
     *
     * @throws MonitorException
     */
    public synchronized void unsubscribe(String experimentId) {
        // This method needs to be synchronized along with subscribe() because
        // unsubscribe() might be called while subscribe() is being executed.
        // TODO - implement this, after experiment execution complete we need to unsubscribe it.
        notifyTerminateListeners(experimentId);
    }

    private void notifyTerminateListeners(String experimentId) {
        for (TerminateListener terminateListener : terminateListeners) {
            terminateListener.terminate(experimentId);
        }
    }

    private void registerTerminateListener(TerminateListener terminateListener) {
        terminateListeners.add(terminateListener);
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    private interface TerminateListener {
        public void terminate(String experimentId);
    }
    private class RabbitMQConsumer implements Runnable, TerminateListener {
        private final Logger logger = LoggerFactory.getLogger(MessageClient.RabbitMQConsumer.class);
        private final String id;
        private QueueingConsumer consumer;
        private boolean isContinue = true;
        RabbitMQConsumer(QueueingConsumer consumer, String experimentId) {
            this.consumer = consumer;
            this.id = experimentId;
            registerTerminateListener(this);
        }

        @Override
        public void run() {
            System.out.println("RabbitMQConsumer started for experiment " + consumer.getConsumerTag());
            try {
                Message message;
                while (isContinue) {
                    QueueingConsumer.Delivery delivery = consumer.nextDelivery(1000);
                    if (delivery == null) {
                        continue;
                    }
                    byte[] body = delivery.getBody();
                    message = new Message();
                    ThriftUtils.createThriftFromBytes(body, message);
                    monitor.handleNotification(message);
                }
                System.out.println("Terminating consumer for experimentId : " + id);
            } catch (InterruptedException e) {
                logger.error("Error while consuming next delivery", e);
                System.out.println("Error while consuming next delivery");
            } catch (TException e) {
                logger.error("Error while creating message from thrift", e);
                System.out.println("Error while creating message from thrift");
            }
        }

        @Override
        public void terminate(String experimentId) {
            if (id.equals(experimentId)) {
                System.out.println("Terminate request came for " + experimentId);
                isContinue = false;
            }
        }
    }
}