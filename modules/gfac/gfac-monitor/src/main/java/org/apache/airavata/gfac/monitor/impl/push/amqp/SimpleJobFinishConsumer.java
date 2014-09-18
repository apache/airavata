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
package org.apache.airavata.gfac.monitor.impl.push.amqp;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SimpleJobFinishConsumer {
    private final static Logger logger = LoggerFactory.getLogger(SimpleJobFinishConsumer.class);

    private List<String> completedJobsFromPush;

    public SimpleJobFinishConsumer(List<String> completedJobsFromPush) {
        this.completedJobsFromPush = completedJobsFromPush;
    }

    public void listen() {
        try {
            String uri = "amqp://localhost";
            String queueName = "SimpleQueue";

            ConnectionFactory connFactory = new ConnectionFactory();
            connFactory.setUri(uri);
            Connection conn = connFactory.newConnection();
            logger.info("--------Created the connection to Rabbitmq server successfully-------");

            final Channel ch = conn.createChannel();

            logger.info("--------Created the channel with Rabbitmq server successfully-------");

            ch.queueDeclare(queueName, false, false, false, null);

            logger.info("--------Declare the queue " + queueName + " in Rabbitmq server successfully-------");

            final QueueingConsumer consumer = new QueueingConsumer(ch);
            ch.basicConsume(queueName, consumer);
            (new Thread() {
                public void run() {
                    try {
                        while (true) {
                            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                            logger.info("---------------- Job Finish message received:"+new String(delivery.getBody())+" --------------");
                            completedJobsFromPush.add(new String(delivery.getBody()));
                            ch.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                        }
                    } catch (Exception ex) {
                        logger.error("--------Cannot connect to a RabbitMQ Server--------" , ex);
                    }
                }

            }).start();
        } catch (Exception ex) {
            logger.error("Cannot connect to a RabbitMQ Server: " , ex);
            logger.info("------------- Push monitoring for HPC jobs is disabled -------------");
        }
    }
}
