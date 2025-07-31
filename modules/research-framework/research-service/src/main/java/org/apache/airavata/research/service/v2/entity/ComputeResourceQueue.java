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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "COMPUTE_RESOURCE_QUEUE_V2")
public class ComputeResourceQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Queue name is required")
    @Size(max = 100, message = "Queue name must not exceed 100 characters")
    private String queueName;

    @Column(columnDefinition = "TEXT")
    private String queueDescription;

    @Column
    @Min(value = 1, message = "Queue max run time must be at least 1 minute")
    private Integer queueMaxRunTime; // in minutes

    @Column
    @Min(value = 1, message = "Queue max nodes must be at least 1")
    private Integer queueMaxNodes;

    @Column
    @Min(value = 1, message = "Queue max processors must be at least 1")
    private Integer queueMaxProcessors;

    @Column
    @Min(value = 1, message = "Max jobs in queue must be at least 1")
    private Integer maxJobsInQueue;

    @Column
    @Min(value = 1, message = "CPUs per node must be at least 1")
    private Integer cpusPerNode;

    @Column
    @Min(value = 1, message = "Default node count must be at least 1")
    private Integer defaultNodeCount;

    @Column
    @Min(value = 1, message = "Default CPU count must be at least 1")
    private Integer defaultCpuCount;

    @Column
    @Min(value = 1, message = "Default wall time must be at least 1 minute")
    private Integer defaultWallTime; // in minutes

    @Column(columnDefinition = "TEXT")
    private String queueSpecificMacros;

    @Column
    private Boolean isDefaultQueue = false;

    // Many-to-one relationship with ComputeResource
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compute_resource_id", nullable = false)
    @JsonIgnore
    private ComputeResource computeResource;

    // Default constructor
    public ComputeResourceQueue() {}

    // Constructor for creating queue entries
    public ComputeResourceQueue(String queueName, String queueDescription, Integer queueMaxRunTime, 
                               Integer queueMaxNodes, Integer queueMaxProcessors, Integer maxJobsInQueue,
                               Integer cpusPerNode, Integer defaultNodeCount, Integer defaultCpuCount,
                               Integer defaultWallTime, String queueSpecificMacros, Boolean isDefaultQueue) {
        this.queueName = queueName;
        this.queueDescription = queueDescription;
        this.queueMaxRunTime = queueMaxRunTime;
        this.queueMaxNodes = queueMaxNodes;
        this.queueMaxProcessors = queueMaxProcessors;
        this.maxJobsInQueue = maxJobsInQueue;
        this.cpusPerNode = cpusPerNode;
        this.defaultNodeCount = defaultNodeCount;
        this.defaultCpuCount = defaultCpuCount;
        this.defaultWallTime = defaultWallTime;
        this.queueSpecificMacros = queueSpecificMacros;
        this.isDefaultQueue = isDefaultQueue != null ? isDefaultQueue : false;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Integer getQueueMaxRunTime() {
        return queueMaxRunTime;
    }

    public void setQueueMaxRunTime(Integer queueMaxRunTime) {
        this.queueMaxRunTime = queueMaxRunTime;
    }

    public Integer getQueueMaxNodes() {
        return queueMaxNodes;
    }

    public void setQueueMaxNodes(Integer queueMaxNodes) {
        this.queueMaxNodes = queueMaxNodes;
    }

    public Integer getQueueMaxProcessors() {
        return queueMaxProcessors;
    }

    public void setQueueMaxProcessors(Integer queueMaxProcessors) {
        this.queueMaxProcessors = queueMaxProcessors;
    }

    public Integer getMaxJobsInQueue() {
        return maxJobsInQueue;
    }

    public void setMaxJobsInQueue(Integer maxJobsInQueue) {
        this.maxJobsInQueue = maxJobsInQueue;
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

    public ComputeResource getComputeResource() {
        return computeResource;
    }

    public void setComputeResource(ComputeResource computeResource) {
        this.computeResource = computeResource;
    }
}