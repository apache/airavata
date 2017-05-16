/**
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
 */
package org.apache.airavata.messaging.core;

import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.thrift.TBase;

import java.sql.Timestamp;

public class MessageContext {
    private final TBase event;
    private final MessageType type;
    private final String messageId;
    private final String gatewayId;
    private Timestamp updatedTime;
    private long deliveryTag;
	private boolean isRedeliver;


    public MessageContext(TBase event, MessageType type, String messageId, String gatewayId) {
        this.event = event;
        this.type = type;
        this.messageId = messageId;
        this.gatewayId = gatewayId;
    }

    public MessageContext(TBase event, MessageType type, String messageId, String gatewayId, long deliveryTag) {
        this.event = event;
        this.type = type;
        this.messageId = messageId;
        this.gatewayId = gatewayId;
        this.deliveryTag = deliveryTag;
    }

    public TBase getEvent() {
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
