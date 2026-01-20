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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.airavata.config.JacksonConfig;
import org.apache.airavata.dapr.monitor.DaprMonitoringHandler;
import org.apache.airavata.monitor.JobStatusResult;
import org.apache.airavata.task.parsing.ProcessCompletionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP endpoint for Dapr Pub/Sub subscription delivery. Dapr POSTs to
 * /api/v1/dapr/pubsub/{topic}. The body is a CloudEvent (with optional "data")
 * or a raw {@link MessageContext}. Dispatches to the handler registered
 * in {@link DaprSubscriptionRegistry} for the topic.
 *
 * <p>Follows Dapr Pub/Sub subscription semantics: 200 = ack (success),
 * 500 = nack (Dapr will retry). See
 * <a href="https://docs.dapr.io/reference/api/pubsub_api/">Pub/Sub API</a>.
 */
@RestController
@RequestMapping("/api/v1/dapr/pubsub")
@ConditionalOnProperty(prefix = "airavata.dapr", name = "enabled", havingValue = "true")
public class DaprSubscriptionController {

    private static final Logger log = LoggerFactory.getLogger(DaprSubscriptionController.class);
    private static final ObjectMapper MAPPER = JacksonConfig.getGlobalMapper();

    private final DaprSubscriptionRegistry registry;
    private final DaprMonitoringHandler monitoringHandler;
    private final DaprParsingHandler parsingHandler;
    private final DaprJobStatusHandler jobStatusHandler;

    public DaprSubscriptionController(
            DaprSubscriptionRegistry registry,
            @Autowired(required = false) DaprMonitoringHandler monitoringHandler,
            @Autowired(required = false) DaprParsingHandler parsingHandler,
            @Autowired(required = false) DaprJobStatusHandler jobStatusHandler) {
        this.registry = registry;
        this.monitoringHandler = monitoringHandler;
        this.parsingHandler = parsingHandler;
        this.jobStatusHandler = jobStatusHandler;
    }

    @PostMapping("/{topic}")
    public ResponseEntity<Void> onMessage(@PathVariable String topic, @RequestBody String body) {
        if (DaprTopics.MONITORING.equals(topic) && monitoringHandler != null) {
            try {
                String[] keyAndPayload = parseMonitoringPayload(body);
                monitoringHandler.onMonitoringMessage(keyAndPayload[0], keyAndPayload[1]);
                return ResponseEntity.ok().build();
            } catch (Exception e) {
                log.error("Error handling Dapr monitoring message for topic={}", topic, e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
        if (DaprTopics.PARSING.equals(topic) && parsingHandler != null) {
            try {
                ProcessCompletionMessage msg = parseDataAs(body, ProcessCompletionMessage.class);
                if (msg != null) {
                    parsingHandler.onParsingMessage(msg);
                }
                return ResponseEntity.ok().build();
            } catch (Exception e) {
                log.error("Error handling Dapr parsing message for topic={}", topic, e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
        if (DaprTopics.MONITORING_JOB_STATUS.equals(topic) && jobStatusHandler != null) {
            try {
                JobStatusResult msg = parseDataAs(body, JobStatusResult.class);
                if (msg != null) {
                    jobStatusHandler.onJobStatusMessage(msg);
                }
                return ResponseEntity.ok().build();
            } catch (Exception e) {
                log.error("Error handling Dapr job status message for topic={}", topic, e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
        MessageHandler handler = registry.get(topic);
        if (handler == null) {
            log.warn("No handler for Dapr topic={}, ignoring", topic);
            return ResponseEntity.ok().build();
        }
        try {
            MessageContext ctx = parseMessageContext(body);
            ctx.setDeliveryTag(0);
            handler.onMessage(ctx);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error handling Dapr message for topic={}", topic, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private static <T> T parseDataAs(String body, Class<T> type) throws Exception {
        JsonNode root = MAPPER.readTree(body);
        JsonNode data = root.has("data") ? root.get("data") : root;
        if (data == null || data.isNull()) {
            return null;
        }
        if (data.isTextual()) {
            return MAPPER.readValue(data.asText(), type);
        }
        return MAPPER.treeToValue(data, type);
    }

    /** Returns [key, payload] from CloudEvent or raw body. */
    private static String[] parseMonitoringPayload(String body) throws Exception {
        JsonNode root = MAPPER.readTree(body);
        String key = "";
        if (root.has("traceid")) {
            key = root.get("traceid").asText();
        } else if (root.has("metadata") && root.get("metadata").has("routingKey")) {
            key = root.get("metadata").get("routingKey").asText();
        }
        JsonNode data = root.has("data") ? root.get("data") : null;
        String payload;
        if (data != null) {
            payload = data.isTextual() ? data.asText() : MAPPER.writeValueAsString(data);
        } else {
            payload = MAPPER.writeValueAsString(root);
        }
        return new String[] {key, payload};
    }

    private static MessageContext parseMessageContext(String body) throws Exception {
        JsonNode root = MAPPER.readTree(body);
        JsonNode data = root.has("data") ? root.get("data") : null;
        if (data != null) {
            if (data.isTextual()) {
                return MAPPER.readValue(data.asText(), MessageContext.class);
            }
            return MAPPER.treeToValue(data, MessageContext.class);
        }
        return MAPPER.treeToValue(root, MessageContext.class);
    }
}
