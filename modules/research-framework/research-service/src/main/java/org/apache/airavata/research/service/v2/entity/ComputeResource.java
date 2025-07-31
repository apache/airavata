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
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
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

    // New fields to match UI requirements
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "COMPUTE_RESOURCE_HOST_ALIASES", joinColumns = @JoinColumn(name = "compute_resource_id"))
    @Column(name = "host_alias")
    private List<String> hostAliases = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "COMPUTE_RESOURCE_IP_ADDRESSES", joinColumns = @JoinColumn(name = "compute_resource_id"))
    @Column(name = "ip_address")
    private List<String> ipAddresses = new ArrayList<>();

    @Column(nullable = false)
    @NotBlank(message = "SSH username is required")
    @Size(max = 100, message = "SSH username must not exceed 100 characters")
    private String sshUsername;

    @Column(nullable = false)
    @NotNull(message = "SSH port is required")
    @Min(value = 1, message = "SSH port must be at least 1")
    private Integer sshPort;

    @Column(nullable = false)
    @NotBlank(message = "Authentication method is required")
    @Size(max = 50, message = "Authentication method must not exceed 50 characters")
    private String authenticationMethod; // SSH_KEY or PASSWORD

    @Column(columnDefinition = "TEXT")
    private String sshKey; // SSH key content for SSH_KEY authentication

    @Column(nullable = false)
    @NotBlank(message = "Working directory is required")
    @Size(max = 500, message = "Working directory must not exceed 500 characters")
    private String workingDirectory;

    @Column(nullable = false)
    @NotBlank(message = "Scheduler type is required")
    @Size(max = 50, message = "Scheduler type must not exceed 50 characters")
    private String schedulerType; // SLURM, PBS, SGE, etc.

    @Column(nullable = false)
    @NotBlank(message = "Data movement protocol is required")
    @Size(max = 50, message = "Data movement protocol must not exceed 50 characters")
    private String dataMovementProtocol; // SCP, SFTP, etc.

    // One-to-many relationship with Queue entities
    @OneToMany(mappedBy = "computeResource", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ComputeResourceQueue> queues = new ArrayList<>();

    @Override
    public ResourceTypeEnum getType() {
        return ResourceTypeEnum.COMPUTE_RESOURCE;
    }

    // Default constructor
    public ComputeResource() {}

    // Constructor for mock data creation
    public ComputeResource(String name, String description, String hostname, String computeType, 
                          Integer cpuCores, Integer memoryGB, String operatingSystem, 
                          String queueSystem, String additionalInfo, String resourceManager,
                          String sshUsername, Integer sshPort, String authenticationMethod, 
                          String workingDirectory, String schedulerType, String dataMovementProtocol) {
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
        this.sshUsername = sshUsername;
        this.sshPort = sshPort;
        this.authenticationMethod = authenticationMethod;
        this.workingDirectory = workingDirectory;
        this.schedulerType = schedulerType;
        this.dataMovementProtocol = dataMovementProtocol;
        
        // Initialize collections
        this.hostAliases = new ArrayList<>();
        this.ipAddresses = new ArrayList<>();
        this.queues = new ArrayList<>();
        
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

    // Getters and Setters for new fields

    public List<String> getHostAliases() {
        return hostAliases;
    }

    public void setHostAliases(List<String> hostAliases) {
        this.hostAliases = hostAliases;
    }

    public List<String> getIpAddresses() {
        return ipAddresses;
    }

    public void setIpAddresses(List<String> ipAddresses) {
        this.ipAddresses = ipAddresses;
    }

    public String getSshUsername() {
        return sshUsername;
    }

    public void setSshUsername(String sshUsername) {
        this.sshUsername = sshUsername;
    }

    public Integer getSshPort() {
        return sshPort;
    }

    public void setSshPort(Integer sshPort) {
        this.sshPort = sshPort;
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

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public String getSchedulerType() {
        return schedulerType;
    }

    public void setSchedulerType(String schedulerType) {
        this.schedulerType = schedulerType;
    }

    public String getDataMovementProtocol() {
        return dataMovementProtocol;
    }

    public void setDataMovementProtocol(String dataMovementProtocol) {
        this.dataMovementProtocol = dataMovementProtocol;
    }

    public List<ComputeResourceQueue> getQueues() {
        return queues;
    }

    public void setQueues(List<ComputeResourceQueue> queues) {
        this.queues = queues;
    }
}