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
package org.apache.airavata.k8s.api.resources.task;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class TaskResource {

    private long id;
    private int taskType;
    private String taskTypeStr;
    private long parentProcessId;
    private long creationTime;
    private long lastUpdateTime;
    private List<TaskStatusResource> taskStatus = new ArrayList<>();
    private String taskDetail;
    private List<Long> taskErrorIds = new ArrayList<>();
    private List<TaskParamResource> taskParams = new ArrayList<>();
    private List<Long> jobIds;
    private int order;

    public long getId() {
        return id;
    }

    public TaskResource setId(long id) {
        this.id = id;
        return this;
    }

    public int getTaskType() {
        return taskType;
    }

    public TaskResource setTaskType(int taskType) {
        this.taskType = taskType;
        return this;
    }

    public long getParentProcessId() {
        return parentProcessId;
    }

    public TaskResource setParentProcessId(long parentProcessId) {
        this.parentProcessId = parentProcessId;
        return this;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public TaskResource setCreationTime(long creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public TaskResource setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
        return this;
    }

    public List<TaskStatusResource> getTaskStatus() {
        return taskStatus;
    }

    public TaskResource setTaskStatus(List<TaskStatusResource> taskStatus) {
        this.taskStatus = taskStatus;
        return this;
    }

    public String getTaskDetail() {
        return taskDetail;
    }

    public TaskResource setTaskDetail(String taskDetail) {
        this.taskDetail = taskDetail;
        return this;
    }

    public List<Long> getTaskErrorIds() {
        return taskErrorIds;
    }

    public TaskResource setTaskErrorIds(List<Long> taskErrorIds) {
        this.taskErrorIds = taskErrorIds;
        return this;
    }

    public List<Long> getJobIds() {
        return jobIds;
    }

    public TaskResource setJobIds(List<Long> jobIds) {
        this.jobIds = jobIds;
        return this;
    }

    public List<TaskParamResource> getTaskParams() {
        return taskParams;
    }

    public TaskResource setTaskParams(List<TaskParamResource> taskParams) {
        this.taskParams = taskParams;
        return this;
    }

    public String getTaskTypeStr() {
        return taskTypeStr;
    }

    public TaskResource setTaskTypeStr(String taskTypeStr) {
        this.taskTypeStr = taskTypeStr;
        return this;
    }

    public int getOrder() {
        return order;
    }

    public TaskResource setOrder(int order) {
        this.order = order;
        return this;
    }

    public static final class TaskTypes {
        public static final int ENV_SETUP = 0;
        public static final int INGRESS_DATA_STAGING = 1;
        public static final int EGRESS_DATA_STAGING = 2;
        public static final int JOB_SUBMISSION = 3;
        public static final int ENV_CLEANUP = 4;
        public static final int MONITORING = 5;
        public static final int OUTPUT_FETCHING = 6;
    }
}
