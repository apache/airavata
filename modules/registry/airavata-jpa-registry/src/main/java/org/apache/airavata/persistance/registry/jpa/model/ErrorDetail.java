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
@Table(name = "ERROR_DETAIL")
public class ErrorDetail implements Serializable {
    @Id
    @GeneratedValue
    @Column(name = "ERROR_ID")
    private int errorID;
    @Column(name = "EXPERIMENT_ID")
    private String expId;
    @Column(name = "TASK_ID")
    private String taskId;
    @Column(name = "NODE_INSTANCE_ID")
    private String nodeId;
    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;
    @Lob
    @Column(name = "ACTUAL_ERROR_MESSAGE")
    private char[] actualErrorMsg;
    
    @Column(name = "USER_FRIEDNLY_ERROR_MSG")
    private String userFriendlyErrorMsg;
    @Column(name = "TRANSIENT_OR_PERSISTENT")
    private boolean transientPersistent;
    @Column(name = "ERROR_CATEGORY")
    private String errorCategory;
    @Column(name = "CORRECTIVE_ACTION")
    private String correctiveAction;
    @Column(name = "ACTIONABLE_GROUP")
    private String actionableGroup;
    @Column(name = "JOB_ID")
    private String jobId;


    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "EXPERIMENT_ID")
    private Experiment experiment;

    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "TASK_ID")
    private TaskDetail task;

    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "NODE_INSTANCE_ID")
    private WorkflowNodeDetail nodeDetails;

    public int getErrorId() {
        return errorID;
    }

    public void setErrorId(int errorID) {
        this.errorID = errorID;
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

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public char[] getActualErrorMsg() {
		return actualErrorMsg;
	}

	public void setActualErrorMsg(char[] actualErrorMsg) {
		this.actualErrorMsg = actualErrorMsg;
	}

	public String getUserFriendlyErrorMsg() {
        return userFriendlyErrorMsg;
    }

    public void setUserFriendlyErrorMsg(String userFriendlyErrorMsg) {
        this.userFriendlyErrorMsg = userFriendlyErrorMsg;
    }

    public boolean isTransientPersistent() {
        return transientPersistent;
    }

    public void setTransientPersistent(boolean transientPersistent) {
        this.transientPersistent = transientPersistent;
    }

    public String getErrorCategory() {
        return errorCategory;
    }

    public void setErrorCategory(String errorCategory) {
        this.errorCategory = errorCategory;
    }

    public String getActionableGroup() {
        return actionableGroup;
    }

    public void setActionableGroup(String actionableGroup) {
        this.actionableGroup = actionableGroup;
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

    public WorkflowNodeDetail getNodeDetails() {
        return nodeDetails;
    }

    public void setNodeDetails(WorkflowNodeDetail nodeDetails) {
        this.nodeDetails = nodeDetails;
    }

    public String getCorrectiveAction() {
        return correctiveAction;
    }

    public void setCorrectiveAction(String correctiveAction) {
        this.correctiveAction = correctiveAction;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
}
