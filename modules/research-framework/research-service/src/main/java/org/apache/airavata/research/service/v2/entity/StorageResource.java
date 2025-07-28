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
package org.apache.airavata.research.service.v2.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "STORAGE_RESOURCE_V2")
@EntityListeners(AuditingEntityListener.class)
public class StorageResource {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(nullable = false, updatable = false, length = 48)
    private String id;

    @Column(nullable = false)
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Description is required")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @Column(nullable = false)
    @NotBlank(message = "Hostname is required")
    @Size(max = 255, message = "Hostname must not exceed 255 characters")
    private String hostname;

    @Column(nullable = false)
    @NotBlank(message = "Storage type is required")
    @Size(max = 100, message = "Storage type must not exceed 100 characters")
    private String storageType; // Object Storage, File System, Database, etc.

    @Column(nullable = false)
    @NotNull(message = "Capacity TB is required")
    @Min(value = 1, message = "Capacity TB must be at least 1")
    private Long capacityTB;

    @Column(nullable = false)
    @NotBlank(message = "Access protocol is required")
    @Size(max = 100, message = "Access protocol must not exceed 100 characters")
    private String accessProtocol; // S3, SFTP, NFS, HTTP, etc.

    @Column(nullable = false)
    @NotBlank(message = "Endpoint is required")
    @Size(max = 500, message = "Endpoint must not exceed 500 characters")
    private String endpoint; // API endpoint or mount point

    @Column(nullable = false)
    private Boolean supportsEncryption = false;

    @Column(nullable = false)
    private Boolean supportsVersioning = false;

    @Column(columnDefinition = "TEXT")
    private String additionalInfo;

    @Column(nullable = false)
    private Boolean isPublic = true;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    @NotBlank(message = "Resource manager is required")
    @Size(max = 255, message = "Resource manager must not exceed 255 characters")
    private String resourceManager; // Gateway name or organization

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private Instant createdAt;

    @Column(nullable = false)
    @LastModifiedDate
    private Instant updatedAt;

    // Default constructor
    public StorageResource() {}

    // Constructor for mock data creation
    public StorageResource(String name, String description, String hostname, String storageType,
                          Long capacityTB, String accessProtocol, String endpoint,
                          Boolean supportsEncryption, Boolean supportsVersioning,
                          String additionalInfo, String resourceManager) {
        this.name = name;
        this.description = description;
        this.hostname = hostname;
        this.storageType = storageType;
        this.capacityTB = capacityTB;
        this.accessProtocol = accessProtocol;
        this.endpoint = endpoint;
        this.supportsEncryption = supportsEncryption;
        this.supportsVersioning = supportsVersioning;
        this.additionalInfo = additionalInfo;
        this.resourceManager = resourceManager;
        this.isPublic = true;
        this.isActive = true;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public Long getCapacityTB() {
        return capacityTB;
    }

    public void setCapacityTB(Long capacityTB) {
        this.capacityTB = capacityTB;
    }

    public String getAccessProtocol() {
        return accessProtocol;
    }

    public void setAccessProtocol(String accessProtocol) {
        this.accessProtocol = accessProtocol;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Boolean getSupportsEncryption() {
        return supportsEncryption;
    }

    public void setSupportsEncryption(Boolean supportsEncryption) {
        this.supportsEncryption = supportsEncryption;
    }

    public Boolean getSupportsVersioning() {
        return supportsVersioning;
    }

    public void setSupportsVersioning(Boolean supportsVersioning) {
        this.supportsVersioning = supportsVersioning;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getResourceManager() {
        return resourceManager;
    }

    public void setResourceManager(String resourceManager) {
        this.resourceManager = resourceManager;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}