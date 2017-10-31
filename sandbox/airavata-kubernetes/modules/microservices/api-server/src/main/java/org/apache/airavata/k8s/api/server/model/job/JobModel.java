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
package org.apache.airavata.k8s.api.server.model.job;

import org.apache.airavata.k8s.api.server.model.task.TaskModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Entity
@Table(name = "JOB_MODEL")
public class JobModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne
    private TaskModel task;

    private String jobDescription;
    private long creationTime;

    @OneToMany(mappedBy = "jobModel", cascade = CascadeType.ALL)
    private List<JobStatus> jobStatuses = new ArrayList<>();

    @ManyToOne
    private TaskModel taskModel;
    
    private String computeResourceConsumed;
    private String jobName;
    private String workingDir;
    private String stdOut;
    private String stdErr;
    private int exitCode;

    public long getId() {
        return id;
    }

    public JobModel setId(long id) {
        this.id = id;
        return this;
    }

    public TaskModel getTask() {
        return task;
    }

    public JobModel setTask(TaskModel task) {
        this.task = task;
        return this;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public JobModel setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
        return this;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public JobModel setCreationTime(long creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public List<JobStatus> getJobStatuses() {
        return jobStatuses;
    }

    public JobModel setJobStatuses(List<JobStatus> jobStatuses) {
        this.jobStatuses = jobStatuses;
        return this;
    }

    public TaskModel getTaskModel() {
        return taskModel;
    }

    public JobModel setTaskModel(TaskModel taskModel) {
        this.taskModel = taskModel;
        return this;
    }

    public String getComputeResourceConsumed() {
        return computeResourceConsumed;
    }

    public JobModel setComputeResourceConsumed(String computeResourceConsumed) {
        this.computeResourceConsumed = computeResourceConsumed;
        return this;
    }

    public String getJobName() {
        return jobName;
    }

    public JobModel setJobName(String jobName) {
        this.jobName = jobName;
        return this;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public JobModel setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
        return this;
    }

    public String getStdOut() {
        return stdOut;
    }

    public JobModel setStdOut(String stdOut) {
        this.stdOut = stdOut;
        return this;
    }

    public String getStdErr() {
        return stdErr;
    }

    public JobModel setStdErr(String stdErr) {
        this.stdErr = stdErr;
        return this;
    }

    public int getExitCode() {
        return exitCode;
    }

    public JobModel setExitCode(int exitCode) {
        this.exitCode = exitCode;
        return this;
    }
}
