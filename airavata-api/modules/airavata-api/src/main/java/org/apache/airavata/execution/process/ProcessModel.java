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
package org.apache.airavata.execution.process;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.airavata.compute.resource.model.ComputationalResourceScheduling;
import org.apache.airavata.compute.resource.model.Job;
import org.apache.airavata.core.model.ProcessState;
import org.apache.airavata.core.model.StatusModel;
import org.apache.airavata.status.model.ErrorModel;

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

    private Instant createdAt;

    private Instant updatedAt;

    private List<StatusModel<ProcessState>> processStatuses;
    private List<ErrorModel> processErrors;
    private List<Job> jobs = new ArrayList<>();

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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    private String groupResourceProfileId;
    private String applicationInterfaceId;
    private String userName;
    private boolean useUserCRPref;
    private String experimentDataDir;
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

    public String getExperimentDataDir() {
        return experimentDataDir;
    }

    public void setExperimentDataDir(String experimentDataDir) {
        this.experimentDataDir = experimentDataDir;
    }

    public String getProviderContext() {
        return providerContext;
    }

    public void setProviderContext(String providerContext) {
        this.providerContext = providerContext;
    }

    /**
     * Overload accepting the legacy {@link ComputationalResourceScheduling} type.
     * Converts it to the new {@code Map<String,Object>} representation.
     * Used by {@code ExperimentUtil.cloneProcessFromExperiment}.
     */
    @JsonIgnore
    public void setResourceSchedule(ComputationalResourceScheduling scheduling) {
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

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
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
                && Objects.equals(resourceSchedule, that.resourceSchedule);
    }

    @Override
    public int hashCode() {
        return Objects.hash(processId, experimentId, applicationId, resourceId, bindingId, resourceSchedule);
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
                + "}";
    }
}
