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
package org.apache.airavata.models.workspace;

/**
 * Plain-POJO replacement for the generated {@code org.apache.airavata.model.workspace.proto.Notification}.
 */
public record Notification(
        String notificationId,
        String gatewayId,
        String title,
        String notificationMessage,
        long creationTime,
        long publishedTime,
        long expirationTime,
        NotificationPriority priority) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String notificationId = "";
        private String gatewayId = "";
        private String title = "";
        private String notificationMessage = "";
        private long creationTime;
        private long publishedTime;
        private long expirationTime;
        private NotificationPriority priority = NotificationPriority.NOTIFICATION_PRIORITY_UNKNOWN;

        private Builder() {}

        private Builder(Notification source) {
            this.notificationId = source.notificationId;
            this.gatewayId = source.gatewayId;
            this.title = source.title;
            this.notificationMessage = source.notificationMessage;
            this.creationTime = source.creationTime;
            this.publishedTime = source.publishedTime;
            this.expirationTime = source.expirationTime;
            this.priority = source.priority;
        }

        public Builder setNotificationId(String notificationId) {
            this.notificationId = notificationId;
            return this;
        }

        public Builder setGatewayId(String gatewayId) {
            this.gatewayId = gatewayId;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setNotificationMessage(String notificationMessage) {
            this.notificationMessage = notificationMessage;
            return this;
        }

        public Builder setCreationTime(long creationTime) {
            this.creationTime = creationTime;
            return this;
        }

        public Builder setPublishedTime(long publishedTime) {
            this.publishedTime = publishedTime;
            return this;
        }

        public Builder setExpirationTime(long expirationTime) {
            this.expirationTime = expirationTime;
            return this;
        }

        public Builder setPriority(NotificationPriority priority) {
            this.priority = priority;
            return this;
        }

        public Notification build() {
            return new Notification(
                    notificationId, gatewayId, title, notificationMessage, creationTime, publishedTime,
                    expirationTime, priority);
        }
    }
}
