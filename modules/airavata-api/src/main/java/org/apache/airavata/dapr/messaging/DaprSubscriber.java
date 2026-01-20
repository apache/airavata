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

import java.util.List;
import java.util.function.BiFunction;
import org.apache.airavata.common.exception.AiravataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dapr Pub/Sub implementation of {@link Subscriber}.
 * Registers the handler with {@link DaprSubscriptionRegistry} for the given topic. The Dapr
 * sidecar must be configured (e.g. via Subscription YAML) to POST to
 * /api/v1/dapr/pubsub/{topic} for delivery; {@link DaprSubscriptionController} dispatches.
 */
public class DaprSubscriber implements Subscriber {

    private static final Logger log = LoggerFactory.getLogger(DaprSubscriber.class);

    private final DaprSubscriptionRegistry registry;
    private final MessageHandler messageHandler;
    private final String defaultTopic;

    public DaprSubscriber(DaprSubscriptionRegistry registry, MessageHandler messageHandler, String defaultTopic) {
        this.registry = registry;
        this.messageHandler = messageHandler;
        this.defaultTopic = defaultTopic;
    }

    public DaprSubscriber(DaprSubscriptionRegistry registry, MessageHandler messageHandler) {
        this(registry, messageHandler, null);
    }

    @Override
    public String listen(BiFunction<Object, Object, Object> supplier, String queueName, List<String> routingKeys)
            throws AiravataException {
        String topic = (queueName != null && !queueName.isEmpty()) ? queueName : defaultTopic;
        if (topic == null || topic.isEmpty()) {
            throw new AiravataException("DaprSubscriber: topic or queueName is required");
        }
        registry.register(topic, messageHandler);
        log.info("DaprSubscriber registered for topic={}", topic);
        return topic;
    }

    @Override
    public void stopListen(String id) throws AiravataException {
        if (id != null) {
            registry.remove(id);
            log.info("DaprSubscriber unregistered topic={}", id);
        }
    }

    @Override
    public void sendAck(long deliveryTag) {
        // Dapr acks by HTTP 200; no-op.
    }
}
