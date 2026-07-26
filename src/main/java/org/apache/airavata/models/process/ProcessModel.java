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
package org.apache.airavata.models.process;

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.models.application.io.InputDataObjectType;
import org.apache.airavata.models.application.io.OutputDataObjectType;
import org.apache.airavata.models.commons.ErrorModel;
import org.apache.airavata.models.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.models.status.ProcessStatus;
import org.apache.airavata.models.task.TaskModel;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.process.proto.ProcessModel}.
 *
 * @param processResourceSchedule {@code null} if unset, matching the generated
 *     {@code hasProcessResourceSchedule()} presence check.
 */
public record ProcessModel(
        String processId,
        String experimentId,
        long creationTime,
        long lastUpdateTime,
        List<ProcessStatus> processStatuses,
        String processDetail,
        String applicationInterfaceId,
        String applicationDeploymentId,
        String computeResourceId,
        List<InputDataObjectType> processInputs,
        List<OutputDataObjectType> processOutputs,
        ComputationalResourceSchedulingModel processResourceSchedule,
        List<TaskModel> tasks,
        String taskDag,
        List<ErrorModel> processErrors,
        String gatewayExecutionId,
        boolean enableEmailNotification,
        List<String> emailAddresses,
        String inputStorageResourceId,
        String outputStorageResourceId,
        String experimentDataDir,
        String userName,
        boolean useUserCrPref,
        String groupResourceProfileId) {

    public ProcessModel {
        processStatuses = processStatuses == null ? List.of() : List.copyOf(processStatuses);
        processInputs = processInputs == null ? List.of() : List.copyOf(processInputs);
        processOutputs = processOutputs == null ? List.of() : List.copyOf(processOutputs);
        tasks = tasks == null ? List.of() : List.copyOf(tasks);
        processErrors = processErrors == null ? List.of() : List.copyOf(processErrors);
        emailAddresses = emailAddresses == null ? List.of() : List.copyOf(emailAddresses);
    }

    public boolean hasProcessResourceSchedule() {
        return processResourceSchedule != null;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String processId = "";
        private String experimentId = "";
        private long creationTime;
        private long lastUpdateTime;
        private List<ProcessStatus> processStatuses = new ArrayList<>();
        private String processDetail = "";
        private String applicationInterfaceId = "";
        private String applicationDeploymentId = "";
        private String computeResourceId = "";
        private List<InputDataObjectType> processInputs = new ArrayList<>();
        private List<OutputDataObjectType> processOutputs = new ArrayList<>();
        private ComputationalResourceSchedulingModel processResourceSchedule;
        private List<TaskModel> tasks = new ArrayList<>();
        private String taskDag = "";
        private List<ErrorModel> processErrors = new ArrayList<>();
        private String gatewayExecutionId = "";
        private boolean enableEmailNotification;
        private List<String> emailAddresses = new ArrayList<>();
        private String inputStorageResourceId = "";
        private String outputStorageResourceId = "";
        private String experimentDataDir = "";
        private String userName = "";
        private boolean useUserCrPref;
        private String groupResourceProfileId = "";

        private Builder() {}

        private Builder(ProcessModel source) {
            this.processId = source.processId;
            this.experimentId = source.experimentId;
            this.creationTime = source.creationTime;
            this.lastUpdateTime = source.lastUpdateTime;
            this.processStatuses = new ArrayList<>(source.processStatuses);
            this.processDetail = source.processDetail;
            this.applicationInterfaceId = source.applicationInterfaceId;
            this.applicationDeploymentId = source.applicationDeploymentId;
            this.computeResourceId = source.computeResourceId;
            this.processInputs = new ArrayList<>(source.processInputs);
            this.processOutputs = new ArrayList<>(source.processOutputs);
            this.processResourceSchedule = source.processResourceSchedule;
            this.tasks = new ArrayList<>(source.tasks);
            this.taskDag = source.taskDag;
            this.processErrors = new ArrayList<>(source.processErrors);
            this.gatewayExecutionId = source.gatewayExecutionId;
            this.enableEmailNotification = source.enableEmailNotification;
            this.emailAddresses = new ArrayList<>(source.emailAddresses);
            this.inputStorageResourceId = source.inputStorageResourceId;
            this.outputStorageResourceId = source.outputStorageResourceId;
            this.experimentDataDir = source.experimentDataDir;
            this.userName = source.userName;
            this.useUserCrPref = source.useUserCrPref;
            this.groupResourceProfileId = source.groupResourceProfileId;
        }

        public Builder setProcessId(String processId) {
            this.processId = processId;
            return this;
        }

        public Builder setExperimentId(String experimentId) {
            this.experimentId = experimentId;
            return this;
        }

        public Builder setCreationTime(long creationTime) {
            this.creationTime = creationTime;
            return this;
        }

        public Builder setLastUpdateTime(long lastUpdateTime) {
            this.lastUpdateTime = lastUpdateTime;
            return this;
        }

        public Builder addProcessStatuses(ProcessStatus value) {
            this.processStatuses.add(value);
            return this;
        }

        public Builder addAllProcessStatuses(Iterable<ProcessStatus> values) {
            values.forEach(this.processStatuses::add);
            return this;
        }

        public Builder clearProcessStatuses() {
            this.processStatuses.clear();
            return this;
        }

        public Builder setProcessDetail(String processDetail) {
            this.processDetail = processDetail;
            return this;
        }

        public Builder setApplicationInterfaceId(String applicationInterfaceId) {
            this.applicationInterfaceId = applicationInterfaceId;
            return this;
        }

        public Builder setApplicationDeploymentId(String applicationDeploymentId) {
            this.applicationDeploymentId = applicationDeploymentId;
            return this;
        }

        public Builder setComputeResourceId(String computeResourceId) {
            this.computeResourceId = computeResourceId;
            return this;
        }

        public Builder addProcessInputs(InputDataObjectType value) {
            this.processInputs.add(value);
            return this;
        }

        public Builder addAllProcessInputs(Iterable<InputDataObjectType> values) {
            values.forEach(this.processInputs::add);
            return this;
        }

        public Builder clearProcessInputs() {
            this.processInputs.clear();
            return this;
        }

        public Builder addProcessOutputs(OutputDataObjectType value) {
            this.processOutputs.add(value);
            return this;
        }

        public Builder addAllProcessOutputs(Iterable<OutputDataObjectType> values) {
            values.forEach(this.processOutputs::add);
            return this;
        }

        public Builder clearProcessOutputs() {
            this.processOutputs.clear();
            return this;
        }

        public Builder setProcessResourceSchedule(ComputationalResourceSchedulingModel processResourceSchedule) {
            this.processResourceSchedule = processResourceSchedule;
            return this;
        }

        public Builder addTasks(TaskModel value) {
            this.tasks.add(value);
            return this;
        }

        public Builder addAllTasks(Iterable<TaskModel> values) {
            values.forEach(this.tasks::add);
            return this;
        }

        public Builder clearTasks() {
            this.tasks.clear();
            return this;
        }

        public Builder setTaskDag(String taskDag) {
            this.taskDag = taskDag;
            return this;
        }

        public Builder addProcessErrors(ErrorModel value) {
            this.processErrors.add(value);
            return this;
        }

        public Builder addAllProcessErrors(Iterable<ErrorModel> values) {
            values.forEach(this.processErrors::add);
            return this;
        }

        public Builder clearProcessErrors() {
            this.processErrors.clear();
            return this;
        }

        public Builder setGatewayExecutionId(String gatewayExecutionId) {
            this.gatewayExecutionId = gatewayExecutionId;
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

        public Builder setInputStorageResourceId(String inputStorageResourceId) {
            this.inputStorageResourceId = inputStorageResourceId;
            return this;
        }

        public Builder setOutputStorageResourceId(String outputStorageResourceId) {
            this.outputStorageResourceId = outputStorageResourceId;
            return this;
        }

        public Builder setExperimentDataDir(String experimentDataDir) {
            this.experimentDataDir = experimentDataDir;
            return this;
        }

        public Builder setUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder setUseUserCrPref(boolean useUserCrPref) {
            this.useUserCrPref = useUserCrPref;
            return this;
        }

        public Builder setGroupResourceProfileId(String groupResourceProfileId) {
            this.groupResourceProfileId = groupResourceProfileId;
            return this;
        }

        public ProcessModel build() {
            return new ProcessModel(
                    processId, experimentId, creationTime, lastUpdateTime, processStatuses, processDetail,
                    applicationInterfaceId, applicationDeploymentId, computeResourceId, processInputs,
                    processOutputs, processResourceSchedule, tasks, taskDag, processErrors, gatewayExecutionId,
                    enableEmailNotification, emailAddresses, inputStorageResourceId, outputStorageResourceId,
                    experimentDataDir, userName, useUserCrPref, groupResourceProfileId);
        }
    }
}
