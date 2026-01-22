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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.sql.Timestamp;
import org.apache.airavata.common.model.MessageType;
import org.apache.airavata.common.model.MessagingEvent;

/**
 * Message context for Dapr Pub/Sub messaging.
 * All Dapr messages use Jackson JSON serialization (never Thrift).
 */
public class MessageContext {
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

    // Convenience constructor without deliveryTag
    public MessageContext(
            MessagingEvent event,
            MessageType type,
            String messageId,
            String gatewayId) {
        this(event, type, messageId, gatewayId, null);
    }

    public MessagingEvent getEvent() {
        return event;
    }

    public MessageType getType() {
        return type;
    }

    public Timestamp getUpdatedTime() {
        return updatedTime;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setUpdatedTime(Timestamp updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public long getDeliveryTag() {
        return deliveryTag;
    }

    public void setDeliveryTag(long deliveryTag) {
        this.deliveryTag = deliveryTag;
    }

    public void setIsRedeliver(boolean isRedeliver) {
        this.isRedeliver = isRedeliver;
    }

    public boolean isRedeliver() {
        return isRedeliver;
    }
}
