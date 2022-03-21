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
 */
package org.apache.airavata.helix.impl.task;

import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.helix.core.util.TaskUtil;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.BatchQueue;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.computeresource.LOCALSubmission;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManager;
import org.apache.airavata.model.appcatalog.computeresource.SSHJobSubmission;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.gatewayprofile.StoragePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.ComputeResourceReservation;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupResourceProfile;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserStoragePreference;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.data.movement.DataMovementInterface;
import org.apache.airavata.model.data.movement.DataMovementProtocol;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.model.status.TaskStatus;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.model.util.GroupComputeResourcePreferenceUtil;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.service.profile.user.cpi.UserProfileService;
import org.apache.airavata.service.security.AiravataSecurityManager;
import org.apache.airavata.service.security.SecurityManagerFactory;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Note: process context property use lazy loading approach. In runtime you will see some properties as null
 * unless you have access it previously. Once that property access using the api,it will be set to correct value.
 */
public class TaskContext {

    private final static Logger logger = LoggerFactory.getLogger(TaskContext.class);

    private Publisher statusPublisher;
    private RegistryService.Client registryClient;
    private UserProfileService.Client profileClient;

    private String processId;
    private String gatewayId;
    private String taskId;

    private ProcessModel processModel;
    private JobModel jobModel;
    private Object subTaskModel = null;

    private String workingDir;
    private String scratchLocation;
    private String inputDir;
    private String outputDir;
    private String stdoutLocation;
    private String stderrLocation;

    private GatewayResourceProfile gatewayResourceProfile;
    private UserResourceProfile userResourceProfile;
    private GroupResourceProfile groupResourceProfile;
    private UserProfile userProfile;

    private StoragePreference gatewayStorageResourcePreference;
    private UserComputeResourcePreference userComputeResourcePreference;
    private UserStoragePreference userStoragePreference;
    private GroupComputeResourcePreference groupComputeResourcePreference;

    private ComputeResourceDescription computeResourceDescription;
    private ApplicationDeploymentDescription applicationDeploymentDescription;
    private ApplicationInterfaceDescription applicationInterfaceDescription;
    private StorageResourceDescription storageResourceDescription;

    private JobSubmissionProtocol jobSubmissionProtocol;
    private DataMovementProtocol dataMovementProtocol;
    private ResourceJobManager resourceJobManager;

    private List<String> taskExecutionOrder;
    private List<TaskModel> taskList;
    private Map<String, TaskModel> taskMap;

