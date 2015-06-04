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
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "EXPERIMENT")
@DataCache
public class Experiment implements Serializable {
    @Id
    @Column(name = "EXPERIMENT_ID")
    private String expId;
    @Column(name = "GATEWAY_ID")
    private String gatewayId;
    @Column(name = "EXECUTION_USER")
    private String executionUser;
    @Column(name = "PROJECT_ID")
    private String projectID;
    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;
    @Column(name = "EXPERIMENT_NAME")
    private String expName;
    @Column(name = "EXPERIMENT_DESCRIPTION")
    private String expDesc;
    @Column(name = "APPLICATION_ID")
    private String applicationId;
    @Column(name = "APPLICATION_VERSION")
    private String appVersion;
    @Column(name = "WORKFLOW_TEMPLATE_ID")
    private String workflowTemplateId;
    @Column(name = "WORKFLOW_TEMPLATE_VERSION")
    private String workflowTemplateVersion;
    @Column(name = "WORKFLOW_EXECUTION_ID")
    private String workflowExecutionId;
    @Column(name = "ALLOW_NOTIFICATION")
    private boolean allowNotification;
    @Column(name = "GATEWAY_EXECUTION_ID")
    private String gatewayExecutionId;

    @ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.MERGE)
    @JoinColumn(name = "GATEWAY_ID")
    private Gateway gateway;

    @ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.MERGE)
    @JoinColumn(name = "PROJECT_ID")
    private Project project;

    @ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.MERGE)
    @JoinColumn(name = "EXECUTION_USER", referencedColumnName = "USER_NAME")
    private Users user;

    @OneToMany (fetch = FetchType.LAZY, mappedBy = "experiment")
    private List<Experiment_Output> experimentOutputs;

    @OneToMany (fetch = FetchType.LAZY, mappedBy = "experiment")
    private List<Experiment_Input> experimentInputs;

    @OneToOne (fetch = FetchType.LAZY, mappedBy = "experiment")
    private Computational_Resource_Scheduling resourceScheduling;

    @OneToOne (fetch = FetchType.LAZY, mappedBy = "experiment")
    private ExperimentConfigData userConfigurationData;

    @OneToMany (fetch = FetchType.LAZY, mappedBy = "experiment")
    private List<WorkflowNodeDetail> workflowNodeDetails;

    @OneToMany (fetch = FetchType.LAZY, mappedBy = "experiment")
    private List<Status> stateChangeList;

    @OneToMany (fetch = FetchType.LAZY, mappedBy = "experiment")
    private List<ErrorDetail> errorDetails;

    @OneToOne (fetch = FetchType.LAZY, mappedBy = "experiment")
    private Status experimentStatus;

    @OneToMany (fetch = FetchType.LAZY, mappedBy = "experiment")
    private List<Notification_Email> notificationEmails;

    @OneToMany(fetch=FetchType.LAZY, mappedBy = "experiment")
    private Collection<Status> statuses;

    public String getExpId() {
        return expId;
    }

    public void setExpId(String expId) {
        this.expId = expId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getExecutionUser() {
        return executionUser;
    }

    public void setExecutionUser(String executionUser) {
        this.executionUser = executionUser;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public String getExpName() {
        return expName;
    }

    public void setExpName(String expName) {
        this.expName = expName;
    }

    public String getExpDesc() {
        return expDesc;
    }

    public void setExpDesc(String expDesc) {
        this.expDesc = expDesc;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getWorkflowTemplateId() {
        return workflowTemplateId;
    }

    public void setWorkflowTemplateId(String workflowTemplateId) {
        this.workflowTemplateId = workflowTemplateId;
    }

    public String getWorkflowTemplateVersion() {
        return workflowTemplateVersion;
    }

    public void setWorkflowTemplateVersion(String workflowTemplateVersion) {
        this.workflowTemplateVersion = workflowTemplateVersion;
    }

    public String getWorkflowExecutionId() {
        return workflowExecutionId;
    }

    public void setWorkflowExecutionId(String workflowExecutionId) {
        this.workflowExecutionId = workflowExecutionId;
    }

    public boolean isAllowNotification() {
        return allowNotification;
    }

    public void setAllowNotification(boolean allowNotification) {
        this.allowNotification = allowNotification;
    }

    public String getGatewayExecutionId() {
        return gatewayExecutionId;
    }

    public String getProjectID() {
        return projectID;
    }

    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }

    public List<Experiment_Output> getExperimentOutputs() {
        return experimentOutputs;
    }

    public void setExperimentOutputs(List<Experiment_Output> experimentOutputs) {
        this.experimentOutputs = experimentOutputs;
    }

    public List<Experiment_Input> getExperimentInputs() {
        return experimentInputs;
    }

    public void setExperimentInputs(List<Experiment_Input> experimentInputs) {
        this.experimentInputs = experimentInputs;
    }

    public Computational_Resource_Scheduling getResourceScheduling() {
        return resourceScheduling;
    }

    public void setResourceScheduling(Computational_Resource_Scheduling resourceScheduling) {
        this.resourceScheduling = resourceScheduling;
    }

    public List<ErrorDetail> getErrorDetails() {
        return errorDetails;
    }

    public ExperimentConfigData getUserConfigurationData() {
        return userConfigurationData;
    }

    public void setUserConfigurationData(ExperimentConfigData userConfigurationData) {
        this.userConfigurationData = userConfigurationData;
    }

    public List<WorkflowNodeDetail> getWorkflowNodeDetails() {
        return workflowNodeDetails;
    }

    public void setWorkflowNodeDetails(List<WorkflowNodeDetail> workflowNodeDetails) {
        this.workflowNodeDetails = workflowNodeDetails;
    }

    public List<Status> getStateChangeList() {
        return stateChangeList;
    }

    public void setStateChangeList(List<Status> stateChangeList) {
        this.stateChangeList = stateChangeList;
    }

    public void setErrorDetails(List<ErrorDetail> errorDetails) {
        this.errorDetails = errorDetails;
    }

    public Status getExperimentStatus() {
        return experimentStatus;
    }

    public void setExperimentStatus(Status experimentStatus) {
        this.experimentStatus = experimentStatus;
    }

    public List<Notification_Email> getNotificationEmails() {
        return notificationEmails;
    }

    public void setNotificationEmails(List<Notification_Email> notificationEmails) {
        this.notificationEmails = notificationEmails;
    }

    public void setGatewayExecutionId(String gatewayExecutionId) {
        this.gatewayExecutionId = gatewayExecutionId;
    }

    public Collection<Status> getStatuses() {
        return statuses;
    }

    public void setStatuses(Collection<Status> statuses) {
        this.statuses = statuses;
    }
}
