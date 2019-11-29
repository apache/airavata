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
package org.apache.airavata.registry.core.entities.expcatalog;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * The persistent class for the job database table.
 */
@Entity
@Table(name = "JOB")
@IdClass(JobPK.class)
public class JobEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "JOB_ID")
    private String jobId;

    @Id
    @Column(name = "TASK_ID")
    private String taskId;

    @Column(name = "PROCESS_ID")
    private String processId;

    @Lob
    @Column(name = "JOB_DESCRIPTION")
    private String jobDescription;

    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;

    @Column(name = "COMPUTE_RESOURCE_CONSUMED")
    private String computeResourceConsumed;

    @Column(name = "JOB_NAME")
    private String jobName;

    @Column(name = "WORKING_DIR")
    private String workingDir;

    @Lob
    @Column(name = "STD_OUT")
    private String stdOut;

    @Lob
    @Column(name = "STD_ERR")
    private String stdErr;

    @Column(name = "EXIT_CODE")
    private int exitCode;

    @OneToMany(targetEntity = JobStatusEntity.class, cascade = CascadeType.ALL,
            mappedBy = "job", fetch = FetchType.EAGER)
    @OrderBy("timeOfStateChange ASC")
    private List<JobStatusEntity> jobStatuses;

    @ManyToOne(targetEntity = TaskEntity.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "TASK_ID", referencedColumnName = "TASK_ID", nullable = false, updatable = false)
    private TaskEntity task;

    public JobEntity() {
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
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

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
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

    public List<JobStatusEntity> getJobStatuses() {
        return jobStatuses;
    }

    public void setJobStatuses(List<JobStatusEntity> jobStatuses) {
        this.jobStatuses = jobStatuses;
    }

    public TaskEntity getTask() {
        return task;
    }

    public void setTask(TaskEntity task) {
        this.task = task;
    }
}
