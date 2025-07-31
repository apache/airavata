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
import java.util.Set;
import org.apache.airavata.research.service.enums.PrivacyEnum;
import org.apache.airavata.research.service.enums.ResourceTypeEnum;
import org.apache.airavata.research.service.enums.StateEnum;
import org.apache.airavata.research.service.enums.StatusEnum;
import org.apache.airavata.research.service.model.entity.Resource;

@Entity
@Table(name = "COMPUTE_RESOURCE_V2")
public class ComputeResource extends Resource {

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
    @NotBlank(message = "Resource manager is required")
    @Size(max = 255, message = "Resource manager must not exceed 255 characters")
    private String resourceManager; // Gateway name or organization

    @Override
    public ResourceTypeEnum getType() {
        return ResourceTypeEnum.COMPUTE_RESOURCE;
    }

    // Default constructor
    public ComputeResource() {}

    // Constructor for mock data creation
    public ComputeResource(String name, String description, String hostname, String computeType, 
                          Integer cpuCores, Integer memoryGB, String operatingSystem, 
                          String queueSystem, String additionalInfo, String resourceManager) {
        this.setName(name);
        this.setDescription(description);
        this.hostname = hostname;
        this.computeType = computeType;
        this.cpuCores = cpuCores;
        this.memoryGB = memoryGB;
        this.operatingSystem = operatingSystem;
        this.queueSystem = queueSystem;
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

    // Getters and Setters for ComputeResource-specific fields
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

    public String getResourceManager() {
        return resourceManager;
    }

    public void setResourceManager(String resourceManager) {
        this.resourceManager = resourceManager;
    }
}