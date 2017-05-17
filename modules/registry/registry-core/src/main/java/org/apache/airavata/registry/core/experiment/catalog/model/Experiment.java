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
import java.util.Collection;

@Entity
@Table(name = "EXPERIMENT")
public class Experiment {
    private final static Logger logger = LoggerFactory.getLogger(Experiment.class);
    private String experimentId;
    private String projectId;
    private String gatewayId;
    private String experimentType;
    private String userName;
    private String experimentName;
    private Timestamp creationTime;
    private String description;
    private String executionId;
    private String gatewayExecutionId;
    private String gatewayInstanceId;

    private Boolean enableEmailNotification;
    private String emailAddresses;
    private Users user;
    private Project project;
    private Collection<ExperimentError> experimentErrors;
    private Collection<ExperimentInput> experimentInputs;
    private Collection<ExperimentOutput> experimentOutputs;
    private Collection<ExperimentStatus> experimentStatuses;
    private Collection<Process> processes;
    private UserConfigurationData userConfigurationData;

    @Id
    @Column(name = "EXPERIMENT_ID")
    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    @Column(name = "GATEWAY_ID")
    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    @Column(name = "PROJECT_ID")
    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    @Column(name = "EXPERIMENT_TYPE")
    public String getExperimentType() {
        return experimentType;
    }

    public void setExperimentType(String experimentType) {
        this.experimentType = experimentType;
    }

    @Column(name = "USER_NAME")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Column(name = "EXPERIMENT_NAME")
    public String getExperimentName() {
        return experimentName;
    }

    public void setExperimentName(String experimentName) {
        this.experimentName = experimentName;
    }

    @Column(name = "CREATION_TIME")
    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    @Column(name = "DESCRIPTION")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "EXECUTION_ID")
    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    @Column(name = "GATEWAY_EXECUTION_ID")
    public String getGatewayExecutionId() {
        return gatewayExecutionId;
    }

    public void setGatewayExecutionId(String gatewayExecutionId) {
        this.gatewayExecutionId = gatewayExecutionId;
    }

    @Column(name = "GATEWAY_INSTANCE_ID")
    public String getGatewayInstanceId() {
        return gatewayInstanceId;
    }

    public void setGatewayInstanceId(String gatewayInstanceId) {
        this.gatewayInstanceId = gatewayInstanceId;
    }

    @Column(name = "ENABLE_EMAIL_NOTIFICATION")
    public Boolean getEnableEmailNotification() {
        return enableEmailNotification;
    }

    public void setEnableEmailNotification(Boolean enableEmailNotification) {
        this.enableEmailNotification = enableEmailNotification;
    }

    @Lob
    @Column(name = "EMAIL_ADDRESSES")
    public String getEmailAddresses() {
        return emailAddresses;
    }

    public void setEmailAddresses(String emailAddresses) {
        this.emailAddresses = emailAddresses;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        Experiment that = (Experiment) o;
//
//        if (creationTime != null ? !creationTime.equals(that.creationTime) : that.creationTime != null) return false;
//        if (description != null ? !description.equals(that.description) : that.description != null) return false;
//        if (emailAddresses != null ? !emailAddresses.equals(that.emailAddresses) : that.emailAddresses != null)
//            return false;
//        if (enableEmailNotification != null ? !enableEmailNotification.equals(that.enableEmailNotification) : that.enableEmailNotification != null)
//            return false;
//        if (executionId != null ? !executionId.equals(that.executionId) : that.executionId != null) return false;
//        if (experimentId != null ? !experimentId.equals(that.experimentId) : that.experimentId != null) return false;
//        if (experimentName != null ? !experimentName.equals(that.experimentName) : that.experimentName != null)
//            return false;
//        if (experimentType != null ? !experimentType.equals(that.experimentType) : that.experimentType != null)
//            return false;
//        if (gatewayExecutionId != null ? !gatewayExecutionId.equals(that.gatewayExecutionId) : that.gatewayExecutionId != null)
//            return false;
//        if (projectId != null ? !projectId.equals(that.projectId) : that.projectId != null) return false;
//        if (gatewayId != null ? !gatewayId.equals(that.gatewayId) : that.gatewayId != null) return false;
//        if (userName != null ? !userName.equals(that.userName) : that.userName != null) return false;
//
//        return true;
//    }
//
//    @Override
//    public int hashCode() {
//        int result = experimentId != null ? experimentId.hashCode() : 0;
//        result = 31 * result + (projectId != null ? projectId.hashCode() : 0);
//        result = 31 * result + (gatewayId != null ? gatewayId.hashCode() : 0);
//        result = 31 * result + (experimentType != null ? experimentType.hashCode() : 0);
//        result = 31 * result + (userName != null ? userName.hashCode() : 0);
//        result = 31 * result + (experimentName != null ? experimentName.hashCode() : 0);
//        result = 31 * result + (creationTime != null ? creationTime.hashCode() : 0);
//        result = 31 * result + (description != null ? description.hashCode() : 0);
//        result = 31 * result + (executionId != null ? executionId.hashCode() : 0);
//        result = 31 * result + (gatewayExecutionId != null ? gatewayExecutionId.hashCode() : 0);
//        result = 31 * result + (enableEmailNotification != null ? enableEmailNotification.hashCode() : 0);
//        result = 31 * result + (emailAddresses != null ? emailAddresses.hashCode() : 0);
//        return result;
//    }

    @ManyToOne
    @JoinColumn(name = "PROJECT_ID", referencedColumnName = "PROJECT_ID", nullable = false)
    public Project getProject() {
        return project;
    }

    public void setProject(Project projectByProjectId) {
        this.project = projectByProjectId;
    }

    @OneToMany(mappedBy = "experiment")
    public Collection<ExperimentError> getExperimentErrors() {
        return experimentErrors;
    }

    public void setExperimentErrors(Collection<ExperimentError> experimentErrorsByExperimentId) {
        this.experimentErrors = experimentErrorsByExperimentId;
    }

    @OneToMany(mappedBy = "experiment")
    public Collection<ExperimentInput> getExperimentInputs() {
        return experimentInputs;
    }

    public void setExperimentInputs(Collection<ExperimentInput> experimentInputsByExperimentId) {
        this.experimentInputs = experimentInputsByExperimentId;
    }

    @OneToMany(mappedBy = "experiment")
    public Collection<ExperimentOutput> getExperimentOutputs() {
        return experimentOutputs;
    }

    public void setExperimentOutputs(Collection<ExperimentOutput> experimentOutputsByExperimentId) {
        this.experimentOutputs = experimentOutputsByExperimentId;
    }

    @OneToMany(mappedBy = "experiment")
    public Collection<ExperimentStatus> getExperimentStatuses() {
        return experimentStatuses;
    }

    public void setExperimentStatuses(Collection<ExperimentStatus> experimentStatusesByExperimentId) {
        this.experimentStatuses = experimentStatusesByExperimentId;
    }

    @OneToMany(mappedBy = "experiment")
    public Collection<Process> getProcesses() {
        return processes;
    }

    public void setProcesses(Collection<Process> processesesByExperimentId) {
        this.processes = processesesByExperimentId;
    }

    @OneToOne(mappedBy = "experiment")
    public UserConfigurationData getUserConfigurationData() {
        return userConfigurationData;
    }

    public void setUserConfigurationData(UserConfigurationData userConfigurationDataByExperimentId) {
        this.userConfigurationData = userConfigurationDataByExperimentId;
    }


}