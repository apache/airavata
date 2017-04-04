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
package org.apache.airavata.registry.core.experiment.catalog.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "NOTIFICATION")
public class Notification {
    private final static Logger logger = LoggerFactory.getLogger(Notification.class);
    private String notificationId;
    private String gatewayId;
    private String title;
    private String notificationMessage;
    private String priority;
    private Timestamp creationDate;
    private Timestamp publishedDate;
    private Timestamp expirationDate;

    @Id
    @Column(name = "NOTIFICATION_ID")
    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    @Column(name = "GATEWAY_ID")
    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    @Column(name = "TITLE")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Column(name = "NOTIFICATION_MESSAGE")
    public String getNotificationMessage() {
        return notificationMessage;
    }

    public void setNotificationMessage(String notificationMessage) {
        this.notificationMessage = notificationMessage;
    }

    @Column(name = "PRIORITY")
    public String getPriority() {
        return priority;
    }

    public void setPriority (String priority) {
        this.priority = priority;
    }

    @Column(name = "CREATION_DATE")
    public Timestamp getCreationDate() {
        return creationDate;
    }

    public void setCreationDate (Timestamp creationDate) {
        this.creationDate = creationDate;
    }

    @Column(name = "PUBLISHED_DATE")
    public Timestamp getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate (Timestamp publishedDate) {
        this.publishedDate = publishedDate;
    }

    @Column(name = "EXPIRATION_DATE")
    public Timestamp getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate (Timestamp expirationDate) {
        this.expirationDate = expirationDate;
    }

}