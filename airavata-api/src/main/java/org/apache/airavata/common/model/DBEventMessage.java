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

import java.util.Objects;
import org.apache.airavata.messaging.core.MessageContext;

/**
 * Domain model: DBEventMessage
 */
public class DBEventMessage extends MessagingEvent {
    private DBEventType dbEventType;
    private DBEventMessageContext messageContext;
    private String publisherService;

    public DBEventMessage() {}

    /**
     * Constructor to extract DBEventMessage from MessageContext.
     * Assumes the MessageContext contains a DBEventMessage event.
     */
    public DBEventMessage(MessageContext messageContext) {
        if (messageContext.getEvent() instanceof DBEventMessage) {
            DBEventMessage event = (DBEventMessage) messageContext.getEvent();
            this.dbEventType = event.getDbEventType();
            this.messageContext = event.getMessageContext();
            this.publisherService = event.getPublisherService();
        } else {
            throw new IllegalArgumentException("MessageContext does not contain a DBEventMessage event");
        }
    }

    public DBEventType getDbEventType() {
        return dbEventType;
    }

    public void setDbEventType(DBEventType dbEventType) {
        this.dbEventType = dbEventType;
    }

    public DBEventMessageContext getMessageContext() {
        return messageContext;
    }

    public void setMessageContext(DBEventMessageContext messageContext) {
        this.messageContext = messageContext;
    }

    public String getPublisherService() {
        return publisherService;
    }

    public void setPublisherService(String publisherService) {
        this.publisherService = publisherService;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DBEventMessage that = (DBEventMessage) o;
        return Objects.equals(dbEventType, that.dbEventType)
                && Objects.equals(messageContext, that.messageContext)
                && Objects.equals(publisherService, that.publisherService);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dbEventType, messageContext, publisherService);
    }

    @Override
    public String toString() {
        return "DBEventMessage{" + "dbEventType=" + dbEventType + ", messageContext=" + messageContext
                + ", publisherService=" + publisherService + "}";
    }
}
