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
package org.apache.airavata.registry.entities.appcatalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import org.apache.airavata.common.model.PreferenceLevel;

/**
 * Unified entity for storage preferences that can be set at gateway or user level.
 *
 * <p>This entity consolidates the following legacy entities:
 * <ul>
 *   <li>StoragePreferenceEntity - gateway-level storage preferences</li>
 *   <li>UserStoragePreferenceEntity - user-level storage preferences</li>
 * </ul>
 *
 * <p>The level discriminator enables hierarchical preference resolution where more specific
 * levels override less specific ones. Resolution order: USER > GATEWAY
 *
 * <p>Owner ID format by level:
 * <ul>
 *   <li>GATEWAY: ownerId = gatewayId</li>
 *   <li>USER: ownerId = userId@gatewayId (airavataInternalUserId format)</li>
 * </ul>
 *
 * @see PreferenceLevel
 * @see UnifiedStoragePreferenceEntityPK
 */
@Entity(name = "UnifiedStoragePreferenceEntity")
@Table(
        name = "UNIFIED_STORAGE_PREFERENCE",
        indexes = {
            @Index(name = "idx_storage_pref_resource", columnList = "STORAGE_RESOURCE_ID"),
            @Index(name = "idx_storage_pref_owner", columnList = "OWNER_ID"),
            @Index(name = "idx_storage_pref_level", columnList = "LEVEL"),
            @Index(name = "idx_storage_pref_owner_level", columnList = "OWNER_ID, LEVEL"),
            @Index(name = "idx_storage_pref_resource_owner", columnList = "STORAGE_RESOURCE_ID, OWNER_ID")
        })
@IdClass(UnifiedStoragePreferenceEntityPK.class)
public class UnifiedStoragePreferenceEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    // ============================================
    // PRIMARY KEY FIELDS
    // ============================================

    @Id
    @Column(name = "STORAGE_RESOURCE_ID", nullable = false)
    private String storageResourceId;

    @Id
    @Column(name = "OWNER_ID", nullable = false, length = 512)
    private String ownerId;

    @Id
    @Column(name = "LEVEL", nullable = false)
    @Enumerated(EnumType.STRING)
    private PreferenceLevel level;

    // ============================================
    // CORE PREFERENCE FIELDS (from both entities)
    // ============================================

    @Column(name = "FS_ROOT_LOCATION")
    private String fileSystemRootLocation;

    @Column(name = "LOGIN_USERNAME")
    private String loginUserName;

    @Column(name = "RESOURCE_CS_TOKEN")
    private String resourceSpecificCredentialStoreToken;

    // ============================================
    // AUDIT FIELDS
    // ============================================

    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;

    @Column(name = "UPDATE_TIME")
    private Timestamp updateTime;

    public UnifiedStoragePreferenceEntity() {}

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

    public String getStorageResourceId() {
        return storageResourceId;
    }

    public void setStorageResourceId(String storageResourceId) {
        this.storageResourceId = storageResourceId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public PreferenceLevel getLevel() {
        return level;
    }

    public void setLevel(PreferenceLevel level) {
        this.level = level;
    }

    public String getFileSystemRootLocation() {
        return fileSystemRootLocation;
    }

    public void setFileSystemRootLocation(String fileSystemRootLocation) {
        this.fileSystemRootLocation = fileSystemRootLocation;
    }

    public String getLoginUserName() {
        return loginUserName;
    }

    public void setLoginUserName(String loginUserName) {
        this.loginUserName = loginUserName;
    }

    public String getResourceSpecificCredentialStoreToken() {
        return resourceSpecificCredentialStoreToken;
    }

    public void setResourceSpecificCredentialStoreToken(String resourceSpecificCredentialStoreToken) {
        this.resourceSpecificCredentialStoreToken = resourceSpecificCredentialStoreToken;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}
