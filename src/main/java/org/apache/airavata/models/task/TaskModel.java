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
package org.apache.airavata.models.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.apache.airavata.models.commons.ErrorModel;
import org.apache.airavata.models.job.JobModel;
import org.apache.airavata.models.status.TaskStatus;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.task.proto.TaskModel}.
 */
public record TaskModel(
        String taskId,
        TaskTypes taskType,
        String parentProcessId,
        long creationTime,
        long lastUpdateTime,
        List<TaskStatus> taskStatuses,
        String taskDetail,
        byte[] subTaskModel,
        List<ErrorModel> taskErrors,
        List<JobModel> jobs,
        int maxRetry,
        int currentRetry) {

    public TaskModel {
        taskStatuses = taskStatuses == null ? List.of() : List.copyOf(taskStatuses);
        subTaskModel = subTaskModel == null ? new byte[0] : subTaskModel.clone();
        taskErrors = taskErrors == null ? List.of() : List.copyOf(taskErrors);
        jobs = jobs == null ? List.of() : List.copyOf(jobs);
    }

    @Override
    public byte[] subTaskModel() {
        return subTaskModel.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TaskModel other)) {
            return false;
        }
        return creationTime == other.creationTime
                && lastUpdateTime == other.lastUpdateTime
                && maxRetry == other.maxRetry
                && currentRetry == other.currentRetry
                && Objects.equals(taskId, other.taskId)
                && taskType == other.taskType
                && Objects.equals(parentProcessId, other.parentProcessId)
                && Objects.equals(taskStatuses, other.taskStatuses)
                && Objects.equals(taskDetail, other.taskDetail)
                && Arrays.equals(subTaskModel, other.subTaskModel)
                && Objects.equals(taskErrors, other.taskErrors)
                && Objects.equals(jobs, other.jobs);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(
                taskId, taskType, parentProcessId, creationTime, lastUpdateTime, taskStatuses, taskDetail,
                taskErrors, jobs, maxRetry, currentRetry);
        return 31 * result + Arrays.hashCode(subTaskModel);
    }

    @Override
    public String toString() {
        return "TaskModel[taskId=" + taskId + ", taskType=" + taskType + ", parentProcessId=" + parentProcessId
                + ", creationTime=" + creationTime + ", lastUpdateTime=" + lastUpdateTime + ", taskStatuses="
                + taskStatuses + ", taskDetail=" + taskDetail + ", subTaskModel=byte[" + subTaskModel.length
                + "], taskErrors=" + taskErrors + ", jobs=" + jobs + ", maxRetry=" + maxRetry + ", currentRetry="
                + currentRetry + "]";
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String taskId = "";
        private TaskTypes taskType = TaskTypes.TASK_TYPES_UNKNOWN;
        private String parentProcessId = "";
        private long creationTime;
        private long lastUpdateTime;
        private List<TaskStatus> taskStatuses = new ArrayList<>();
        private String taskDetail = "";
        private byte[] subTaskModel = new byte[0];
        private List<ErrorModel> taskErrors = new ArrayList<>();
        private List<JobModel> jobs = new ArrayList<>();
        private int maxRetry;
        private int currentRetry;

        private Builder() {}

        private Builder(TaskModel source) {
            this.taskId = source.taskId;
            this.taskType = source.taskType;
            this.parentProcessId = source.parentProcessId;
            this.creationTime = source.creationTime;
            this.lastUpdateTime = source.lastUpdateTime;
            this.taskStatuses = new ArrayList<>(source.taskStatuses);
            this.taskDetail = source.taskDetail;
            this.subTaskModel = source.subTaskModel();
            this.taskErrors = new ArrayList<>(source.taskErrors);
            this.jobs = new ArrayList<>(source.jobs);
            this.maxRetry = source.maxRetry;
            this.currentRetry = source.currentRetry;
        }

        public Builder setTaskId(String taskId) {
            this.taskId = taskId;
            return this;
        }

        public Builder setTaskType(TaskTypes taskType) {
            this.taskType = taskType;
            return this;
        }

        public Builder setParentProcessId(String parentProcessId) {
            this.parentProcessId = parentProcessId;
            return this;
        }

        public Builder setCreationTime(long creationTime) {
            this.creationTime = creationTime;
            return this;
        }

        public Builder setLastUpdateTime(long lastUpdateTime) {
            this.lastUpdateTime = lastUpdateTime;
            return this;
        }

        public Builder addTaskStatuses(TaskStatus value) {
            this.taskStatuses.add(value);
            return this;
        }

        public Builder addAllTaskStatuses(Iterable<TaskStatus> values) {
            values.forEach(this.taskStatuses::add);
            return this;
        }

        public Builder clearTaskStatuses() {
            this.taskStatuses.clear();
            return this;
        }

        public Builder setTaskDetail(String taskDetail) {
            this.taskDetail = taskDetail;
            return this;
        }

        public Builder setSubTaskModel(byte[] subTaskModel) {
            this.subTaskModel = subTaskModel == null ? new byte[0] : subTaskModel.clone();
            return this;
        }

        public Builder addTaskErrors(ErrorModel value) {
            this.taskErrors.add(value);
            return this;
        }

        public Builder addAllTaskErrors(Iterable<ErrorModel> values) {
            values.forEach(this.taskErrors::add);
            return this;
        }

        public Builder clearTaskErrors() {
            this.taskErrors.clear();
            return this;
        }

        public Builder addJobs(JobModel value) {
            this.jobs.add(value);
            return this;
        }

        public Builder addAllJobs(Iterable<JobModel> values) {
            values.forEach(this.jobs::add);
            return this;
        }

        public Builder clearJobs() {
            this.jobs.clear();
            return this;
        }

        public Builder setMaxRetry(int maxRetry) {
            this.maxRetry = maxRetry;
            return this;
        }

        public Builder setCurrentRetry(int currentRetry) {
            this.currentRetry = currentRetry;
            return this;
        }

        public TaskModel build() {
            return new TaskModel(
                    taskId, taskType, parentProcessId, creationTime, lastUpdateTime, taskStatuses, taskDetail,
                    subTaskModel, taskErrors, jobs, maxRetry, currentRetry);
        }
    }
}
