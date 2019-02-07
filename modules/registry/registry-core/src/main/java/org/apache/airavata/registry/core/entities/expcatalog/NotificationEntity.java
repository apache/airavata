/*
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
 *
*/
package org.apache.airavata.registry.core.entities.expcatalog;

import org.apache.airavata.model.workspace.NotificationPriority;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * The persistent class for the notification database table.
 */
@Entity
@Table(name = "NOTIFICATION")
public class NotificationEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "NOTIFICATION_ID")
    private String notificationId;

    @Column(name = "GATEWAY_ID")
    private String gatewayId;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "NOTIFICATION_MESSAGE", length = 4096)
    private String notificationMessage;

    @Column(name = "CREATION_DATE")
    private Timestamp creationTime;

    @Column(name = "PUBLISHED_DATE")
    private Timestamp publishedTime;

    @Column(name = "EXPIRATION_DATE")
    private Timestamp expirationTime;

    @Column(name = "PRIORITY")
    @Enumerated(EnumType.STRING)
    private NotificationPriority priority;

    public NotificationEntity() {
    }

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

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public Timestamp getPublishedTime() {
        return publishedTime;
    }

    public void setPublishedTime(Timestamp publishedTime) {
        this.publishedTime = publishedTime;
    }

    public Timestamp getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Timestamp expirationTime) {
        this.expirationTime = expirationTime;
    }

    public NotificationPriority getPriority() {
        return priority;
    }

    public void setPriority(NotificationPriority priority) {
        this.priority = priority;
    }
}