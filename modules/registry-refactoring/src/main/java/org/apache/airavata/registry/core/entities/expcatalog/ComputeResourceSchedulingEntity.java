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
package org.apache.airavata.registry.core.entities.expcatalog;

import javax.persistence.*;

@Entity
@Table(name = "COMPUTE_RESOURCE_SCHEDULING")
public class ComputeResourceSchedulingEntity {
    private String experimentId;
    private String resourceHostId;
    private int totalCPUCount;
    private int nodeCount;
    private int numberOfThreads;
    private String queueName;
    private int wallTimeLimit;
    private int totalPhysicalMemory;
    private String chessisNumber;
    private String staticWorkingDir;
    private String overrideLoginUserName;
    private String overrideScratchLocation;
    private String overrideAllocationProjectNumber;
    private UserConfigurationEntity userConfiguration;

    @Id
    @Column(name = "EXPERIMENT_ID")
    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    @Column(name = "RESOURCE_HOST_ID")
    public String getResourceHostId() {
        return resourceHostId;
    }

    public void setResourceHostId(String resourceHostId) {
        this.resourceHostId = resourceHostId;
    }

    @Column(name = "CPU_COUNT")
    public int getTotalCPUCount() {
        return totalCPUCount;
    }

    public void setTotalCPUCount(int totalCPUCount) {
        this.totalCPUCount = totalCPUCount;
    }

    @Column(name = "NODE_COUNT")
    public int getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }

    @Column(name = "NUMBER_OF_THREADS")
    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public void setNumberOfThreads(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

    @Column(name = "QUEUE_NAME")
    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    @Column(name = "WALL_TIME_LIMIT")
    public int getWallTimeLimit() {
        return wallTimeLimit;
    }

    public void setWallTimeLimit(int wallTimeLimit) {
        this.wallTimeLimit = wallTimeLimit;
    }

    @Column(name = "TOTAL_PHYSICAL_MEMORY")
    public int getTotalPhysicalMemory() {
        return totalPhysicalMemory;
    }

    public void setTotalPhysicalMemory(int totalPhysicalMemory) {
        this.totalPhysicalMemory = totalPhysicalMemory;
    }

    @Column(name = "CHESSIS_NUMBER")
    public String getChessisNumber() {
        return chessisNumber;
    }

    public void setChessisNumber(String chessisNumber) {
        this.chessisNumber = chessisNumber;
    }

    @Column(name = "STATIC_WORKING_DIRECTORY")
    public String getStaticWorkingDir() {
        return staticWorkingDir;
    }

    public void setStaticWorkingDir(String staticWorkingDir) {
        this.staticWorkingDir = staticWorkingDir;
    }

    @Column(name = "OVERRIDE_LOGIN_USERNAME")
    public String getOverrideLoginUserName() {
        return overrideLoginUserName;
    }

    public void setOverrideLoginUserName(String overrideLoginUserName) {
        this.overrideLoginUserName = overrideLoginUserName;
    }

    @Column(name = "OVERRIDE_SCRATCH_LOCATION")
    public String getOverrideScratchLocation() {
        return overrideScratchLocation;
    }

    public void setOverrideScratchLocation(String overrideScratchLocation) {
        this.overrideScratchLocation = overrideScratchLocation;
    }

    @Column(name = "OVERRIDE_ALLOCATION_PROJECT_NUMBER")
    public String getOverrideAllocationProjectNumber() {
        return overrideAllocationProjectNumber;
    }

    public void setOverrideAllocationProjectNumber(String overrideAllocationProjectNumber) {
        this.overrideAllocationProjectNumber = overrideAllocationProjectNumber;
    }

    @OneToOne(targetEntity = UserConfigurationEntity.class, cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn(name = "EXPERIMENT_ID", referencedColumnName = "EXPERIMENT_ID")
    public UserConfigurationEntity getUserConfiguration() {
        return userConfiguration;
    }

    public void setUserConfiguration(UserConfigurationEntity userConfiguration) {
        this.userConfiguration = userConfiguration;
    }
}