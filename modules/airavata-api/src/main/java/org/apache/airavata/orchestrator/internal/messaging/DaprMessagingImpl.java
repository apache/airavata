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
package org.apache.airavata.orchestrator.internal.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dapr.client.DaprClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.apache.airavata.common.exception.CoreExceptions.AiravataException;
import org.apache.airavata.common.model.ExperimentStatusChangeEvent;
import org.apache.airavata.common.model.JobStatusChangeEvent;
import org.apache.airavata.common.model.MessageType;
import org.apache.airavata.common.model.ProcessStatusChangeEvent;
import org.apache.airavata.common.model.TaskOutputChangeEvent;
import org.apache.airavata.common.model.TaskStatusChangeEvent;
import org.apache.airavata.config.JacksonConfig;
import org.apache.airavata.monitor.JobStatusResult;
import org.apache.airavata.orchestrator.internal.config.DaprConfigConstants;
import org.apache.airavata.orchestrator.internal.monitoring.JobStatusEventToResultConverter;
import org.apache.airavata.orchestrator.messaging.MessagingFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dapr-based messaging implementation: factory, publisher, subscriber, subscription registry and controller.
 */
public final class DaprMessagingImpl {

    private DaprMessagingImpl() {}

    @Component
    public static class DaprMessagingFactory implements MessagingFactory {

        private final DaprClient daprClient;
        private final DaprSubscriptionRegistry registry;
        private final ObjectMapper objectMapper;
        private final String pubsubName;
        private final boolean daprEnabled;

        @Autowired
        public DaprMessagingFactory(
                @Autowired(required = false) DaprClient daprClient,
                DaprSubscriptionRegistry registry,
                ObjectMapper objectMapper,
                @Value("${" + DaprConfigConstants.DAPR_ENABLED + ":false}") boolean daprEnabled,
                @Value("${" + DaprConfigConstants.DAPR_PUBSUB_NAME + ":" + DaprConfigConstants.DEFAULT_PUBSUB_NAME + "}")
                        String pubsubName) {
            this.daprClient = daprClient;
            this.registry = registry;
            this.objectMapper = objectMapper;
            this.daprEnabled = daprEnabled;
            this.pubsubName = pubsubName;
        }

        @Override
        public MessagingContracts.Subscriber getSubscriber(MessagingContracts.MessageHandler messageHandler,
                List<String> routingKeys, MessagingContracts.Type type) throws AiravataException {
            var topic = topicForType(type);
            var sub = new DaprSubscriber(registry, messageHandler, topic);
            sub.listen((a, b) -> null, topic, routingKeys != null ? routingKeys : List.of());
            return sub;
        }

        @Override
        public MessagingContracts.Publisher getPublisher(MessagingContracts.Type type) throws AiravataException {
            var topic = topicForType(type);
            return switch (type) {
                case EXPERIMENT_LAUNCH -> new DaprPublisher(daprClient, pubsubName, topic, objectMapper, ctx -> topic);
                case PROCESS_LAUNCH -> new DaprPublisher(daprClient, pubsubName, topic, objectMapper, ctx -> topic);
                case STATUS -> new DaprPublisher(daprClient, pubsubName, topic, objectMapper, this::statusRoutingKey);
                case PARSING -> new DaprPublisher(daprClient, pubsubName, topic, objectMapper, ctx -> topic);
                default -> throw new IllegalArgumentException("Publisher " + type + " is not handled");
            };
        }

        @Override
        public boolean isAvailable() {
            return daprEnabled && daprClient != null;
        }

        private String topicForType(MessagingContracts.Type type) {
            return switch (type) {
                case EXPERIMENT_LAUNCH -> MessagingContracts.DaprTopics.EXPERIMENT;
                case PROCESS_LAUNCH -> MessagingContracts.DaprTopics.PROCESS;
                case STATUS -> MessagingContracts.DaprTopics.STATUS;
                case PARSING -> MessagingContracts.DaprTopics.PARSING;
                default -> "airavata-topic";
            };
        }

        private String statusRoutingKey(MessagingContracts.MessageContext msgCtx) {
            var gatewayId = msgCtx.getGatewayId();
            if (msgCtx.getType() == MessageType.EXPERIMENT) {
                var e = (ExperimentStatusChangeEvent) msgCtx.getEvent();
                return gatewayId + "." + e.getExperimentId();
            }
            if (msgCtx.getType() == MessageType.TASK) {
                var e = (TaskStatusChangeEvent) msgCtx.getEvent();
                return gatewayId + "." + e.getTaskIdentity().getExperimentId() + "."
                        + e.getTaskIdentity().getProcessId() + "."
                        + e.getTaskIdentity().getTaskId();
            }
            if (msgCtx.getType() == MessageType.PROCESSOUTPUT) {
                var e = (TaskOutputChangeEvent) msgCtx.getEvent();
                return gatewayId + "." + e.getTaskIdentity().getExperimentId() + "."
                        + e.getTaskIdentity().getProcessId() + "."
                        + e.getTaskIdentity().getTaskId();
            }
            if (msgCtx.getType() == MessageType.PROCESS) {
                var e = (ProcessStatusChangeEvent) msgCtx.getEvent();
                var p = e.getProcessIdentity();
                return gatewayId + "." + p.getExperimentId() + "." + p.getProcessId();
            }
            if (msgCtx.getType() == MessageType.JOB) {
                var e = (JobStatusChangeEvent) msgCtx.getEvent();
                var j = e.getJobIdentity();
                return gatewayId + "." + j.getExperimentId() + "." + j.getProcessId() + "." + j.getTaskId() + "."
                        + j.getJobId();
            }
            return gatewayId;
        }
    }

