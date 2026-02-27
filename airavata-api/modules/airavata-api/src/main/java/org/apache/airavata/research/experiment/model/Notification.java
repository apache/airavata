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
package org.apache.airavata.research.experiment.model;

import java.util.Objects;

/**
 * Domain model: Notification
 */
public class Notification {
    private String notificationId;
    private String gatewayId;
    private String title;
    private String notificationMessage;
    private long createdAt;
    private long publishedAt;
    private long expiresAt;
    private NotificationPriority priority;

    public Notification() {}

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNotificationMessage() {
        return notificationMessage;
    }

    public void setNotificationMessage(String notificationMessage) {
        this.notificationMessage = notificationMessage;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(long publishedAt) {
        this.publishedAt = publishedAt;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public NotificationPriority getPriority() {
        return priority;
    }

    public void setPriority(NotificationPriority priority) {
        this.priority = priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return Objects.equals(notificationId, that.notificationId)
                && Objects.equals(gatewayId, that.gatewayId)
                && Objects.equals(title, that.title)
                && Objects.equals(notificationMessage, that.notificationMessage)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(publishedAt, that.publishedAt)
                && Objects.equals(expiresAt, that.expiresAt)
                && Objects.equals(priority, that.priority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                notificationId, gatewayId, title, notificationMessage, createdAt, publishedAt, expiresAt, priority);
    }

    @Override
    public String toString() {
        return "Notification{" + "notificationId=" + notificationId + ", gatewayId=" + gatewayId + ", title=" + title
                + ", notificationMessage=" + notificationMessage + ", createdAt=" + createdAt + ", publishedAt="
                + publishedAt + ", expiresAt=" + expiresAt + ", priority=" + priority + "}";
    }
}
