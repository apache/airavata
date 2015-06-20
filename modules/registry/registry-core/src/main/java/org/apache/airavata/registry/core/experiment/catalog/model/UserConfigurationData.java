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
package org.apache.airavata.registry.core.experiment.catalog.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;

@Entity
@Table(name = "USER_CONFIGURATION_DATA")
public class UserConfigurationData {
    private final static Logger logger = LoggerFactory.getLogger(UserConfigurationData.class);
    private String experimentId;
    private Boolean airavataAutoSchedule;
    private Boolean overrideManualScheduledParams;
    private Boolean shareExperimentPublically;
    private Boolean throttleResources;
    private String userDn;
    private Boolean generateCert;
    private String resourceHostId;
    private Integer totalCpuCount;
    private Integer nodeCount;
    private Integer numberOfThreads;
    private String queueName;
    private Integer wallTimeLimit;
    private Integer totalPhysicalMemory;
    private Experiment experiment;

    @Id
    @Column(name = "EXPERIMENT_ID")
    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    @Basic
    @Column(name = "AIRAVATA_AUTO_SCHEDULE")
    public Boolean getAiravataAutoSchedule() {
        return airavataAutoSchedule;
    }

    public void setAiravataAutoSchedule(Boolean airavataAutoSchedule) {
        this.airavataAutoSchedule = airavataAutoSchedule;
    }

    @Basic
    @Column(name = "OVERRIDE_MANUAL_SCHEDULED_PARAMS")
    public Boolean getOverrideManualScheduledParams() {
        return overrideManualScheduledParams;
    }

    public void setOverrideManualScheduledParams(Boolean overrideManualScheduledParams) {
        this.overrideManualScheduledParams = overrideManualScheduledParams;
    }

    @Basic
    @Column(name = "SHARE_EXPERIMENT_PUBLICALLY")
    public Boolean getShareExperimentPublically() {
        return shareExperimentPublically;
    }

    public void setShareExperimentPublically(Boolean shareExperimentPublically) {
        this.shareExperimentPublically = shareExperimentPublically;
    }

    @Basic
    @Column(name = "THROTTLE_RESOURCES")
    public Boolean getThrottleResources() {
        return throttleResources;
    }

    public void setThrottleResources(Boolean throttleResources) {
        this.throttleResources = throttleResources;
    }

    @Basic
    @Column(name = "USER_DN")
    public String getUserDn() {
        return userDn;
    }

    public void setUserDn(String userDn) {
        this.userDn = userDn;
    }

    @Basic
    @Column(name = "GENERATE_CERT")
    public Boolean getGenerateCert() {
        return generateCert;
    }

    public void setGenerateCert(Boolean generateCert) {
        this.generateCert = generateCert;
    }

    @Basic
    @Column(name = "RESOURCE_HOST_ID")
    public String getResourceHostId() {
        return resourceHostId;
    }

    public void setResourceHostId(String resourceHostId) {
        this.resourceHostId = resourceHostId;
    }

    @Basic
    @Column(name = "TOTAL_CPU_COUNT")
    public Integer getTotalCpuCount() {
        return totalCpuCount;
    }

    public void setTotalCpuCount(Integer totalCpuCount) {
        this.totalCpuCount = totalCpuCount;
    }

    @Basic
    @Column(name = "NODE_COUNT")
    public Integer getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(Integer nodeCount) {
        this.nodeCount = nodeCount;
    }

    @Basic
    @Column(name = "NUMBER_OF_THREADS")
    public Integer getNumberOfThreads() {
        return numberOfThreads;
    }

