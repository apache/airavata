/**
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
 */
package org.apache.airavata.registry.core.experiment.catalog.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Collection;

@Entity
@Table(name = "JOB")
@IdClass(JobPK.class)
public class Job {
    private final static Logger logger = LoggerFactory.getLogger(Job.class);
    private String jobId;
    private String taskId;
	private String processId;
    private char[] jobDescription;
    private char[] stdOut;
    private char[] stdErr;
    private int exitCode;
    private Timestamp creationTime;
    private String computeResourceConsumed;
    private String jobName;
    private String workingDir;
	private Process process;
    private Collection<JobStatus> jobStatuses;

    @Id
    @Column(name = "JOB_ID")
    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

	@Column(name = "PROCESS_ID")
	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

    @Id
    @Column(name = "TASK_ID")
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Lob
    @Column(name = "JOB_DESCRIPTION")
    public char[] getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(char[] jobDescription) {
        this.jobDescription = jobDescription;
    }

    @Lob
    @Column(name = "STD_OUT")
    public char[] getStdOut() {
        return stdOut;
    }

    public void setStdOut(char[] stdOut) {
        this.stdOut = stdOut;
    }

    @Lob
    @Column(name = "STD_ERR")
    public char[] getStdErr() {
        return stdErr;
    }

    public void setStdErr(char[] stdErr) {
        this.stdErr = stdErr;
    }

    @Column(name = "EXIT_CODE")
    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    @Column(name = "CREATION_TIME")
    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    @Column(name = "COMPUTE_RESOURCE_CONSUMED")
    public String getComputeResourceConsumed() {
        return computeResourceConsumed;
    }

    public void setComputeResourceConsumed(String computeResourceConsumed) {
        this.computeResourceConsumed = computeResourceConsumed;
    }

    @Column(name = "JOB_NAME")
    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Column(name = "WORKING_DIR")
    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        Job job = (Job) o;
//
//        if (computeResourceConsumed != null ? !computeResourceConsumed.equals(job.computeResourceConsumed) : job.computeResourceConsumed != null)
//            return false;
//        if (creationTime != null ? !creationTime.equals(job.creationTime) : job.creationTime != null) return false;
//        if (jobDescription != null ? !jobDescription.equals(job.jobDescription) : job.jobDescription != null)
//            return false;
//        if (jobId != null ? !jobId.equals(job.jobId) : job.jobId != null) return false;
//        if (jobName != null ? !jobName.equals(job.jobName) : job.jobName != null) return false;
//        if (taskId != null ? !taskId.equals(job.taskId) : job.taskId != null) return false;
//        if (workingDir != null ? !workingDir.equals(job.workingDir) : job.workingDir != null) return false;
//
//        return true;
//    }
//
//    @Override
//    public int hashCode() {
//        int result = jobId != null ? jobId.hashCode() : 0;
//        result = 31 * result + (taskId != null ? taskId.hashCode() : 0);
//        result = 31 * result + (jobDescription != null ? jobDescription.hashCode() : 0);
//        result = 31 * result + (creationTime != null ? creationTime.hashCode() : 0);
//        result = 31 * result + (computeResourceConsumed != null ? computeResourceConsumed.hashCode() : 0);
//        result = 31 * result + (jobName != null ? jobName.hashCode() : 0);
//        result = 31 * result + (workingDir != null ? workingDir.hashCode() : 0);
//        return result;
//    }

	@OneToOne
	@JoinColumn(name = "PROCESS_ID", referencedColumnName = "PROCESS_ID")
	public Process getProcess() {
		return process;
	}

	public void setProcess(Process processByProcessId) {
		this.process = processByProcessId;
	}

    @OneToMany(mappedBy = "job")
    public Collection<JobStatus> getJobStatuses() {
        return jobStatuses;
    }

    public void setJobStatuses(Collection<JobStatus> jobStatusesByJobId) {
        this.jobStatuses = jobStatusesByJobId;
    }
}