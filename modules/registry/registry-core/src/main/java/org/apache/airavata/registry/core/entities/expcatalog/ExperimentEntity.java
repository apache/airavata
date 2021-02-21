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

import org.apache.airavata.model.experiment.ExperimentType;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * The persistent class for the experiment database table.
 */
@Entity
@Table(name = "EXPERIMENT")
public class ExperimentEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "EXPERIMENT_ID")
    public String experimentId;

    @Column(name = "PROJECT_ID")
    public String projectId;

    @Column(name = "GATEWAY_ID")
    public String gatewayId;

    @Column(name = "EXPERIMENT_TYPE")
    @Enumerated(EnumType.STRING)
    public ExperimentType experimentType;

    @Column(name = "USER_NAME")
    public String userName;

    @Column(name = "EXPERIMENT_NAME")
    public String experimentName;

    @Column(name = "CREATION_TIME")
    public Timestamp creationTime;

    @Column(name = "DESCRIPTION")
    public String description;

    @Column(name = "EXECUTION_ID")
    public String executionId;

    @Column(name = "GATEWAY_EXECUTION_ID")
    public String gatewayExecutionId;

    @Column(name = "GATEWAY_INSTANCE_ID")
    public String gatewayInstanceId;

    @Column(name = "ENABLE_EMAIL_NOTIFICATION")
    public boolean enableEmailNotification;

    @Lob
    @Column(name = "EMAIL_ADDRESSES")
    public String emailAddresses;

    @OneToOne(targetEntity = UserConfigurationDataEntity.class, cascade = CascadeType.ALL,
            mappedBy = "experiment", fetch = FetchType.EAGER)
    private UserConfigurationDataEntity userConfigurationData;

    @OneToMany(targetEntity = ExperimentInputEntity.class, cascade = CascadeType.ALL,
            mappedBy = "experiment", fetch = FetchType.EAGER)
    private List<ExperimentInputEntity> experimentInputs;

    @OneToMany(targetEntity = ExperimentOutputEntity.class, cascade = CascadeType.ALL,
            mappedBy = "experiment", fetch = FetchType.EAGER)
    private List<ExperimentOutputEntity> experimentOutputs;

    @OneToMany(targetEntity = ExperimentStatusEntity.class, cascade = CascadeType.ALL,
            mappedBy = "experiment", fetch = FetchType.EAGER)
    @OrderBy("timeOfStateChange ASC")
    private List<ExperimentStatusEntity> experimentStatus;

    @OneToMany(targetEntity = ExperimentErrorEntity.class, cascade = CascadeType.ALL,
            mappedBy = "experiment", fetch = FetchType.EAGER)
    private List<ExperimentErrorEntity> errors;

    @OneToMany(targetEntity = ProcessEntity.class, cascade = CascadeType.ALL,
            mappedBy = "experiment", fetch = FetchType.EAGER)
    private List<ProcessEntity> processes;

    public ExperimentEntity() {
    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public ExperimentType getExperimentType() {
        return experimentType;
    }

    public void setExperimentType(ExperimentType experimentType) {
        this.experimentType = experimentType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public void setExperimentName(String experimentName) {
        this.experimentName = experimentName;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getGatewayExecutionId() {
        return gatewayExecutionId;
    }

    public void setGatewayExecutionId(String gatewayExecutionId) {
        this.gatewayExecutionId = gatewayExecutionId;
    }

    public String getGatewayInstanceId() {
        return gatewayInstanceId;
    }

    public void setGatewayInstanceId(String gatewayInstanceId) {
        this.gatewayInstanceId = gatewayInstanceId;
    }

    public boolean isEnableEmailNotification() {
        return enableEmailNotification;
    }

    public void setEnableEmailNotification(boolean enableEmailNotification) {
        this.enableEmailNotification = enableEmailNotification;
    }

    public String getEmailAddresses() {
        return emailAddresses;
    }

    public void setEmailAddresses(String emailAddresses) {
        this.emailAddresses = emailAddresses;
    }

    public UserConfigurationDataEntity getUserConfigurationData() {
        return userConfigurationData;
    }

    public void setUserConfigurationData(UserConfigurationDataEntity userConfiguration) {
        this.userConfigurationData = userConfiguration;
    }

    public List<ExperimentInputEntity> getExperimentInputs() {
        return experimentInputs;
    }

    public void setExperimentInputs(List<ExperimentInputEntity> experimentInputs) {
        this.experimentInputs = experimentInputs;
    }

    public List<ExperimentOutputEntity> getExperimentOutputs() {
        return experimentOutputs;
    }

    public void setExperimentOutputs(List<ExperimentOutputEntity> experimentOutputs) {
        this.experimentOutputs = experimentOutputs;
    }

    public List<ExperimentErrorEntity> getErrors() {
        return errors;
    }

    public void setErrors(List<ExperimentErrorEntity> errors) {
        this.errors = errors;
    }

    public List<ExperimentStatusEntity> getExperimentStatus() {
        return experimentStatus;
    }

    public void setExperimentStatus(List<ExperimentStatusEntity> experimentStatus) {
        this.experimentStatus = experimentStatus;
    }

    public List<ProcessEntity> getProcesses() {
        return processes;
    }

    public void setProcesses(List<ProcessEntity> processes) {
        this.processes = processes;
    }
}