    public void setNumberOfThreads(Integer numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

    @Basic
    @Column(name = "QUEUE_NAME")
    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    @Basic
    @Column(name = "WALL_TIME_LIMIT")
    public Integer getWallTimeLimit() {
        return wallTimeLimit;
    }

    public void setWallTimeLimit(Integer wallTimeLimit) {
        this.wallTimeLimit = wallTimeLimit;
    }

    @Basic
    @Column(name = "TOTAL_PHYSICAL_MEMORY")
    public Integer getTotalPhysicalMemory() {
        return totalPhysicalMemory;
    }

    public void setTotalPhysicalMemory(Integer totalPhysicalMemory) {
        this.totalPhysicalMemory = totalPhysicalMemory;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        UserConfigurationData that = (UserConfigurationData) o;
//
//        if (airavataAutoSchedule != null ? !airavataAutoSchedule.equals(that.airavataAutoSchedule) : that.airavataAutoSchedule != null)
//            return false;
//        if (experimentId != null ? !experimentId.equals(that.experimentId) : that.experimentId != null) return false;
//        if (generateCert != null ? !generateCert.equals(that.generateCert) : that.generateCert != null) return false;
//        if (nodeCount != null ? !nodeCount.equals(that.nodeCount) : that.nodeCount != null) return false;
//        if (numberOfThreads != null ? !numberOfThreads.equals(that.numberOfThreads) : that.numberOfThreads != null)
//            return false;
//        if (overrideManualScheduledParams != null ? !overrideManualScheduledParams.equals(that.overrideManualScheduledParams) : that.overrideManualScheduledParams != null)
//            return false;
//        if (queueName != null ? !queueName.equals(that.queueName) : that.queueName != null) return false;
//        if (resourceHostId != null ? !resourceHostId.equals(that.resourceHostId) : that.resourceHostId != null)
//            return false;
//        if (shareExperimentPublically != null ? !shareExperimentPublically.equals(that.shareExperimentPublically) : that.shareExperimentPublically != null)
//            return false;
//        if (throttleResources != null ? !throttleResources.equals(that.throttleResources) : that.throttleResources != null)
//            return false;
//        if (totalCpuCount != null ? !totalCpuCount.equals(that.totalCpuCount) : that.totalCpuCount != null)
//            return false;
//        if (totalPhysicalMemory != null ? !totalPhysicalMemory.equals(that.totalPhysicalMemory) : that.totalPhysicalMemory != null)
//            return false;
//        if (userDn != null ? !userDn.equals(that.userDn) : that.userDn != null) return false;
//        if (wallTimeLimit != null ? !wallTimeLimit.equals(that.wallTimeLimit) : that.wallTimeLimit != null)
//            return false;
//
//        return true;
//    }
//
//    @Override
//    public int hashCode() {
//        int result = experimentId != null ? experimentId.hashCode() : 0;
//        result = 31 * result + (airavataAutoSchedule != null ? airavataAutoSchedule.hashCode() : 0);
//        result = 31 * result + (overrideManualScheduledParams != null ? overrideManualScheduledParams.hashCode() : 0);
//        result = 31 * result + (shareExperimentPublically != null ? shareExperimentPublically.hashCode() : 0);
//        result = 31 * result + (throttleResources != null ? throttleResources.hashCode() : 0);
//        result = 31 * result + (userDn != null ? userDn.hashCode() : 0);
//        result = 31 * result + (generateCert != null ? generateCert.hashCode() : 0);
//        result = 31 * result + (resourceHostId != null ? resourceHostId.hashCode() : 0);
//        result = 31 * result + (totalCpuCount != null ? totalCpuCount.hashCode() : 0);
//        result = 31 * result + (nodeCount != null ? nodeCount.hashCode() : 0);
//        result = 31 * result + (numberOfThreads != null ? numberOfThreads.hashCode() : 0);
//        result = 31 * result + (queueName != null ? queueName.hashCode() : 0);
//        result = 31 * result + (wallTimeLimit != null ? wallTimeLimit.hashCode() : 0);
//        result = 31 * result + (totalPhysicalMemory != null ? totalPhysicalMemory.hashCode() : 0);
//        return result;
//    }

    @OneToOne
    @JoinColumn(name = "EXPERIMENT_ID", referencedColumnName = "EXPERIMENT_ID", nullable = false)
    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experimentByExperimentId) {
        this.experiment = experimentByExperimentId;
    }
}