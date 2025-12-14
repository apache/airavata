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
package org.apache.airavata.common.model;

import java.util.List;
import java.util.Objects;

/**
 * Domain model: ExperimentModel
 */
public class ExperimentModel {
    private String experimentId;
    private String projectId;
    private String gatewayId;
    private ExperimentType experimentType;
    private String userName;
    private String experimentName;
    private long creationTime;
    private String description;
    private String executionId;
    private String gatewayExecutionId;
    private String gatewayInstanceId;
    private boolean enableEmailNotification;
    private List<String> emailAddresses;
    private UserConfigurationDataModel userConfigurationData;
    private List<InputDataObjectType> experimentInputs;
    private List<OutputDataObjectType> experimentOutputs;
    private List<ExperimentStatus> experimentStatus;
    private List<ErrorModel> errors;
    private List<ProcessModel> processes;
    private AiravataWorkflow workflow;
    private ExperimentCleanupStrategy cleanUpStrategy;

    public ExperimentModel() {}

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

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
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

    public boolean getEnableEmailNotification() {
        return enableEmailNotification;
    }

    public void setEnableEmailNotification(boolean enableEmailNotification) {
        this.enableEmailNotification = enableEmailNotification;
    }

    public List<String> getEmailAddresses() {
        return emailAddresses;
    }

    public void setEmailAddresses(List<String> emailAddresses) {
        this.emailAddresses = emailAddresses;
    }

    public UserConfigurationDataModel getUserConfigurationData() {
        return userConfigurationData;
    }

    public void setUserConfigurationData(UserConfigurationDataModel userConfigurationData) {
        this.userConfigurationData = userConfigurationData;
    }

    public List<InputDataObjectType> getExperimentInputs() {
        return experimentInputs;
    }

    public void setExperimentInputs(List<InputDataObjectType> experimentInputs) {
        this.experimentInputs = experimentInputs;
    }

    public List<OutputDataObjectType> getExperimentOutputs() {
        return experimentOutputs;
    }

    public void setExperimentOutputs(List<OutputDataObjectType> experimentOutputs) {
        this.experimentOutputs = experimentOutputs;
    }

    public List<ExperimentStatus> getExperimentStatus() {
        return experimentStatus;
    }

    public void setExperimentStatus(List<ExperimentStatus> experimentStatus) {
        this.experimentStatus = experimentStatus;
    }

    public List<ErrorModel> getErrors() {
        return errors;
    }

    public void setErrors(List<ErrorModel> errors) {
        this.errors = errors;
    }

    public List<ProcessModel> getProcesses() {
        return processes;
    }

    public void setProcesses(List<ProcessModel> processes) {
        this.processes = processes;
    }

    public AiravataWorkflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(AiravataWorkflow workflow) {
        this.workflow = workflow;
    }

    public ExperimentCleanupStrategy getCleanUpStrategy() {
        return cleanUpStrategy;
    }

    public void setCleanUpStrategy(ExperimentCleanupStrategy cleanUpStrategy) {
        this.cleanUpStrategy = cleanUpStrategy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExperimentModel that = (ExperimentModel) o;
        return Objects.equals(experimentId, that.experimentId)
                && Objects.equals(projectId, that.projectId)
                && Objects.equals(gatewayId, that.gatewayId)
                && Objects.equals(experimentType, that.experimentType)
                && Objects.equals(userName, that.userName)
                && Objects.equals(experimentName, that.experimentName)
                && Objects.equals(creationTime, that.creationTime)
                && Objects.equals(description, that.description)
                && Objects.equals(executionId, that.executionId)
                && Objects.equals(gatewayExecutionId, that.gatewayExecutionId)
                && Objects.equals(gatewayInstanceId, that.gatewayInstanceId)
                && Objects.equals(enableEmailNotification, that.enableEmailNotification)
                && Objects.equals(emailAddresses, that.emailAddresses)
                && Objects.equals(userConfigurationData, that.userConfigurationData)
                && Objects.equals(experimentInputs, that.experimentInputs)
                && Objects.equals(experimentOutputs, that.experimentOutputs)
                && Objects.equals(experimentStatus, that.experimentStatus)
                && Objects.equals(errors, that.errors)
                && Objects.equals(processes, that.processes)
                && Objects.equals(workflow, that.workflow)
                && Objects.equals(cleanUpStrategy, that.cleanUpStrategy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                experimentId,
                projectId,
                gatewayId,
                experimentType,
                userName,
                experimentName,
                creationTime,
                description,
                executionId,
                gatewayExecutionId,
                gatewayInstanceId,
                enableEmailNotification,
                emailAddresses,
                userConfigurationData,
                experimentInputs,
                experimentOutputs,
                experimentStatus,
                errors,
                processes,
                workflow,
                cleanUpStrategy);
    }

    @Override
    public String toString() {
        return "ExperimentModel{" + "experimentId=" + experimentId + ", projectId=" + projectId + ", gatewayId="
                + gatewayId + ", experimentType=" + experimentType + ", userName=" + userName + ", experimentName="
                + experimentName + ", creationTime=" + creationTime + ", description=" + description + ", executionId="
                + executionId + ", gatewayExecutionId=" + gatewayExecutionId + ", gatewayInstanceId="
                + gatewayInstanceId + ", enableEmailNotification=" + enableEmailNotification + ", emailAddresses="
                + emailAddresses + ", userConfigurationData=" + userConfigurationData + ", experimentInputs="
                + experimentInputs + ", experimentOutputs=" + experimentOutputs + ", experimentStatus="
                + experimentStatus + ", errors=" + errors + ", processes=" + processes + ", workflow=" + workflow
                + ", cleanUpStrategy=" + cleanUpStrategy + "}";
    }
}
