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
import java.util.List;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.model.ExperimentStatusChangeEvent;
import org.apache.airavata.common.model.JobStatusChangeEvent;
import org.apache.airavata.common.model.MessageType;
import org.apache.airavata.common.model.ProcessStatusChangeEvent;
import org.apache.airavata.common.model.TaskOutputChangeEvent;
import org.apache.airavata.common.model.TaskStatusChangeEvent;
import org.apache.airavata.orchestrator.internal.config.DaprConfigConstants;
import org.apache.airavata.orchestrator.messaging.MessagingFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Dapr-based implementation of the messaging factory. Provides {@link Publisher} and
 * {@link Subscriber} via Dapr Pub/Sub (Redis). Replaces the RabbitMQ MessagingFactory.
 *
 * <p>Topic mapping: status_exchange → status-topic,
 * experiment_exchange → experiment-topic, process_exchange → process-topic.
 */
@Component
public class DaprMessagingFactory implements MessagingFactory {

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
    public Subscriber getSubscriber(MessageHandler messageHandler, List<String> routingKeys, Type type)
            throws AiravataException {
        var topic = topicForType(type);
        var sub = new DaprSubscriber(registry, messageHandler, topic);
        sub.listen((a, b) -> null, topic, routingKeys != null ? routingKeys : List.of());
        return sub;
    }

    @Override
    public Publisher getPublisher(Type type) throws AiravataException {
        var topic = topicForType(type);
        switch (type) {
            case EXPERIMENT_LAUNCH:
                return new DaprPublisher(daprClient, pubsubName, topic, objectMapper, ctx -> topic);
            case PROCESS_LAUNCH:
                return new DaprPublisher(daprClient, pubsubName, topic, objectMapper, ctx -> topic);
            case STATUS:
                return new DaprPublisher(daprClient, pubsubName, topic, objectMapper, this::statusRoutingKey);
            case PARSING:
                return new DaprPublisher(daprClient, pubsubName, topic, objectMapper, ctx -> topic);
            default:
                throw new IllegalArgumentException("Publisher " + type + " is not handled");
        }
    }

    @Override
    public boolean isAvailable() {
        return daprEnabled && daprClient != null;
    }

    private String topicForType(Type type) {
        return switch (type) {
            case EXPERIMENT_LAUNCH -> DaprTopics.EXPERIMENT;
            case PROCESS_LAUNCH -> DaprTopics.PROCESS;
            case STATUS -> DaprTopics.STATUS;
            case PARSING -> DaprTopics.PARSING;
            default -> "airavata-topic";
        };
    }

    private String statusRoutingKey(MessageContext msgCtx) {
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
