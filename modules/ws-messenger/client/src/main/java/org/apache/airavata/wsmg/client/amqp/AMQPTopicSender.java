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

package org.apache.airavata.wsmg.client.amqp;

import org.apache.axiom.om.OMElement;

/**
 * AMQPTopicSender defines an interface that should be implemented by a message sender that
 * sends messages to one or more consumers that have subscribed to topics. A message
 * would only be delivered to a topic subscriber if and only if the routing key of message
 * satisfies the topic.
 */
public interface AMQPTopicSender {

    /**
     * Send a message.
     *
     * @param message Message to be delivered.
     * @throws AMQPException on error.
     */
    public void Send(OMElement message) throws AMQPException;
}
