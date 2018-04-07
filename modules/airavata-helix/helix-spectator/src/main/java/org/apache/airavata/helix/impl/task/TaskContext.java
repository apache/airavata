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
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserStoragePreference;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
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
import org.apache.airavata.registry.cpi.AppCatalog;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.ExperimentCatalog;
import org.apache.airavata.registry.cpi.ExperimentCatalogModelType;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class TaskContext {

    private final static Logger logger = LoggerFactory.getLogger(TaskContext.class);
    // process model
    private Publisher statusPublisher;
    private final String processId;
    private final String gatewayId;
    //private final String tokenId;
    private ProcessModel processModel;
    private String workingDir;
    private String scratchLocation;
    private String inputDir;
    private String outputDir;
    private String localWorkingDir;
    private GatewayResourceProfile gatewayResourceProfile;
    private ComputeResourcePreference gatewayComputeResourcePreference;
    private StoragePreference gatewayStorageResourcePreference;
    private UserResourceProfile userResourceProfile;
    private UserComputeResourcePreference userComputeResourcePreference;
    private UserStoragePreference userStoragePreference;
    private ComputeResourceDescription computeResourceDescription;
    private ApplicationDeploymentDescription applicationDeploymentDescription;
    private ApplicationInterfaceDescription applicationInterfaceDescription;
    private Map<String, String> sshProperties;
    private String stdoutLocation;
    private String stderrLocation;
    private JobSubmissionProtocol jobSubmissionProtocol;
    private DataMovementProtocol dataMovementProtocol;
    private JobModel jobModel;
    private StorageResourceDescription storageResource;
    private MonitorMode monitorMode;
    private ResourceJobManager resourceJobManager;
    private boolean handOver;
    private boolean cancel;
    private List<String> taskExecutionOrder;
    private List<TaskModel> taskList;
    private Map<String, TaskModel> taskMap;
    private boolean pauseTaskExecution = false;  // Task can pause task execution by setting this value
    private boolean complete = false; // all tasks executed?
    private boolean recovery = false; // is process in recovery mode?
    private TaskModel currentExecutingTaskModel; // current execution task model in case we pause process execution we need this to continue process exectuion again
    private boolean acknowledge;
    private boolean recoveryWithCancel = false;
    private String usageReportingGatewayId;
    private List<String> queueSpecificMacros;
    private String taskId;
    private Object subTaskModel = null;
    private RegistryService.Client registryClient;


    /**
     * Note: process context property use lazy loading approach. In runtime you will see some properties as null
     * unless you have access it previously. Once that property access using the api,it will be set to correct value.
     */
    private TaskContext(String processId, String gatewayId, String taskId) {
        this.processId = processId;
        this.gatewayId = gatewayId;
        this.taskId = taskId;
    }

    public String getGatewayId() {
        return gatewayId;
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
            }else {
                scratchLocation = gatewayComputeResourcePreference.getScratchLocation();
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


    public Map<String, String> getSshProperties() {
        return sshProperties;
    }

    public void setSshProperties(Map<String, String> sshProperties) {
        this.sshProperties = sshProperties;
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
            jobSubmissionProtocol = gatewayComputeResourcePreference.getPreferredJobSubmissionProtocol();
        }
        return jobSubmissionProtocol;
    }

    public void setJobSubmissionProtocol(JobSubmissionProtocol jobSubmissionProtocol) {
        this.jobSubmissionProtocol = jobSubmissionProtocol;
    }

    public DataMovementProtocol getDataMovementProtocol() {
        if (dataMovementProtocol == null) {
            dataMovementProtocol = gatewayComputeResourcePreference.getPreferredDataMovementProtocol();
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

    public ComputeResourcePreference getGatewayComputeResourcePreference() {
        return gatewayComputeResourcePreference;
    }

    public void setGatewayComputeResourcePreference(ComputeResourcePreference gatewayComputeResourcePreference) {
        this.gatewayComputeResourcePreference = gatewayComputeResourcePreference;
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
            return gatewayComputeResourcePreference.getComputeResourceId();
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
        } else {
            if (isValid(gatewayComputeResourcePreference.getResourceSpecificCredentialStoreToken())) {
                return gatewayComputeResourcePreference.getResourceSpecificCredentialStoreToken();
            } else {
                return gatewayResourceProfile.getCredentialStoreToken();
            }
        }
    }

    public String getStorageResourceCredentialToken(){
        if (isValid(gatewayStorageResourcePreference.getResourceSpecificCredentialStoreToken())) {
            return gatewayStorageResourcePreference.getResourceSpecificCredentialStoreToken();
        } else {
            return gatewayResourceProfile.getCredentialStoreToken();
        }
    }

    public JobSubmissionProtocol getPreferredJobSubmissionProtocol(){
        return gatewayComputeResourcePreference.getPreferredJobSubmissionProtocol();
    }

    public DataMovementProtocol getPreferredDataMovementProtocol() {
        return gatewayComputeResourcePreference.getPreferredDataMovementProtocol();
    }

    public void setMonitorMode(MonitorMode monitorMode) {
        this.monitorMode = monitorMode;
    }

    public MonitorMode getMonitorMode() {
        return monitorMode;
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

    public StorageResourceDescription getStorageResource() {
        return storageResource;
    }

    public void setStorageResource(StorageResourceDescription storageResource) {
        this.storageResource = storageResource;
    }

    public void setAcknowledge(boolean acknowledge) {
        this.acknowledge = acknowledge;
    }

    public boolean isAcknowledge() {
        return acknowledge;
    }

    public boolean isRecoveryWithCancel() {
        return recoveryWithCancel;
    }

    public void setRecoveryWithCancel(boolean recoveryWithCancel) {
        this.recoveryWithCancel = recoveryWithCancel;
    }

    public boolean isUseUserCRPref() {
        return getProcessModel().isUseUserCRPref();
    }

    public String getComputeResourceLoginUserName(){
        if (isUseUserCRPref() &&
                userComputeResourcePreference != null &&
                isValid(userComputeResourcePreference.getLoginUserName())) {
            return userComputeResourcePreference.getLoginUserName();
        } else if (isValid(processModel.getProcessResourceSchedule().getOverrideLoginUserName())) {
            return processModel.getProcessResourceSchedule().getOverrideLoginUserName();
        } else {
            return gatewayComputeResourcePreference.getLoginUserName();
        }
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

    public String getUsageReportingGatewayId() {
        return gatewayComputeResourcePreference.getUsageReportingGatewayId();
    }

    public String getAllocationProjectNumber() {
        return gatewayComputeResourcePreference.getAllocationProjectNumber();
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
            reservation = gatewayComputeResourcePreference.getReservation();
            start = gatewayComputeResourcePreference.getReservationStartTime();
            end = gatewayComputeResourcePreference.getReservationEndTime();
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
            return gatewayComputeResourcePreference.getQualityOfService();
        }
    }


    public String getQueueName() {
        if (isUseUserCRPref() &&
                userComputeResourcePreference != null &&
                isValid(userComputeResourcePreference.getPreferredBatchQueue())) {
            return userComputeResourcePreference.getPreferredBatchQueue();
        } else if (isValid(processModel.getProcessResourceSchedule().getQueueName())) {
            return processModel.getProcessResourceSchedule().getQueueName();
        } else {
            return gatewayComputeResourcePreference.getPreferredBatchQueue();
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
        private GatewayResourceProfile gatewayResourceProfile;
        private ComputeResourcePreference gatewayComputeResourcePreference;
        private StoragePreference gatewayStorageResourcePreference;
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

        public TaskContextBuilder setGatewayResourceProfile(GatewayResourceProfile gatewayResourceProfile) {
            this.gatewayResourceProfile = gatewayResourceProfile;
            return this;
        }

        public TaskContextBuilder setGatewayComputeResourcePreference(ComputeResourcePreference gatewayComputeResourcePreference) {
            this.gatewayComputeResourcePreference = gatewayComputeResourcePreference;
            return this;
        }

        public TaskContextBuilder setGatewayStorageResourcePreference(StoragePreference gatewayStorageResourcePreference) {
            this.gatewayStorageResourcePreference = gatewayStorageResourcePreference;
            return this;
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
            if (notValid(gatewayResourceProfile)) {
                throwError("Invalid GatewayResourceProfile");
            }
            if (notValid(gatewayComputeResourcePreference)) {
                throwError("Invalid Gateway ComputeResourcePreference");
            }
            if (notValid(gatewayStorageResourcePreference)) {
                throwError("Invalid Gateway StoragePreference");
            }
            if (notValid(processModel)) {
                throwError("Invalid Process Model");
            }
            if (notValid(registryClient)) {
                throwError("Invalid Registry Client");
            }
            if (notValid(statusPublisher)) {
                throwError("Invalid Status Publisher");
            }

            TaskContext ctx = new TaskContext(processId, gatewayId, taskId);
            ctx.setRegistryClient(registryClient);
            ctx.setStatusPublisher(statusPublisher);
            ctx.setProcessModel(processModel);
            ctx.setGatewayResourceProfile(gatewayResourceProfile);
            ctx.setGatewayComputeResourcePreference(gatewayComputeResourcePreference);
            ctx.setGatewayStorageResourcePreference(gatewayStorageResourcePreference);
            ctx.setApplicationDeploymentDescription(registryClient.getApplicationDeployment(processModel.getApplicationDeploymentId()));
            ctx.setApplicationInterfaceDescription(registryClient.getApplicationInterface(processModel.getApplicationInterfaceId()));
            ctx.setComputeResourceDescription(registryClient.getComputeResource(ctx.getComputeResourceId()));
            ctx.setStorageResource(registryClient.getStorageResource(ctx.getStorageResourceId()));

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


