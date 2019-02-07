/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package org.apache.airavata.registry.core.entities.appcatalog;

import javax.persistence.*;
import java.io.Serializable;

/**
 * The persistent class for the batch_queue database table.
 */
@Entity
@Table(name = "BATCH_QUEUE")
@IdClass(BatchQueuePK.class)
public class BatchQueueEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name="COMPUTE_RESOURCE_ID")
    private String computeResourceId;

    @Id
    @Column(name="QUEUE_NAME")
    private String queueName;

    @Column(name = "MAX_JOB_IN_QUEUE")
    private int maxJobsInQueue;

    @Column(name = "MAX_MEMORY")
    private int maxMemory;

    @Column(name = "MAX_NODES")
    private int maxNodes;

    @Column(name = "MAX_PROCESSORS")
    private int maxProcessors;

    @Column(name = "MAX_RUNTIME")
    private int maxRuntime;

    @Column(name = "QUEUE_DESCRIPTION")
    private String queueDescription;

    @ManyToOne(targetEntity = ComputeResourceEntity.class, cascade = CascadeType.MERGE)
    @JoinColumn(name = "COMPUTE_RESOURCE_ID")
    private ComputeResourceEntity computeResource;

    @Column(name = "CPU_PER_NODE")
    private int cpuPerNode;

    @Column(name = "DEFAULT_NODE_COUNT")
    private int defaultNodeCount;

    @Column(name = "DEFAULT_CPU_COUNT")
    private int defaultCPUCount;

    @Column(name = "DEFAULT_WALLTIME")
    private int defaultWalltime;

    @Column(name = "QUEUE_SPECIFIC_MACROS")
    private String queueSpecificMacros;

    @Column(name = "IS_DEFAULT_QUEUE")
    private boolean isDefaultQueue;

    public BatchQueueEntity() {
    }

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public int getMaxJobsInQueue() {
        return maxJobsInQueue;
    }

    public void setMaxJobsInQueue(int maxJobsInQueue) {
        this.maxJobsInQueue = maxJobsInQueue;
    }

    public int getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(int maxMemory) {
        this.maxMemory = maxMemory;
    }

    public int getMaxNodes() {
        return maxNodes;
    }

    public void setMaxNodes(int maxNodes) {
        this.maxNodes = maxNodes;
    }

    public int getMaxProcessors() {
        return maxProcessors;
    }

    public void setMaxProcessors(int maxProcessors) {
        this.maxProcessors = maxProcessors;
    }

    public int getMaxRuntime() {
        return maxRuntime;
    }

    public void setMaxRuntime(int maxRuntime) {
        this.maxRuntime = maxRuntime;
    }

    public String getQueueDescription() {
        return queueDescription;
    }

    public void setQueueDescription(String queueDescription) {
        this.queueDescription = queueDescription;
    }

    public ComputeResourceEntity getComputeResource() {
        return computeResource;
    }

    public void setComputeResource(ComputeResourceEntity computeResource) {
        this.computeResource = computeResource;
    }

    public int getCpuPerNode() {
        return cpuPerNode;
    }

    public void setCpuPerNode(int cpuPerNode) {
        this.cpuPerNode = cpuPerNode;
    }

    public int getDefaultNodeCount() {
        return defaultNodeCount;
    }

    public void setDefaultNodeCount(int defaultNodeCount) {
        this.defaultNodeCount = defaultNodeCount;
    }

    public int getDefaultCPUCount() {
        return defaultCPUCount;
    }

    public void setDefaultCPUCount(int defaultCPUCount) {
        this.defaultCPUCount = defaultCPUCount;
    }

    public int getDefaultWalltime() {
        return defaultWalltime;
    }

    public void setDefaultWalltime(int defaultWalltime) {
        this.defaultWalltime = defaultWalltime;
    }

    public String getQueueSpecificMacros() {
        return queueSpecificMacros;
    }

    public void setQueueSpecificMacros(String queueSpecificMacros) {
        this.queueSpecificMacros = queueSpecificMacros;
    }

    public boolean getIsDefaultQueue() {
        return isDefaultQueue;
    }

    public void setIsDefaultQueue(boolean defaultQueue) {
        isDefaultQueue = defaultQueue;
    }
}