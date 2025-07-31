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
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import org.apache.airavata.research.service.enums.PrivacyEnum;
import org.apache.airavata.research.service.enums.ResourceTypeEnum;
import org.apache.airavata.research.service.enums.StateEnum;
import org.apache.airavata.research.service.enums.StatusEnum;
import org.apache.airavata.research.service.model.entity.Resource;

@Entity
@Table(name = "STORAGE_RESOURCE_V2")
public class StorageResource extends Resource {

    @Column(nullable = false)
    @NotBlank(message = "Hostname is required")
    @Size(max = 255, message = "Hostname must not exceed 255 characters")
    private String hostname;

    @Column(nullable = false)
    @NotBlank(message = "Storage type is required")
    @Size(max = 100, message = "Storage type must not exceed 100 characters")
    private String storageType; // S3, SCP, NFS, etc.

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

    // S3-specific fields
    @Column
    @Size(max = 255, message = "Bucket name must not exceed 255 characters")
    private String bucketName;

    @Column
    @Size(max = 255, message = "Access key must not exceed 255 characters")
    private String accessKey;

    @Column
    @Size(max = 255, message = "Secret key must not exceed 255 characters")
    private String secretKey;

    // SCP-specific fields
    @Column
    private Integer port;

    @Column
    @Size(max = 255, message = "Username must not exceed 255 characters")
    private String username;

    @Column
    @Size(max = 50, message = "Authentication method must not exceed 50 characters")
    private String authenticationMethod; // "SSH_KEY", "PASSWORD"

    @Column(columnDefinition = "TEXT")
    private String sshKey;

    @Column
    @Size(max = 500, message = "Remote path must not exceed 500 characters")
    private String remotePath;

    @Column(columnDefinition = "TEXT")
    private String additionalInfo;

    @Column(nullable = false)
    @NotBlank(message = "Resource manager is required")
    @Size(max = 255, message = "Resource manager must not exceed 255 characters")
    private String resourceManager; // Gateway name or organization

    @Override
    public ResourceTypeEnum getType() {
        return ResourceTypeEnum.STORAGE_RESOURCE;
    }

    // Default constructor
    public StorageResource() {}

    // Constructor for mock data creation
    public StorageResource(String name, String description, String hostname, String storageType,
                          Long capacityTB, String accessProtocol, String endpoint,
                          Boolean supportsEncryption, Boolean supportsVersioning,
                          String additionalInfo, String resourceManager) {
        this.setName(name);
        this.setDescription(description);
        this.hostname = hostname;
        this.storageType = storageType;
        this.capacityTB = capacityTB;
        this.accessProtocol = accessProtocol;
        this.endpoint = endpoint;
        this.supportsEncryption = supportsEncryption;
        this.supportsVersioning = supportsVersioning;
        this.additionalInfo = additionalInfo;
        this.resourceManager = resourceManager;
        
        // Set inherited v1 Resource fields (required)
        this.setPrivacy(PrivacyEnum.PUBLIC);
        this.setState(StateEnum.ACTIVE);
        this.setStatus(StatusEnum.VERIFIED);
        this.setAuthors(new HashSet<>());
        this.setTags(new HashSet<>());
        this.setHeaderImage(""); // Default empty header image
    }

    // S3-specific constructor
    public StorageResource(String name, String description, String storageType, String endpoint,
                          String bucketName, String accessKey, String secretKey,
                          String resourceManager) {
        this(name, description, endpoint, storageType, 1000L, "S3", endpoint, true, true, null, resourceManager);
        this.bucketName = bucketName;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    // SCP-specific constructor
    public StorageResource(String name, String description, String hostname, String storageType,
                          Integer port, String username, String authenticationMethod, String sshKey,
                          String remotePath, String resourceManager) {
        this(name, description, hostname, storageType, 100L, "SCP", hostname, false, false, null, resourceManager);
        this.port = port;
        this.username = username;
        this.authenticationMethod = authenticationMethod;
        this.sshKey = sshKey;
        this.remotePath = remotePath;
    }

    // Getters and Setters for StorageResource-specific fields
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

    public String getResourceManager() {
        return resourceManager;
    }

    public void setResourceManager(String resourceManager) {
        this.resourceManager = resourceManager;
    }

    // S3-specific getters and setters
    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    // SCP-specific getters and setters
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAuthenticationMethod() {
        return authenticationMethod;
    }

    public void setAuthenticationMethod(String authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }

    public String getSshKey() {
        return sshKey;
    }

    public void setSshKey(String sshKey) {
        this.sshKey = sshKey;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }
}