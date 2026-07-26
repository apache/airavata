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
package org.apache.airavata.models.user;

/**
 * Plain-POJO replacement for the generated {@code org.apache.airavata.model.user.proto.CustomDashboard}.
 */
public record CustomDashboard(
        String airavataInternalUserId,
        String experimentId,
        String name,
        String description,
        String project,
        String owner,
        String application,
        String computeResource,
        String jobName,
        String jobId,
        String jobStatus,
        String jobCreationTime,
        String notificationsTo,
        String workingDir,
        String jobDescription,
        String creationTime,
        String lastModifiedTime,
        String wallTime,
        String cpuCount,
        String nodeCount,
        String queue,
        String inputs,
        String outputs,
        String storageDir,
        String errors) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String airavataInternalUserId = "";
        private String experimentId = "";
        private String name = "";
        private String description = "";
        private String project = "";
        private String owner = "";
        private String application = "";
        private String computeResource = "";
        private String jobName = "";
        private String jobId = "";
        private String jobStatus = "";
        private String jobCreationTime = "";
        private String notificationsTo = "";
        private String workingDir = "";
        private String jobDescription = "";
        private String creationTime = "";
        private String lastModifiedTime = "";
        private String wallTime = "";
        private String cpuCount = "";
        private String nodeCount = "";
        private String queue = "";
        private String inputs = "";
        private String outputs = "";
        private String storageDir = "";
        private String errors = "";

        private Builder() {}

        private Builder(CustomDashboard source) {
            this.airavataInternalUserId = source.airavataInternalUserId;
            this.experimentId = source.experimentId;
            this.name = source.name;
            this.description = source.description;
            this.project = source.project;
            this.owner = source.owner;
            this.application = source.application;
            this.computeResource = source.computeResource;
            this.jobName = source.jobName;
            this.jobId = source.jobId;
            this.jobStatus = source.jobStatus;
            this.jobCreationTime = source.jobCreationTime;
            this.notificationsTo = source.notificationsTo;
            this.workingDir = source.workingDir;
            this.jobDescription = source.jobDescription;
            this.creationTime = source.creationTime;
            this.lastModifiedTime = source.lastModifiedTime;
            this.wallTime = source.wallTime;
            this.cpuCount = source.cpuCount;
            this.nodeCount = source.nodeCount;
            this.queue = source.queue;
            this.inputs = source.inputs;
            this.outputs = source.outputs;
            this.storageDir = source.storageDir;
            this.errors = source.errors;
        }

        public Builder setAiravataInternalUserId(String airavataInternalUserId) {
            this.airavataInternalUserId = airavataInternalUserId;
            return this;
        }

        public Builder setExperimentId(String experimentId) {
            this.experimentId = experimentId;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setProject(String project) {
            this.project = project;
            return this;
        }

        public Builder setOwner(String owner) {
            this.owner = owner;
            return this;
        }

        public Builder setApplication(String application) {
            this.application = application;
            return this;
        }

        public Builder setComputeResource(String computeResource) {
            this.computeResource = computeResource;
            return this;
        }

        public Builder setJobName(String jobName) {
            this.jobName = jobName;
            return this;
        }

        public Builder setJobId(String jobId) {
            this.jobId = jobId;
            return this;
        }

        public Builder setJobStatus(String jobStatus) {
            this.jobStatus = jobStatus;
            return this;
        }

        public Builder setJobCreationTime(String jobCreationTime) {
            this.jobCreationTime = jobCreationTime;
            return this;
        }

        public Builder setNotificationsTo(String notificationsTo) {
            this.notificationsTo = notificationsTo;
            return this;
        }

        public Builder setWorkingDir(String workingDir) {
            this.workingDir = workingDir;
            return this;
        }

        public Builder setJobDescription(String jobDescription) {
            this.jobDescription = jobDescription;
            return this;
        }

        public Builder setCreationTime(String creationTime) {
            this.creationTime = creationTime;
            return this;
        }

        public Builder setLastModifiedTime(String lastModifiedTime) {
            this.lastModifiedTime = lastModifiedTime;
            return this;
        }

        public Builder setWallTime(String wallTime) {
            this.wallTime = wallTime;
            return this;
        }

        public Builder setCpuCount(String cpuCount) {
            this.cpuCount = cpuCount;
            return this;
        }

        public Builder setNodeCount(String nodeCount) {
            this.nodeCount = nodeCount;
            return this;
        }

        public Builder setQueue(String queue) {
            this.queue = queue;
            return this;
        }

        public Builder setInputs(String inputs) {
            this.inputs = inputs;
            return this;
        }

        public Builder setOutputs(String outputs) {
            this.outputs = outputs;
            return this;
        }

        public Builder setStorageDir(String storageDir) {
            this.storageDir = storageDir;
            return this;
        }

        public Builder setErrors(String errors) {
            this.errors = errors;
            return this;
        }

        public CustomDashboard build() {
            return new CustomDashboard(
                    airavataInternalUserId, experimentId, name, description, project, owner, application,
                    computeResource, jobName, jobId, jobStatus, jobCreationTime, notificationsTo, workingDir,
                    jobDescription, creationTime, lastModifiedTime, wallTime, cpuCount, nodeCount, queue, inputs,
                    outputs, storageDir, errors);
        }
    }
}
