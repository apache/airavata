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
package org.apache.airavata.registry.entities.expcatalog;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import org.apache.airavata.registry.entities.StatusEntity;

/**
 * The persistent class for the job database table.
 */
@Entity
@Table(name = "JOB")
@IdClass(JobPK.class)
public class JobEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "JOB_ID", nullable = false)
    private String jobId;

    @Id
    @Column(name = "TASK_ID", nullable = false)
    private String taskId;

    @Column(name = "PROCESS_ID")
    private String processId;

    @Lob
    @Column(name = "JOB_DESCRIPTION", nullable = false)
    private String jobDescription;

    @Column(name = "CREATION_TIME", nullable = false, columnDefinition = "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP")
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

    @OneToMany(targetEntity = StatusEntity.class, cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @jakarta.persistence.JoinColumns(value = {
        @JoinColumn(
                name = "PARENT_ID",
                referencedColumnName = "JOB_ID",
                insertable = false,
                updatable = false)
    }, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @org.hibernate.annotations.Where(clause = "PARENT_TYPE = 'JOB'")
    @OrderBy("sequenceNum ASC")
    private List<StatusEntity> jobStatuses;

    // Note: No cascade - this is a read-only relationship (insertable=false, updatable=false)
    @ManyToOne(targetEntity = TaskEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "TASK_ID",
            referencedColumnName = "TASK_ID",
            nullable = false,
            insertable = false,
            updatable = false)
    private TaskEntity task;

    public JobEntity() {}

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

    public List<StatusEntity> getJobStatuses() {
        return jobStatuses;
    }

    public void setJobStatuses(List<StatusEntity> jobStatuses) {
        this.jobStatuses = jobStatuses;
    }

    public TaskEntity getTask() {
        return task;
    }

    // Note: No setter for 'task' - the relationship is read-only (insertable=false, updatable=false)
    // The taskId field should be set instead, and Hibernate will resolve the relationship via the @JoinColumn
}
