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
package org.apache.airavata.models.dbevent;

/**
 * Actual db-event message transmitted.
 *
 * <p>Plain-POJO replacement for the generated {@code org.apache.airavata.model.dbevent.proto.DBEventMessage}.
 */
public record DBEventMessage(DBEventType dbEventType, DBEventMessageContext messageContext, String publisherService) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private DBEventType dbEventType = DBEventType.DB_EVENT_TYPE_UNKNOWN;
        private DBEventMessageContext messageContext;
        private String publisherService = "";

        private Builder() {}

        private Builder(DBEventMessage source) {
            this.dbEventType = source.dbEventType;
            this.messageContext = source.messageContext;
            this.publisherService = source.publisherService;
        }

        public Builder setDbEventType(DBEventType dbEventType) {
            this.dbEventType = dbEventType;
            return this;
        }

        public Builder setMessageContext(DBEventMessageContext messageContext) {
            this.messageContext = messageContext;
            return this;
        }

        public Builder setPublisherService(String publisherService) {
            this.publisherService = publisherService;
            return this;
        }

        public DBEventMessage build() {
            return new DBEventMessage(dbEventType, messageContext, publisherService);
        }
    }
}
