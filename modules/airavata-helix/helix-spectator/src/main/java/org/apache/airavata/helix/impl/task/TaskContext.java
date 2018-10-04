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
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.gatewayprofile.StoragePreference;
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
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.model.status.TaskStatus;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * Note: process context property use lazy loading approach. In runtime you will see some properties as null
 * unless you have access it previously. Once that property access using the api,it will be set to correct value.
 */
public class TaskContext {

    private final static Logger logger = LoggerFactory.getLogger(TaskContext.class);

    private Publisher statusPublisher;
    private RegistryService.Client registryClient;

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

    public String getWorkingDir() {
        if (workingDir == null) {
            if (processModel.getProcessResourceSchedule().getStaticWorkingDir() != null){
                workingDir = processModel.getProcessResourceSchedule().getStaticWorkingDir();
            }else {
                String scratchLocation = getScratchLocation();
                workingDir = (scratchLocation.endsWith("/") ? scratchLocation + processId : scratchLocation + "/" +
                        processId);
            }
        }
        return workingDir;
    }

    public String getScratchLocation() {
        if (scratchLocation == null) {
            if (isUseUserCRPref() &&
                    userComputeResourcePreference != null &&
                    isValid(userComputeResourcePreference.getScratchLocation())) {
                scratchLocation = userComputeResourcePreference.getScratchLocation();
            } else if (isValid(processModel.getProcessResourceSchedule().getOverrideScratchLocation())) {
                scratchLocation = processModel.getProcessResourceSchedule().getOverrideScratchLocation();
            } else if (isSetGroupResourceProfile() && groupComputeResourcePreference != null &&
                    isValid(groupComputeResourcePreference.getScratchLocation())) {
                scratchLocation = groupComputeResourcePreference.getScratchLocation();
            } else {
                throw new RuntimeException("Can't find a specified scratch location for compute resource " + getComputeResourceId());
            }
        }
        return scratchLocation;
    }


    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public GatewayResourceProfile getGatewayResourceProfile() {
        return gatewayResourceProfile;
    }

    public void setGatewayResourceProfile(GatewayResourceProfile gatewayResourceProfile) {
        this.gatewayResourceProfile = gatewayResourceProfile;
    }

    public GroupResourceProfile getGroupResourceProfile() {
        return groupResourceProfile;
    }

    public void setGroupResourceProfile(GroupResourceProfile groupResourceProfile) {
        this.groupResourceProfile = groupResourceProfile;
    }

    public GroupComputeResourcePreference getGroupComputeResourcePreference() {
        return groupComputeResourcePreference;
    }

    public void setGroupComputeResourcePreference(GroupComputeResourcePreference groupComputeResourcePreference) {
        this.groupComputeResourcePreference = groupComputeResourcePreference;
    }

    public UserResourceProfile getUserResourceProfile() {
        return userResourceProfile;
    }

    public void setUserResourceProfile(UserResourceProfile userResourceProfile) {
        this.userResourceProfile = userResourceProfile;
    }

