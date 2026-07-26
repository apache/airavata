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
package org.apache.airavata.models.job;

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.models.status.JobStatus;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.job.proto.JobModel}.
 */
public record JobModel(
        String jobId,
        String taskId,
        String processId,
        String jobDescription,
        long creationTime,
        List<JobStatus> jobStatuses,
        String computeResourceConsumed,
        String jobName,
        String workingDir,
        String stdOut,
        String stdErr,
        int exitCode) {

    public JobModel {
        jobStatuses = jobStatuses == null ? List.of() : List.copyOf(jobStatuses);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String jobId = "";
        private String taskId = "";
        private String processId = "";
        private String jobDescription = "";
        private long creationTime;
        private List<JobStatus> jobStatuses = new ArrayList<>();
        private String computeResourceConsumed = "";
        private String jobName = "";
        private String workingDir = "";
        private String stdOut = "";
        private String stdErr = "";
        private int exitCode;

        private Builder() {}

        private Builder(JobModel source) {
            this.jobId = source.jobId;
            this.taskId = source.taskId;
            this.processId = source.processId;
            this.jobDescription = source.jobDescription;
            this.creationTime = source.creationTime;
            this.jobStatuses = new ArrayList<>(source.jobStatuses);
            this.computeResourceConsumed = source.computeResourceConsumed;
            this.jobName = source.jobName;
            this.workingDir = source.workingDir;
            this.stdOut = source.stdOut;
            this.stdErr = source.stdErr;
            this.exitCode = source.exitCode;
        }

        public Builder setJobId(String jobId) {
            this.jobId = jobId;
            return this;
        }

        public Builder setTaskId(String taskId) {
            this.taskId = taskId;
            return this;
        }

        public Builder setProcessId(String processId) {
            this.processId = processId;
            return this;
        }

        public Builder setJobDescription(String jobDescription) {
            this.jobDescription = jobDescription;
            return this;
        }

        public Builder setCreationTime(long creationTime) {
            this.creationTime = creationTime;
            return this;
        }

        public Builder addJobStatuses(JobStatus value) {
            this.jobStatuses.add(value);
            return this;
        }

        public Builder addAllJobStatuses(Iterable<JobStatus> values) {
            values.forEach(this.jobStatuses::add);
            return this;
        }

        public Builder clearJobStatuses() {
            this.jobStatuses.clear();
            return this;
        }

        public Builder setComputeResourceConsumed(String computeResourceConsumed) {
            this.computeResourceConsumed = computeResourceConsumed;
            return this;
        }

        public Builder setJobName(String jobName) {
            this.jobName = jobName;
            return this;
        }

        public Builder setWorkingDir(String workingDir) {
            this.workingDir = workingDir;
            return this;
        }

        public Builder setStdOut(String stdOut) {
            this.stdOut = stdOut;
            return this;
        }

        public Builder setStdErr(String stdErr) {
            this.stdErr = stdErr;
            return this;
        }

        public Builder setExitCode(int exitCode) {
            this.exitCode = exitCode;
            return this;
        }

        public JobModel build() {
            return new JobModel(
                    jobId, taskId, processId, jobDescription, creationTime, jobStatuses, computeResourceConsumed,
                    jobName, workingDir, stdOut, stdErr, exitCode);
        }
    }
}
