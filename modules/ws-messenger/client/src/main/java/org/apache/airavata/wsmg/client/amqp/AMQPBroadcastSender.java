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
 * AMQPBroadcastSender defines an interface that should be implemented by a message sender that
 * sends messages that can be consumed by any downstream client, irrespective of message routing key.
 */
public interface AMQPBroadcastSender {

    /**
     * Send a message.
     *
     * @param message Message to be delivered.
     * @throws AMQPException
     */
    public void Send(OMElement message) throws AMQPException;
}
