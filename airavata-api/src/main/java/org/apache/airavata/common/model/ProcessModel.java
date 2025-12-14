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
 * Domain model: ProcessModel
 */
public class ProcessModel {
    private String processId;
    private String experimentId;
    private long creationTime;
    private long lastUpdateTime;
    private List<ProcessStatus> processStatuses;
    private String processDetail;
    private String applicationInterfaceId;
    private String applicationDeploymentId;
    private String computeResourceId;
    private List<InputDataObjectType> processInputs;
    private List<OutputDataObjectType> processOutputs;
    private ComputationalResourceSchedulingModel processResourceSchedule;
    private List<TaskModel> tasks;
    private String taskDag;
    private List<ErrorModel> processErrors;
    private String gatewayExecutionId;
    private boolean enableEmailNotification;
    private List<String> emailAddresses;
    private String inputStorageResourceId;
    private String outputStorageResourceId;
    private String userDn;
    private boolean generateCert;
    private String experimentDataDir;
    private String userName;
    private boolean useUserCRPref;
    private String groupResourceProfileId;
    private List<ProcessWorkflow> processWorkflows;

    public ProcessModel() {}

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public List<ProcessStatus> getProcessStatuses() {
        return processStatuses;
    }

    public void setProcessStatuses(List<ProcessStatus> processStatuses) {
        this.processStatuses = processStatuses;
    }

    public String getProcessDetail() {
        return processDetail;
    }

    public void setProcessDetail(String processDetail) {
        this.processDetail = processDetail;
    }

    public String getApplicationInterfaceId() {
        return applicationInterfaceId;
    }

    public void setApplicationInterfaceId(String applicationInterfaceId) {
        this.applicationInterfaceId = applicationInterfaceId;
    }

    public String getApplicationDeploymentId() {
        return applicationDeploymentId;
    }

    public void setApplicationDeploymentId(String applicationDeploymentId) {
        this.applicationDeploymentId = applicationDeploymentId;
    }

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    public List<InputDataObjectType> getProcessInputs() {
        return processInputs;
    }

    public void setProcessInputs(List<InputDataObjectType> processInputs) {
        this.processInputs = processInputs;
    }

    public List<OutputDataObjectType> getProcessOutputs() {
        return processOutputs;
    }

    public void setProcessOutputs(List<OutputDataObjectType> processOutputs) {
        this.processOutputs = processOutputs;
    }

    public ComputationalResourceSchedulingModel getProcessResourceSchedule() {
        return processResourceSchedule;
    }

    public void setProcessResourceSchedule(ComputationalResourceSchedulingModel processResourceSchedule) {
        this.processResourceSchedule = processResourceSchedule;
    }

