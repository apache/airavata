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
package org.apache.airavata.messaging.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import org.apache.airavata.common.model.MessageType;
import org.apache.airavata.common.model.MessagingEvent;

/**
 * Wrapper class for serializing MessageContext to/from JSON.
 * Used for RabbitMQ message transport.
 */
public class MessageWrapper {
    @JsonProperty("event")
    private MessagingEvent event;

    @JsonProperty("messageType")
    private MessageType messageType;

    @JsonProperty("messageId")
    private String messageId;

    @JsonProperty("gatewayId")
    private String gatewayId;

    @JsonProperty("updatedTime")
    private Long updatedTime;

    @JsonProperty("deliveryTag")
    private Long deliveryTag;

    @JsonProperty("isRedeliver")
    private Boolean isRedeliver;

    public MessageWrapper() {}

    public MessageWrapper(MessageContext messageContext) {
        this.event = messageContext.getEvent();
        this.messageType = messageContext.getType();
        this.messageId = messageContext.getMessageId();
        this.gatewayId = messageContext.getGatewayId();
        if (messageContext.getUpdatedTime() != null) {
            this.updatedTime = messageContext.getUpdatedTime().getTime();
        }
        this.deliveryTag = messageContext.getDeliveryTag();
        this.isRedeliver = messageContext.isRedeliver();
    }

    public MessageContext toMessageContext() {
        MessageContext context;
        if (deliveryTag != null && deliveryTag > 0) {
            context = new MessageContext(event, messageType, messageId, gatewayId, deliveryTag);
        } else {
            context = new MessageContext(event, messageType, messageId, gatewayId);
        }
        if (updatedTime != null) {
            context.setUpdatedTime(new Timestamp(updatedTime));
        }
        if (isRedeliver != null) {
            context.setIsRedeliver(isRedeliver);
        }
        return context;
    }

    // Getters and setters
    public MessagingEvent getEvent() {
        return event;
    }

    public void setEvent(MessagingEvent event) {
        this.event = event;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public Long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Long updatedTime) {
        this.updatedTime = updatedTime;
    }

    public Long getDeliveryTag() {
        return deliveryTag;
    }

    public void setDeliveryTag(Long deliveryTag) {
        this.deliveryTag = deliveryTag;
    }

    public Boolean getIsRedeliver() {
        return isRedeliver;
    }

    public void setIsRedeliver(Boolean isRedeliver) {
        this.isRedeliver = isRedeliver;
    }
}
