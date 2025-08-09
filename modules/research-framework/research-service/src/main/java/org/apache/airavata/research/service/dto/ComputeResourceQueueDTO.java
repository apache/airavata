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
import jakarta.validation.constraints.Size;

/**
 * UI-specific DTO for Compute Resource Queue
 * Maps directly to airavata-api BatchQueue model
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComputeResourceQueueDTO {

    @NotBlank(message = "Queue name is required")
    @Size(max = 255, message = "Queue name must not exceed 255 characters")
    private String queueName;

    @Size(max = 1000, message = "Queue description must not exceed 1000 characters")
    private String queueDescription;

    @Min(value = 1, message = "Max run time must be at least 1 hour")
    private Integer maxRunTime;

    @Min(value = 1, message = "Max nodes must be at least 1")
    private Integer maxNodes;

    @Min(value = 1, message = "Max processors must be at least 1")
    private Integer maxProcessors;

    @Min(value = 1, message = "Max jobs in queue must be at least 1")
    private Integer maxJobsInQueue;

    @Min(value = 1, message = "Max memory must be at least 1")
    private Integer maxMemory;

    @Min(value = 1, message = "CPUs per node must be at least 1")
    private Integer cpusPerNode;

    @Min(value = 1, message = "Default node count must be at least 1")
    private Integer defaultNodeCount;

    @Min(value = 1, message = "Default CPU count must be at least 1")
    private Integer defaultCpuCount;

    @Min(value = 1, message = "Default wall time must be at least 1")
    private Integer defaultWallTime;

    @Size(max = 1000, message = "Queue specific macros must not exceed 1000 characters")
    private String queueSpecificMacros;

    private Boolean isDefaultQueue = false;

    // Default constructor
    public ComputeResourceQueueDTO() {}

    // Constructor for quick creation
    public ComputeResourceQueueDTO(String queueName, String queueDescription) {
        this.queueName = queueName;
        this.queueDescription = queueDescription;
    }

    // Getters and Setters
    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getQueueDescription() {
        return queueDescription;
    }

    public void setQueueDescription(String queueDescription) {
        this.queueDescription = queueDescription;
    }

    public Integer getMaxRunTime() {
        return maxRunTime;
    }

    public void setMaxRunTime(Integer maxRunTime) {
        this.maxRunTime = maxRunTime;
    }

    public Integer getMaxNodes() {
        return maxNodes;
    }

    public void setMaxNodes(Integer maxNodes) {
        this.maxNodes = maxNodes;
    }

    public Integer getMaxProcessors() {
        return maxProcessors;
    }

    public void setMaxProcessors(Integer maxProcessors) {
        this.maxProcessors = maxProcessors;
    }

    public Integer getMaxJobsInQueue() {
        return maxJobsInQueue;
    }

    public void setMaxJobsInQueue(Integer maxJobsInQueue) {
        this.maxJobsInQueue = maxJobsInQueue;
    }

    public Integer getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(Integer maxMemory) {
        this.maxMemory = maxMemory;
    }

    public Integer getCpusPerNode() {
        return cpusPerNode;
    }

    public void setCpusPerNode(Integer cpusPerNode) {
        this.cpusPerNode = cpusPerNode;
    }

    public Integer getDefaultNodeCount() {
        return defaultNodeCount;
    }

    public void setDefaultNodeCount(Integer defaultNodeCount) {
        this.defaultNodeCount = defaultNodeCount;
    }

    public Integer getDefaultCpuCount() {
        return defaultCpuCount;
    }

    public void setDefaultCpuCount(Integer defaultCpuCount) {
        this.defaultCpuCount = defaultCpuCount;
    }

    public Integer getDefaultWallTime() {
        return defaultWallTime;
    }

    public void setDefaultWallTime(Integer defaultWallTime) {
        this.defaultWallTime = defaultWallTime;
    }

    public String getQueueSpecificMacros() {
        return queueSpecificMacros;
    }

    public void setQueueSpecificMacros(String queueSpecificMacros) {
        this.queueSpecificMacros = queueSpecificMacros;
    }

    public Boolean getIsDefaultQueue() {
        return isDefaultQueue;
    }

    public void setIsDefaultQueue(Boolean isDefaultQueue) {
        this.isDefaultQueue = isDefaultQueue;
    }
}