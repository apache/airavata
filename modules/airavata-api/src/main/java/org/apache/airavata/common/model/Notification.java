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

/**
 * Domain model: Notification
 */
public class Notification {
    private String notificationId;
    private String gatewayId;
    private String title;
    private String notificationMessage;
    private long creationTime;
    private long publishedTime;
    private long expirationTime;
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

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getPublishedTime() {
        return publishedTime;
    }

    public void setPublishedTime(long publishedTime) {
        this.publishedTime = publishedTime;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
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
                && Objects.equals(creationTime, that.creationTime)
                && Objects.equals(publishedTime, that.publishedTime)
                && Objects.equals(expirationTime, that.expirationTime)
                && Objects.equals(priority, that.priority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                notificationId,
                gatewayId,
                title,
                notificationMessage,
                creationTime,
                publishedTime,
                expirationTime,
                priority);
    }

    @Override
    public String toString() {
        return "Notification{" + "notificationId=" + notificationId + ", gatewayId=" + gatewayId + ", title=" + title
                + ", notificationMessage=" + notificationMessage + ", creationTime=" + creationTime + ", publishedTime="
                + publishedTime + ", expirationTime=" + expirationTime + ", priority=" + priority + "}";
    }
}
