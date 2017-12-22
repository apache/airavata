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
@Table(name = "PROCESS")
public class Process {
    private final static Logger logger = LoggerFactory.getLogger(Process.class);
    private String processId;
    private String experimentId;
    private Timestamp creationTime;
    private Timestamp lastUpdateTime;
    private String processDetail;
    private String applicationInterfaceId;
    private String taskDag;
    private String applicationDeploymentId;
    private String computeResourceId;
    private String gatewayExecutionId;
    private boolean enableEmailNotification;
    private String emailAddresses;
    private String storageId;
    private String experimentDataDir;
    private String userName;
    private Experiment experiment;
    private Collection<ProcessError> processErrors;
    private Collection<ProcessInput> processInputs;
    private Collection<ProcessOutput> processOutputs;
    private ProcessResourceSchedule processResourceSchedule;
    private Collection<ProcessStatus> processStatuses;
    private Collection<Task> tasks;
    private String userDn;
    private boolean generateCert;
    private boolean useUserCRPref;
    private Integer processTypeValue;

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
    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    @Column(name = "LAST_UPDATE_TIME")
    public Timestamp getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Timestamp lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @Lob
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

    @Column(name = "USERNAME")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Column(name = "STORAGE_RESOURCE_ID")
    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }

    @Lob
    @Column(name = "TASK_DAG")
    public String getTaskDag() {
        return taskDag;
    }

    public void setTaskDag(String taskDag) {
        this.taskDag = taskDag;
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

    @Column(name = "GATEWAY_EXECUTION_ID")
    public String getGatewayExecutionId() {
        return gatewayExecutionId;
    }

    public void setGatewayExecutionId(String gatewayExecutionId) {
        this.gatewayExecutionId = gatewayExecutionId;
    }

    @Column(name = "ENABLE_EMAIL_NOTIFICATION")
    public boolean getEnableEmailNotification() {
        return enableEmailNotification;
    }

    public void setEnableEmailNotification(boolean enableEmailNotification) {
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

    @Column(name = "USER_DN")
    public String getUserDn() {
        return userDn;
    }

    public void setUserDn(String userDn) {
        this.userDn = userDn;
    }

    @Column(name = "GENERATE_CERT")
    public boolean getGenerateCert() {
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

    //    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        Process process = (Process) o;
//
//        if (applicationInterfaceId != null ? !applicationInterfaceId.equals(process.applicationInterfaceId) : process.applicationInterfaceId != null)
//            return false;
//        if (creationTime != null ? !creationTime.equals(process.creationTime) : process.creationTime != null)
//            return false;
//        if (experimentId != null ? !experimentId.equals(process.experimentId) : process.experimentId != null)
//            return false;
//        if (lastUpdateTime != null ? !lastUpdateTime.equals(process.lastUpdateTime) : process.lastUpdateTime != null)
//            return false;
//        if (processDetail != null ? !processDetail.equals(process.processDetail) : process.processDetail != null)
//            return false;
//        if (processId != null ? !processId.equals(process.processId) : process.processId != null) return false;
//        if (taskDag != null ? !taskDag.equals(process.taskDag) : process.taskDag != null) return false;
//
//        return true;
//    }
//
//    @Override
//    public int hashCode() {
//        int result = processId != null ? processId.hashCode() : 0;
//        result = 31 * result + (experimentId != null ? experimentId.hashCode() : 0);
//        result = 31 * result + (creationTime != null ? creationTime.hashCode() : 0);
//        result = 31 * result + (lastUpdateTime != null ? lastUpdateTime.hashCode() : 0);
//        result = 31 * result + (processDetail != null ? processDetail.hashCode() : 0);
//        result = 31 * result + (applicationInterfaceId != null ? applicationInterfaceId.hashCode() : 0);
//        result = 31 * result + (taskDag != null ? taskDag.hashCode() : 0);
//        return result;
//    }

    @ManyToOne
    @JoinColumn(name = "EXPERIMENT_ID", referencedColumnName = "EXPERIMENT_ID")
    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experimentByExperimentId) {
        this.experiment = experimentByExperimentId;
    }

    @OneToMany(mappedBy = "process")
    public Collection<ProcessError> getProcessErrors() {
        return processErrors;
    }

    public void setProcessErrors(Collection<ProcessError> processErrorsByProcessId) {
        this.processErrors = processErrorsByProcessId;
    }

    @OneToMany(mappedBy = "process")
    public Collection<ProcessInput> getProcessInputs() {
        return processInputs;
    }

    public void setProcessInputs(Collection<ProcessInput> processInputsByProcessId) {
        this.processInputs = processInputsByProcessId;
    }

    @OneToMany(mappedBy = "process")
    public Collection<ProcessOutput> getProcessOutputs() {
        return processOutputs;
    }

    public void setProcessOutputs(Collection<ProcessOutput> processOutputsByProcessId) {
        this.processOutputs = processOutputsByProcessId;
    }

    @OneToOne(mappedBy = "process")
    public ProcessResourceSchedule getProcessResourceSchedule() {
        return processResourceSchedule;
    }

    public void setProcessResourceSchedule(ProcessResourceSchedule processResourceSchedulesByProcessId) {
        this.processResourceSchedule = processResourceSchedulesByProcessId;
    }

    @OneToMany(mappedBy = "process")
    public Collection<ProcessStatus> getProcessStatuses() {
        return processStatuses;
    }

    public void setProcessStatuses(Collection<ProcessStatus> processStatusesByProcessId) {
        this.processStatuses = processStatusesByProcessId;
    }

    @OneToMany(mappedBy = "process")
    public Collection<Task> getTasks() {
        return tasks;
    }

    public void setTasks(Collection<Task> taskByProcessId) {
        this.tasks = taskByProcessId;
    }

    @Column(name = "USE_USER_CR_PREF")
    public boolean isUseUserCRPref() {
        return useUserCRPref;
    }

    public void setUseUserCRPref(boolean useUserCRPref) {
        this.useUserCRPref = useUserCRPref;
    }

    @Column(name = "PROCESS_TYPE")
    public Integer getProcessTypeValue() {
        return processTypeValue;
    }

    public void setProcessTypeValue(Integer processTypeValue) {
        this.processTypeValue = processTypeValue;
    }
}