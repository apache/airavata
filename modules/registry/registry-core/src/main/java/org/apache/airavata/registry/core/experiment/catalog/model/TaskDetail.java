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

package org.apache.airavata.registry.core.experiment.catalog.model;

import org.apache.openjpa.persistence.DataCache;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

@DataCache
@Entity
@Table(name = "TASK_DETAIL")
public class TaskDetail implements Serializable {
    @Id
    @Column(name = "TASK_ID")
    private String taskId;
    @Column(name = "NODE_INSTANCE_ID")
    private String nodeId;
    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;
    @Column(name = "APPLICATION_ID")
    private String appId;
    @Column(name = "APPLICATION_VERSION")
    private String appVersion;
    @Column(name = "ALLOW_NOTIFICATION")
    private boolean allowNotification;

    @Column(name = "APPLICATION_DEPLOYMENT_ID")
    private String applicationDeploymentId;

    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "NODE_INSTANCE_ID")
    private WorkflowNodeDetail nodeDetail;

    @OneToMany (fetch = FetchType.LAZY, mappedBy = "task")
    private List<ApplicationOutput> applicationOutputs;

    @OneToMany (fetch = FetchType.LAZY, mappedBy = "task")
    private List<ApplicationInput> applicationInputs;

    @OneToOne (fetch = FetchType.LAZY, mappedBy = "task")
    private Computational_Resource_Scheduling resourceScheduling;

    @OneToOne (fetch = FetchType.LAZY, mappedBy = "task")
    private AdvancedInputDataHandling inputDataHandling;

    @OneToOne (fetch = FetchType.LAZY, mappedBy = "task")
    private AdvancedOutputDataHandling outputDataHandling;

    @OneToOne (fetch = FetchType.LAZY, mappedBy = "task")
    private Status taskStatus;

    @OneToMany (fetch = FetchType.LAZY, mappedBy = "task")
    private List<JobDetail> jobDetails;

    @OneToMany (fetch = FetchType.LAZY, mappedBy = "task")
    private List<DataTransferDetail> dataTransferDetails;

    @OneToMany (fetch = FetchType.LAZY, mappedBy = "task")
    private List<Notification_Email> notificationEmails;

    @OneToMany (fetch = FetchType.LAZY, mappedBy = "task")
    private List<ErrorDetail> errorDetails;

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

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

	public String getApplicationDeploymentId() {
		return applicationDeploymentId;
	}

	public void setApplicationDeploymentId(String applicationDeploymentId) {
		this.applicationDeploymentId = applicationDeploymentId;
	}

    public boolean isAllowNotification() {
        return allowNotification;
    }

    public void setAllowNotification(boolean allowNotification) {
        this.allowNotification = allowNotification;
    }

    public List<ApplicationOutput> getApplicationOutputs() {
        return applicationOutputs;
    }

    public void setApplicationOutputs(List<ApplicationOutput> applicationOutputs) {
        this.applicationOutputs = applicationOutputs;
    }

    public List<ApplicationInput> getApplicationInputs() {
        return applicationInputs;
    }

    public void setApplicationInputs(List<ApplicationInput> applicationInputs) {
        this.applicationInputs = applicationInputs;
    }

    public Computational_Resource_Scheduling getResourceScheduling() {
        return resourceScheduling;
    }

    public void setResourceScheduling(Computational_Resource_Scheduling resourceScheduling) {
        this.resourceScheduling = resourceScheduling;
    }

    public AdvancedInputDataHandling getInputDataHandling() {
        return inputDataHandling;
    }

    public void setInputDataHandling(AdvancedInputDataHandling inputDataHandling) {
        this.inputDataHandling = inputDataHandling;
    }

    public AdvancedOutputDataHandling getOutputDataHandling() {
        return outputDataHandling;
    }

    public void setOutputDataHandling(AdvancedOutputDataHandling outputDataHandling) {
        this.outputDataHandling = outputDataHandling;
    }

    public List<JobDetail> getJobDetails() {
        return jobDetails;
    }

    public void setJobDetails(List<JobDetail> jobDetails) {
        this.jobDetails = jobDetails;
    }

    public List<DataTransferDetail> getDataTransferDetails() {
        return dataTransferDetails;
    }

    public void setDataTransferDetails(List<DataTransferDetail> dataTransferDetails) {
        this.dataTransferDetails = dataTransferDetails;
    }

    public List<Notification_Email> getNotificationEmails() {
        return notificationEmails;
    }

    public void setNotificationEmails(List<Notification_Email> notificationEmails) {
        this.notificationEmails = notificationEmails;
    }

    public Status getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(Status taskStatus) {
        this.taskStatus = taskStatus;
    }

    public List<ErrorDetail> getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(List<ErrorDetail> errorDetails) {
        this.errorDetails = errorDetails;
    }
}
