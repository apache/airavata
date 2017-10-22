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


package org.apache.airavata.service.profile.commons.user.entities;

import javax.persistence.*;

@Entity
@Table(name = "CUSTOMIZED_DASHBOARD")
public class CustomizedDashboardEntity {

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

    private UserProfileEntity userProfileEntity;

    @Id
    @Column(name = "AIRAVATA_INTERNAL_USER_ID")
    public String getAiravataInternalUserId() {
        return airavataInternalUserId;
    }

    public void setAiravataInternalUserId(String airavataInternalUserId) {
        this.airavataInternalUserId = airavataInternalUserId;
    }

    @Column(name = "ENABLED_EXPERIMENT_ID")
    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    @Column(name = "ENABLED_NAME")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "ENABLED_DESCRIPTION")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "ENABLED_PROJECT")
    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    @Column(name = "ENABLED_OWNER")
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Column(name = "ENABLED_APPLICATION")
    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    @Column(name = "ENABLED_COMPUTE_RESOURCE")
    public String getComputeResource() {
        return computeResource;
    }

    public void setComputeResource(String computeResource) {
        this.computeResource = computeResource;
    }

    @Column(name = "ENABLED_JOB_NAME")
    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Column(name = "ENABLED_JOB_ID")
    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    @Column(name = "ENABLED_JOB_STATUS")
    public String getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }

    @Column(name = "ENABLED_JOB_CREATION_TIME")
    public String getJobCreationTime() {
        return jobCreationTime;
    }

    public void setJobCreationTime(String jobCreationTime) {
        this.jobCreationTime = jobCreationTime;
    }

    @Column(name = "ENABLED_NOTIFICATIONS_TO")
    public String getNotificationsTo() {
        return notificationsTo;
    }

    public void setNotificationsTo(String notificationsTo) {
        this.notificationsTo = notificationsTo;
    }

    @Column(name = "ENABLED_WORKING_DIR")
    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    @Column(name = "ENABLED_JOB_DESCRIPTION")
    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    @Column(name = "ENABLED_CREATION_TIME")
    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    @Column(name = "ENABLED_LAST_MODIFIED_TIME")
    public String getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(String lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    @Column(name = "ENABLED_WALL_TIME")
    public String getWallTime() {
        return wallTime;
    }

    public void setWallTime(String wallTime) {
        this.wallTime = wallTime;
    }

    @Column(name = "ENABLED_CPU_COUNT")
    public String getCpuCount() {
        return cpuCount;
    }

    public void setCpuCount(String cpuCount) {
        this.cpuCount = cpuCount;
    }

    @Column(name = "ENABLED_NODE_COUNT")
    public String getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(String nodeCount) {
        this.nodeCount = nodeCount;
    }

    @Column(name = "ENABLED_QUEUE")
    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    @Column(name = "ENABLED_INPUTS")
    public String getInputs() {
        return inputs;
    }

    public void setInputs(String inputs) {
        this.inputs = inputs;
    }

    @Column(name = "ENABLED_OUTPUTS")
    public String getOutputs() {
        return outputs;
    }

    public void setOutputs(String outputs) {
        this.outputs = outputs;
    }

    @Column(name = "ENABLED_STORAGE_DIR")
    public String getStorageDir() {
        return storageDir;
    }

    public void setStorageDir(String storageDir) {
        this.storageDir = storageDir;
    }

    @Column(name = "ENABLED_ERRORS")
    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    @OneToOne(targetEntity = UserProfileEntity.class, cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn(name = "AIRAVATA_INTERNAL_USER_ID", referencedColumnName = "AIRAVATA_INTERNAL_USER_ID")
    public UserProfileEntity getUserProfileEntity() {
        return userProfileEntity;
    }

    public void setUserProfileEntity(UserProfileEntity userProfileEntity) {
        this.userProfileEntity = userProfileEntity;
    }

    @Override
    public String toString() {
        return "CustomizedDashboardEntity{" +
                "airavataInternalUserId='" + airavataInternalUserId + '\'' +
                ", experimentId='" + experimentId + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", project='" + project + '\'' +
                ", owner='" + owner + '\'' +
                ", application='" + application + '\'' +
                ", computeResource='" + computeResource + '\'' +
                ", jobName='" + jobName + '\'' +
                ", jobId='" + jobId + '\'' +
                ", jobStatus='" + jobStatus + '\'' +
                ", jobCreationTime='" + jobCreationTime + '\'' +
                ", notificationsTo='" + notificationsTo + '\'' +
                ", workingDir='" + workingDir + '\'' +
                ", jobDescription='" + jobDescription + '\'' +
                ", creationTime='" + creationTime + '\'' +
                ", lastModifiedTime='" + lastModifiedTime + '\'' +
                ", wallTime='" + wallTime + '\'' +
                ", cpuCount='" + cpuCount + '\'' +
                ", nodeCount='" + nodeCount + '\'' +
                ", queue='" + queue + '\'' +
                ", inputs='" + inputs + '\'' +
                ", outputs='" + outputs + '\'' +
                ", storageDir='" + storageDir + '\'' +
                ", errors='" + errors + '\'' +
                '}';
    }
}
