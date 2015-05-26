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

package org.apache.airavata.persistance.registry.jpa.model;

import org.apache.openjpa.persistence.DataCache;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@DataCache
@Entity
@Table(name = "COMPUTATIONAL_RESOURCE_SCHEDULING")
public class Computational_Resource_Scheduling implements Serializable {
    @Id
    @GeneratedValue
    @Column(name = "RESOURCE_SCHEDULING_ID")
    private int schedulingId;
    @Column(name = "EXPERIMENT_ID")
    private String expId;
    @Column(name = "TASK_ID")
    private String taskId;
    @Column(name = "RESOURCE_HOST_ID")
    private String resourceHostId;
    @Column(name = "CPU_COUNT")
    private int cpuCount;
    @Column(name = "NODE_COUNT")
    private int nodeCount;
    @Column(name = "NO_OF_THREADS")
    private int numberOfThreads;
    @Column(name = "QUEUE_NAME")
    private String queueName;
    @Column(name = "WALLTIME_LIMIT")
    private int wallTimeLimit;
    @Column(name = "JOB_START_TIME")
    private Timestamp jobStartTime;
    @Column(name = "TOTAL_PHYSICAL_MEMORY")
    private int totalPhysicalmemory;
    @Column(name = "COMPUTATIONAL_PROJECT_ACCOUNT")
    private String projectName;
    @Column(name = "CHESSIS_NAME")
    private String chessisName;

    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "EXPERIMENT_ID")
    private Experiment experiment;

    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "TASK_ID")
    private TaskDetail task;

    public String getChessisName() {
        return chessisName;
    }

    public void setChessisName(String chessisName) {
        this.chessisName = chessisName;
    }

    public int getSchedulingId() {
        return schedulingId;
    }

    public void setSchedulingId(int schedulingId) {
        this.schedulingId = schedulingId;
    }

    public String getExpId() {
        return expId;
    }

    public void setExpId(String expId) {
        this.expId = expId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getResourceHostId() {
        return resourceHostId;
    }

    public void setResourceHostId(String resourceHostId) {
        this.resourceHostId = resourceHostId;
    }

    public int getCpuCount() {
        return cpuCount;
    }

    public void setCpuCount(int cpuCount) {
        this.cpuCount = cpuCount;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public void setNumberOfThreads(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public int getWallTimeLimit() {
        return wallTimeLimit;
    }

    public void setWallTimeLimit(int wallTimeLimit) {
        this.wallTimeLimit = wallTimeLimit;
    }

    public Timestamp getJobStartTime() {
        return jobStartTime;
    }

    public void setJobStartTime(Timestamp jobStartTime) {
        this.jobStartTime = jobStartTime;
    }

    public int getTotalPhysicalmemory() {
        return totalPhysicalmemory;
    }

    public void setTotalPhysicalmemory(int totalPhysicalmemory) {
        this.totalPhysicalmemory = totalPhysicalmemory;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

    public TaskDetail getTask() {
        return task;
    }

    public void setTask(TaskDetail task) {
        this.task = task;
    }
}