    public static class DaprPublisher implements MessagingContracts.Publisher {

        private static final Logger log = LoggerFactory.getLogger(DaprPublisher.class);
        private static final String META_ROUTING_KEY = "routingKey";
        private static final String META_CONTENT_TYPE = "content-type";

        private final DaprClient daprClient;
        private final String pubsubName;
        private final String topicName;
        private final ObjectMapper objectMapper;
        private final Function<MessagingContracts.MessageContext, String> routingKeySupplier;

        public DaprPublisher(DaprClient daprClient, String pubsubName, String topicName, ObjectMapper objectMapper,
                Function<MessagingContracts.MessageContext, String> routingKeySupplier) {
            this.daprClient = daprClient;
            this.pubsubName = pubsubName;
            this.topicName = topicName;
            this.objectMapper = objectMapper;
            this.routingKeySupplier = routingKeySupplier;
        }

        public DaprPublisher(DaprClient daprClient, String pubsubName, String topicName, ObjectMapper objectMapper) {
            this(daprClient, pubsubName, topicName, objectMapper, null);
        }

        @Override
        public void publish(MessagingContracts.MessageContext messageContext) throws AiravataException {
            var routingKey = routingKeySupplier != null ? routingKeySupplier.apply(messageContext) : "";
            publish(messageContext, routingKey);
        }

        @Override
        public void publish(MessagingContracts.MessageContext messageContext, String routingKey)
                throws AiravataException {
            try {
                var metadata = new HashMap<String, String>();
                metadata.put(META_CONTENT_TYPE, "application/json");
                if (routingKey != null && !routingKey.isEmpty()) {
                    metadata.put(META_ROUTING_KEY, routingKey);
                }
                daprClient.publishEvent(pubsubName, topicName, messageContext, metadata).block();
                log.debug("Published to Dapr pubsub={}, topic={}, routingKey={}", pubsubName, topicName, routingKey);
            } catch (Exception e) {
                var msg = "Error publishing message to Dapr topic: " + topicName;
                log.error(msg, e);
                throw new AiravataException(msg, e);
            }
        }

        public void close() {
            log.debug("DaprPublisher close() called - DaprClient is managed externally");
        }
    }

    public static class DaprSubscriber implements MessagingContracts.Subscriber {

        private static final Logger log = LoggerFactory.getLogger(DaprSubscriber.class);

        private final DaprSubscriptionRegistry registry;
        private final MessagingContracts.MessageHandler messageHandler;
        private final String defaultTopic;

        public DaprSubscriber(DaprSubscriptionRegistry registry,
                MessagingContracts.MessageHandler messageHandler, String defaultTopic) {
            this.registry = registry;
            this.messageHandler = messageHandler;
            this.defaultTopic = defaultTopic;
        }

