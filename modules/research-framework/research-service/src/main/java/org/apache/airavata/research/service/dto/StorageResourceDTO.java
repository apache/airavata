/**
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
package org.apache.airavata.research.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * UI-specific DTO for Storage Resource
 * Maps to airavata-api StorageResourceDescription with UI-specific extensions
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StorageResourceDTO {

    // Core fields from StorageResourceDescription
    private String storageResourceId;
    
    @NotBlank(message = "Storage resource name is required")
    @Size(max = 255, message = "Storage resource name must not exceed 255 characters")
    private String name;
    
    @NotBlank(message = "Hostname is required")
    @Size(max = 255, message = "Hostname must not exceed 255 characters")
    private String hostName;

    @Size(max = 1000, message = "Storage resource description must not exceed 1000 characters")
    private String storageResourceDescription;

    // UI-specific extensions stored in storageResourceDescription as JSON
    @NotBlank(message = "Storage type is required")
    @Size(max = 100, message = "Storage type must not exceed 100 characters")
    private String storageType; // S3, SCP, NFS, etc.

    @NotNull(message = "Capacity TB is required")
    @Min(value = 1, message = "Capacity TB must be at least 1")
    private Long capacityTB;

    @NotBlank(message = "Access protocol is required")
    @Size(max = 100, message = "Access protocol must not exceed 100 characters")
    private String accessProtocol; // S3, SFTP, NFS, HTTP, etc.

    @NotBlank(message = "Endpoint is required")
    @Size(max = 500, message = "Endpoint must not exceed 500 characters")
    private String endpoint; // API endpoint or mount point

    private Boolean supportsEncryption = false;
    private Boolean supportsVersioning = false;

    // S3-specific fields
    @Size(max = 255, message = "Bucket name must not exceed 255 characters")
    private String bucketName;

    @Size(max = 255, message = "Access key must not exceed 255 characters")
    private String accessKey;

    @Size(max = 255, message = "Secret key must not exceed 255 characters")
    private String secretKey;

    // SCP-specific fields
    private Integer port;

    @Size(max = 255, message = "Username must not exceed 255 characters")
    private String username;

    @Size(max = 50, message = "Authentication method must not exceed 50 characters")
    private String authenticationMethod; // "SSH_KEY", "PASSWORD"

    private String sshKey;

    @Size(max = 500, message = "Remote path must not exceed 500 characters")
    private String remotePath;

    private String additionalInfo;

    @NotBlank(message = "Resource manager is required")
    @Size(max = 255, message = "Resource manager must not exceed 255 characters")
    private String resourceManager; // Gateway name or organization

    // System fields
    private boolean enabled = true;
    private Long creationTime;
    private Long updateTime;

    // Default constructor
    public StorageResourceDTO() {}

    // Constructor for basic creation
    public StorageResourceDTO(String hostName, String storageResourceDescription) {
        this.hostName = hostName;
        this.storageResourceDescription = storageResourceDescription;
    }

    // S3-specific constructor
    public StorageResourceDTO(String hostName, String storageResourceDescription, String storageType, 
                             String endpoint, String bucketName, String accessKey, String secretKey, 
                             String resourceManager) {
        this.hostName = hostName;
        this.storageResourceDescription = storageResourceDescription;
        this.storageType = storageType;
        this.endpoint = endpoint;
        this.bucketName = bucketName;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.resourceManager = resourceManager;
        this.accessProtocol = "S3";
        this.supportsEncryption = true;
        this.supportsVersioning = true;
    }

    // SCP-specific constructor
    public StorageResourceDTO(String hostName, String storageResourceDescription, String storageType,
                             Integer port, String username, String authenticationMethod, String sshKey,
                             String remotePath, String resourceManager) {
        this.hostName = hostName;
        this.storageResourceDescription = storageResourceDescription;
        this.storageType = storageType;
        this.port = port;
        this.username = username;
        this.authenticationMethod = authenticationMethod;
        this.sshKey = sshKey;
        this.remotePath = remotePath;
        this.resourceManager = resourceManager;
        this.accessProtocol = "SCP";
        this.endpoint = hostName;
    }

    // Getters and Setters
    public String getStorageResourceId() {
        return storageResourceId;
    }

    public void setStorageResourceId(String storageResourceId) {
        this.storageResourceId = storageResourceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getStorageResourceDescription() {
        return storageResourceDescription;
    }

    public void setStorageResourceDescription(String storageResourceDescription) {
        this.storageResourceDescription = storageResourceDescription;
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }
}