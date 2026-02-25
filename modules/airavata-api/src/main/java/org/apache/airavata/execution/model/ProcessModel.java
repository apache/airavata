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
package org.apache.airavata.execution.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.airavata.research.application.model.ApplicationInput;
import org.apache.airavata.research.application.model.ApplicationOutput;
import org.apache.airavata.core.model.StatusModel;
import org.apache.airavata.execution.model.ProcessState;
import org.apache.airavata.status.model.ErrorModel;
import org.apache.airavata.compute.resource.model.ComputationalResourceSchedulingModel;
import org.apache.airavata.compute.resource.model.JobModel;
import org.apache.airavata.execution.model.TaskModel;

/**
 * Domain model: ProcessModel
 *
 * <p>Resource scheduling is expressed as a plain {@code Map<String,Object>} so that no generated
 * preference or scheduling types leak into the core domain. Inputs and outputs have been removed
 * from the process level; they are carried at the experiment level only.
 */
public class ProcessModel {

    private String processId;
    private String experimentId;

    /** Identifier of the application deployment executed by this process. */
    private String applicationId;

    /** Identifier of the compute resource on which the process runs. */
    private String resourceId;

    /** Identifier of the credential/binding used for resource access. */
    private String bindingId;

    /**
     * Compute resource scheduling parameters (queue, node count, wall time, etc.) expressed as a
     * plain map so no generated scheduling types are required in the domain layer.
     */
    private Map<String, Object> resourceSchedule;

    /**
     * Process type classification (e.g., APPLICATION_RUN, JOB_SUBMISSION, DATA_MOVEMENT).
     * Tasks are represented as processes with appropriate process types.
     */
    private String processType;

    /**
     * Arbitrary metadata for the process including task-specific parameters,
     * parent process references, and job details.
     */
    private Map<String, Object> processMetadata;

    /** Epoch milliseconds when the process was created. */
    private long creationTime;

    /** Epoch milliseconds of the last status update. */
    private long lastUpdateTime;

    private List<StatusModel<ProcessState>> processStatuses;
    private List<ErrorModel> processErrors;
    private List<TaskModel> tasks = new ArrayList<>();
    private List<JobModel> jobs = new ArrayList<>();

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

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getBindingId() {
        return bindingId;
    }

    public void setBindingId(String bindingId) {
        this.bindingId = bindingId;
    }

    public Map<String, Object> getResourceSchedule() {
        return resourceSchedule;
    }

    public void setResourceSchedule(Map<String, Object> resourceSchedule) {
        this.resourceSchedule = resourceSchedule;
    }

    public String getProcessType() {
        return processType;
    }

    public void setProcessType(String processType) {
        this.processType = processType;
    }

    public Map<String, Object> getProcessMetadata() {
        return processMetadata;
    }