        public DaprSubscriber(DaprSubscriptionRegistry registry,
                MessagingContracts.MessageHandler messageHandler) {
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

    @Component
    public static class DaprSubscriptionRegistry {

        private final Map<String, MessagingContracts.MessageHandler> topicToHandler = new ConcurrentHashMap<>();

        public void register(String topic, MessagingContracts.MessageHandler handler) {
            topicToHandler.put(topic, handler);
        }

        public MessagingContracts.MessageHandler get(String topic) {
            return topicToHandler.get(topic);
        }

        public MessagingContracts.MessageHandler remove(String topic) {
            return topicToHandler.remove(topic);
        }
    }

    @RestController
    @RequestMapping("/api/v1/dapr/pubsub")
    @ConditionalOnProperty(prefix = "airavata.dapr", name = "enabled", havingValue = "true")
    public static class DaprSubscriptionController {

        private static final Logger log = LoggerFactory.getLogger(DaprSubscriptionController.class);
        private static final ObjectMapper MAPPER = JacksonConfig.getGlobalMapper();

        private final DaprSubscriptionRegistry registry;
        private final org.apache.airavata.orchestrator.JobStatusHandler jobStatusHandler;
        private final JobStatusEventToResultConverter jobStatusConverter;
        private final org.apache.airavata.service.registry.RegistryService registryService;

        public DaprSubscriptionController(DaprSubscriptionRegistry registry,
                @Autowired(required = false) org.apache.airavata.orchestrator.JobStatusHandler jobStatusHandler,
                JobStatusEventToResultConverter jobStatusConverter,
                @Autowired(required = false) org.apache.airavata.service.registry.RegistryService registryService) {
            this.registry = registry;
            this.jobStatusHandler = jobStatusHandler;
            this.jobStatusConverter = jobStatusConverter;
            this.registryService = registryService;
        }

        @PostMapping("/{topic}")
        public ResponseEntity<Void> onMessage(@PathVariable String topic, @RequestBody String body) {
            if (MessagingContracts.DaprTopics.STATUS_CHANGE.equals(topic)) {
                try {
                    // Discriminate by payload: job status event (any source) vs MessageContext (orchestrator)
                    MessagingContracts.JobStatusUpdateEvent jobEvent = tryParseJobStatusEvent(body);
                    if (jobEvent != null) {
                        // Single path for all job status updates (email, realtime, notify API)
                        if (jobStatusHandler != null && jobStatusConverter != null && registryService != null) {
                            JobStatusResult result = jobStatusConverter.convert(jobEvent, registryService);
                            if (result != null) {
                                jobStatusHandler.onJobStatusMessage(result);
                            }
                        } else {
                            log.warn("JobStatusHandler or converter or RegistryService not available; job status ignored");
                        }
                        return ResponseEntity.ok().build();
                    }
                    MessagingContracts.MessageContext ctx = tryParseMessageContext(body);
                    if (ctx != null) {
                        ctx.setDeliveryTag(0);
                        var handler = registry.get(topic);
                        if (handler != null) {
                            handler.onMessage(ctx);
                        } else {
                            log.warn("No handler for Dapr topic={}, ignoring", topic);
                        }
                        return ResponseEntity.ok().build();
                    }
                    log.warn("status-change-topic payload is neither JobStatusUpdateEvent nor MessageContext; ignoring");
                    return ResponseEntity.ok().build();
                } catch (Exception e) {
                    log.error("Error handling Dapr status-change message for topic={}", topic, e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            }
            var handler = registry.get(topic);
            if (handler == null) {
                log.warn("No handler for Dapr topic={}, ignoring", topic);
                return ResponseEntity.ok().build();
            }
            try {
                var ctx = parseMessageContext(body);
                ctx.setDeliveryTag(0);
                handler.onMessage(ctx);
                return ResponseEntity.ok().build();
            } catch (Exception e) {
                log.error("Error handling Dapr message for topic={}", topic, e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        /** Parse body as JobStatusUpdateEvent if it has jobName, status, task. */
        private static MessagingContracts.JobStatusUpdateEvent tryParseJobStatusEvent(String body) {
            try {
                var root = MAPPER.readTree(body);
                var data = root.has("data") ? root.get("data") : null;
                var node = data != null ? data : root;
                String payloadStr = node.isTextual() ? node.asText() : MAPPER.writeValueAsString(node);
                var payloadNode = node.isTextual() ? MAPPER.readTree(payloadStr) : node;
                if (!payloadNode.has("jobName") || !payloadNode.has("status") || !payloadNode.has("task")) {
                    return null;
                }
                MessagingContracts.JobStatusUpdateEvent evt = node.isTextual()
                        ? MAPPER.readValue(payloadStr, MessagingContracts.JobStatusUpdateEvent.class)
                        : MAPPER.treeToValue(payloadNode, MessagingContracts.JobStatusUpdateEvent.class);
                String routingKey = "";
                if (root.has("traceid")) routingKey = root.get("traceid").asText();
                else if (root.has("metadata") && root.get("metadata").has("routingKey")) routingKey = root.get("metadata").get("routingKey").asText();
                if (!routingKey.isEmpty() && (evt.getRoutingKey() == null || evt.getRoutingKey().isEmpty())) {
                    evt = new MessagingContracts.JobStatusUpdateEvent(evt.getJobName(), evt.getStatus(), evt.getTaskId(), evt.getPublisherName(), routingKey);
                }
                return evt;
            } catch (Exception e) {
                return null;
            }
        }

        /** Parse body as MessageContext if it has messageType and event. */
        private static MessagingContracts.MessageContext tryParseMessageContext(String body) {
            try {
                var root = MAPPER.readTree(body);
                var data = root.has("data") ? root.get("data") : null;
                var node = data != null ? data : root;
                String payloadStr = node.isTextual() ? node.asText() : MAPPER.writeValueAsString(node);
                var payloadNode = node.isTextual() ? MAPPER.readTree(payloadStr) : node;
                if (!payloadNode.has("messageType") || !payloadNode.has("event")) {
                    return null;
                }
                return node.isTextual()
                        ? MAPPER.readValue(payloadStr, MessagingContracts.MessageContext.class)
                        : MAPPER.treeToValue(payloadNode, MessagingContracts.MessageContext.class);
            } catch (Exception e) {
                return null;
            }
        }

        private static MessagingContracts.MessageContext parseMessageContext(String body) throws Exception {
            var root = MAPPER.readTree(body);
            var data = root.has("data") ? root.get("data") : null;
            if (data != null) {
                if (data.isTextual()) {
                    return MAPPER.readValue(data.asText(), MessagingContracts.MessageContext.class);
                }
                return MAPPER.treeToValue(data, MessagingContracts.MessageContext.class);
            }
            return MAPPER.treeToValue(root, MessagingContracts.MessageContext.class);
        }
    }
}
