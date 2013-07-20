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
import org.apache.airavata.wsmg.client.amqp.AMQPException;
import org.apache.airavata.wsmg.client.amqp.AMQPRoutingAwareClient;
import org.apache.airavata.wsmg.client.amqp.AMQPSender;
import org.apache.airavata.wsmg.client.amqp.AMQPUtil;
import org.apache.axiom.om.OMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * AMQPSenderImpl class provides functionality to send messages with a unique routing key
 * so that a receiver can consume them selectively.
 */
public class AMQPSenderImpl extends AMQPRoutingAwareClient implements AMQPSender {
    private static final Logger log = LoggerFactory.getLogger(AMQPSenderImpl.class);

    public AMQPSenderImpl(Properties properties) {
        super(properties);
    }

    public void Send(OMElement message) throws AMQPException {
        try {
            if (isRoutable(message)) {
                Connection connection = connectionFactory.newConnection();
                Channel channel = connection.createChannel();
                channel.exchangeDeclare(AMQPUtil.EXCHANGE_NAME_DIRECT, AMQPUtil.EXCHANGE_TYPE_DIRECT);

                List<String> routingKeys = new ArrayList<String>();
                getRoutingKeys(message, routingKeys);

                for (String routingKey : routingKeys) {
                    channel.basicPublish(
                            AMQPUtil.EXCHANGE_NAME_DIRECT, routingKey, null, message.toString().getBytes());
                }
                
                channel.close();
                connection.close();
            }
        } catch (IOException e) {
            throw new AMQPException(e);
        }
    }
}
