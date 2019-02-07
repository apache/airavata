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

import org.apache.airavata.model.task.TaskTypes;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * The persistent class for the task database table.
 */
@Entity
@Table(name = "TASK")
public class TaskEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "TASK_ID")
    private String taskId;

    @Column(name = "TASK_TYPE")
    @Enumerated(EnumType.STRING)
    private TaskTypes taskType;

    @Column(name = "PARENT_PROCESS_ID")
    private String parentProcessId;

    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;

    @Column(name = "LAST_UPDATE_TIME")
    private Timestamp lastUpdateTime;

    @Lob
    @Column(name = "TASK_DETAIL")
    private String taskDetail;

    @Lob
    @Column(name = "SUB_TASK_MODEL")
    private byte[] subTaskModel;

    @OneToMany(targetEntity = TaskStatusEntity.class, cascade = CascadeType.ALL,
            mappedBy = "task", fetch = FetchType.EAGER)
    @OrderBy("timeOfStateChange ASC")
    private List<TaskStatusEntity> taskStatuses;

    @OneToMany(targetEntity = TaskErrorEntity.class, cascade = CascadeType.ALL,
            mappedBy = "task", fetch = FetchType.EAGER)
    private List<TaskErrorEntity> taskErrors;

    @OneToMany(targetEntity = JobEntity.class, cascade = CascadeType.ALL,
            mappedBy = "task", fetch = FetchType.EAGER)
    private List<JobEntity> jobs;

    @ManyToOne(targetEntity = ProcessEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_PROCESS_ID", referencedColumnName = "PROCESS_ID", nullable = false, updatable = false)
    private ProcessEntity process;

    public TaskEntity() {
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public TaskTypes getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskTypes taskType) {
        this.taskType = taskType;
    }

    public String getParentProcessId() {
        return parentProcessId;
    }

    public void setParentProcessId(String parentProcessId) {
        this.parentProcessId = parentProcessId;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public Timestamp getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Timestamp lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getTaskDetail() {
        return taskDetail;
    }

    public void setTaskDetail(String taskDetail) {
        this.taskDetail = taskDetail;
    }

    public byte[] getSubTaskModel() {
        return subTaskModel;
    }

    public void setSubTaskModel(byte[] subTaskModel) {
        this.subTaskModel = subTaskModel;
    }

    public List<TaskStatusEntity> getTaskStatuses() {
        return taskStatuses;
    }

    public void setTaskStatuses(List<TaskStatusEntity> taskStatuses) {
        this.taskStatuses = taskStatuses;
    }

    public List<TaskErrorEntity> getTaskErrors() {
        return taskErrors;
    }

    public void setTaskErrors(List<TaskErrorEntity> taskErrors) {
        this.taskErrors = taskErrors;
    }

    public List<JobEntity> getJobs() {
        return jobs;
    }

    public void setJobs(List<JobEntity> jobs) {
        this.jobs = jobs;
    }

    public ProcessEntity getProcess() {
        return process;
    }

    public void setProcess(ProcessEntity process) {
        this.process = process;
    }
}