    public List<TaskModel> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskModel> tasks) {
        this.tasks = tasks;
    }

    public String getTaskDag() {
        return taskDag;
    }

    public void setTaskDag(String taskDag) {
        this.taskDag = taskDag;
    }

    public List<ErrorModel> getProcessErrors() {
        return processErrors;
    }

    public void setProcessErrors(List<ErrorModel> processErrors) {
        this.processErrors = processErrors;
    }

    public String getGatewayExecutionId() {
        return gatewayExecutionId;
    }

    public void setGatewayExecutionId(String gatewayExecutionId) {
        this.gatewayExecutionId = gatewayExecutionId;
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

    public String getInputStorageResourceId() {
        return inputStorageResourceId;
    }

    public void setInputStorageResourceId(String inputStorageResourceId) {
        this.inputStorageResourceId = inputStorageResourceId;
    }

    public String getOutputStorageResourceId() {
        return outputStorageResourceId;
    }

    public void setOutputStorageResourceId(String outputStorageResourceId) {
        this.outputStorageResourceId = outputStorageResourceId;
    }

    public String getUserDn() {
        return userDn;
    }

    public void setUserDn(String userDn) {
        this.userDn = userDn;
    }

    public boolean getGenerateCert() {
        return generateCert;
    }

    public void setGenerateCert(boolean generateCert) {
        this.generateCert = generateCert;
    }

    public String getExperimentDataDir() {
        return experimentDataDir;
    }

    public void setExperimentDataDir(String experimentDataDir) {
        this.experimentDataDir = experimentDataDir;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean getUseUserCRPref() {
        return useUserCRPref;
    }

    public void setUseUserCRPref(boolean useUserCRPref) {
        this.useUserCRPref = useUserCRPref;
    }

    public String getGroupResourceProfileId() {
        return groupResourceProfileId;
    }

    public void setGroupResourceProfileId(String groupResourceProfileId) {
        this.groupResourceProfileId = groupResourceProfileId;
    }

    public List<ProcessWorkflow> getProcessWorkflows() {
        return processWorkflows;
    }

    public void setProcessWorkflows(List<ProcessWorkflow> processWorkflows) {
        this.processWorkflows = processWorkflows;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessModel that = (ProcessModel) o;
        return Objects.equals(processId, that.processId)
                && Objects.equals(experimentId, that.experimentId)
                && Objects.equals(creationTime, that.creationTime)
                && Objects.equals(lastUpdateTime, that.lastUpdateTime)
                && Objects.equals(processStatuses, that.processStatuses)
                && Objects.equals(processDetail, that.processDetail)
                && Objects.equals(applicationInterfaceId, that.applicationInterfaceId)
                && Objects.equals(applicationDeploymentId, that.applicationDeploymentId)
                && Objects.equals(computeResourceId, that.computeResourceId)
                && Objects.equals(processInputs, that.processInputs)
                && Objects.equals(processOutputs, that.processOutputs)
                && Objects.equals(processResourceSchedule, that.processResourceSchedule)
                && Objects.equals(tasks, that.tasks)
                && Objects.equals(taskDag, that.taskDag)
                && Objects.equals(processErrors, that.processErrors)
                && Objects.equals(gatewayExecutionId, that.gatewayExecutionId)
                && Objects.equals(enableEmailNotification, that.enableEmailNotification)
                && Objects.equals(emailAddresses, that.emailAddresses)
                && Objects.equals(inputStorageResourceId, that.inputStorageResourceId)
                && Objects.equals(outputStorageResourceId, that.outputStorageResourceId)
                && Objects.equals(userDn, that.userDn)
                && Objects.equals(generateCert, that.generateCert)
                && Objects.equals(experimentDataDir, that.experimentDataDir)
                && Objects.equals(userName, that.userName)
                && Objects.equals(useUserCRPref, that.useUserCRPref)
                && Objects.equals(groupResourceProfileId, that.groupResourceProfileId)
                && Objects.equals(processWorkflows, that.processWorkflows);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                processId,
                experimentId,
                creationTime,
                lastUpdateTime,
                processStatuses,
                processDetail,
                applicationInterfaceId,
                applicationDeploymentId,
                computeResourceId,
                processInputs,
                processOutputs,
                processResourceSchedule,
                tasks,
                taskDag,
                processErrors,
                gatewayExecutionId,
                enableEmailNotification,
                emailAddresses,
                inputStorageResourceId,
                outputStorageResourceId,
                userDn,
                generateCert,
                experimentDataDir,
                userName,
                useUserCRPref,
                groupResourceProfileId,
                processWorkflows);
    }

    @Override
    public String toString() {
        return "ProcessModel{" + "processId=" + processId + ", experimentId=" + experimentId + ", creationTime="
                + creationTime + ", lastUpdateTime=" + lastUpdateTime + ", processStatuses=" + processStatuses
                + ", processDetail=" + processDetail + ", applicationInterfaceId=" + applicationInterfaceId
                + ", applicationDeploymentId=" + applicationDeploymentId + ", computeResourceId=" + computeResourceId
                + ", processInputs=" + processInputs + ", processOutputs=" + processOutputs
                + ", processResourceSchedule=" + processResourceSchedule + ", tasks=" + tasks + ", taskDag=" + taskDag
                + ", processErrors=" + processErrors + ", gatewayExecutionId=" + gatewayExecutionId
                + ", enableEmailNotification=" + enableEmailNotification + ", emailAddresses=" + emailAddresses
                + ", inputStorageResourceId=" + inputStorageResourceId + ", outputStorageResourceId="
                + outputStorageResourceId + ", userDn=" + userDn + ", generateCert=" + generateCert
                + ", experimentDataDir=" + experimentDataDir + ", userName=" + userName + ", useUserCRPref="
                + useUserCRPref + ", groupResourceProfileId=" + groupResourceProfileId + ", processWorkflows="
                + processWorkflows + "}";
    }
}