    public void setProcessMetadata(Map<String, Object> processMetadata) {
        this.processMetadata = processMetadata;
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

    // Legacy fields that older code still references. These should be removed once
    // all callers are updated to use the new unified resourceId / resourceSchedule fields.

    private String groupResourceProfileId;
    private String applicationInterfaceId;
    private String computeResourceCredentialToken;
    private String userName;
    private boolean useUserCRPref;
    private List<ApplicationInput> processInputs;
    private List<ApplicationOutput> processOutputs;
    private String inputStorageResourceId;
    private String outputStorageResourceId;
    private String experimentDataDir;
    private boolean enableEmailNotification;
    private List<String> emailAddresses;
    /** JSON blob used by AWS tasks to persist EC2 context between workflow steps. */
    private String providerContext;

    /** @return the compute resource ID (alias for {@link #getResourceId()}). */
    public String getComputeResourceId() {
        return resourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.resourceId = computeResourceId;
    }

    public String getGroupResourceProfileId() {
        return groupResourceProfileId;
    }

    public void setGroupResourceProfileId(String groupResourceProfileId) {
        this.groupResourceProfileId = groupResourceProfileId;
    }

    public String getApplicationInterfaceId() {
        return applicationInterfaceId;
    }

    public void setApplicationInterfaceId(String applicationInterfaceId) {
        this.applicationInterfaceId = applicationInterfaceId;
    }

    public String getComputeResourceCredentialToken() {
        return computeResourceCredentialToken;
    }

    public void setComputeResourceCredentialToken(String computeResourceCredentialToken) {
        this.computeResourceCredentialToken = computeResourceCredentialToken;
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

    public List<ApplicationInput> getProcessInputs() {
        return processInputs;
    }

    public void setProcessInputs(List<ApplicationInput> processInputs) {
        this.processInputs = processInputs;
    }

    public List<ApplicationOutput> getProcessOutputs() {
        return processOutputs;
    }

    public void setProcessOutputs(List<ApplicationOutput> processOutputs) {
        this.processOutputs = processOutputs;
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

    public String getExperimentDataDir() {
        return experimentDataDir;
    }

    public void setExperimentDataDir(String experimentDataDir) {
        this.experimentDataDir = experimentDataDir;
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

    public String getProviderContext() {
        return providerContext;
    }

    public void setProviderContext(String providerContext) {
        this.providerContext = providerContext;
    }

    /** @return resource schedule (alias for {@link #getResourceSchedule()}). */
    public Map<String, Object> getProcessResourceSchedule() {
        return resourceSchedule;
    }

    public void setProcessResourceSchedule(Map<String, Object> processResourceSchedule) {
        this.resourceSchedule = processResourceSchedule;
    }

    /**
     * Overload accepting the legacy {@link ComputationalResourceSchedulingModel} type.
     * Converts it to the new {@code Map<String,Object>} representation.
     * Used by {@code ExperimentModelUtil.cloneProcessFromExperiment}.
     */
    @JsonIgnore
    public void setProcessResourceSchedule(ComputationalResourceSchedulingModel scheduling) {
        if (scheduling == null) {
            this.resourceSchedule = null;
            return;
        }
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        if (scheduling.getResourceHostId() != null) map.put("resourceHostId", scheduling.getResourceHostId());
        if (scheduling.getQueueName() != null) map.put("queueName", scheduling.getQueueName());
        map.put("wallTimeLimit", scheduling.getWallTimeLimit());
        map.put("totalCPUCount", scheduling.getTotalCPUCount());
        map.put("nodeCount", scheduling.getNodeCount());
        map.put("numberOfThreads", scheduling.getNumberOfThreads());
        map.put("totalPhysicalMemory", scheduling.getTotalPhysicalMemory());
        if (scheduling.getOverrideLoginUserName() != null)
            map.put("overrideLoginUserName", scheduling.getOverrideLoginUserName());
        if (scheduling.getOverrideScratchLocation() != null)
            map.put("overrideScratchLocation", scheduling.getOverrideScratchLocation());
        if (scheduling.getStaticWorkingDir() != null) map.put("staticWorkingDir", scheduling.getStaticWorkingDir());
        this.resourceSchedule = map;
    }

    public List<StatusModel<ProcessState>> getProcessStatuses() {
        return processStatuses;
    }

    public void setProcessStatuses(List<StatusModel<ProcessState>> processStatuses) {
        this.processStatuses = processStatuses;
    }

    public List<ErrorModel> getProcessErrors() {
        return processErrors;
    }

    public void setProcessErrors(List<ErrorModel> processErrors) {
        this.processErrors = processErrors;
    }

    public List<TaskModel> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskModel> tasks) {
        this.tasks = tasks != null ? tasks : new ArrayList<>();
    }

    public void appendTask(TaskModel task) {
        if (this.tasks == null) {
            this.tasks = new ArrayList<>();
        }
        // Create mutable copy if the current list is immutable (e.g., from List.of())
        try {
            this.tasks.add(task);
        } catch (UnsupportedOperationException e) {
            this.tasks = new ArrayList<>(this.tasks);
            this.tasks.add(task);
        }
    }

    public List<JobModel> getJobs() {
        return jobs;
    }

    public void setJobs(List<JobModel> jobs) {
        this.jobs = jobs != null ? jobs : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessModel that = (ProcessModel) o;
        return Objects.equals(processId, that.processId)
                && Objects.equals(experimentId, that.experimentId)
                && Objects.equals(applicationId, that.applicationId)
                && Objects.equals(resourceId, that.resourceId)
                && Objects.equals(bindingId, that.bindingId)
                && Objects.equals(resourceSchedule, that.resourceSchedule)
                && Objects.equals(processStatuses, that.processStatuses)
                && Objects.equals(processErrors, that.processErrors)
                && Objects.equals(tasks, that.tasks)
                && Objects.equals(jobs, that.jobs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                processId,
                experimentId,
                applicationId,
                resourceId,
                bindingId,
                resourceSchedule,
                processStatuses,
                processErrors,
                tasks,
                jobs);
    }

    @Override
    public String toString() {
        return "ProcessModel{"
                + "processId=" + processId
                + ", experimentId=" + experimentId
                + ", applicationId=" + applicationId
                + ", resourceId=" + resourceId
                + ", bindingId=" + bindingId
                + ", resourceSchedule=" + resourceSchedule
                + ", processStatuses=" + processStatuses
                + ", processErrors=" + processErrors
                + ", tasks=" + tasks
                + ", jobs=" + jobs
                + "}";
    }
}
