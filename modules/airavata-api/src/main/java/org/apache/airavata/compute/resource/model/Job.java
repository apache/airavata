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
package org.apache.airavata.compute.resource.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.apache.airavata.core.model.StatusModel;

/**
 * Domain model: Job
 */
public class Job {
    private String jobId;
    private String processId;
    private String jobDescription;
    private Instant createdAt;
    private List<StatusModel<JobState>> jobStatuses;
    private String computeResourceConsumed;
    private String jobName;
    private String workingDir;
    private String stdOut;
    private String stdErr;
    private int exitCode;

    public Job() {}

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<StatusModel<JobState>> getJobStatuses() {
        return jobStatuses;
    }

    public void setJobStatuses(List<StatusModel<JobState>> jobStatuses) {
        this.jobStatuses = jobStatuses;
    }

    public String getComputeResourceConsumed() {
        return computeResourceConsumed;
    }

    public void setComputeResourceConsumed(String computeResourceConsumed) {
        this.computeResourceConsumed = computeResourceConsumed;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public String getStdOut() {
        return stdOut;
    }

    public void setStdOut(String stdOut) {
        this.stdOut = stdOut;
    }

    public String getStdErr() {
        return stdErr;
    }

    public void setStdErr(String stdErr) {
        this.stdErr = stdErr;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Job that = (Job) o;
        return Objects.equals(jobId, that.jobId)
                && Objects.equals(processId, that.processId)
                && Objects.equals(jobDescription, that.jobDescription)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(jobStatuses, that.jobStatuses)
                && Objects.equals(computeResourceConsumed, that.computeResourceConsumed)
                && Objects.equals(jobName, that.jobName)
                && Objects.equals(workingDir, that.workingDir)
                && Objects.equals(stdOut, that.stdOut)
                && Objects.equals(stdErr, that.stdErr)
                && Objects.equals(exitCode, that.exitCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                jobId,
                processId,
                jobDescription,
                createdAt,
                jobStatuses,
                computeResourceConsumed,
                jobName,
                workingDir,
                stdOut,
                stdErr,
                exitCode);
    }

    @Override
    public String toString() {
        return "Job{" + "jobId=" + jobId + ", processId=" + processId + ", jobDescription="
                + jobDescription + ", createdAt=" + createdAt + ", jobStatuses=" + jobStatuses
                + ", computeResourceConsumed=" + computeResourceConsumed + ", jobName=" + jobName + ", workingDir="
                + workingDir + ", stdOut=" + stdOut + ", stdErr=" + stdErr + ", exitCode=" + exitCode + "}";
    }
}
