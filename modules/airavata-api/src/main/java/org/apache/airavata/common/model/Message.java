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
package org.apache.airavata.common.model;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Domain model: Message
 */
public class Message {
    private ByteBuffer event;
    private String messageId;
    private MessageType messageType;
    private long updatedTime;
    private MessageLevel messageLevel;

    public Message() {}

    public ByteBuffer getEvent() {
        return event;
    }

    public void setEvent(ByteBuffer event) {
        this.event = event;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
    }

    public MessageLevel getMessageLevel() {
        return messageLevel;
    }

    public void setMessageLevel(MessageLevel messageLevel) {
        this.messageLevel = messageLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message that = (Message) o;
        return Objects.equals(event, that.event)
                && Objects.equals(messageId, that.messageId)
                && Objects.equals(messageType, that.messageType)
                && Objects.equals(updatedTime, that.updatedTime)
                && Objects.equals(messageLevel, that.messageLevel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(event, messageId, messageType, updatedTime, messageLevel);
    }

    @Override
    public String toString() {
        return "Message{" + "event=" + event + ", messageId=" + messageId + ", messageType=" + messageType
                + ", updatedTime=" + updatedTime + ", messageLevel=" + messageLevel + "}";
    }
}
