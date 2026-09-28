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
import java.util.ArrayList;
import java.util.List;

/**
 * UI-specific DTO for Compute Resource
 * Maps to airavata-api ComputeResourceDescription with UI-specific extensions
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComputeResourceDTO {

    // Core fields from ComputeResourceDescription
    private String computeResourceId;
    
    @NotBlank(message = "Compute resource name is required")
    @Size(max = 255, message = "Compute resource name must not exceed 255 characters")
    private String name;
    
    @NotBlank(message = "Resource description is required")
    @Size(max = 1000, message = "Resource description must not exceed 1000 characters")
    private String resourceDescription;
    
    @NotBlank(message = "Hostname is required")
    @Size(max = 255, message = "Hostname must not exceed 255 characters")
    private String hostName;

    // UI-specific extensions stored in resourceDescription as JSON
    @NotBlank(message = "Compute type is required")
    @Size(max = 100, message = "Compute type must not exceed 100 characters")
    private String computeType; // HPC, Cloud, Local, etc.

    @NotNull(message = "CPU cores is required")
    @Min(value = 1, message = "CPU cores must be at least 1")
    private Integer cpuCores;

    @NotNull(message = "Memory GB is required")
    @Min(value = 1, message = "Memory GB must be at least 1")
    private Integer memoryGB;

    @NotBlank(message = "Operating system is required")
    @Size(max = 100, message = "Operating system must not exceed 100 characters")
    private String operatingSystem;

    // Queue system is represented by BatchQueue entities, not a simple string
    // Keeping for backward compatibility but making optional
    private String queueSystem; // SLURM, PBS, SGE, etc.

    private String additionalInfo;

    // Resource manager type handled by ResourceJobManager entity
    // Keeping for backward compatibility but making optional  
    private String resourceManager; // Gateway name or organization

    // Direct mappings from ComputeResourceDescription
    private List<String> hostAliases = new ArrayList<>();
    private List<String> ipAddresses = new ArrayList<>();

    // SSH configuration fields (mapped from JobSubmissionInterface -> SSHJobSubmission)
    private Integer sshPort; // Optional - defaults to 22 if not specified
    private String alternativeSSHHostName; // Optional alternative hostname
    private String securityProtocol; // SSH_KEYS, USERNAME_PASSWORD (from SecurityProtocol enum)

    // Job management fields (mapped from JobSubmissionInterface -> SSHJobSubmission -> ResourceJobManager)
    private String resourceJobManagerType; // PBS, SLURM, UGE, etc. (from ResourceJobManagerType enum)
    
    // Data movement fields (mapped from DataMovementInterface)
    private String dataMovementProtocol; // SCP, SFTP, GRIDFTP, etc. (from DataMovementProtocol enum)

    // Queue management
    private List<ComputeResourceQueueDTO> queues = new ArrayList<>();

    // System fields
    private boolean enabled = true;
    private Long creationTime;
    private Long updateTime;

    // Default constructor
    public ComputeResourceDTO() {}

    // Constructor for mapping from existing data
    public ComputeResourceDTO(String computeResourceId, String hostName, String resourceDescription) {
        this.computeResourceId = computeResourceId;
        this.hostName = hostName;
        this.resourceDescription = resourceDescription;
    }

    // Getters and Setters
    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResourceDescription() {
        return resourceDescription;
    }

    public void setResourceDescription(String resourceDescription) {
        this.resourceDescription = resourceDescription;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
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

    public String getAlternativeSSHHostName() {
        return alternativeSSHHostName;
    }

    public void setAlternativeSSHHostName(String alternativeSSHHostName) {
        this.alternativeSSHHostName = alternativeSSHHostName;
    }

    public Integer getSshPort() {
        return sshPort;
    }

    public void setSshPort(Integer sshPort) {
        this.sshPort = sshPort;
    }

    public String getSecurityProtocol() {
        return securityProtocol;
    }

    public void setSecurityProtocol(String securityProtocol) {
        this.securityProtocol = securityProtocol;
    }

    public String getResourceJobManagerType() {
        return resourceJobManagerType;
    }

    public void setResourceJobManagerType(String resourceJobManagerType) {
        this.resourceJobManagerType = resourceJobManagerType;
    }

    public String getDataMovementProtocol() {
        return dataMovementProtocol;
    }

    public void setDataMovementProtocol(String dataMovementProtocol) {
        this.dataMovementProtocol = dataMovementProtocol;
    }

    public List<ComputeResourceQueueDTO> getQueues() {
        return queues;
    }

    public void setQueues(List<ComputeResourceQueueDTO> queues) {
        this.queues = queues;
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