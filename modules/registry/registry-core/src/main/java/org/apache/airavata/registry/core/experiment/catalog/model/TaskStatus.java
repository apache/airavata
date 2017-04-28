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

@Entity
@Table(name = "TASK_STATUS")
@IdClass(TaskStatusPK.class)
public class TaskStatus {
    private final static Logger logger = LoggerFactory.getLogger(TaskStatus.class);
    private String statusId;
    private String taskId;
    private String state;
    private Timestamp timeOfStateChange;
    private String reason;
    private Task task;

    @Id
    @Column(name = "STATUS_ID")
    public String getStatusId() {
        return statusId;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    @Id
    @Column(name = "TASK_ID")
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Column(name = "STATE")
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Column(name = "TIME_OF_STATE_CHANGE")
    public Timestamp getTimeOfStateChange() {
        return timeOfStateChange;
    }

    public void setTimeOfStateChange(Timestamp timeOfStateChange) {
        this.timeOfStateChange = timeOfStateChange;
    }

    @Lob
    @Column(name = "REASON")
    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        TaskStatus that = (TaskStatus) o;
//        if (statusId != null ? !statusId.equals(that.statusId) : that.statusId != null) return false;
//        if (reason != null ? !reason.equals(that.reason) : that.reason != null) return false;
//        if (state != null ? !state.equals(that.state) : that.state != null) return false;
//        if (taskId != null ? !taskId.equals(that.taskId) : that.taskId != null) return false;
//        if (timeOfStateChange != null ? !timeOfStateChange.equals(that.timeOfStateChange) : that.timeOfStateChange != null)
//            return false;
//
//        return true;
//    }
//
//    @Override
//    public int hashCode() {
//        int result = statusId != null ? statusId.hashCode() : 0;
//        result = 31 * result + (taskId != null ? taskId.hashCode() : 0);
//        result = 31 * result + (state != null ? state.hashCode() : 0);
//        result = 31 * result + (timeOfStateChange != null ? timeOfStateChange.hashCode() : 0);
//        result = 31 * result + (reason != null ? reason.hashCode() : 0);
//        return result;
//    }

    @ManyToOne
    @JoinColumn(name = "TASK_ID", referencedColumnName = "TASK_ID", nullable = false)
    public Task getTask() {
        return task;
    }

    public void setTask(Task taskByTaskId) {
        this.task = taskByTaskId;
    }
}