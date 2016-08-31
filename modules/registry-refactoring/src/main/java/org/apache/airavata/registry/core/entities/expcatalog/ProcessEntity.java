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
package org.apache.airavata.registry.core.entities.expcatalog;


import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "EXPCAT_PROCESS")
public class ProcessEntity {
    private String processId;
    private String experimentId;
    private long creationTime;
    private long lastUpdateTime;
    private String processDetail;
    private String applicationInterfaceId;
    private String applicationDeploymentId;
    private String computeResourceId;
    private String taskDag;
    private String gatewayExecutionId;
    private boolean enableEmailNotification;
    private List<String> emailAddresses;
    private String storageResourceId;
    private String userDn;
    private boolean generateCert;
    private String experimentDataDir;
    private String userName;

    private List<ProcessStatusEntity> processStatuses;
    private List<ProcessErrorEntity> processErrors;
    private List<ProcessInputEntity> processInputs;
    private List<ProcessOutputEntity> processOutputs;
    private ProcessResourceSchedulingEntity processResourceSchedule;
    private List<TaskEntity> tasks;

    private ExperimentEntity experiment;

    @Id
    @Column(name = "PROCESS_ID")
    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    @Column(name = "EXPERIMENT_ID")
    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    @Column(name = "CREATION_TIME")
    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    @Column(name = "LAST_UPDATE_TIME")
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @Column(name = "PROCESS_DETAIL")
    public String getProcessDetail() {
        return processDetail;
    }

    public void setProcessDetail(String processDetail) {
        this.processDetail = processDetail;
    }

    @Column(name = "APPLICATION_INTERFACE_ID")
    public String getApplicationInterfaceId() {
        return applicationInterfaceId;
    }

    public void setApplicationInterfaceId(String applicationInterfaceId) {
        this.applicationInterfaceId = applicationInterfaceId;
    }

    @Column(name = "APPLICATION_DEPLOYMENT_ID")
    public String getApplicationDeploymentId() {
        return applicationDeploymentId;
    }

    public void setApplicationDeploymentId(String applicationDeploymentId) {
        this.applicationDeploymentId = applicationDeploymentId;
    }


    @Column(name = "COMPUTE_RESOURCE_ID")
    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    @Column(name = "TASK_DAG")
    public String getTaskDag() {
        return taskDag;
    }

    public void setTaskDag(String taskDag) {
        this.taskDag = taskDag;
    }

    @Column(name = "GATEWAY_EXECUTION_ID")
    public String getGatewayExecutionId() {
        return gatewayExecutionId;
    }

    public void setGatewayExecutionId(String gatewayExecutionId) {
        this.gatewayExecutionId = gatewayExecutionId;
    }

    @Column(name = "ENABLE_EMAIL_NOTIFICATION")
    public boolean isEnableEmailNotification() {
        return enableEmailNotification;
    }

    public void setEnableEmailNotification(boolean enableEmailNotification) {
        this.enableEmailNotification = enableEmailNotification;
    }

    @ElementCollection
    @CollectionTable(name="PROCESS_EMAIL", joinColumns = @JoinColumn(name="PROCESS_ID"))
    public List<String> getEmailAddresses() {
        return emailAddresses;
    }

    public void setEmailAddresses(List<String> emailAddresses) {
        this.emailAddresses = emailAddresses;
    }

    @Column(name = "STORAGE_RESOURCE_ID")
    public String getStorageResourceId() {
        return storageResourceId;
    }

    public void setStorageResourceId(String storageResourceId) {
        this.storageResourceId = storageResourceId;
    }

    @Column(name = "USER_DN")
    public String getUserDn() {
        return userDn;
    }

    public void setUserDn(String userDn) {
        this.userDn = userDn;
    }

    @Column(name = "GENERATE_CERT")
    public boolean isGenerateCert() {
        return generateCert;
    }

    public void setGenerateCert(boolean generateCert) {
        this.generateCert = generateCert;
    }

    @Column(name = "EXPERIMENT_DATA_DIR")
    public String getExperimentDataDir() {
        return experimentDataDir;
    }

    public void setExperimentDataDir(String experimentDataDir) {
        this.experimentDataDir = experimentDataDir;
    }

    @Column(name = "USER_NAME")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @OneToMany(targetEntity = ProcessStatusEntity.class, cascade = CascadeType.ALL, mappedBy = "process")
    public List<ProcessStatusEntity> getProcessStatuses() {
        return processStatuses;
    }

    public void setProcessStatuses(List<ProcessStatusEntity> processStatus) {
        this.processStatuses = processStatus;
    }

    @OneToMany(targetEntity = ProcessErrorEntity.class, cascade = CascadeType.ALL, mappedBy = "process")
    public List<ProcessErrorEntity> getProcessErrors() {
        return processErrors;
    }

    public void setProcessErrors(List<ProcessErrorEntity> processError) {
        this.processErrors = processError;
    }

    @OneToMany(targetEntity = ProcessInputEntity.class, cascade = CascadeType.ALL, mappedBy = "process")
    public List<ProcessInputEntity> getProcessInputs() {
        return processInputs;
    }

    public void setProcessInputs(List<ProcessInputEntity> processInputs) {
        this.processInputs = processInputs;
    }

    @OneToMany(targetEntity = ProcessOutputEntity.class, cascade = CascadeType.ALL, mappedBy = "process")
    public List<ProcessOutputEntity> getProcessOutputs() {
        return processOutputs;
    }

    public void setProcessOutputs(List<ProcessOutputEntity> processOutputs) {
        this.processOutputs = processOutputs;
    }

    @OneToOne(targetEntity = ProcessResourceSchedulingEntity.class, cascade = CascadeType.ALL, mappedBy = "process")
    public ProcessResourceSchedulingEntity getProcessResourceSchedule() {
        return processResourceSchedule;
    }

    public void setProcessResourceSchedule(ProcessResourceSchedulingEntity proceeResourceSchedule) {
        this.processResourceSchedule = proceeResourceSchedule;
    }

    @OneToMany(targetEntity = TaskEntity.class, cascade = CascadeType.ALL, mappedBy = "process")
    public List<TaskEntity> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskEntity> tasks) {
        this.tasks = tasks;
    }

    @ManyToOne(targetEntity = ExperimentEntity.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "EXPERIMENT_ID", referencedColumnName = "EXPERIMENT_ID")
    public ExperimentEntity getExperiment() {
        return experiment;
    }

    public void setExperiment(ExperimentEntity experiment) {
        this.experiment = experiment;
    }
}