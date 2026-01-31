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
package org.apache.airavata.orchestrator.messaging;

import java.util.List;
import org.apache.airavata.common.exception.CoreExceptions.AiravataException;
import org.apache.airavata.orchestrator.internal.messaging.MessagingContracts;

/**
 * Public interface for creating messaging publishers and subscribers.
 *
 * <p>This interface abstracts the underlying messaging implementation (currently Dapr)
 * and provides a clean API for external packages.
 *
 * <p>Implementations are provided by the orchestrator's internal messaging layer.
 *
 * <p>Related types from internal.messaging package:
 * <ul>
 *   <li>{@link MessagingContracts.Publisher} - for publishing messages</li>
 *   <li>{@link MessagingContracts.Subscriber} - for subscribing to messages</li>
 *   <li>{@link MessagingContracts.MessageContext} - message context</li>
 *   <li>{@link MessagingContracts.MessageHandler} - handler for processing messages</li>
 *   <li>{@link MessagingContracts.Type} - message type enumeration</li>
 *   <li>{@link Topics} - topic name constants (public re-export)</li>
 * </ul>
 */
public interface MessagingFactory {

    /**
     * Get a subscriber for the specified message type.
     *
     * @param messageHandler the handler for processing messages
     * @param routingKeys optional routing keys to filter messages
     * @param type the type of messages to subscribe to
     * @return a subscriber instance
     * @throws AiravataException if subscriber creation fails
     */
    MessagingContracts.Subscriber getSubscriber(MessagingContracts.MessageHandler messageHandler,
            List<String> routingKeys, MessagingContracts.Type type) throws AiravataException;

    /**
     * Get a publisher for the specified message type.
     *
     * @param type the type of messages to publish
     * @return a publisher instance
     * @throws AiravataException if publisher creation fails
     */
    MessagingContracts.Publisher getPublisher(MessagingContracts.Type type) throws AiravataException;

    /**
     * Check if the messaging system is available.
     *
     * @return true if messaging is available
     */
    boolean isAvailable();
}
