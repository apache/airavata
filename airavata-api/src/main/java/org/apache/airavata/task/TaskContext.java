/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.task;

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
import org.apache.airavata.interfaces.GroupComputeResourcePreferenceUtil;
import org.apache.airavata.interfaces.RegistryHandler;
import org.apache.airavata.interfaces.UserProfileProvider;
import org.apache.airavata.messaging.service.Publisher;
import org.apache.airavata.model.appcatalog.appdeployment.proto.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appinterface.proto.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.proto.BatchQueue;
import org.apache.airavata.model.appcatalog.computeresource.proto.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.proto.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.proto.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.computeresource.proto.LOCALSubmission;
import org.apache.airavata.model.appcatalog.computeresource.proto.ResourceJobManager;
import org.apache.airavata.model.appcatalog.computeresource.proto.SSHJobSubmission;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.StoragePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.ComputeResourceReservation;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.EnvironmentSpecificPreferences;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupResourceProfile;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.ResourceType;
import org.apache.airavata.model.appcatalog.storageresource.proto.StorageResourceDescription;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserStoragePreference;
import org.apache.airavata.model.application.io.proto.DataType;
import org.apache.airavata.model.application.io.proto.OutputDataObjectType;
import org.apache.airavata.model.data.movement.proto.DataMovementInterface;
import org.apache.airavata.model.data.movement.proto.DataMovementProtocol;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.model.job.proto.JobModel;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.model.scheduling.proto.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.proto.ProcessState;
import org.apache.airavata.model.status.proto.ProcessStatus;
import org.apache.airavata.model.status.proto.TaskState;
import org.apache.airavata.model.status.proto.TaskStatus;
import org.apache.airavata.model.task.proto.TaskModel;
import org.apache.airavata.model.user.proto.UserProfile;
import org.apache.airavata.util.AiravataUtils;
import org.apache.airavata.util.ThriftUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Note: process context property use lazy loading approach. In runtime you will see some properties as null
 * unless you have access it previously. Once that property access using the api,it will be set to correct value.
 */
public class TaskContext {

    private static final Logger logger = LoggerFactory.getLogger(TaskContext.class);

    private Publisher statusPublisher;
    private RegistryHandler registryClient;
    private UserProfileProvider userProfileRepository;

    private String processId;
    private String gatewayId;
    private String taskId;

    private ExperimentModel experimentModel;
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
    private ResourceType resourceType;

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

    public void setExperimentModel(ExperimentModel experimentModel) {
        this.experimentModel = experimentModel;
    }

    public String getWorkingDir() throws Exception {
        if (workingDir == null) {
            if (processModel.getProcessResourceSchedule().getStaticWorkingDir() != null) {
                workingDir = processModel.getProcessResourceSchedule().getStaticWorkingDir();
            } else {
                String scratchLocation = getScratchLocation();
                workingDir = (scratchLocation.endsWith("/")
                        ? scratchLocation + processId
                        : scratchLocation + "/" + processId);
            }
        }
        return workingDir;
    }