    private UserComputeResourcePreference getUserComputeResourcePreference() {
        return userComputeResourcePreference;
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

    public StoragePreference getGatewayStorageResourcePreference() {
        return gatewayStorageResourcePreference;
    }

    public void setGatewayStorageResourcePreference(StoragePreference gatewayStorageResourcePreference) {
        this.gatewayStorageResourcePreference = gatewayStorageResourcePreference;
    }

    public ComputeResourceDescription getComputeResourceDescription() {
        return computeResourceDescription;
    }

    public void setComputeResourceDescription(ComputeResourceDescription computeResourceDescription) {
        this.computeResourceDescription = computeResourceDescription;
    }

    public ApplicationDeploymentDescription getApplicationDeploymentDescription() {
        return applicationDeploymentDescription;
    }

    public void setApplicationDeploymentDescription(ApplicationDeploymentDescription
                                                            applicationDeploymentDescription) {
        this.applicationDeploymentDescription = applicationDeploymentDescription;
    }

    public ApplicationInterfaceDescription getApplicationInterfaceDescription() {
        return applicationInterfaceDescription;
    }

    public void setApplicationInterfaceDescription(ApplicationInterfaceDescription applicationInterfaceDescription) {
        this.applicationInterfaceDescription = applicationInterfaceDescription;
    }

    public String getStdoutLocation() {
        return stdoutLocation;
    }

    public void setStdoutLocation(String stdoutLocation) {
        this.stdoutLocation = stdoutLocation;
    }

    public String getStderrLocation() {
        return stderrLocation;
    }

    public void setStderrLocation(String stderrLocation) {
        this.stderrLocation = stderrLocation;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public String getOutputDir() {
        if (outputDir == null) {
            outputDir = getWorkingDir();
        }
        return outputDir;
    }

    public String getInputDir() {
        if (inputDir == null) {
            inputDir = getWorkingDir();
        }
        return inputDir;
    }

    public void setInputDir(String inputDir) {
        this.inputDir = inputDir;
    }

    public JobSubmissionProtocol getJobSubmissionProtocol() {
        if (jobSubmissionProtocol == null) {
            // Take highest priority one
            List<JobSubmissionInterface> jobSubmissionInterfaces = computeResourceDescription.getJobSubmissionInterfaces();
            Collections.sort(jobSubmissionInterfaces, Comparator.comparingInt(JobSubmissionInterface::getPriorityOrder));
            jobSubmissionProtocol = jobSubmissionInterfaces.get(0).getJobSubmissionProtocol();
        }
        return jobSubmissionProtocol;
    }

    public void setJobSubmissionProtocol(JobSubmissionProtocol jobSubmissionProtocol) {
        this.jobSubmissionProtocol = jobSubmissionProtocol;
    }

    public DataMovementProtocol getDataMovementProtocol() {
        if (dataMovementProtocol == null) {
            // Take highest priority one
            List<DataMovementInterface> dataMovementInterfaces = computeResourceDescription.getDataMovementInterfaces();
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

    public JobModel getJobModel() {
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
        if(getCurrentTaskModel().getTaskStatuses() != null)
            return getCurrentTaskModel().getTaskStatuses().get(0).getState();
        else
            return null;
    }

    public TaskStatus getTaskStatus() {
        if(getCurrentTaskModel().getTaskStatuses() != null)
            return getCurrentTaskModel().getTaskStatuses().get(0);
        else
            return null;
    }

    public String getComputeResourceId() {
        if (isUseUserCRPref() &&
                userComputeResourcePreference != null &&
                isValid(userComputeResourcePreference.getComputeResourceId())) {
            return userComputeResourcePreference.getComputeResourceId();
        } else {
            return groupComputeResourcePreference.getComputeResourceId();
        }
    }

    public String getComputeResourceCredentialToken(){
        if (isUseUserCRPref()) {
            if (userComputeResourcePreference != null &&
                    isValid(userComputeResourcePreference.getResourceSpecificCredentialStoreToken())) {
                return userComputeResourcePreference.getResourceSpecificCredentialStoreToken();
            } else {
                return userResourceProfile.getCredentialStoreToken();
            }
        }  else if (isSetGroupResourceProfile() &&
                groupComputeResourcePreference != null &&
                isValid(groupComputeResourcePreference.getResourceSpecificCredentialStoreToken())) {
            return groupComputeResourcePreference.getResourceSpecificCredentialStoreToken();
        } else {
            return groupResourceProfile.getDefaultCredentialStoreToken();
        }
    }

    public String getStorageResourceCredentialToken(){
        if (isValid(gatewayStorageResourcePreference.getResourceSpecificCredentialStoreToken())) {
            return gatewayStorageResourcePreference.getResourceSpecificCredentialStoreToken();
        } else {
            return groupResourceProfile.getDefaultCredentialStoreToken();
        }
    }

    public JobSubmissionProtocol getPreferredJobSubmissionProtocol(){
        return getJobSubmissionProtocol();
    }

    public DataMovementProtocol getPreferredDataMovementProtocol() {
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

    public StorageResourceDescription getStorageResourceDescription() {
        return storageResourceDescription;
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

    public String getComputeResourceLoginUserName() {
        if (isUseUserCRPref() &&
                userComputeResourcePreference != null &&
                isValid(userComputeResourcePreference.getLoginUserName())) {
            return userComputeResourcePreference.getLoginUserName();
        } else if (isValid(processModel.getProcessResourceSchedule().getOverrideLoginUserName())) {
            return processModel.getProcessResourceSchedule().getOverrideLoginUserName();
        } else if (isSetGroupResourceProfile() &&
                groupComputeResourcePreference != null &&
                isValid(groupComputeResourcePreference.getLoginUserName())){
            return groupComputeResourcePreference.getLoginUserName();
        }
        throw new RuntimeException("Can't find login username for compute resource");
    }

    public String getStorageResourceLoginUserName(){
        return gatewayStorageResourcePreference.getLoginUserName();
    }

    public String getStorageFileSystemRootLocation(){
        return gatewayStorageResourcePreference.getFileSystemRootLocation();
    }

    public String getStorageResourceId() {
        return gatewayStorageResourcePreference.getStorageResourceId();
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

    private boolean isValid(String str) {
        return str != null && !str.trim().isEmpty();
    }

    public String getAllocationProjectNumber() {
        if (isUseUserCRPref() &&
                userComputeResourcePreference != null &&
                userComputeResourcePreference.getAllocationProjectNumber() != null) {
            return userComputeResourcePreference.getAllocationProjectNumber();
        } else if (isSetGroupResourceProfile() &&
                groupComputeResourcePreference != null &&
                isValid(groupComputeResourcePreference.getAllocationProjectNumber())){
            return groupComputeResourcePreference.getAllocationProjectNumber();
        } else {
            return null;
        }
    }

    public String getReservation() {
        long start = 0, end = 0;
        String reservation = null;
        if (isUseUserCRPref() &&
                userComputeResourcePreference != null &&
                isValid(userComputeResourcePreference.getReservation())) {
            reservation = userComputeResourcePreference.getReservation();
            start = userComputeResourcePreference.getReservationStartTime();
            end = userComputeResourcePreference.getReservationEndTime();
        } else {
            reservation = groupComputeResourcePreference.getReservation();
            start = groupComputeResourcePreference.getReservationStartTime();
            end = groupComputeResourcePreference.getReservationEndTime();
        }
        if (reservation != null && start > 0 && start < end) {
            long now = Calendar.getInstance().getTimeInMillis();
            if (now > start && now < end) {
                return reservation;
            }
        }
        return null;
    }

    public String getQualityOfService() {
        if (isUseUserCRPref() &&
                userComputeResourcePreference != null &&
                isValid(userComputeResourcePreference.getQualityOfService())) {
            return userComputeResourcePreference.getQualityOfService();
        } else {
            return groupComputeResourcePreference.getQualityOfService();
        }
    }


    public String getQueueName() {
        if (isUseUserCRPref() &&
                userComputeResourcePreference != null &&
                isValid(userComputeResourcePreference.getPreferredBatchQueue())) {
            return userComputeResourcePreference.getPreferredBatchQueue();
        } else if (isValid(processModel.getProcessResourceSchedule().getQueueName())) {
            return processModel.getProcessResourceSchedule().getQueueName();
        }  else {
            Optional<BatchQueue> defaultQueue = computeResourceDescription.getBatchQueues().stream().filter(q -> q.isIsDefaultQueue()).findFirst();
            if (defaultQueue.isPresent()) {
                return defaultQueue.get().getQueueName();
            } else {
                throw new RuntimeException("Can't find default queue for resource " + computeResourceDescription.getComputeResourceId());
            }
        }
    }

    public List<String> getQueueSpecificMacros() {
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

    public JobSubmissionInterface getPreferredJobSubmissionInterface() throws TaskOnFailException {
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
        private Publisher statusPublisher;
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

        public TaskContextBuilder setStatusPublisher(Publisher statusPublisher) {
            this.statusPublisher = statusPublisher;
            return this;
        }

        public TaskContext build() throws Exception {

            if (notValid(processModel)) {
                throwError("Invalid Process Model");
            }
            if (notValid(registryClient)) {
                throwError("Invalid Registry Client");
            }
            if (notValid(statusPublisher)) {
                throwError("Invalid Status Publisher");
            }

            TaskContext ctx = new TaskContext();
            ctx.setRegistryClient(registryClient);
            ctx.setStatusPublisher(statusPublisher);
            ctx.setProcessModel(processModel);
            ctx.setTaskId(taskId);
            ctx.setGatewayId(gatewayId);
            ctx.setProcessId(processId);

            ctx.setGroupComputeResourcePreference(registryClient.getGroupComputeResourcePreference(processModel.getComputeResourceId(),
                    processModel.getGroupResourceProfileId()));

            ctx.setGroupResourceProfile(registryClient.getGroupResourceProfile(processModel.getGroupResourceProfileId()));

            ctx.setGatewayResourceProfile(
                    Optional.ofNullable(registryClient.getGatewayResourceProfile(gatewayId))
                            .orElseThrow(() -> new Exception("Invalid GatewayResourceProfile")));

            ctx.setGatewayStorageResourcePreference(
                    Optional.ofNullable(registryClient.getGatewayStoragePreference(
                            gatewayId,
                            processModel.getStorageResourceId()))
                            .orElseThrow(() -> new Exception("Invalid Gateway StoragePreference")));

            ctx.setApplicationDeploymentDescription(
                    Optional.ofNullable(registryClient.getApplicationDeployment(
                            processModel.getApplicationDeploymentId()))
                            .orElseThrow(() -> new Exception("Invalid Application Deployment")));

            ctx.setApplicationInterfaceDescription(
                    Optional.ofNullable(registryClient.getApplicationInterface(
                            processModel.getApplicationInterfaceId()))
                            .orElseThrow(() -> new Exception("Invalid Application Interface")));

            ctx.setComputeResourceDescription(
                    Optional.ofNullable(registryClient.getComputeResource(
                            ctx.getComputeResourceId()))
                            .orElseThrow(() -> new Exception("Invalid Compute Resource Description")));

            ctx.setStorageResourceDescription(
                    Optional.ofNullable(registryClient.getStorageResource(
                            ctx.getStorageResourceId()))
                            .orElseThrow(() -> new Exception("Invalid Storage Resource Description")));

            if (processModel.isUseUserCRPref()) {
                ctx.setUserResourceProfile(registryClient.getUserResourceProfile(processModel.getUserName(), gatewayId));
                ctx.setUserComputeResourcePreference(registryClient.getUserComputeResourcePreference(
                                processModel.getUserName(),
                                gatewayId,
                                processModel.getComputeResourceId()));
            }

            List<OutputDataObjectType> applicationOutputs = ctx.getApplicationInterfaceDescription().getApplicationOutputs();
            if (applicationOutputs != null && !applicationOutputs.isEmpty()) {
                for (OutputDataObjectType outputDataObjectType : applicationOutputs) {
                    if (outputDataObjectType.getType().equals(DataType.STDOUT)) {
                        if (outputDataObjectType.getValue() == null || outputDataObjectType.getValue().equals("")) {
                            String stdOut = (ctx.getWorkingDir().endsWith(File.separator) ? ctx.getWorkingDir() : ctx.getWorkingDir() + File.separator)
                                    + ctx.getApplicationInterfaceDescription().getApplicationName() + ".stdout";
                            outputDataObjectType.setValue(stdOut);
                            ctx.setStdoutLocation(stdOut);
                        } else {
                            ctx.setStdoutLocation(outputDataObjectType.getValue());
                        }
                    }
                    if (outputDataObjectType.getType().equals(DataType.STDERR)) {
                        if (outputDataObjectType.getValue() == null || outputDataObjectType.getValue().equals("")) {
                            String stderrLocation = (ctx.getWorkingDir().endsWith(File.separator) ? ctx.getWorkingDir() : ctx.getWorkingDir() + File.separator)
                                    + ctx.getApplicationInterfaceDescription().getApplicationName() + ".stderr";
                            outputDataObjectType.setValue(stderrLocation);
                            ctx.setStderrLocation(stderrLocation);
                        } else {
                            ctx.setStderrLocation(outputDataObjectType.getValue());
                        }
                    }
                }
            }

            // TODO move this to some where else as this is not the correct place to do so
            registryClient.updateProcess(processModel, processId);
            processModel.setProcessOutputs(applicationOutputs);
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


