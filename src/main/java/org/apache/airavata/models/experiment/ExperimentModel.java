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
package org.apache.airavata.models.experiment;

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.models.application.io.InputDataObjectType;
import org.apache.airavata.models.application.io.OutputDataObjectType;
import org.apache.airavata.models.commons.ErrorModel;
import org.apache.airavata.models.process.ProcessModel;
import org.apache.airavata.models.status.ExperimentStatus;
import org.apache.airavata.models.workflow.AiravataWorkflow;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.experiment.proto.ExperimentModel}.
 *
 * @param userConfigurationData {@code null} if unset, matching the generated
 *     {@code hasUserConfigurationData()} presence check.
 * @param workflow {@code null} if unset, matching the generated {@code hasWorkflow()} presence
 *     check.
 */
public record ExperimentModel(
        String experimentId,
        String projectId,
        String gatewayId,
        ExperimentType experimentType,
        String userName,
        String experimentName,
        long creationTime,
        String description,
        String executionId,
        String gatewayExecutionId,
        String gatewayInstanceId,
        boolean enableEmailNotification,
        List<String> emailAddresses,
        UserConfigurationDataModel userConfigurationData,
        List<InputDataObjectType> experimentInputs,
        List<OutputDataObjectType> experimentOutputs,
        List<ExperimentStatus> experimentStatus,
        List<ErrorModel> errors,
        List<ProcessModel> processes,
        AiravataWorkflow workflow,
        ExperimentCleanupStrategy cleanUpStrategy) {

    public ExperimentModel {
        emailAddresses = emailAddresses == null ? List.of() : List.copyOf(emailAddresses);
        experimentInputs = experimentInputs == null ? List.of() : List.copyOf(experimentInputs);
        experimentOutputs = experimentOutputs == null ? List.of() : List.copyOf(experimentOutputs);
        experimentStatus = experimentStatus == null ? List.of() : List.copyOf(experimentStatus);
        errors = errors == null ? List.of() : List.copyOf(errors);
        processes = processes == null ? List.of() : List.copyOf(processes);
    }

    public boolean hasUserConfigurationData() {
        return userConfigurationData != null;
    }

    public boolean hasWorkflow() {
        return workflow != null;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String experimentId = "";
        private String projectId = "";
        private String gatewayId = "";
        private ExperimentType experimentType = ExperimentType.EXPERIMENT_TYPE_UNKNOWN;
        private String userName = "";
        private String experimentName = "";
        private long creationTime;
        private String description = "";
        private String executionId = "";
        private String gatewayExecutionId = "";
        private String gatewayInstanceId = "";
        private boolean enableEmailNotification;
        private List<String> emailAddresses = new ArrayList<>();
        private UserConfigurationDataModel userConfigurationData;
        private List<InputDataObjectType> experimentInputs = new ArrayList<>();
        private List<OutputDataObjectType> experimentOutputs = new ArrayList<>();
        private List<ExperimentStatus> experimentStatus = new ArrayList<>();
        private List<ErrorModel> errors = new ArrayList<>();
        private List<ProcessModel> processes = new ArrayList<>();
        private AiravataWorkflow workflow;
        private ExperimentCleanupStrategy cleanUpStrategy = ExperimentCleanupStrategy.EXPERIMENT_CLEANUP_STRATEGY_UNKNOWN;

        private Builder() {}

        private Builder(ExperimentModel source) {
            this.experimentId = source.experimentId;
            this.projectId = source.projectId;
            this.gatewayId = source.gatewayId;
            this.experimentType = source.experimentType;
            this.userName = source.userName;
            this.experimentName = source.experimentName;
            this.creationTime = source.creationTime;
            this.description = source.description;
            this.executionId = source.executionId;
            this.gatewayExecutionId = source.gatewayExecutionId;
            this.gatewayInstanceId = source.gatewayInstanceId;
            this.enableEmailNotification = source.enableEmailNotification;
            this.emailAddresses = new ArrayList<>(source.emailAddresses);
            this.userConfigurationData = source.userConfigurationData;
            this.experimentInputs = new ArrayList<>(source.experimentInputs);
            this.experimentOutputs = new ArrayList<>(source.experimentOutputs);
            this.experimentStatus = new ArrayList<>(source.experimentStatus);
            this.errors = new ArrayList<>(source.errors);
            this.processes = new ArrayList<>(source.processes);
            this.workflow = source.workflow;
            this.cleanUpStrategy = source.cleanUpStrategy;
        }

        public Builder setExperimentId(String experimentId) {
            this.experimentId = experimentId;
            return this;
        }

        public Builder setProjectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder setGatewayId(String gatewayId) {
            this.gatewayId = gatewayId;
            return this;
        }

        public Builder setExperimentType(ExperimentType experimentType) {
            this.experimentType = experimentType;
            return this;
        }

        public Builder setUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder setExperimentName(String experimentName) {
            this.experimentName = experimentName;
            return this;
        }

        public Builder setCreationTime(long creationTime) {
            this.creationTime = creationTime;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setExecutionId(String executionId) {
            this.executionId = executionId;
            return this;
        }

        public Builder setGatewayExecutionId(String gatewayExecutionId) {
            this.gatewayExecutionId = gatewayExecutionId;
            return this;
        }

        public Builder setGatewayInstanceId(String gatewayInstanceId) {
            this.gatewayInstanceId = gatewayInstanceId;
            return this;
        }

        public Builder setEnableEmailNotification(boolean enableEmailNotification) {
            this.enableEmailNotification = enableEmailNotification;
            return this;
        }

        public Builder addEmailAddresses(String value) {
            this.emailAddresses.add(value);
            return this;
        }

        public Builder addAllEmailAddresses(Iterable<String> values) {
            values.forEach(this.emailAddresses::add);
            return this;
        }

        public Builder clearEmailAddresses() {
            this.emailAddresses.clear();
            return this;
        }

        public Builder setUserConfigurationData(UserConfigurationDataModel userConfigurationData) {
            this.userConfigurationData = userConfigurationData;
            return this;
        }

        public Builder addExperimentInputs(InputDataObjectType value) {
            this.experimentInputs.add(value);
            return this;
        }

        public Builder addAllExperimentInputs(Iterable<InputDataObjectType> values) {
            values.forEach(this.experimentInputs::add);
            return this;
        }

        public Builder clearExperimentInputs() {
            this.experimentInputs.clear();
            return this;
        }

        public Builder addExperimentOutputs(OutputDataObjectType value) {
            this.experimentOutputs.add(value);
            return this;
        }

        public Builder addAllExperimentOutputs(Iterable<OutputDataObjectType> values) {
            values.forEach(this.experimentOutputs::add);
            return this;
        }

        public Builder clearExperimentOutputs() {
            this.experimentOutputs.clear();
            return this;
        }

        public Builder addExperimentStatus(ExperimentStatus value) {
            this.experimentStatus.add(value);
            return this;
        }

        public Builder addAllExperimentStatus(Iterable<ExperimentStatus> values) {
            values.forEach(this.experimentStatus::add);
            return this;
        }

        public Builder clearExperimentStatus() {
            this.experimentStatus.clear();
            return this;
        }

        public Builder addErrors(ErrorModel value) {
            this.errors.add(value);
            return this;
        }

        public Builder addAllErrors(Iterable<ErrorModel> values) {
            values.forEach(this.errors::add);
            return this;
        }

        public Builder clearErrors() {
            this.errors.clear();
            return this;
        }

        public Builder addProcesses(ProcessModel value) {
            this.processes.add(value);
            return this;
        }

        public Builder addAllProcesses(Iterable<ProcessModel> values) {
            values.forEach(this.processes::add);
            return this;
        }

        public Builder clearProcesses() {
            this.processes.clear();
            return this;
        }

        public Builder setWorkflow(AiravataWorkflow workflow) {
            this.workflow = workflow;
            return this;
        }

        public Builder setCleanUpStrategy(ExperimentCleanupStrategy cleanUpStrategy) {
            this.cleanUpStrategy = cleanUpStrategy;
            return this;
        }

        public ExperimentModel build() {
            return new ExperimentModel(
                    experimentId, projectId, gatewayId, experimentType, userName, experimentName, creationTime,
                    description, executionId, gatewayExecutionId, gatewayInstanceId, enableEmailNotification,
                    emailAddresses, userConfigurationData, experimentInputs, experimentOutputs, experimentStatus,
                    errors, processes, workflow, cleanUpStrategy);
        }
    }
}
