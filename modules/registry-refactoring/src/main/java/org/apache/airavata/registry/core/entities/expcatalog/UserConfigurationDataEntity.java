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
import java.io.Serializable;

/**
 * The persistent class for the user_configuration_data database table.
 */
@Entity
@Table(name = "USER_CONFIGURATION_DATA")
public class UserConfigurationDataEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "EXPERIMENT_ID")
    private String experimentId;

    @Column(name = "AIRAVATA_AUTO_SCHEDULE")
    private boolean airavataAutoSchedule;

    @Column(name = "OVERRIDE_MANUAL_SCHEDULED_PARAMS")
    private boolean overrideManualScheduledParams;

    @Column(name = "SHARE_EXPERIMENT_PUBLICALLY")
    private boolean shareExperimentPublicly;

    @Column(name = "THROTTLE_RESOURCES")
    private boolean throttleResources;

    @Column(name = "USER_DN")
    private String userDN;

    @Column(name = "GENERATE_CERT")
    private boolean generateCert;

    @Column(name = "RESOURCE_HOST_ID")
    private String resourceHostId;

    @Column(name = "TOTAL_CPU_COUNT")
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

    @Column(name = "STATIC_WORKING_DIR")
    private String staticWorkingDir;

    @Column(name = "OVERRIDE_LOGIN_USER_NAME")
    private String overrideLoginUserName;

    @Column(name = "OVERRIDE_SCRATCH_LOCATION")
    private String overrideScratchLocation;

    @Column(name = "OVERRIDE_ALLOCATION_PROJECT_NUMBER")
    private String overrideAllocationProjectNumber;

    @Column(name = "STORAGE_RESOURCE_ID")
    private String storageId;

    @Column(name = "EXPERIMENT_DATA_DIR", length = 512)
    private String experimentDataDir;

    @Column(name = "GROUP_RESOURCE_PROFILE_ID")
    private String groupResourceProfileId;

    @Column(name = "IS_USE_USER_CR_PREF")
    private boolean useUserCRPref;

    @OneToOne(targetEntity = ExperimentEntity.class, cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn(name = "EXPERIMENT_ID", referencedColumnName = "EXPERIMENT_ID")
    private ExperimentEntity experiment;

    public UserConfigurationDataEntity() {
    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public boolean isAiravataAutoSchedule() {
        return airavataAutoSchedule;
    }

    public void setAiravataAutoSchedule(boolean airavataAutoSchedule) {
        this.airavataAutoSchedule = airavataAutoSchedule;
    }

    public boolean isOverrideManualScheduledParams() {
        return overrideManualScheduledParams;
    }

    public void setOverrideManualScheduledParams(boolean overrideManualScheduledParams) {
        this.overrideManualScheduledParams = overrideManualScheduledParams;
    }

    public boolean isShareExperimentPublicly() {
        return shareExperimentPublicly;
    }

    public void setShareExperimentPublicly(boolean shareExperimentPublicly) {
        this.shareExperimentPublicly = shareExperimentPublicly;
    }

    public boolean isThrottleResources() {
        return throttleResources;
    }

    public void setThrottleResources(boolean throttleResources) {
        this.throttleResources = throttleResources;
    }

    public String getUserDN() {
        return userDN;
    }

    public void setUserDN(String userDN) {
        this.userDN = userDN;
    }

    public boolean isGenerateCert() {
        return generateCert;
    }

    public void setGenerateCert(boolean generateCert) {
        this.generateCert = generateCert;
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

    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }

    public String getExperimentDataDir() {
        return experimentDataDir;
    }

    public void setExperimentDataDir(String experimentDataDir) {
        this.experimentDataDir = experimentDataDir;
    }

    public String getGroupResourceProfileId() {
        return groupResourceProfileId;
    }

    public void setGroupResourceProfileId(String groupResourceProfileId) {
        this.groupResourceProfileId = groupResourceProfileId;
    }

    public boolean isUseUserCRPref() {
        return useUserCRPref;
    }

    public void setUseUserCRPref(boolean useUserCRPref) {
        this.useUserCRPref = useUserCRPref;
    }

    public ExperimentEntity getExperiment() {
        return experiment;
    }

    public void setExperiment(ExperimentEntity experiment) {
        this.experiment = experiment;
    }
}