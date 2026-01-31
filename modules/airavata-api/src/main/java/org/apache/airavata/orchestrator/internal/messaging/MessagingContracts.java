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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.function.BiFunction;
import org.apache.airavata.common.exception.CoreExceptions.AiravataException;
import org.apache.airavata.common.model.MessageType;
import org.apache.airavata.common.model.MessagingEvent;

/**
 * Contracts and shared types for orchestrator messaging (interfaces, MessageContext, Type, DaprTopics).
 */
public final class MessagingContracts {

    private MessagingContracts() {}

    // ----- Interfaces -----

    /** Basic publisher interface. */
    public interface Publisher {
        void publish(MessageContext messageContext) throws AiravataException;
        void publish(MessageContext messageContext, String routingKey) throws AiravataException;
    }

    /** Subscriber interface for message consumption. */
    public interface Subscriber {
        String listen(BiFunction<Object, Object, Object> supplier, String queueName, List<String> routingKeys)
                throws AiravataException;
        void stopListen(String id) throws AiravataException;
        void sendAck(long deliveryTag);
    }

    @FunctionalInterface
    public interface MessageHandler {
        void onMessage(MessageContext messageContext);
    }

    // ----- Canonical job status event (any source: email, realtime, notify API) -----

    /**
     * Canonical job status update event. Same shape regardless of source (email, realtime, job-callback).
     * Payload shape: { "jobName", "status", "task", "publisherName"?, "routingKey"? }.
     */
    public static final class JobStatusUpdateEvent {
        @JsonProperty("jobName")
        private final String jobName;
        @JsonProperty("status")
        private final String status;
        @JsonProperty("task")
        private final String taskId;
        @JsonProperty("publisherName")
        private final String publisherName;
        @JsonProperty("routingKey")
        private final String routingKey;

        @JsonCreator
        public JobStatusUpdateEvent(
                @JsonProperty("jobName") String jobName,
                @JsonProperty("status") String status,
                @JsonProperty("task") String taskId,
                @JsonProperty(value = "publisherName", required = false) String publisherName,
                @JsonProperty(value = "routingKey", required = false) String routingKey) {
            this.jobName = jobName;
            this.status = status;
            this.taskId = taskId;
            this.publisherName = publisherName != null ? publisherName : "realtime";
            this.routingKey = routingKey != null ? routingKey : "";
        }

        public JobStatusUpdateEvent(String jobName, String status, String taskId, String publisherName) {
            this(jobName, status, taskId, publisherName, null);
        }

        public JobStatusUpdateEvent(String jobName, String status, String taskId) {
            this(jobName, status, taskId, null, null);
        }

        public String getJobName() { return jobName; }
        public String getStatus() { return status; }
        public String getTaskId() { return taskId; }
        public String getPublisherName() { return publisherName; }
        public String getRoutingKey() { return routingKey; }
    }

    // ----- Message type -----

    public enum Type {
        EXPERIMENT_LAUNCH,
        PROCESS_LAUNCH,
        STATUS,
        PARSING
    }

    // ----- Message context -----

    /**
     * Message context for Dapr Pub/Sub messaging.
     * All Dapr messages use Jackson JSON serialization (never Thrift).
     */
    public static class MessageContext {
        @JsonProperty("event")
        private final MessagingEvent event;

        @JsonProperty("messageType")
        private final MessageType type;

        @JsonProperty("messageId")
        private final String messageId;

        @JsonProperty("gatewayId")
        private final String gatewayId;

        @JsonProperty("updatedTime")
        @JsonSerialize(using = TimestampSerializer.class)
        @JsonDeserialize(using = TimestampDeserializer.class)
        private Timestamp updatedTime;

        @JsonProperty("deliveryTag")
        private long deliveryTag;

        @JsonProperty("redeliver")
        private boolean isRedeliver;

        @JsonCreator
        public MessageContext(
                @JsonProperty("event") MessagingEvent event,
                @JsonProperty("messageType") MessageType type,
                @JsonProperty("messageId") String messageId,
                @JsonProperty("gatewayId") String gatewayId,
                @JsonProperty(value = "deliveryTag", required = false) Long deliveryTag) {
            this.event = event;
            this.type = type;
            this.messageId = messageId;
            this.gatewayId = gatewayId;
            this.deliveryTag = (deliveryTag != null) ? deliveryTag : 0L;
        }

        public MessageContext(MessagingEvent event, MessageType type, String messageId, String gatewayId) {
            this(event, type, messageId, gatewayId, null);
        }

        public MessagingEvent getEvent() { return event; }
        public MessageType getType() { return type; }
        public Timestamp getUpdatedTime() { return updatedTime; }
        public String getMessageId() { return messageId; }
        public void setUpdatedTime(Timestamp updatedTime) { this.updatedTime = updatedTime; }
        public String getGatewayId() { return gatewayId; }
        public long getDeliveryTag() { return deliveryTag; }
        public void setDeliveryTag(long deliveryTag) { this.deliveryTag = deliveryTag; }
        public void setIsRedeliver(boolean isRedeliver) { this.isRedeliver = isRedeliver; }
        public boolean isRedeliver() { return isRedeliver; }
    }

    // ----- Dapr topic constants -----

    /**
     * Centralized constants for Dapr Pub/Sub topic names.
     * STATUS and job status updates share one topic; controller discriminates
     * JobStatusUpdateEvent (any source) vs MessageContext (orchestrator).
     */
    public static final class DaprTopics {
        /** Unified topic for job status (JobStatusUpdateEvent) and orchestrator (MessageContext). */
        public static final String STATUS_CHANGE = "status-change-topic";
        public static final String STATUS = STATUS_CHANGE;
        public static final String MONITORING = STATUS_CHANGE;
        public static final String EXPERIMENT = "experiment-topic";
        public static final String PROCESS = "process-topic";
        public static final String PARSING = "parsing-data-topic";
        public static final String MONITORING_JOB_STATUS = "monitoring-job-status-topic";

        private DaprTopics() {}
    }

    /** Serializes java.sql.Timestamp to Long (milliseconds since epoch) for JSON. */
    public static final class TimestampSerializer extends JsonSerializer<Timestamp> {
        @Override
        public void serialize(Timestamp value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
            } else {
                gen.writeNumber(value.getTime());
            }
        }
    }

    /** Deserializes Long (milliseconds since epoch) to java.sql.Timestamp from JSON. */
    public static final class TimestampDeserializer extends JsonDeserializer<Timestamp> {
        @Override
        public Timestamp deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.getCurrentToken() == JsonToken.VALUE_NULL) {
                return null;
            }
            long milliseconds = p.getLongValue();
            return new Timestamp(milliseconds);
        }
    }
}
