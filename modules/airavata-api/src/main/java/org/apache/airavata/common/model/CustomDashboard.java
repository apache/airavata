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

import java.util.Objects;

/**
 * Domain model: CustomDashboard
 */
public class CustomDashboard {
    private String airavataInternalUserId;
    private String experimentId;
    private String name;
    private String description;
    private String project;
    private String owner;
    private String application;
    private String computeResource;
    private String jobName;
    private String jobId;
    private String jobStatus;
    private String jobCreationTime;
    private String notificationsTo;
    private String workingDir;
    private String jobDescription;
    private String creationTime;
    private String lastModifiedTime;
    private String wallTime;
    private String cpuCount;
    private String nodeCount;
    private String queue;
    private String inputs;
    private String outputs;
    private String storageDir;
    private String errors;

    public CustomDashboard() {}

    public String getAiravataInternalUserId() {
        return airavataInternalUserId;
    }

    public void setAiravataInternalUserId(String airavataInternalUserId) {
        this.airavataInternalUserId = airavataInternalUserId;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getComputeResource() {
        return computeResource;
    }

    public void setComputeResource(String computeResource) {
        this.computeResource = computeResource;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }

    public String getJobCreationTime() {
        return jobCreationTime;
    }

    public void setJobCreationTime(String jobCreationTime) {
        this.jobCreationTime = jobCreationTime;
    }

    public String getNotificationsTo() {
        return notificationsTo;
    }

    public void setNotificationsTo(String notificationsTo) {
        this.notificationsTo = notificationsTo;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    public String getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(String lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public String getWallTime() {
        return wallTime;
    }

    public void setWallTime(String wallTime) {
        this.wallTime = wallTime;
    }

    public String getCpuCount() {
        return cpuCount;
    }

    public void setCpuCount(String cpuCount) {
        this.cpuCount = cpuCount;
    }

    public String getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(String nodeCount) {
        this.nodeCount = nodeCount;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public String getInputs() {
        return inputs;
    }

    public void setInputs(String inputs) {
        this.inputs = inputs;
    }

    public String getOutputs() {
        return outputs;
    }

    public void setOutputs(String outputs) {
        this.outputs = outputs;
    }

    public String getStorageDir() {
        return storageDir;
    }

    public void setStorageDir(String storageDir) {
        this.storageDir = storageDir;
    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomDashboard that = (CustomDashboard) o;
        return Objects.equals(airavataInternalUserId, that.airavataInternalUserId)
                && Objects.equals(experimentId, that.experimentId)
                && Objects.equals(name, that.name)
                && Objects.equals(description, that.description)
                && Objects.equals(project, that.project)
                && Objects.equals(owner, that.owner)
                && Objects.equals(application, that.application)
                && Objects.equals(computeResource, that.computeResource)
                && Objects.equals(jobName, that.jobName)
                && Objects.equals(jobId, that.jobId)
                && Objects.equals(jobStatus, that.jobStatus)
                && Objects.equals(jobCreationTime, that.jobCreationTime)
                && Objects.equals(notificationsTo, that.notificationsTo)
                && Objects.equals(workingDir, that.workingDir)
                && Objects.equals(jobDescription, that.jobDescription)
                && Objects.equals(creationTime, that.creationTime)
                && Objects.equals(lastModifiedTime, that.lastModifiedTime)
                && Objects.equals(wallTime, that.wallTime)
                && Objects.equals(cpuCount, that.cpuCount)
                && Objects.equals(nodeCount, that.nodeCount)
                && Objects.equals(queue, that.queue)
                && Objects.equals(inputs, that.inputs)
                && Objects.equals(outputs, that.outputs)
                && Objects.equals(storageDir, that.storageDir)
                && Objects.equals(errors, that.errors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                airavataInternalUserId,
                experimentId,
                name,
                description,
                project,
                owner,
                application,
                computeResource,
                jobName,
                jobId,
                jobStatus,
                jobCreationTime,
                notificationsTo,
                workingDir,
                jobDescription,
                creationTime,
                lastModifiedTime,
                wallTime,
                cpuCount,
                nodeCount,
                queue,
                inputs,
                outputs,
                storageDir,
                errors);
    }

    @Override
    public String toString() {
        return "CustomDashboard{" + "airavataInternalUserId=" + airavataInternalUserId + ", experimentId="
                + experimentId + ", name=" + name + ", description=" + description + ", project=" + project + ", owner="
                + owner + ", application=" + application + ", computeResource=" + computeResource + ", jobName="
                + jobName + ", jobId=" + jobId + ", jobStatus=" + jobStatus + ", jobCreationTime=" + jobCreationTime
                + ", notificationsTo=" + notificationsTo + ", workingDir=" + workingDir + ", jobDescription="
                + jobDescription + ", creationTime=" + creationTime + ", lastModifiedTime=" + lastModifiedTime
                + ", wallTime=" + wallTime + ", cpuCount=" + cpuCount + ", nodeCount=" + nodeCount + ", queue=" + queue
                + ", inputs=" + inputs + ", outputs=" + outputs + ", storageDir=" + storageDir + ", errors=" + errors
                + "}";
    }
}
