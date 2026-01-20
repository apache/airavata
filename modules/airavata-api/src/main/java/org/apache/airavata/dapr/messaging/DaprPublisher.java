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
package org.apache.airavata.dapr.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.apache.airavata.common.exception.AiravataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dapr Pub/Sub implementation of {@link Publisher}.
 * Messages are JSON-serialized directly from {@link MessageContext}.
 *
 * <p>Topic naming maps from legacy exchanges: dbevent_exchange → dbevent-topic,
 * status_exchange → status-topic, experiment_exchange → experiment-topic,
 * process_exchange → process-topic.
 */
public class DaprPublisher implements Publisher {

    private static final Logger log = LoggerFactory.getLogger(DaprPublisher.class);

    private static final String META_ROUTING_KEY = "routingKey";
    private static final String META_CONTENT_TYPE = "content-type";

    private final io.dapr.client.DaprClient daprClient;
    private final String pubsubName;
    private final String topicName;
    private final ObjectMapper objectMapper;
    private final Function<MessageContext, String> routingKeySupplier;

    public DaprPublisher(
            io.dapr.client.DaprClient daprClient,
            String pubsubName,
            String topicName,
            ObjectMapper objectMapper,
            Function<MessageContext, String> routingKeySupplier) {
        this.daprClient = daprClient;
        this.pubsubName = pubsubName;
        this.topicName = topicName;
        this.objectMapper = objectMapper;
        this.routingKeySupplier = routingKeySupplier;
    }

    public DaprPublisher(
            io.dapr.client.DaprClient daprClient, String pubsubName, String topicName, ObjectMapper objectMapper) {
        this(daprClient, pubsubName, topicName, objectMapper, null);
    }

    @Override
    public void publish(MessageContext messageContext) throws AiravataException {
        String routingKey = routingKeySupplier != null ? routingKeySupplier.apply(messageContext) : "";
        publish(messageContext, routingKey);
    }

    @Override
    public void publish(MessageContext messageContext, String routingKey) throws AiravataException {
        try {
            Map<String, String> metadata = new HashMap<>();
            metadata.put(META_CONTENT_TYPE, "application/json");
            if (routingKey != null && !routingKey.isEmpty()) {
                metadata.put(META_ROUTING_KEY, routingKey);
            }
            daprClient.publishEvent(pubsubName, topicName, messageContext, metadata).block();
            log.debug("Published to Dapr pubsub={}, topic={}, routingKey={}", pubsubName, topicName, routingKey);
        } catch (Exception e) {
            String msg = "Error publishing message to Dapr topic: " + topicName;
            log.error(msg, e);
            throw new AiravataException(msg, e);
        }
    }

    /**
     * No-op; Dapr client lifecycle is managed by the application.
     */
    public void close() {
        log.debug("DaprPublisher close() called - DaprClient is managed externally");
    }
}
