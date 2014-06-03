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
@Table(name = "STATUS")
public class Status implements Serializable {
    @Id
    @GeneratedValue
    @Column(name = "STATUS_ID")
    private int statusId;
    @Column(name = "EXPERIMENT_ID")
    private String expId;
    @Column(name = "NODE_INSTANCE_ID")
    private String nodeId;
    @Column(name = "TRANSFER_ID")
    private String transferId;
    @Column(name = "TASK_ID")
    private String taskId;
    @Column(name = "JOB_ID")
    private String jobId;
    @Column(name = "STATE")
    private String state;
    @Column(name = "STATUS_UPDATE_TIME")
    private Timestamp statusUpdateTime;
    @Column(name = "STATUS_TYPE")
    private String statusType;

    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "EXPERIMENT_ID")
    private Experiment experiment;

    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "TASK_ID")
    private TaskDetail task;

    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "NODE_INSTANCE_ID")
    private WorkflowNodeDetail node;

    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "TRANSFER_ID")
    private DataTransferDetail transferDetail;

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public String getExpId() {
        return expId;
    }

    public void setExpId(String expId) {
        this.expId = expId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getTransferId() {
        return transferId;
    }

    public void setTransferId(String transferId) {
        this.transferId = transferId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Timestamp getStatusUpdateTime() {
        return statusUpdateTime;
    }

    public void setStatusUpdateTime(Timestamp statusUpdateTime) {
        this.statusUpdateTime = statusUpdateTime;
    }

    public String getStatusType() {
        return statusType;
    }

    public void setStatusType(String statusType) {
        this.statusType = statusType;
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

    public WorkflowNodeDetail getNode() {
        return node;
    }

    public void setNode(WorkflowNodeDetail node) {
        this.node = node;
    }

    public DataTransferDetail getTransferDetail() {
        return transferDetail;
    }

    public void setTransferDetail(DataTransferDetail transferDetail) {
        this.transferDetail = transferDetail;
    }
}
