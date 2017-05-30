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
import java.nio.ByteBuffer;
import java.util.List;

@Entity
@Table(name = "EXPCAT_TASK")
public class TaskEntity {
    private String taskId;
    private TaskTypes taskType;
    private String parentProcessId;
    private long creationTime;
    private long lastUpdateTime;
    private String taskDetail;
    private ByteBuffer subTaskModel;

    private List<TaskStatusEntity> taskStatuses;
    private List<TaskErrorEntity> taskErrors;
    private List<JobEntity> jobs;

    private ProcessEntity process;

    @Id
    @Column(name = "TASK_ID")
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Column(name = "TASK_TYPE")
    public TaskTypes getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskTypes taskType) {
        this.taskType = taskType;
    }

    @Column(name = "PARENT_PROCESS_ID")
    public String getParentProcessId() {
        return parentProcessId;
    }

    public void setParentProcessId(String parentProcessId) {
        this.parentProcessId = parentProcessId;
    }

    @Column(name = "CREATION_TIME")
    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    @Column(name = "LAST_UPDATE_TIME")
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @Column(name = "TASK_DETAIL")
    public String getTaskDetail() {
        return taskDetail;
    }

    public void setTaskDetail(String taskDetail) {
        this.taskDetail = taskDetail;
    }

    @Lob
    @Column(name = "SUB_TASK_MODEL")
    public ByteBuffer getSubTaskModel() {
        return subTaskModel;
    }

    public void setSubTaskModel(ByteBuffer subTaskModel) {
        this.subTaskModel = subTaskModel;
    }

    @OneToMany(targetEntity = TaskStatusEntity.class, cascade = CascadeType.ALL, mappedBy = "task")
    public List<TaskStatusEntity> getTaskStatuses() {
        return taskStatuses;
    }

    public void setTaskStatuses(List<TaskStatusEntity> taskStatus) {
        this.taskStatuses = taskStatus;
    }

    @OneToMany(targetEntity = TaskErrorEntity.class, cascade = CascadeType.ALL, mappedBy = "task")
    public List<TaskErrorEntity> getTaskErrors() {
        return taskErrors;
    }

    public void setTaskErrors(List<TaskErrorEntity> taskError) {
        this.taskErrors = taskError;
    }

    @OneToMany(targetEntity = JobEntity.class, cascade = CascadeType.ALL, mappedBy = "task")
    public List<JobEntity> getJobs() {
        return jobs;
    }

    public void setJobs(List<JobEntity> jobs) {
        this.jobs = jobs;
    }

    @ManyToOne(targetEntity = ProcessEntity.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_PROCESS_ID", referencedColumnName = "PROCESS_ID")
    public ProcessEntity getProcess() {
        return process;
    }

    public void setProcess(ProcessEntity process) {
        this.process = process;
    }
}