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
package org.apache.airavata.compute.resource.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import org.apache.airavata.compute.resource.model.JobState;
import org.apache.airavata.core.model.StatusModel;
import org.apache.airavata.execution.entity.ProcessEntity;

@Entity
@Table(
        name = "job",
        indexes = {
            @Index(name = "idx_job_process_id", columnList = "process_id"),
            @Index(name = "idx_job_name", columnList = "job_name")
        })
public class JobEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "job_id", nullable = false, length = 255)
    private String jobId;

    @Column(name = "process_id", nullable = false, length = 255)
    private String processId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_id", insertable = false, updatable = false)
    private ProcessEntity process;

    @Column(name = "job_name", length = 255)
    private String jobName;

    @Column(name = "working_dir", columnDefinition = "MEDIUMTEXT")
    private String workingDir;

    @Column(name = "job_description", columnDefinition = "MEDIUMTEXT")
    private String jobDescription;

    @Column(name = "std_out", columnDefinition = "MEDIUMTEXT")
    private String stdOut;

    @Column(name = "std_err", columnDefinition = "MEDIUMTEXT")
    private String stdErr;

    @Column(name = "exit_code")
    private int exitCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "compute_resource_consumed", length = 255)
    private String computeResourceConsumed;

    /** Job status history loaded from the EVENT table. Not persisted as a JSON column. */
    @Transient
    private List<StatusModel<JobState>> jobStatuses;

    public JobEntity() {}

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

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getComputeResourceConsumed() {
        return computeResourceConsumed;
    }

    public void setComputeResourceConsumed(String computeResourceConsumed) {
        this.computeResourceConsumed = computeResourceConsumed;
    }

    /**
     * Returns the transient job status list. This field is loaded from the EVENT table
     * at the service layer and is not persisted directly on this entity.
     */
    public List<StatusModel<JobState>> getJobStatuses() {
        return jobStatuses;
    }

    public void setJobStatuses(List<StatusModel<JobState>> jobStatuses) {
        this.jobStatuses = jobStatuses;
    }
}
