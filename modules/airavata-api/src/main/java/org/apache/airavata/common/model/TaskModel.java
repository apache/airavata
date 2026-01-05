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
package org.apache.airavata.common.model;

import java.util.List;
import java.util.Objects;

/**
 * Domain model: TaskModel
 */
public class TaskModel {
    private String taskId;
    private TaskTypes taskType;
    private String parentProcessId;
    private long creationTime;
    private long lastUpdateTime;
    private List<TaskStatus> taskStatuses;
    private String taskDetail;
    private Object subTaskModel;
    private List<ErrorModel> taskErrors;
    private List<JobModel> jobs;
    private int maxRetry;
    private int currentRetry;

    public TaskModel() {}

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

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public List<TaskStatus> getTaskStatuses() {
        return taskStatuses;
    }

    public void setTaskStatuses(List<TaskStatus> taskStatuses) {
        this.taskStatuses = taskStatuses;
    }

    public String getTaskDetail() {
        return taskDetail;
    }

    public void setTaskDetail(String taskDetail) {
        this.taskDetail = taskDetail;
    }

    public Object getSubTaskModel() {
        return subTaskModel;
    }

    public void setSubTaskModel(Object subTaskModel) {
        this.subTaskModel = subTaskModel;
    }

    public List<ErrorModel> getTaskErrors() {
        return taskErrors;
    }

    public void setTaskErrors(List<ErrorModel> taskErrors) {
        this.taskErrors = taskErrors;
    }

    public List<JobModel> getJobs() {
        return jobs;
    }

    public void setJobs(List<JobModel> jobs) {
        this.jobs = jobs;
    }

    public int getMaxRetry() {
        return maxRetry;
    }

    public void setMaxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    public int getCurrentRetry() {
        return currentRetry;
    }

    public void setCurrentRetry(int currentRetry) {
        this.currentRetry = currentRetry;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskModel that = (TaskModel) o;
        return Objects.equals(taskId, that.taskId)
                && Objects.equals(taskType, that.taskType)
                && Objects.equals(parentProcessId, that.parentProcessId)
                && Objects.equals(creationTime, that.creationTime)
                && Objects.equals(lastUpdateTime, that.lastUpdateTime)
                && Objects.equals(taskStatuses, that.taskStatuses)
                && Objects.equals(taskDetail, that.taskDetail)
                && Objects.equals(subTaskModel, that.subTaskModel)
                && Objects.equals(taskErrors, that.taskErrors)
                && Objects.equals(jobs, that.jobs)
                && Objects.equals(maxRetry, that.maxRetry)
                && Objects.equals(currentRetry, that.currentRetry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                taskId,
                taskType,
                parentProcessId,
                creationTime,
                lastUpdateTime,
                taskStatuses,
                taskDetail,
                subTaskModel,
                taskErrors,
                jobs,
                maxRetry,
                currentRetry);
    }

    @Override
    public String toString() {
        return "TaskModel{" + "taskId=" + taskId + ", taskType=" + taskType + ", parentProcessId=" + parentProcessId
                + ", creationTime=" + creationTime + ", lastUpdateTime=" + lastUpdateTime + ", taskStatuses="
                + taskStatuses + ", taskDetail=" + taskDetail + ", subTaskModel=" + subTaskModel + ", taskErrors="
                + taskErrors + ", jobs=" + jobs + ", maxRetry=" + maxRetry + ", currentRetry=" + currentRetry + "}";
    }
}
