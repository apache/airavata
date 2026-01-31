/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
package org.apache.airavata.registry.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Entity for explicit user selection when multiple groups have conflicting
 * preferences/credentials for a resource (Zanzibar-like conflict resolution).
 */
@Entity
@Table(
        name = "USER_GROUP_SELECTION",
        indexes = {
            @Index(name = "idx_ugs_user_domain", columnList = "USER_ID, DOMAIN_ID"),
            @Index(name = "idx_ugs_selected_group", columnList = "SELECTED_GROUP_ID")
        })
@IdClass(UserGroupSelectionEntityPK.class)
public class UserGroupSelectionEntity {

    @Id
    @Column(name = "USER_ID", nullable = false, length = 512)
    private String userId;

    @Id
    @Column(name = "DOMAIN_ID", nullable = false, length = 255)
    private String domainId;

    @Id
    @Column(name = "RESOURCE_TYPE", nullable = false, length = 50)
    private String resourceType;

    @Id
    @Column(name = "RESOURCE_ID", nullable = false, length = 255)
    private String resourceId;

    @Id
    @Column(name = "SELECTION_KEY", nullable = false, length = 255)
    private String selectionKey;

    @Column(name = "SELECTED_GROUP_ID", nullable = false, length = 255)
    private String selectedGroupId;

    @Column(name = "CREATED_TIME")
    private Long createdTime;

    @Column(name = "UPDATED_TIME")
    private Long updatedTime;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getSelectionKey() {
        return selectionKey;
    }

    public void setSelectionKey(String selectionKey) {
        this.selectionKey = selectionKey;
    }

    public String getSelectedGroupId() {
        return selectedGroupId;
    }

    public void setSelectedGroupId(String selectedGroupId) {
        this.selectedGroupId = selectedGroupId;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    public Long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Long updatedTime) {
        this.updatedTime = updatedTime;
    }
}
