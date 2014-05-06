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

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "EXPERIMENT")
public class Experiment {
    @Id
    @Column(name = "EXPERIMENT_ID")
    private String expId;
    @Column(name = "GATEWAY_NAME")
    private String gatewayName;
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


    @ManyToOne(cascade=CascadeType.MERGE)
    @JoinColumn(name = "gateway_name")
    private Gateway gateway;

    @ManyToOne(cascade=CascadeType.MERGE)
    @JoinColumn(name = "PROJECT_ID")
    private Project project;

//    @ManyToOne(cascade=CascadeType.MERGE)
//    @JoinColumn(name = "EXECUTION_USER", referencedColumnName = "USER_NAME")
//    private Users user;

    public String getExpId() {
        return expId;
    }

    public void setExpId(String expId) {
        this.expId = expId;
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public String getExecutionUser() {
        return executionUser;
    }

    public void setExecutionUser(String executionUser) {
        this.executionUser = executionUser;
    }

    public String getProjectId() {
        return projectID;
    }

    public void setProjectId(String projectId) {
        this.projectID = projectId;
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

    public Gateway getGateway() {
        return gateway;
    }

    public void setGateway(Gateway gateway) {
        this.gateway = gateway;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