    public String getScratchLocation() throws Exception {
        if (scratchLocation == null) {
            if (isUseUserCRPref()
                    && getUserComputeResourcePreference() != null
                    && isValid(getUserComputeResourcePreference().getScratchLocation())) {
                scratchLocation = getUserComputeResourcePreference().getScratchLocation();
            } else if (isValid(processModel.getProcessResourceSchedule().getOverrideScratchLocation())) {
                scratchLocation = processModel.getProcessResourceSchedule().getOverrideScratchLocation();
            } else if (isSetGroupResourceProfile()
                    && getGroupComputeResourcePreference() != null
                    && isValid(getGroupComputeResourcePreference().getScratchLocation())) {
                scratchLocation = getGroupComputeResourcePreference().getScratchLocation();
            } else {
                throw new RuntimeException(
                        "Can't find a specified scratch location for compute resource " + getComputeResourceId());
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
            } catch (Exception e) {
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
            } catch (Exception e) {
                logger.error(
                        "Failed to find a group resource proifle with id {}", processModel.getGroupResourceProfileId());
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
                        processModel.getComputeResourceId(), processModel.getGroupResourceProfileId());
            } catch (Exception e) {
                logger.error(
                        "Failed to find group compute resource preference for compute  {}, and group resource profile {}",
                        processModel.getComputeResourceId(),
                        processModel.getGroupResourceProfileId());
                throw e;
            }
        }
        return groupComputeResourcePreference;
    }

    public void setGroupComputeResourcePreference(GroupComputeResourcePreference groupComputeResourcePreference) {
        this.groupComputeResourcePreference = groupComputeResourcePreference;
    }

    public ResourceType getResourceType() throws Exception {
        if (resourceType == null) {
            GroupComputeResourcePreference pref = getGroupComputeResourcePreference();
            resourceType = pref.getResourceType();
        }
        return resourceType;
    }

    public UserResourceProfile getUserResourceProfile() throws Exception {

        if (userResourceProfile == null && processModel.getUseUserCrPref()) {
            try {
                this.userResourceProfile = registryClient.getUserResourceProfile(processModel.getUserName(), gatewayId);
            } catch (Exception e) {
                logger.error(
                        "Failed to fetch user resource profile for user {} in gateway {}",
                        processModel.getUserName(),
                        gatewayId,
                        e);
                throw e;
            }
        }
        return userResourceProfile;
    }

    public void setUserResourceProfile(UserResourceProfile userResourceProfile) {
        this.userResourceProfile = userResourceProfile;
    }

    private UserComputeResourcePreference getUserComputeResourcePreference() throws Exception {
        if (this.userComputeResourcePreference == null && processModel.getUseUserCrPref()) {
            try {
                this.userComputeResourcePreference = registryClient.getUserComputeResourcePreference(
                        processModel.getUserName(), gatewayId, processModel.getComputeResourceId());
            } catch (Exception e) {
                logger.error(
                        "Failed to fetch user compute resource preference for user {} compute resource {} in gateway {}",
                        processModel.getUserName(),
                        processModel.getComputeResourceId(),
                        gatewayId,
                        e);
                throw e;
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

    /**
     * Returns the default storage preference for the gateway.
     * Prefers gateway-specific storage (ID starting with gatewayId), otherwise uses the first available preference.
     * Used as the fallback when no specific input/output storage resource is configured.
     */
    public StoragePreference getGatewayStorageResourcePreference() throws Exception {
        if (this.gatewayStorageResourcePreference == null) {
            try {
                GatewayResourceProfile gatewayProfile = getGatewayResourceProfile();
                List<StoragePreference> storagePreferences = gatewayProfile.getStoragePreferencesList();

                if (storagePreferences == null || storagePreferences.isEmpty()) {
                    throw new Exception("No storage preferences found for gateway " + gatewayId);
                }

                String gatewayPrefix = gatewayId + "_";
                this.gatewayStorageResourcePreference = storagePreferences.stream()
                        .filter(pref -> {
                            String id = pref.getStorageResourceId();
                            return id != null && id.startsWith(gatewayPrefix);
                        })
                        .findFirst()
                        .orElseGet(() -> {
                            logger.debug(
                                    "No gateway-specific storage found, using first available: {}",
                                    storagePreferences.get(0).getStorageResourceId());
                            return storagePreferences.get(0);
                        });

                if (this.gatewayStorageResourcePreference.getStorageResourceId().startsWith(gatewayPrefix)) {
                    logger.debug(
                            "Using gateway-specific storage preference: {}",
                            this.gatewayStorageResourcePreference.getStorageResourceId());
                }
            } catch (Exception e) {
                logger.error("Failed to fetch gateway storage preference for gateway {}", gatewayId, e);
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
                this.applicationDeploymentDescription =
                        registryClient.getApplicationDeployment(processModel.getApplicationDeploymentId());
            } catch (Exception e) {
                logger.error(
                        "Failed to fetch application deployment with id {}",
                        processModel.getApplicationDeploymentId(),
                        e);
                throw e;
            }
        }
        return applicationDeploymentDescription;
    }

    public void setApplicationDeploymentDescription(ApplicationDeploymentDescription applicationDeploymentDescription) {
        this.applicationDeploymentDescription = applicationDeploymentDescription;
    }

    public ApplicationInterfaceDescription getApplicationInterfaceDescription() throws Exception {
        if (this.applicationInterfaceDescription == null) {
            try {
                this.applicationInterfaceDescription =
                        registryClient.getApplicationInterface(processModel.getApplicationInterfaceId());
            } catch (Exception e) {
                logger.error(
                        "Failed to fetch application interface with id {}",
                        processModel.getApplicationInterfaceId(),
                        e);
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
            List<OutputDataObjectType> applicationOutputs =
                    getApplicationInterfaceDescription().getApplicationOutputsList();
            if (applicationOutputs != null && !applicationOutputs.isEmpty()) {
                for (OutputDataObjectType outputDataObjectType : applicationOutputs) {
                    if (outputDataObjectType.getType().equals(DataType.STDOUT)) {
                        if (outputDataObjectType.getValue() == null
                                || outputDataObjectType.getValue().equals("")) {
                            stdoutLocation = (getWorkingDir().endsWith(File.separator)
                                            ? getWorkingDir()
                                            : getWorkingDir() + File.separator)
                                    + getApplicationInterfaceDescription().getApplicationName() + ".stdout";
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
            List<OutputDataObjectType> applicationOutputs =
                    getApplicationInterfaceDescription().getApplicationOutputsList();
            if (applicationOutputs != null && !applicationOutputs.isEmpty()) {
                for (OutputDataObjectType outputDataObjectType : applicationOutputs) {
                    if (outputDataObjectType.getType().equals(DataType.STDERR)) {
                        if (outputDataObjectType.getValue() == null
                                || outputDataObjectType.getValue().equals("")) {
                            this.stderrLocation = (getWorkingDir().endsWith(File.separator)
                                            ? getWorkingDir()
                                            : getWorkingDir() + File.separator)
                                    + getApplicationInterfaceDescription().getApplicationName() + ".stderr";
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
            List<JobSubmissionInterface> jobSubmissionInterfaces =
                    getComputeResourceDescription().getJobSubmissionInterfacesList();
            Collections.sort(
                    jobSubmissionInterfaces, Comparator.comparingInt(JobSubmissionInterface::getPriorityOrder));
            jobSubmissionProtocol = jobSubmissionInterfaces.get(0).getJobSubmissionProtocol();
        }
        return jobSubmissionProtocol;
    }

    public void setJobSubmissionProtocol(JobSubmissionProtocol jobSubmissionProtocol) {
        this.jobSubmissionProtocol = jobSubmissionProtocol;
    }

    public DataMovementProtocol getDataMovementProtocol() throws Exception {
        if (dataMovementProtocol == null) {
            // Data movement is now a storage-only concept — take highest priority from storage resource
            List<DataMovementInterface> dataMovementInterfaces =
                    getStorageResourceDescription().getDataMovementInterfacesList();
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
            synchronized (TaskModel.class) {
                if (taskList == null) {
                    taskList = getProcessModel().getTasksList();
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
            jobModel = JobModel.newBuilder()
                    .setProcessId(processId)
                    .setWorkingDir(getWorkingDir())
                    .setCreationTime(AiravataUtils.getCurrentTimestamp().getTime())
                    .build();
        }
        return jobModel;
    }

    public void setJobModel(JobModel jobModel) {
        this.jobModel = jobModel;
    }

    public ProcessState getProcessState() {
        if (processModel.getProcessStatusesList() != null
                && processModel.getProcessStatusesList().size() > 0)
            return processModel.getProcessStatusesList().get(0).getState();
        else return null;
    }

    public void setProcessStatus(ProcessStatus status) {
        if (status != null) {
            logger.info(
                    "expId: {}, processId: {} :- Process status changed {} -> {}",
                    getExperimentId(),
                    processId,
                    getProcessState().name(),
                    status.getState().name());
            processModel = processModel.toBuilder()
                    .clearProcessStatuses()
                    .addProcessStatuses(status)
                    .build();
        }
    }

    public ProcessStatus getProcessStatus() {
        if (processModel.getProcessStatusesCount() > 0)
            return processModel.getProcessStatusesList().get(0);
        else return null;
    }

    public TaskState getTaskState() {
        if (getCurrentTaskModel() != null && getCurrentTaskModel().getTaskStatusesList() != null) {
            return getCurrentTaskModel().getTaskStatusesList().get(0).getState();
        } else {
            return null;
        }
    }

    public TaskStatus getTaskStatus() {
        if (getCurrentTaskModel().getTaskStatusesList() != null)
            return getCurrentTaskModel().getTaskStatusesList().get(0);
        else return null;
    }

    public String getComputeResourceId() throws Exception {
        if (isUseUserCRPref()
                && getUserComputeResourcePreference() != null
                && isValid(getUserComputeResourcePreference().getComputeResourceId())) {
            return getUserComputeResourcePreference().getComputeResourceId();
        } else {
            return getGroupComputeResourcePreference().getComputeResourceId();
        }
    }

    public String getComputeResourceCredentialToken() throws Exception {
        if (isUseUserCRPref()) {
            if (getUserComputeResourcePreference() != null
                    && isValid(getUserComputeResourcePreference().getResourceSpecificCredentialStoreToken())) {
                return getUserComputeResourcePreference().getResourceSpecificCredentialStoreToken();
            } else {
                return getUserResourceProfile().getCredentialStoreToken();
            }
        } else if (isSetGroupResourceProfile()
                && getGroupComputeResourcePreference() != null
                && isValid(getGroupComputeResourcePreference().getResourceSpecificCredentialStoreToken())) {
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
                SSHJobSubmission sshJobSubmission =
                        getRegistryClient().getSSHJobSubmission(jsInterface.getJobSubmissionInterfaceId());
                resourceJobManager = sshJobSubmission.getResourceJobManager();

            } else if (jsInterface.getJobSubmissionProtocol() == JobSubmissionProtocol.LOCAL) {
                LOCALSubmission localSubmission =
                        getRegistryClient().getLocalJobSubmission(jsInterface.getJobSubmissionInterfaceId());
                resourceJobManager = localSubmission.getResourceJobManager();

            } else if (jsInterface.getJobSubmissionProtocol() == JobSubmissionProtocol.SSH_FORK) {
                SSHJobSubmission sshJobSubmission =
                        getRegistryClient().getSSHJobSubmission(jsInterface.getJobSubmissionInterfaceId());
                resourceJobManager = sshJobSubmission.getResourceJobManager();

            } else if (jsInterface.getJobSubmissionProtocol() == JobSubmissionProtocol.JSP_CLOUD) {
                SSHJobSubmission sshJobSubmission =
                        getRegistryClient().getSSHJobSubmission(jsInterface.getJobSubmissionInterfaceId());
                resourceJobManager = sshJobSubmission.getResourceJobManager();

            } else {
                throw new Exception("Unsupported JobSubmissionProtocol - "
                        + jsInterface.getJobSubmissionProtocol().name());
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
        return getProcessModel().getUseUserCrPref();
    }

    public boolean isSetGroupResourceProfile() {
        return !getProcessModel().getGroupResourceProfileId().isEmpty();
    }

    public String getComputeResourceLoginUserName() throws Exception {
        if (isUseUserCRPref()
                && getUserComputeResourcePreference() != null
                && isValid(getUserComputeResourcePreference().getLoginUserName())) {
            return getUserComputeResourcePreference().getLoginUserName();
        } else if (isValid(processModel.getProcessResourceSchedule().getOverrideLoginUserName())) {
            return processModel.getProcessResourceSchedule().getOverrideLoginUserName();
        } else if (isSetGroupResourceProfile()
                && getGroupComputeResourcePreference() != null
                && isValid(getGroupComputeResourcePreference().getLoginUserName())) {
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

    public String getInputStorageResourceId() throws Exception {
        if (processModel.getInputStorageResourceId() != null
                && !processModel.getInputStorageResourceId().trim().isEmpty()) {
            return processModel.getInputStorageResourceId();
        }
        return getStorageResourceId();
    }

    public StoragePreference getInputGatewayStorageResourcePreference() throws Exception {
        String inputStorageId = getInputStorageResourceId();
        try {
            return registryClient.getGatewayStoragePreference(gatewayId, inputStorageId);
        } catch (Exception e) {
            logger.error(
                    "Failed to fetch gateway storage preference for input storage {} in gateway {}",
                    inputStorageId,
                    gatewayId,
                    e);
            throw e;
        }
    }

    public String getOutputStorageResourceId() throws Exception {
        if (processModel.getOutputStorageResourceId() != null
                && !processModel.getOutputStorageResourceId().trim().isEmpty()) {
            return processModel.getOutputStorageResourceId();
        }
        return getStorageResourceId();
    }

    public StoragePreference getOutputGatewayStorageResourcePreference() throws Exception {
        String outputStorageId = getOutputStorageResourceId();
        try {
            return registryClient.getGatewayStoragePreference(gatewayId, outputStorageId);
        } catch (Exception e) {
            logger.error(
                    "Failed to fetch gateway storage preference for output storage {} in gateway {}",
                    outputStorageId,
                    gatewayId,
                    e);
            throw e;
        }
    }

    public StorageResourceDescription getOutputStorageResourceDescription() throws Exception {
        return registryClient.getStorageResource(getOutputStorageResourceId());
    }

    private ComputationalResourceSchedulingModel getProcessCRSchedule() {
        if (getProcessModel() != null) {
            return getProcessModel().getProcessResourceSchedule();
        } else {
            return null;
        }
    }

    public void setRegistryClient(RegistryHandler registryClient) {
        this.registryClient = registryClient;
    }

    public RegistryHandler getRegistryClient() {
        return registryClient;
    }

    public UserProfileProvider getUserProfileProvider() {
        return userProfileRepository;
    }

    public void setUserProfileProvider(UserProfileProvider userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    public UserProfile getUserProfile() throws TaskOnFailException {

        if (this.userProfile == null) {
            try {
                this.userProfile = getUserProfileProvider()
                        .getUserProfileByIdAndGateWay(getProcessModel().getUserName(), getGatewayId());
                if (this.userProfile == null) {
                    throw new Exception("User profile not found for user "
                            + getProcessModel().getUserName() + " in gateway " + getGatewayId());
                }
            } catch (Exception e) {
                logger.error("Failed to fetch the user profile for user id {}", processModel.getUserName(), e);
                throw new TaskOnFailException(
                        "Failed to fetch the user profile for user id " + processModel.getUserName(), true, e);
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
        } else if (isUseUserCRPref()
                && getUserComputeResourcePreference() != null
                && getUserComputeResourcePreference().getAllocationProjectNumber() != null) {
            return getUserComputeResourcePreference().getAllocationProjectNumber();
        } else if (isSetGroupResourceProfile()
                && getGroupComputeResourcePreference() != null
                && isValid(extractSlurmAllocationProject(getGroupComputeResourcePreference()))) {
            return extractSlurmAllocationProject(getGroupComputeResourcePreference());
        } else {
            return null;
        }
    }

    public String getReservation() throws Exception {
        long start = 0, end = 0;
        String reservation = null;
        if (isUseUserCRPref()
                && getUserComputeResourcePreference() != null
                && isValid(getUserComputeResourcePreference().getReservation())) {
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
        ComputeResourceReservation computeResourceReservation =
                GroupComputeResourcePreferenceUtil.getActiveReservationForQueue(
                        getGroupComputeResourcePreference(), queueName);
        if (computeResourceReservation != null) {
            return computeResourceReservation.getReservationName();
        }
        return null;
    }

    public String getQualityOfService() throws Exception {
        if (isUseUserCRPref()
                && getUserComputeResourcePreference() != null
                && isValid(getUserComputeResourcePreference().getQualityOfService())) {
            return getUserComputeResourcePreference().getQualityOfService();
        } else {
            return extractSlurmQoS(getGroupComputeResourcePreference());
        }
    }

    public String getQueueName() throws Exception {
        if (isUseUserCRPref()
                && getUserComputeResourcePreference() != null
                && isValid(getUserComputeResourcePreference().getPreferredBatchQueue())) {
            return getUserComputeResourcePreference().getPreferredBatchQueue();
        } else if (isValid(processModel.getProcessResourceSchedule().getQueueName())) {
            return processModel.getProcessResourceSchedule().getQueueName();
        } else {
            Optional<BatchQueue> defaultQueue = getComputeResourceDescription().getBatchQueuesList().stream()
                    .filter(q -> q.getIsDefaultQueue())
                    .findFirst();
            if (defaultQueue.isPresent()) {
                return defaultQueue.get().getQueueName();
            } else {
                throw new RuntimeException("Can't find default queue for resource "
                        + getComputeResourceDescription().getComputeResourceId());
            }
        }
    }

    public List<String> getQueueSpecificMacros() throws Exception {
        String queueName = getProcessCRSchedule().getQueueName();
        Optional<BatchQueue> queue = getComputeResourceDescription().getBatchQueuesList().stream()
                .filter(x -> x.getQueueName().equals(queueName))
                .findFirst();
        if (queue.isPresent()) {
            if (queue.get().getQueueSpecificMacros() != null
                    && !queue.get().getQueueSpecificMacros().equals("")) {
                return Arrays.asList(queue.get().getQueueSpecificMacros().split(","));
            }
        }
        return null;
    }

    public JobSubmissionInterface getPreferredJobSubmissionInterface() throws Exception {
        JobSubmissionProtocol preferredJobSubmissionProtocol = getJobSubmissionProtocol();
        ComputeResourceDescription resourceDescription = getComputeResourceDescription();
        List<JobSubmissionInterface> jobSubmissionInterfaces = resourceDescription.getJobSubmissionInterfacesList();
        Map<JobSubmissionProtocol, List<JobSubmissionInterface>> orderedInterfaces = new HashMap<>();
        List<JobSubmissionInterface> interfaces = new ArrayList<>();
        if (jobSubmissionInterfaces != null && !jobSubmissionInterfaces.isEmpty()) {
            for (JobSubmissionInterface submissionInterface : jobSubmissionInterfaces) {

                if (preferredJobSubmissionProtocol != null) {
                    if (preferredJobSubmissionProtocol
                            .toString()
                            .equals(submissionInterface
                                    .getJobSubmissionProtocol()
                                    .toString())) {
                        if (orderedInterfaces.containsKey(submissionInterface.getJobSubmissionProtocol())) {
                            List<JobSubmissionInterface> interfaceList =
                                    orderedInterfaces.get(submissionInterface.getJobSubmissionProtocol());
                            interfaceList.add(submissionInterface);
                        } else {
                            interfaces.add(submissionInterface);
                            orderedInterfaces.put(submissionInterface.getJobSubmissionProtocol(), interfaces);
                        }
                    }
                } else {
                    jobSubmissionInterfaces.sort(Comparator.comparingInt(JobSubmissionInterface::getPriorityOrder));
                }
            }
            interfaces = orderedInterfaces.get(preferredJobSubmissionProtocol);
            interfaces.sort(Comparator.comparingInt(JobSubmissionInterface::getPriorityOrder));
        } else {
            throw new TaskOnFailException(
                    "Compute resource should have at least one job submission interface defined...", true, null);
        }
        return interfaces.get(0);
    }

    @SuppressWarnings("WeakerAccess")
    public TaskModel getCurrentTaskModel() {
        return getTaskMap().get(taskId);
    }

    public Object getSubTaskModel() throws Exception {
        if (subTaskModel == null) {
            subTaskModel = ThriftUtils.getSubTaskModel(getCurrentTaskModel());
        }
        return subTaskModel;
    }

    public static class TaskContextBuilder {
        private final String processId;
        private final String gatewayId;
        private final String taskId;
        private RegistryHandler registryClient;
        private UserProfileProvider userProfileRepository;
        private ProcessModel processModel;
        private ExperimentModel experimentModel;

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

        public TaskContextBuilder setExperimentModel(ExperimentModel experimentModel) {
            this.experimentModel = experimentModel;
            return this;
        }

        public TaskContextBuilder setRegistryClient(RegistryHandler registryClient) {
            this.registryClient = registryClient;
            return this;
        }

        public TaskContextBuilder setUserProfileProvider(UserProfileProvider userProfileRepository) {
            this.userProfileRepository = userProfileRepository;
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
            ctx.setExperimentModel(experimentModel);
            ctx.setUserProfileProvider(userProfileRepository);
            return ctx;
        }

        private boolean notValid(Object value) {
            return value == null;
        }

        private void throwError(String msg) throws Exception {
            throw new Exception(msg);
        }
    }

    private String extractSlurmAllocationProject(GroupComputeResourcePreference pref) {
        if (pref.getResourceType() == ResourceType.SLURM && pref.hasSpecificPreferences()) {
            EnvironmentSpecificPreferences esp = pref.getSpecificPreferences();
            if (esp.hasSlurm()) {
                return esp.getSlurm().getAllocationProjectNumber();
            }
        }
        return null;
    }

    private String extractSlurmQoS(GroupComputeResourcePreference pref) {
        if (pref.getResourceType() == ResourceType.SLURM && pref.hasSpecificPreferences()) {
            EnvironmentSpecificPreferences esp = pref.getSpecificPreferences();
            if (esp.hasSlurm()) {
                return esp.getSlurm().getQualityOfService();
            }
        }
        return null;
    }
}
