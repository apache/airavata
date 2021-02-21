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
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

/**
 * The persistent class for the process database table.
 */
@Entity
@Table(name = "PROCESS")
public class ProcessEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PROCESS_ID")
    private String processId;

    @Column(name = "EXPERIMENT_ID")
    private String experimentId;

    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;

    @Column(name = "LAST_UPDATE_TIME")
    private Timestamp lastUpdateTime;

    @Lob
    @Column(name = "PROCESS_DETAIL")
    private String processDetail;

    @Column(name = "APPLICATION_INTERFACE_ID")
    private String applicationInterfaceId;

    @Column(name = "APPLICATION_DEPLOYMENT_ID")
    private String applicationDeploymentId;

    @Column(name = "COMPUTE_RESOURCE_ID")
    private String computeResourceId;

    @Lob
    @Column(name = "TASK_DAG")
    private String taskDag;

    @Column(name = "GATEWAY_EXECUTION_ID")
    private String gatewayExecutionId;

    @Column(name = "ENABLE_EMAIL_NOTIFICATION")
    private boolean enableEmailNotification;

    @Lob
    @Column(name = "EMAIL_ADDRESSES")
    private String emailAddresses;

    @Column(name = "STORAGE_RESOURCE_ID")
    private String storageResourceId;

    @Column(name = "USER_DN")
    private String userDn;

    @Column(name = "GENERATE_CERT")
    private boolean generateCert;

    @Column(name = "EXPERIMENT_DATA_DIR", length = 512)
    private String experimentDataDir;

    @Column(name = "USERNAME")
    private String userName;

    @Column(name = "GROUP_RESOURCE_PROFILE_ID")
    private String groupResourceProfileId;

    @Column(name = "USE_USER_CR_PREF")
    private boolean useUserCRPref;

    @OneToMany(targetEntity = ProcessStatusEntity.class, cascade = CascadeType.ALL,
            mappedBy = "process", fetch = FetchType.EAGER)
    @OrderBy("timeOfStateChange ASC")
    private List<ProcessStatusEntity> processStatuses;

    @OneToMany(targetEntity = ProcessErrorEntity.class, cascade = CascadeType.ALL,
            mappedBy = "process", fetch = FetchType.EAGER)
    private List<ProcessErrorEntity> processErrors;

    @OneToMany(targetEntity = ProcessInputEntity.class, cascade = CascadeType.ALL,
            mappedBy = "process", fetch = FetchType.EAGER)
    private List<ProcessInputEntity> processInputs;

    @OneToMany(targetEntity = ProcessOutputEntity.class, cascade = CascadeType.ALL,
            mappedBy = "process", fetch = FetchType.EAGER)
    private List<ProcessOutputEntity> processOutputs;

    @OneToOne(targetEntity = ProcessResourceScheduleEntity.class, cascade = CascadeType.ALL,
            mappedBy = "process", fetch = FetchType.EAGER)
    private ProcessResourceScheduleEntity processResourceSchedule;

    @OneToMany(targetEntity = TaskEntity.class, cascade = CascadeType.ALL,
            mappedBy = "process", fetch = FetchType.EAGER)
    private List<TaskEntity> tasks;

    @ManyToOne(targetEntity = ExperimentEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "EXPERIMENT_ID", referencedColumnName = "EXPERIMENT_ID", nullable = false, updatable = false)
    private ExperimentEntity experiment;

    @OneToMany(targetEntity = ProcessWorkflowEntity.class, cascade = CascadeType.ALL,
            mappedBy = "process", fetch = FetchType.EAGER)
    private Collection<ProcessWorkflowEntity> processWorkflows;

    public ProcessEntity() {
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public Timestamp getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Timestamp lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getProcessDetail() {
        return processDetail;
    }

    public void setProcessDetail(String processDetail) {
        this.processDetail = processDetail;
    }

    public String getApplicationInterfaceId() {
        return applicationInterfaceId;
    }

    public void setApplicationInterfaceId(String applicationInterfaceId) {
        this.applicationInterfaceId = applicationInterfaceId;
    }

    public String getApplicationDeploymentId() {
        return applicationDeploymentId;
    }

    public void setApplicationDeploymentId(String applicationDeploymentId) {
        this.applicationDeploymentId = applicationDeploymentId;
    }

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    public String getTaskDag() {
        return taskDag;
    }

    public void setTaskDag(String taskDag) {
        this.taskDag = taskDag;
    }

    public String getGatewayExecutionId() {
        return gatewayExecutionId;
    }

    public void setGatewayExecutionId(String gatewayExecutionId) {
        this.gatewayExecutionId = gatewayExecutionId;
    }

    public boolean isEnableEmailNotification() {
        return enableEmailNotification;
    }

    public void setEnableEmailNotification(boolean enableEmailNotification) {
        this.enableEmailNotification = enableEmailNotification;
    }

    public String getEmailAddresses() {
        return emailAddresses;
    }

    public void setEmailAddresses(String emailAddresses) {
        this.emailAddresses = emailAddresses;
    }

    public String getStorageResourceId() {
        return storageResourceId;
    }

    public void setStorageResourceId(String storageResourceId) {
        this.storageResourceId = storageResourceId;
    }

    public String getUserDn() {
        return userDn;
    }

    public void setUserDn(String userDn) {
        this.userDn = userDn;
    }

    public boolean isGenerateCert() {
        return generateCert;
    }

    public void setGenerateCert(boolean generateCert) {
        this.generateCert = generateCert;
    }

    public String getExperimentDataDir() {
        return experimentDataDir;
    }

    public void setExperimentDataDir(String experimentDataDir) {
        this.experimentDataDir = experimentDataDir;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGroupResourceProfileId() {
        return groupResourceProfileId;
    }

    public void setGroupResourceProfileId(String groupResourceProfileId) {
        this.groupResourceProfileId = groupResourceProfileId;
    }

    public boolean isUseUserCRPref() {
        return useUserCRPref;
    }

    public void setUseUserCRPref(boolean useUserCRPref) {
        this.useUserCRPref = useUserCRPref;
    }

    public List<ProcessStatusEntity> getProcessStatuses() {
        return processStatuses;
    }

    public void setProcessStatuses(List<ProcessStatusEntity> processStatuses) {
        this.processStatuses = processStatuses;
    }

    public List<ProcessErrorEntity> getProcessErrors() {
        return processErrors;
    }

    public void setProcessErrors(List<ProcessErrorEntity> processErrors) {
        this.processErrors = processErrors;
    }

    public List<ProcessInputEntity> getProcessInputs() {
        return processInputs;
    }

    public void setProcessInputs(List<ProcessInputEntity> processInputs) {
        this.processInputs = processInputs;
    }

    public List<ProcessOutputEntity> getProcessOutputs() {
        return processOutputs;
    }

    public void setProcessOutputs(List<ProcessOutputEntity> processOutputs) {
        this.processOutputs = processOutputs;
    }

    public ProcessResourceScheduleEntity getProcessResourceSchedule() {
        return processResourceSchedule;
    }

    public void setProcessResourceSchedule(ProcessResourceScheduleEntity processResourceSchedule) {
        this.processResourceSchedule = processResourceSchedule;
    }

    public Collection<ProcessWorkflowEntity> getProcessWorkflows() {
        return processWorkflows;
    }

    public void setProcessWorkflows(Collection<ProcessWorkflowEntity> processWorkflows) {
        this.processWorkflows = processWorkflows;
    }

    public List<TaskEntity> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskEntity> tasks) {
        this.tasks = tasks;
    }

    public ExperimentEntity getExperiment() {
        return experiment;
    }

    public void setExperiment(ExperimentEntity experiment) {
        this.experiment = experiment;
    }
}