    /**
     * Note: process context property use lazy loading approach. In runtime you will see some properties as null
     * unless you have access it previously. Once that property access using the api,it will be set to correct value.
     */
    private TaskContext(String processId, String gatewayId, String taskId) {
        this.processId = processId;
        this.gatewayId = gatewayId;
        this.taskId = taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getProcessId() {
        return processId;
    }

    public String getTaskId() {
        return taskId;
    }

    public Publisher getStatusPublisher() {
        return statusPublisher;
    }

    public void setStatusPublisher(Publisher statusPublisher) {
        this.statusPublisher = statusPublisher;
    }

    public ProcessModel getProcessModel() {
        return processModel;
    }

    public void setProcessModel(ProcessModel processModel) {
        this.processModel = processModel;
    }

    public String getWorkingDir() throws Exception {
        if (workingDir == null) {
            if (processModel.getProcessResourceSchedule().getStaticWorkingDir() != null) {
                workingDir = processModel.getProcessResourceSchedule().getStaticWorkingDir();
            } else {
                String scratchLocation = getScratchLocation();
                workingDir = (scratchLocation.endsWith("/") ? scratchLocation + processId : scratchLocation + "/" +
                        processId);
            }
        }
        return workingDir;
    }

    public String getScratchLocation() throws Exception {
        if (scratchLocation == null) {
            if (isUseUserCRPref() &&
                    getUserComputeResourcePreference() != null &&
                    isValid(getUserComputeResourcePreference().getScratchLocation())) {
                scratchLocation = getUserComputeResourcePreference().getScratchLocation();
            } else if (isValid(processModel.getProcessResourceSchedule().getOverrideScratchLocation())) {
                scratchLocation = processModel.getProcessResourceSchedule().getOverrideScratchLocation();
            } else if (isSetGroupResourceProfile() && getGroupComputeResourcePreference() != null &&
                    isValid(getGroupComputeResourcePreference().getScratchLocation())) {
                scratchLocation = getGroupComputeResourcePreference().getScratchLocation();
            } else {
                throw new RuntimeException("Can't find a specified scratch location for compute resource " + getComputeResourceId());
            }
        }
        return scratchLocation;
    }


    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public GatewayResourceProfile getGatewayResourceProfile() throws Exception {
        if (this.groupResourceProfile == null) {
            try {
                gatewayResourceProfile = registryClient.getGatewayResourceProfile(gatewayId);
            } catch (TException e) {
                logger.error("Failed to fetch gateway resource profile for gateway {}", gatewayId);
                throw e;
            }
        }
        return gatewayResourceProfile;
    }

    public void setGatewayResourceProfile(GatewayResourceProfile gatewayResourceProfile) {
        this.gatewayResourceProfile = gatewayResourceProfile;
    }

    public GroupResourceProfile getGroupResourceProfile() throws Exception {
        if (groupResourceProfile == null) {
            try {
                groupResourceProfile = registryClient.getGroupResourceProfile(processModel.getGroupResourceProfileId());
            } catch (TException e) {
                logger.error("Failed to find a group resource proifle with id {}", processModel.getGroupResourceProfileId());
                throw e;
            }
        }
        return groupResourceProfile;
    }

    public void setGroupResourceProfile(GroupResourceProfile groupResourceProfile) {
        this.groupResourceProfile = groupResourceProfile;
    }

    public GroupComputeResourcePreference getGroupComputeResourcePreference() throws Exception {

        if (groupComputeResourcePreference == null) {
            try {
                groupComputeResourcePreference = registryClient.getGroupComputeResourcePreference(
                        processModel.getComputeResourceId(),
                        processModel.getGroupResourceProfileId());
            } catch (TException e) {
                logger.error("Failed to find group compute resource preference for compute  {}, and group resource profile {}",
                        processModel.getComputeResourceId(), processModel.getGroupResourceProfileId());
                throw e;
            }
        }
        return groupComputeResourcePreference;
    }

    public void setGroupComputeResourcePreference(GroupComputeResourcePreference groupComputeResourcePreference) {
        this.groupComputeResourcePreference = groupComputeResourcePreference;
    }

    public UserResourceProfile getUserResourceProfile() throws Exception {

        if (userResourceProfile == null && processModel.isUseUserCRPref()) {
            try {
                this.userResourceProfile = registryClient.getUserResourceProfile(processModel.getUserName(), gatewayId);
            } catch (TException e) {
                logger.error("Failed to fetch user resource profile for user {} in gateway {}",
                        processModel.getUserName(), gatewayId, e);
                throw e;
            }
        }
        return userResourceProfile;
    }

    public void setUserResourceProfile(UserResourceProfile userResourceProfile) {
        this.userResourceProfile = userResourceProfile;
    }

    private UserComputeResourcePreference getUserComputeResourcePreference() throws Exception {
        if (this.userComputeResourcePreference == null && processModel.isUseUserCRPref()) {
            try {
                this.userComputeResourcePreference = registryClient.getUserComputeResourcePreference(
                        processModel.getUserName(),
                        gatewayId,
                        processModel.getComputeResourceId());
            } catch (TException e) {
                logger.error("Failed to fetch user compute resource preference for user {} compute resource {} in gateway {}",
                        processModel.getUserName(), processModel.getComputeResourceId(), gatewayId, e);
                throw  e;
            }
        }
        return this.userComputeResourcePreference;
    }

    public void setUserComputeResourcePreference(UserComputeResourcePreference userComputeResourcePreference) {
        this.userComputeResourcePreference = userComputeResourcePreference;
    }

    public UserStoragePreference getUserStoragePreference() {
        return userStoragePreference;
    }

    public void setUserStoragePreference(UserStoragePreference userStoragePreference) {
        this.userStoragePreference = userStoragePreference;
    }

    public StoragePreference getGatewayStorageResourcePreference() throws Exception {
        if (this.gatewayStorageResourcePreference == null) {
            try {
                this.gatewayStorageResourcePreference = registryClient.getGatewayStoragePreference(
                        gatewayId,
                        processModel.getStorageResourceId());
            } catch (TException e) {
                logger.error("Failed to fetch gateway storage preference for gateway {} and storage {}",
                        gatewayId,
                        processModel.getStorageResourceId(), e);
                throw e;
            }
        }
        return gatewayStorageResourcePreference;
    }

    public void setGatewayStorageResourcePreference(StoragePreference gatewayStorageResourcePreference) {
        this.gatewayStorageResourcePreference = gatewayStorageResourcePreference;
    }

    public ComputeResourceDescription getComputeResourceDescription() throws Exception {
        if (this.computeResourceDescription == null) {
            this.computeResourceDescription = registryClient.getComputeResource(getComputeResourceId());
        }
        return computeResourceDescription;
    }

    public void setComputeResourceDescription(ComputeResourceDescription computeResourceDescription) {
        this.computeResourceDescription = computeResourceDescription;
    }

    public ApplicationDeploymentDescription getApplicationDeploymentDescription() throws Exception {
        if (this.applicationDeploymentDescription == null) {
            try {
                this.applicationDeploymentDescription = registryClient.getApplicationDeployment(
                        processModel.getApplicationDeploymentId());
            } catch (TException e) {
                logger.error("Failed to fetch application deployment with id {}",
                        processModel.getApplicationDeploymentId(), e);
                throw e;
            }
        }
        return applicationDeploymentDescription;
    }

    public void setApplicationDeploymentDescription(ApplicationDeploymentDescription
                                                            applicationDeploymentDescription) {
        this.applicationDeploymentDescription = applicationDeploymentDescription;
    }

    public ApplicationInterfaceDescription getApplicationInterfaceDescription() throws Exception {
        if (this.applicationInterfaceDescription == null) {
            try {
                this.applicationInterfaceDescription = registryClient.getApplicationInterface(
                        processModel.getApplicationInterfaceId());
            } catch (TException e) {
                logger.error("Failed to fetch application interface with id {}",
                        processModel.getApplicationInterfaceId(), e);
                throw e;
            }
        }
        return applicationInterfaceDescription;
    }

    public void setApplicationInterfaceDescription(ApplicationInterfaceDescription applicationInterfaceDescription) {
        this.applicationInterfaceDescription = applicationInterfaceDescription;
    }

    public String getStdoutLocation() throws Exception {
        if (stdoutLocation == null) {
            List<OutputDataObjectType> applicationOutputs = getApplicationInterfaceDescription().getApplicationOutputs();
            if (applicationOutputs != null && !applicationOutputs.isEmpty()) {
                for (OutputDataObjectType outputDataObjectType : applicationOutputs) {
                    if (outputDataObjectType.getType().equals(DataType.STDOUT)) {
                        if (outputDataObjectType.getValue() == null || outputDataObjectType.getValue().equals("")) {
                            String stdOut = (getWorkingDir().endsWith(File.separator) ? getWorkingDir() : getWorkingDir() + File.separator)
                                    + getApplicationInterfaceDescription().getApplicationName() + ".stdout";
                            outputDataObjectType.setValue(stdOut);
                            stdoutLocation = stdOut;
                        } else {
                            stdoutLocation = outputDataObjectType.getValue();
                        }
                    }
                }
            }
        }
        return stdoutLocation;
    }

    public void setStdoutLocation(String stdoutLocation) {
        this.stdoutLocation = stdoutLocation;
    }

    public String getStderrLocation() throws Exception {
        if (stderrLocation == null) {
            List<OutputDataObjectType> applicationOutputs = getApplicationInterfaceDescription().getApplicationOutputs();
            if (applicationOutputs != null && !applicationOutputs.isEmpty()) {
                for (OutputDataObjectType outputDataObjectType : applicationOutputs) {
                    if (outputDataObjectType.getType().equals(DataType.STDERR)) {
                        if (outputDataObjectType.getValue() == null || outputDataObjectType.getValue().equals("")) {
                            String stderrLocation = (getWorkingDir().endsWith(File.separator) ? getWorkingDir() : getWorkingDir() + File.separator)
                                    + getApplicationInterfaceDescription().getApplicationName() + ".stderr";
                            outputDataObjectType.setValue(stderrLocation);
                            this.stderrLocation = stderrLocation;
                        } else {
                            this.stderrLocation = outputDataObjectType.getValue();
                        }
                    }
                }
            }
        }
        return stderrLocation;
    }

    public void setStderrLocation(String stderrLocation) {
        this.stderrLocation = stderrLocation;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public String getOutputDir() throws Exception {
        if (outputDir == null) {
            outputDir = getWorkingDir();
        }
        return outputDir;
    }

    public String getInputDir() throws Exception {
        if (inputDir == null) {
            inputDir = getWorkingDir();
        }
        return inputDir;
    }

    public void setInputDir(String inputDir) {
        this.inputDir = inputDir;
    }

    public JobSubmissionProtocol getJobSubmissionProtocol() throws Exception {
        if (jobSubmissionProtocol == null) {
            // Take highest priority one
            List<JobSubmissionInterface> jobSubmissionInterfaces = getComputeResourceDescription()
                    .getJobSubmissionInterfaces();
            Collections.sort(jobSubmissionInterfaces, Comparator.comparingInt(JobSubmissionInterface::getPriorityOrder));
            jobSubmissionProtocol = jobSubmissionInterfaces.get(0).getJobSubmissionProtocol();
        }
        return jobSubmissionProtocol;
    }

    public void setJobSubmissionProtocol(JobSubmissionProtocol jobSubmissionProtocol) {
        this.jobSubmissionProtocol = jobSubmissionProtocol;
    }

    public DataMovementProtocol getDataMovementProtocol() throws Exception {
        if (dataMovementProtocol == null) {
            // Take highest priority one
            List<DataMovementInterface> dataMovementInterfaces = getComputeResourceDescription().getDataMovementInterfaces();
            Collections.sort(dataMovementInterfaces, Comparator.comparingInt(DataMovementInterface::getPriorityOrder));
            dataMovementProtocol = dataMovementInterfaces.get(0).getDataMovementProtocol();
        }
        return dataMovementProtocol;
    }

    public void setDataMovementProtocol(DataMovementProtocol dataMovementProtocol) {
        this.dataMovementProtocol = dataMovementProtocol;
    }

    public String getTaskDag() {
        return getProcessModel().getTaskDag();
    }

    public List<TaskModel> getTaskList() {
        if (taskList == null) {
            synchronized (TaskModel.class){
                if (taskList == null) {
                    taskList = getProcessModel().getTasks();
                }
            }
        }
        return taskList;
    }


    public List<String> getTaskExecutionOrder() {
        return taskExecutionOrder;
    }

    public void setTaskExecutionOrder(List<String> taskExecutionOrder) {
        this.taskExecutionOrder = taskExecutionOrder;
    }

    public Map<String, TaskModel> getTaskMap() {
        if (taskMap == null) {
            synchronized (TaskModel.class) {
                if (taskMap == null) {
                    taskMap = new HashMap<>();
                    for (TaskModel taskModel : getTaskList()) {
                        taskMap.put(taskModel.getTaskId(), taskModel);
                    }
                }
            }
        }
        return taskMap;
    }

    public JobModel getJobModel() throws Exception {
        if (jobModel == null) {
            jobModel = new JobModel();
            jobModel.setProcessId(processId);
            jobModel.setWorkingDir(getWorkingDir());
            jobModel.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());
        }
        return jobModel;
    }

    public void setJobModel(JobModel jobModel) {
        this.jobModel = jobModel;
    }

    public ProcessState getProcessState() {
        if(processModel.getProcessStatuses() != null && processModel.getProcessStatuses().size() > 0)
            return processModel.getProcessStatuses().get(0).getState();
        else
            return null;
    }

    public void setProcessStatus(ProcessStatus status) {
        if (status != null) {
            logger.info("expId: {}, processId: {} :- Process status changed {} -> {}", getExperimentId(), processId,
                    getProcessState().name(), status.getState().name());
            List<ProcessStatus> processStatuses = new ArrayList<>();
            processStatuses.add(status);
            processModel.setProcessStatuses(processStatuses);
        }
    }

    public ProcessStatus getProcessStatus(){
        if(processModel.getProcessStatuses() != null)
            return processModel.getProcessStatuses().get(0);
        else
            return null;
    }

    public TaskState getTaskState() {
        if(getCurrentTaskModel() != null && getCurrentTaskModel().getTaskStatuses() != null) {
            return getCurrentTaskModel().getTaskStatuses().get(0).getState();
        } else {
            return null;
        }
    }

    public TaskStatus getTaskStatus() {
        if(getCurrentTaskModel().getTaskStatuses() != null)
            return getCurrentTaskModel().getTaskStatuses().get(0);
        else
            return null;
    }

    public String getComputeResourceId() throws Exception {
        if (isUseUserCRPref() &&
                getUserComputeResourcePreference() != null &&
                isValid(getUserComputeResourcePreference().getComputeResourceId())) {
            return getUserComputeResourcePreference().getComputeResourceId();
        } else {
            return getGroupComputeResourcePreference().getComputeResourceId();
        }
    }

    public String getComputeResourceCredentialToken() throws Exception {
        if (isUseUserCRPref()) {
            if (getUserComputeResourcePreference() != null &&
                    isValid(getUserComputeResourcePreference().getResourceSpecificCredentialStoreToken())) {
                return getUserComputeResourcePreference().getResourceSpecificCredentialStoreToken();
            } else {
                return getUserResourceProfile().getCredentialStoreToken();
            }
        }  else if (isSetGroupResourceProfile() &&
                getGroupComputeResourcePreference() != null &&
                isValid(getGroupComputeResourcePreference().getResourceSpecificCredentialStoreToken())) {
            return getGroupComputeResourcePreference().getResourceSpecificCredentialStoreToken();
        } else {
            return getGroupResourceProfile().getDefaultCredentialStoreToken();
        }
    }

    public String getStorageResourceCredentialToken() throws Exception {
        if (isValid(getGatewayStorageResourcePreference().getResourceSpecificCredentialStoreToken())) {
            return getGatewayStorageResourcePreference().getResourceSpecificCredentialStoreToken();
        } else {
            return getGatewayResourceProfile().getCredentialStoreToken();
        }
    }

    public JobSubmissionProtocol getPreferredJobSubmissionProtocol() throws Exception {
        return getJobSubmissionProtocol();
    }

    public DataMovementProtocol getPreferredDataMovementProtocol() throws Exception {
        return getDataMovementProtocol();
    }

    public void setResourceJobManager(ResourceJobManager resourceJobManager) {
        this.resourceJobManager = resourceJobManager;
    }

    public ResourceJobManager getResourceJobManager() throws Exception {

        if (this.resourceJobManager == null) {
            JobSubmissionInterface jsInterface = getPreferredJobSubmissionInterface();

            if (jsInterface == null) {
                throw new Exception("Job Submission interface cannot be empty at this point");

            } else if (jsInterface.getJobSubmissionProtocol() == JobSubmissionProtocol.SSH) {
                SSHJobSubmission sshJobSubmission = getRegistryClient()
                        .getSSHJobSubmission(jsInterface.getJobSubmissionInterfaceId());
                resourceJobManager = sshJobSubmission.getResourceJobManager();

            } else if (jsInterface.getJobSubmissionProtocol() == JobSubmissionProtocol.LOCAL) {
                LOCALSubmission localSubmission = getRegistryClient()
                        .getLocalJobSubmission(jsInterface.getJobSubmissionInterfaceId());
                resourceJobManager = localSubmission.getResourceJobManager();

            } else if (jsInterface.getJobSubmissionProtocol() == JobSubmissionProtocol.SSH_FORK) {
                SSHJobSubmission sshJobSubmission = getRegistryClient()
                        .getSSHJobSubmission(jsInterface.getJobSubmissionInterfaceId());
                resourceJobManager = sshJobSubmission.getResourceJobManager();

            } else if (jsInterface.getJobSubmissionProtocol() == JobSubmissionProtocol.CLOUD) {
                return null;

            } else {
                throw new Exception("Unsupported JobSubmissionProtocol - " + jsInterface.getJobSubmissionProtocol()
                        .name());
            }

            if (resourceJobManager == null) {
                throw new Exception("Resource Job Manager is empty.");
            }
        }
        return this.resourceJobManager;
    }

    public String getExperimentId() {
        return processModel.getExperimentId();
    }

    public StorageResourceDescription getStorageResourceDescription() throws Exception {
        if (this.storageResourceDescription == null) {
            this.storageResourceDescription = registryClient.getStorageResource(getStorageResourceId());
        }
        return this.storageResourceDescription;
    }

    public void setStorageResourceDescription(StorageResourceDescription storageResourceDescription) {
        this.storageResourceDescription = storageResourceDescription;
    }

    public boolean isUseUserCRPref() {
        return getProcessModel().isUseUserCRPref();
    }

    public boolean isSetGroupResourceProfile() {
        return getProcessModel().isSetGroupResourceProfileId();
    }

    public String getComputeResourceLoginUserName() throws Exception {
        if (isUseUserCRPref() &&
                getUserComputeResourcePreference() != null &&
                isValid(getUserComputeResourcePreference().getLoginUserName())) {
            return getUserComputeResourcePreference().getLoginUserName();
        } else if (isValid(processModel.getProcessResourceSchedule().getOverrideLoginUserName())) {
            return processModel.getProcessResourceSchedule().getOverrideLoginUserName();
        } else if (isSetGroupResourceProfile() &&
                getGroupComputeResourcePreference() != null &&
                isValid(getGroupComputeResourcePreference().getLoginUserName())){
            return getGroupComputeResourcePreference().getLoginUserName();
        }
        throw new RuntimeException("Can't find login username for compute resource");
    }

    public String getStorageResourceLoginUserName() throws Exception {
        return getGatewayStorageResourcePreference().getLoginUserName();
    }

    public String getStorageFileSystemRootLocation() throws Exception {
        return getGatewayStorageResourcePreference().getFileSystemRootLocation();
    }

    public String getStorageResourceId() throws Exception {
        return getGatewayStorageResourcePreference().getStorageResourceId();
    }

    private ComputationalResourceSchedulingModel getProcessCRSchedule() {
        if (getProcessModel() != null) {
            return getProcessModel().getProcessResourceSchedule();
        } else {
            return null;
        }
    }

    public void setRegistryClient(RegistryService.Client registryClient) {
        this.registryClient = registryClient;
    }

    public RegistryService.Client getRegistryClient() {
        return registryClient;
    }

    public UserProfileService.Client getProfileClient() {
        return profileClient;
    }

    public void setProfileClient(UserProfileService.Client profileClient) {
        this.profileClient = profileClient;
    }

    public UserProfile getUserProfile() throws TaskOnFailException {

        if (this.userProfile == null) {
            try {
                AiravataSecurityManager securityManager = SecurityManagerFactory.getSecurityManager();
                AuthzToken authzToken = securityManager.getUserManagementServiceAccountAuthzToken(getGatewayId());
                this.userProfile = getProfileClient().getUserProfileById(authzToken, getProcessModel().getUserName(), getGatewayId());
            } catch (Exception e) {
                logger.error("Failed to fetch the user profile for user id " + processModel.getUserName(), e);
                throw new TaskOnFailException("Failed to fetch the user profile for user id " + processModel.getUserName(), true, e);
            }
        }
        return this.userProfile;
    }

    private boolean isValid(String str) {
        return str != null && !str.trim().isEmpty();
    }

    public String getAllocationProjectNumber() throws Exception {
        if (isValid(processModel.getProcessResourceSchedule().getOverrideAllocationProjectNumber())) {
            return processModel.getProcessResourceSchedule().getOverrideAllocationProjectNumber();
        } else if (isUseUserCRPref() &&
                getUserComputeResourcePreference() != null &&
                getUserComputeResourcePreference().getAllocationProjectNumber() != null) {
            return getUserComputeResourcePreference().getAllocationProjectNumber();
        } else if (isSetGroupResourceProfile() &&
                getGroupComputeResourcePreference() != null &&
                isValid(getGroupComputeResourcePreference().getAllocationProjectNumber())){
            return getGroupComputeResourcePreference().getAllocationProjectNumber();
        } else {
            return null;
        }
    }

    public String getReservation() throws Exception {
        long start = 0, end = 0;
        String reservation = null;
        if (isUseUserCRPref() &&
                getUserComputeResourcePreference() != null &&
                isValid(getUserComputeResourcePreference().getReservation())) {
            reservation = getUserComputeResourcePreference().getReservation();
            start = getUserComputeResourcePreference().getReservationStartTime();
            end = getUserComputeResourcePreference().getReservationEndTime();
        }
        if (reservation != null && start > 0 && start < end) {
            long now = Calendar.getInstance().getTimeInMillis();
            if (now > start && now < end) {
                return reservation;
            }
        }
        String queueName = getQueueName();
        ComputeResourceReservation computeResourceReservation = GroupComputeResourcePreferenceUtil
                .getActiveReservationForQueue(getGroupComputeResourcePreference(), queueName);
        if (computeResourceReservation != null) {
            return computeResourceReservation.getReservationName();
        }
        return null;
    }

    public String getQualityOfService() throws Exception {
        if (isUseUserCRPref() &&
                getUserComputeResourcePreference() != null &&
                isValid(getUserComputeResourcePreference().getQualityOfService())) {
            return getUserComputeResourcePreference().getQualityOfService();
        } else {
            return getGroupComputeResourcePreference().getQualityOfService();
        }
    }


    public String getQueueName() throws Exception {
        if (isUseUserCRPref() &&
                getUserComputeResourcePreference() != null &&
                isValid(getUserComputeResourcePreference().getPreferredBatchQueue())) {
            return getUserComputeResourcePreference().getPreferredBatchQueue();
        } else if (isValid(processModel.getProcessResourceSchedule().getQueueName())) {
            return processModel.getProcessResourceSchedule().getQueueName();
        }  else {
            Optional<BatchQueue> defaultQueue = getComputeResourceDescription().getBatchQueues().stream().filter(q -> q.isIsDefaultQueue()).findFirst();
            if (defaultQueue.isPresent()) {
                return defaultQueue.get().getQueueName();
            } else {
                throw new RuntimeException("Can't find default queue for resource " + getComputeResourceDescription().getComputeResourceId());
            }
        }
    }

    public List<String> getQueueSpecificMacros() throws Exception {
        String queueName = getProcessCRSchedule().getQueueName();
        Optional<BatchQueue> queue = getComputeResourceDescription().getBatchQueues().stream()
                .filter(x->x.getQueueName().equals(queueName)).findFirst();
        if(queue.isPresent()){
            if(queue.get().getQueueSpecificMacros() != null && !queue.get().getQueueSpecificMacros().equals("")){
                return Arrays.asList(queue.get().getQueueSpecificMacros().split(","));
            }
        }
        return null;
    }

    public JobSubmissionInterface getPreferredJobSubmissionInterface() throws Exception {
        JobSubmissionProtocol preferredJobSubmissionProtocol = getJobSubmissionProtocol();
        ComputeResourceDescription resourceDescription = getComputeResourceDescription();
        List<JobSubmissionInterface> jobSubmissionInterfaces = resourceDescription.getJobSubmissionInterfaces();
        Map<JobSubmissionProtocol, List<JobSubmissionInterface>> orderedInterfaces = new HashMap<>();
        List<JobSubmissionInterface> interfaces = new ArrayList<>();
        if (jobSubmissionInterfaces != null && !jobSubmissionInterfaces.isEmpty()) {
            for (JobSubmissionInterface submissionInterface : jobSubmissionInterfaces){

                if (preferredJobSubmissionProtocol != null){
                    if (preferredJobSubmissionProtocol.toString().equals(submissionInterface.getJobSubmissionProtocol().toString())){
                        if (orderedInterfaces.containsKey(submissionInterface.getJobSubmissionProtocol())){
                            List<JobSubmissionInterface> interfaceList = orderedInterfaces.get(submissionInterface.getJobSubmissionProtocol());
                            interfaceList.add(submissionInterface);
                        }else {
                            interfaces.add(submissionInterface);
                            orderedInterfaces.put(submissionInterface.getJobSubmissionProtocol(), interfaces);
                        }
                    }
                }else {
                    jobSubmissionInterfaces.sort(Comparator.comparingInt(JobSubmissionInterface::getPriorityOrder));
                }
            }
            interfaces = orderedInterfaces.get(preferredJobSubmissionProtocol);
            interfaces.sort(Comparator.comparingInt(JobSubmissionInterface::getPriorityOrder));
        } else {
            throw new TaskOnFailException("Compute resource should have at least one job submission interface defined...", true, null);
        }
        return interfaces.get(0);
    }

    @SuppressWarnings("WeakerAccess")
    public TaskModel getCurrentTaskModel() {
        return getTaskMap().get(taskId);
    }

    public Object getSubTaskModel() throws TException {
        if (subTaskModel == null) {
            subTaskModel = ThriftUtils.getSubTaskModel(getCurrentTaskModel());
        }
        return subTaskModel;
    }

    public static class TaskContextBuilder {
        private final String processId;
        private final String gatewayId;
        private final String taskId;
        private RegistryService.Client registryClient;
        private UserProfileService.Client profileClient;
        private ProcessModel processModel;

        @SuppressWarnings("WeakerAccess")
        public TaskContextBuilder(String processId, String gatewayId, String taskId) throws Exception {
            if (notValid(processId) || notValid(gatewayId) || notValid(taskId)) {
                throwError("Process Id, Gateway Id and Task Id must be not null");
            }
            this.processId = processId;
            this.gatewayId = gatewayId;
            this.taskId = taskId;
        }

        public TaskContextBuilder setProcessModel(ProcessModel processModel) {
            this.processModel = processModel;
            return this;
        }

        public TaskContextBuilder setRegistryClient(RegistryService.Client registryClient) {
            this.registryClient = registryClient;
            return this;
        }

        public TaskContextBuilder setProfileClient(UserProfileService.Client profileClient) {
            this.profileClient = profileClient;
            return this;
        }

        public TaskContext build() throws Exception {

            if (notValid(processModel)) {
                throwError("Invalid Process Model");
            }
            if (notValid(registryClient)) {
                throwError("Invalid Registry Client");
            }

            TaskContext ctx = new TaskContext(processId, gatewayId, taskId);
            ctx.setRegistryClient(registryClient);
            ctx.setProcessModel(processModel);
            ctx.setProfileClient(profileClient);
            return ctx;
        }

        private boolean notValid(Object value) {
            return value == null;
        }

        private void throwError(String msg) throws Exception {
            throw new Exception(msg);
        }
    }
}

