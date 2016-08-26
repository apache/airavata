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
@Table(name = "USER_CONFIGURATION_ENTITY")
public class UserConfigurationEntity {
    private String experimentId;
    private boolean airavataAutoSchedule;
    private boolean overrideManualScheduledParams;
    private boolean throttleResources;
    private String userDN;
    private boolean generateCert;
    private String storageId;
    private String experimentDataDir;

    private ComputeResourceSchedulingEntity computeResourceSchedulingEntity;
    private ExperimentEntity experiment;

    @Id
    @Column(name = "EXPERIMENT_ID")
    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    @Column(name = "AIRAVATA_AUTO_SCHEDULE")
    public boolean isAiravataAutoSchedule() {
        return airavataAutoSchedule;
    }

    public void setAiravataAutoSchedule(boolean airavataAutoSchedule) {
        this.airavataAutoSchedule = airavataAutoSchedule;
    }

    @Column(name = "OVERRIDE_MANUAL_SCHEDULED_PARAMS")
    public boolean isOverrideManualScheduledParams() {
        return overrideManualScheduledParams;
    }

    public void setOverrideManualScheduledParams(boolean overrideManualScheduledParams) {
        this.overrideManualScheduledParams = overrideManualScheduledParams;
    }

    @Column(name = "THROTTLE_RESOURCE")
    public boolean isThrottleResources() {
        return throttleResources;
    }

    public void setThrottleResources(boolean throttleResources) {
        this.throttleResources = throttleResources;
    }

    @Column(name = "USER_DN")
    public String getUserDN() {
        return userDN;
    }

    public void setUserDN(String userDN) {
        this.userDN = userDN;
    }

    @Column(name = "GENERATE_CERT")
    public boolean isGenerateCert() {
        return generateCert;
    }

    public void setGenerateCert(boolean generateCert) {
        this.generateCert = generateCert;
    }

    @Column(name = "STORAGE_ID")
    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }

    @Column(name = "EXPERIMENT_DATA_DIR")
    public String getExperimentDataDir() {
        return experimentDataDir;
    }

    public void setExperimentDataDir(String experimentDataDir) {
        this.experimentDataDir = experimentDataDir;
    }

    @OneToOne(targetEntity = ComputeResourceSchedulingEntity.class, cascade = CascadeType.ALL, mappedBy = "userConfiguration")
    public ComputeResourceSchedulingEntity getComputeResourceSchedulingEntity() {
        return computeResourceSchedulingEntity;
    }

    public void setComputeResourceSchedulingEntity(ComputeResourceSchedulingEntity computeResourceSchedulingEntity) {
        this.computeResourceSchedulingEntity = computeResourceSchedulingEntity;
    }

    @OneToOne(targetEntity = ExperimentEntity.class, cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn(name = "EXPERIMENT_ID", referencedColumnName = "EXPERIMENT_ID")
    public ExperimentEntity getExperiment() {
        return experiment;
    }

    public void setExperiment(ExperimentEntity experiment) {
        this.experiment = experiment;
    }
}