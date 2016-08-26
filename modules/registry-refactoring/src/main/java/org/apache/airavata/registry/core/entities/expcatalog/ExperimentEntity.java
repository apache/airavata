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
import java.util.List;

@Entity
@Table(name = "EXPERIMENT")
public class ExperimentEntity {
    public String experimentId;
    public String projectId;
    public String gatewayId;
    public String experimentType;
    public String userName;
    public String experimentName;
    public long creationTime;
    public String description;
    public String executionId;
    public String gatewayExecutionId;
    public String gatewayInstanceId;
    public boolean enableEmailNotification;
    public List<String> emailAddresses;

    private List<ExperimentInputEntity> experimentInputs;
    private List<ExperimentOutputEntity> experimentOutputs;
    private List<ExperimentErrorEntity> experimentErrors;

    private UserConfigurationEntity userConfiguration;

    @Id
    @Column(name = "EXPERIMENT_ID")
    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    @Column(name = "PROJECT_ID")
    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    @Column(name = "GATEWAY_ID")
    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    @Column(name = "EXPERIMENT_TYPE")
    public String getExperimentType() {
        return experimentType;
    }

    public void setExperimentType(String experimentType) {
        this.experimentType = experimentType;
    }

    @Column(name = "USER_NAME")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Column(name = "EXPERIMENT_NAME")
    public String getExperimentName() {
        return experimentName;
    }

    public void setExperimentName(String experimentName) {
        this.experimentName = experimentName;
    }

    @Column(name = "CREATION_TIME")
    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    @Column(name = "DESCRIPTION")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "EXECUTION_ID")
    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    @Column(name = "GATEWAY_EXECUTION_ID")
    public String getGatewayExecutionId() {
        return gatewayExecutionId;
    }

    public void setGatewayExecutionId(String gatewayExecutionId) {
        this.gatewayExecutionId = gatewayExecutionId;
    }

    @Column(name = "GATEWAY_INSTANCE_ID")
    public String getGatewayInstanceId() {
        return gatewayInstanceId;
    }

    public void setGatewayInstanceId(String gatewayInstanceId) {
        this.gatewayInstanceId = gatewayInstanceId;
    }

    @Column(name = "ENABLE_EMAIL_NOTIFICATION")
    public boolean isEnableEmailNotification() {
        return enableEmailNotification;
    }

    public void setEnableEmailNotification(boolean enableEmailNotification) {
        this.enableEmailNotification = enableEmailNotification;
    }

    @ElementCollection
    @CollectionTable(name="EXPERIMENT_EMAIL", joinColumns = @JoinColumn(name="EXPERIMENT_ID"))
    public List<String> getEmailAddresses() {
        return emailAddresses;
    }

    public void setEmailAddresses(List<String> emailAddresses) {
        this.emailAddresses = emailAddresses;
    }

    @OneToOne(targetEntity = UserConfigurationEntity.class, cascade = CascadeType.ALL, mappedBy = "experiment")
    public UserConfigurationEntity getUserConfiguration() {
        return userConfiguration;
    }

    public void setUserConfiguration(UserConfigurationEntity userConfiguration) {
        this.userConfiguration = userConfiguration;
    }

    @OneToMany(targetEntity = ExperimentInputEntity.class, cascade = CascadeType.ALL, mappedBy = "experiment")
    public List<ExperimentInputEntity> getExperimentInputs() {
        return experimentInputs;
    }

    public void setExperimentInputs(List<ExperimentInputEntity> experimentInputs) {
        this.experimentInputs = experimentInputs;
    }

    @OneToMany(targetEntity = ExperimentOutputEntity.class, cascade = CascadeType.ALL, mappedBy = "experiment")
    public List<ExperimentOutputEntity> getExperimentOutputs() {
        return experimentOutputs;
    }

    public void setExperimentOutputs(List<ExperimentOutputEntity> experimentOutputs) {
        this.experimentOutputs = experimentOutputs;
    }

    @OneToMany(targetEntity = ExperimentErrorEntity.class, cascade = CascadeType.ALL, mappedBy = "experiment")
    public List<ExperimentErrorEntity> getExperimentErrors() {
        return experimentErrors;
    }

    public void setExperimentErrors(List<ExperimentErrorEntity> experimentErrors) {
        this.experimentErrors = experimentErrors;
    }
}