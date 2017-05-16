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
package org.apache.airavata.monitoring.consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

public class StatusReceiver extends Thread {
    private static volatile Connection connection;
    private static volatile Channel channel;
    private static final String EXCHANGE_TYPE = "fanout";

    private String exchangeName;
    private String queueName;
    private String brokerURI;
    private Thread recieverThread;

    public StatusReceiver(String exchangeName, String queueName, String brokerURI) {
        this.exchangeName = exchangeName;
        this.queueName = queueName;
        this.brokerURI = brokerURI;
    }

    public void startThread() throws KeyManagementException, NoSuchAlgorithmException, URISyntaxException, IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setAutomaticRecoveryEnabled(true);
        factory.setUri(brokerURI);
        connection = factory.newConnection();
        recieverThread = new Thread(this);
        recieverThread.start();
    }

    public void shutdown() throws IOException, TimeoutException {
        channel.close();
        connection.close();
        System.out.println("Email receiver thread succesfully shutdown");
    }

    @Override
    public void run() {
        try {
            channel = connection.createChannel();
            channel.exchangeDeclare(exchangeName, EXCHANGE_TYPE);
            channel.queueDeclare(queueName, true, false, false, null).getQueue();
            channel.queueBind(queueName, exchangeName, "");
            System.out.println(" [*] Waiting for messages.");
            Consumer consumer = new StatusConsumer(channel);
            channel.basicConsume(queueName, true, consumer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}