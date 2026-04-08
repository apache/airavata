/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.messaging.service;

import java.util.List;
import org.apache.airavata.exception.AiravataException;
import org.springframework.amqp.core.MessageListener;

/**
 * This is the basic consumer
 */
public interface Subscriber {
    /**
     * Start listening for messages. Returns a unique id that can be used to stop listening.
     * @param listener - Spring AMQP MessageListener
     * @param queueName - queue name (null for auto-generated)
     * @param routingKeys - routing keys to bind
     * @return string id
     * @throws AiravataException
     */
    String listen(MessageListener listener, String queueName, List<String> routingKeys) throws AiravataException;

    void stopListen(final String id) throws AiravataException;

    void sendAck(long deliveryTag);
}
