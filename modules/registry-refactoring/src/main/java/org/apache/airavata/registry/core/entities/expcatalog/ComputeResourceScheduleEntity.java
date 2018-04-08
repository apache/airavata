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
*//*

package org.apache.airavata.registry.core.entities.expcatalog;

import javax.persistence.*;
import java.io.Serializable;

*/
/**
 * The persistent class for the compute_resource_schedule database table.
 *//*

@Entity
@Table(name = "COMPUTE_RESOURCE_SCHEDULE")
public class ComputeResourceScheduleEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "EXPERIMENT_ID")
    private String experimentId;

    @Column(name = "RESOURCE_HOST_ID")
    private String resourceHostId;

    @Column(name = "CPU_COUNT")
    private int totalCPUCount;

    @Column(name = "NODE_COUNT")
    private int nodeCount;

    @Column(name = "NUMBER_OF_THREADS")
    private int numberOfThreads;

    @Column(name = "QUEUE_NAME")
    private String queueName;

    @Column(name = "WALL_TIME_LIMIT")
    private int wallTimeLimit;

    @Column(name = "TOTAL_PHYSICAL_MEMORY")
    private int totalPhysicalMemory;

    @Column(name = "CHESSIS_NUMBER")
    private String chessisNumber;

    @Column(name = "STATIC_WORKING_DIRECTORY")
    private String staticWorkingDir;

    @Column(name = "OVERRIDE_LOGIN_USERNAME")
    private String overrideLoginUserName;

    @Column(name = "OVERRIDE_SCRATCH_LOCATION")
    private String overrideScratchLocation;

    @Column(name = "OVERRIDE_ALLOCATION_PROJECT_NUMBER")
    private String overrideAllocationProjectNumber;

    @OneToOne(targetEntity = UserConfigurationDataEntity.class, cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn(name = "EXPERIMENT_ID", referencedColumnName = "EXPERIMENT_ID")
    private UserConfigurationDataEntity userConfiguration;

    public ComputeResourceScheduleEntity() {
    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getResourceHostId() {
        return resourceHostId;
    }

    public void setResourceHostId(String resourceHostId) {
        this.resourceHostId = resourceHostId;
    }

    public int getTotalCPUCount() {
        return totalCPUCount;
    }

    public void setTotalCPUCount(int totalCPUCount) {
        this.totalCPUCount = totalCPUCount;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public void setNumberOfThreads(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public int getWallTimeLimit() {
        return wallTimeLimit;
    }

    public void setWallTimeLimit(int wallTimeLimit) {
        this.wallTimeLimit = wallTimeLimit;
    }

    public int getTotalPhysicalMemory() {
        return totalPhysicalMemory;
    }

    public void setTotalPhysicalMemory(int totalPhysicalMemory) {
        this.totalPhysicalMemory = totalPhysicalMemory;
    }

    public String getChessisNumber() {
        return chessisNumber;
    }

    public void setChessisNumber(String chessisNumber) {
        this.chessisNumber = chessisNumber;
    }

    public String getStaticWorkingDir() {
        return staticWorkingDir;
    }

    public void setStaticWorkingDir(String staticWorkingDir) {
        this.staticWorkingDir = staticWorkingDir;
    }

    public String getOverrideLoginUserName() {
        return overrideLoginUserName;
    }

    public void setOverrideLoginUserName(String overrideLoginUserName) {
        this.overrideLoginUserName = overrideLoginUserName;
    }

    public String getOverrideScratchLocation() {
        return overrideScratchLocation;
    }

    public void setOverrideScratchLocation(String overrideScratchLocation) {
        this.overrideScratchLocation = overrideScratchLocation;
    }

    public String getOverrideAllocationProjectNumber() {
        return overrideAllocationProjectNumber;
    }

    public void setOverrideAllocationProjectNumber(String overrideAllocationProjectNumber) {
        this.overrideAllocationProjectNumber = overrideAllocationProjectNumber;
    }

    public UserConfigurationDataEntity getUserConfiguration() {
        return userConfiguration;
    }

    public void setUserConfiguration(UserConfigurationDataEntity userConfiguration) {
        this.userConfiguration = userConfiguration;
    }
}*/
