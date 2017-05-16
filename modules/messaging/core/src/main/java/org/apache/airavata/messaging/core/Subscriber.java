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
package org.apache.airavata.messaging.core;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import org.apache.airavata.common.exception.AiravataException;

import java.util.List;
import java.util.function.BiFunction;

/**
 * This is the basic consumer
 */
public interface Subscriber {
    /**
     * Start listening for messages, The binding properties are specified in the handler.
     * Returns and unique id to this Subscriber. This id can be used to stop the listening
     * @param supplier - return RabbitMQ Consumer
     * @return string id
     * @throws AiravataException
     */
    String listen(BiFunction<Connection, Channel, Consumer>  supplier,
                  String queueName,
                  List<String> routingKeys) throws AiravataException;

    void stopListen(final String id) throws AiravataException;

    void sendAck(long deliveryTag);

}
