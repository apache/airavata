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
import java.util.List;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "COMPUTE_RESOURCE_V2")
@EntityListeners(AuditingEntityListener.class)
public class ComputeResource {

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
    @NotBlank(message = "Compute type is required")
    @Size(max = 100, message = "Compute type must not exceed 100 characters")
    private String computeType; // HPC, Cloud, Local, etc.

    @Column(nullable = false)
    @NotNull(message = "CPU cores is required")
    @Min(value = 1, message = "CPU cores must be at least 1")
    private Integer cpuCores;

    @Column(nullable = false)
    @NotNull(message = "Memory GB is required")
    @Min(value = 1, message = "Memory GB must be at least 1")
    private Integer memoryGB;

    @Column(nullable = false)
    @NotBlank(message = "Operating system is required")
    @Size(max = 100, message = "Operating system must not exceed 100 characters")
    private String operatingSystem;

    @Column(nullable = false)
    @NotBlank(message = "Queue system is required")
    @Size(max = 100, message = "Queue system must not exceed 100 characters")
    private String queueSystem; // SLURM, PBS, SGE, etc.

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
    public ComputeResource() {}

    // Constructor for mock data creation
    public ComputeResource(String name, String description, String hostname, String computeType, 
                          Integer cpuCores, Integer memoryGB, String operatingSystem, 
                          String queueSystem, String additionalInfo, String resourceManager) {
        this.name = name;
        this.description = description;
        this.hostname = hostname;
        this.computeType = computeType;
        this.cpuCores = cpuCores;
        this.memoryGB = memoryGB;
        this.operatingSystem = operatingSystem;
        this.queueSystem = queueSystem;
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

    public String getComputeType() {
        return computeType;
    }

    public void setComputeType(String computeType) {
        this.computeType = computeType;
    }

    public Integer getCpuCores() {
        return cpuCores;
    }

    public void setCpuCores(Integer cpuCores) {
        this.cpuCores = cpuCores;
    }

    public Integer getMemoryGB() {
        return memoryGB;
    }

    public void setMemoryGB(Integer memoryGB) {
        this.memoryGB = memoryGB;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getQueueSystem() {
        return queueSystem;
    }

    public void setQueueSystem(String queueSystem) {
        this.queueSystem = queueSystem;
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