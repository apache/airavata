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
package org.apache.airavata.gfac.impl;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.common.utils.ZkConstants;
import org.apache.airavata.credential.store.store.CredentialStoreException;
import org.apache.airavata.gfac.core.GFacConstants;
import org.apache.airavata.gfac.core.GFacEngine;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.GFacUtils;
import org.apache.airavata.gfac.core.cluster.ServerInfo;
import org.apache.airavata.gfac.core.context.ProcessContext;
import org.apache.airavata.gfac.core.context.TaskContext;
import org.apache.airavata.gfac.core.monitor.JobMonitor;
import org.apache.airavata.gfac.core.task.JobSubmissionTask;
import org.apache.airavata.gfac.core.task.Task;
import org.apache.airavata.gfac.core.task.TaskException;
import org.apache.airavata.gfac.impl.task.DataStreamingTask;
import org.apache.airavata.gfac.impl.task.EnvironmentSetupTask;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.computeresource.LOCALSubmission;
import org.apache.airavata.model.appcatalog.computeresource.MonitorMode;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManager;
import org.apache.airavata.model.appcatalog.computeresource.SSHJobSubmission;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserResourceProfile;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.data.movement.SecurityProtocol;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.model.status.TaskStatus;
import org.apache.airavata.model.task.DataStageType;
import org.apache.airavata.model.task.DataStagingTaskModel;
import org.apache.airavata.model.task.EnvironmentSetupTaskModel;
import org.apache.airavata.model.task.JobSubmissionTaskModel;
import org.apache.airavata.model.task.MonitorTaskModel;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.airavata.registry.cpi.AppCatalog;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.ExpCatChildDataType;
import org.apache.airavata.registry.cpi.ExperimentCatalog;
import org.apache.airavata.registry.cpi.ExperimentCatalogModelType;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.utils.Constants;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class GFacEngineImpl implements GFacEngine {

    private static final Logger log = LoggerFactory.getLogger(GFacEngineImpl.class);

    public GFacEngineImpl() throws GFacException {

    }

    @Override
    public ProcessContext populateProcessContext(String processId, String gatewayId, String
            tokenId) throws GFacException, CredentialStoreException {

        // NOTE: Process context gives precedence to data come with process Computer resources;
        ProcessContext processContext = null;
        ProcessContext.ProcessContextBuilder builder = new ProcessContext.ProcessContextBuilder(processId, gatewayId, tokenId);
        try {
            AppCatalog appCatalog = Factory.getDefaultAppCatalog();
            ExperimentCatalog expCatalog = Factory.getDefaultExpCatalog();
            ProcessModel processModel = (ProcessModel) expCatalog.get(ExperimentCatalogModelType.PROCESS, processId);
            builder.setAppCatalog(appCatalog)
                    .setExperimentCatalog(expCatalog)
                    .setCuratorClient(Factory.getCuratorClient())
                    .setStatusPublisher(Factory.getStatusPublisher())
                    .setProcessModel(processModel)
                    .setGatewayResourceProfile(appCatalog.getGatewayProfile().getGatewayProfile(gatewayId))
                    .setGatewayComputeResourcePreference(
                            appCatalog.getGatewayProfile()
                                    .getComputeResourcePreference(gatewayId, processModel.getComputeResourceId()))
                    .setGatewayStorageResourcePreference(
                            appCatalog.getGatewayProfile()
                                    .getStoragePreference(gatewayId, processModel.getStorageResourceId()));

            processContext = builder.build();
            /* check point */
            checkpoint(processContext);

            if (processModel.isUseUserCRPref()) {
                setUserResourceProfile(gatewayId, processContext);
                setUserComputeResourcePreference(gatewayId, processContext);
            }

            String scratchLocation = processContext.getScratchLocation();
            String workingDirectory = scratchLocation + File.separator + processId + File.separator;
            StorageResourceDescription storageResource = appCatalog.getStorageResource()
                    .getStorageResource(processModel.getStorageResourceId());
            if (storageResource != null){
                processContext.setStorageResource(storageResource);
            }else {
                // we need to fail the process which will fail the experiment
                processContext.setProcessStatus(new ProcessStatus(ProcessState.FAILED));
                GFacUtils.saveAndPublishProcessStatus(processContext);
                throw new GFacException("expId: " + processModel.getExperimentId() + ", processId: " + processId +
                        ":- Couldn't find storage resource for storage resource id :" + processModel.getStorageResourceId());
            }

/*            StorageResourceDescription storageResource = appCatalog.getStorageResource().getStorageResource(processModel.getStorageResourceId());
            if (storageResource != null){
                processContext.setStorageResource(storageResource);
            }*/
            processContext.setComputeResourceDescription(appCatalog.getComputeResource().getComputeResource
                    (processContext.getComputeResourceId()));
            processContext.setApplicationDeploymentDescription(appCatalog.getApplicationDeployment()
                    .getApplicationDeployement(processModel.getApplicationDeploymentId()));
            ApplicationInterfaceDescription applicationInterface = appCatalog.getApplicationInterface()
                    .getApplicationInterface(processModel.getApplicationInterfaceId());
            processContext.setApplicationInterfaceDescription(applicationInterface);
            List<OutputDataObjectType> applicationOutputs = applicationInterface.getApplicationOutputs();
            if (applicationOutputs != null && !applicationOutputs.isEmpty()) {
                for (OutputDataObjectType outputDataObjectType : applicationOutputs) {
                    if (outputDataObjectType.getType().equals(DataType.STDOUT)) {
                        if (outputDataObjectType.getValue() == null || outputDataObjectType.getValue().equals("")) {
                            outputDataObjectType.setValue(workingDirectory + applicationInterface.getApplicationName() + ".stdout");
                            processContext.setStdoutLocation(workingDirectory + applicationInterface.getApplicationName() + ".stdout");
                        } else {
                            processContext.setStdoutLocation(outputDataObjectType.getValue());
                        }
                    }
                    if (outputDataObjectType.getType().equals(DataType.STDERR)) {
                        if (outputDataObjectType.getValue() == null || outputDataObjectType.getValue().equals("")) {
                            String stderrLocation = workingDirectory + applicationInterface.getApplicationName() + ".stderr";
                            outputDataObjectType.setValue(stderrLocation);
                            processContext.setStderrLocation(stderrLocation);
                        } else {
                            processContext.setStderrLocation(outputDataObjectType.getValue());
                        }
                    }
                }
            }
            expCatalog.update(ExperimentCatalogModelType.PROCESS, processModel, processId);
            processModel.setProcessOutputs(applicationOutputs);

            if (processContext.getJobSubmissionProtocol() == JobSubmissionProtocol.UNICORE) {
                // process monitor mode set in getResourceJobManager method, but unicore doesn't have resource job manager.
                // hence we set process monitor mode here.
                processContext.setMonitorMode(MonitorMode.FORK);
            } else {
                processContext.setResourceJobManager(getResourceJobManager(processContext));
                processContext.setJobSubmissionRemoteCluster(Factory.getJobSubmissionRemoteCluster(processContext));
                processContext.setDataMovementRemoteCluster(Factory.getDataMovementRemoteCluster(processContext));
            }

            String inputPath = ServerSettings.getLocalDataLocation();
            if (inputPath != null) {
                processContext.setLocalWorkingDir((inputPath.endsWith("/") ? inputPath : inputPath + "/") +
                        processContext.getProcessId());
            }

            List<Object> jobModels = expCatalog.get(ExperimentCatalogModelType.JOB, "processId", processId);
            if (jobModels != null && !jobModels.isEmpty()) {
                if (jobModels.size() > 1) {
                    log.warn("Process has more than one job model, take first one");
                }
                processContext.setJobModel(((JobModel) jobModels.get(0)));
            }



            return processContext;
        } catch (AppCatalogException e) {
            String msg = "App catalog access exception ";
            saveErrorModel(processContext, e, msg);
            updateProcessFailure(processContext, msg);
            throw new GFacException(msg, e);
        } catch (RegistryException e) {
            String msg = "Registry access exception";
            saveErrorModel(processContext, e, msg);
            updateProcessFailure(processContext, msg);
            throw new GFacException(msg, e);
        } catch (AiravataException e) {
            String msg = "Remote cluster initialization error";
            saveErrorModel(processContext, e, msg);
            updateProcessFailure(processContext, msg);
            throw new GFacException(msg, e);
        }

    }

    private void checkpoint(ProcessContext processContext) {
        try {
            checkRecoveryWithCancel(processContext);
        } catch (Exception e) {
            log.error("expId: {}, processId: {}, Error while checking process cancel data in zookeeper",
                    processContext.getExperimentId(), processContext.getProcessId());
        }
    }

    private void setUserResourceProfile(String gatewayId, ProcessContext processContext) throws AppCatalogException {
        AppCatalog appCatalog = processContext.getAppCatalog();
        ProcessModel processModel = processContext.getProcessModel();

        UserResourceProfile userResourceProfile =
                appCatalog.getUserResourceProfile()
                        .getUserResourceProfile(processModel.getUserName(), gatewayId);

        processContext.setUserResourceProfile(userResourceProfile);
    }

    private void setUserComputeResourcePreference(String gatewayId, ProcessContext processContext) throws AppCatalogException {
        AppCatalog appCatalog = processContext.getAppCatalog();
        ProcessModel processModel = processContext.getProcessModel();
        UserComputeResourcePreference userComputeResourcePreference =
                appCatalog.getUserResourceProfile().getUserComputeResourcePreference(
                        processModel.getUserName(),
                        gatewayId,
                        processModel.getComputeResourceId());
        processContext.setUserComputeResourcePreference(userComputeResourcePreference);
    }

    private void checkRecoveryWithCancel(ProcessContext processContext) throws Exception {
        CuratorFramework curatorClient = processContext.getCuratorClient();
        String experimentId = processContext.getExperimentId();
        String processId = processContext.getProcessId();
        String processCancelNodePath = ZKPaths.makePath(ZKPaths.makePath(ZKPaths.makePath(
                ZkConstants.ZOOKEEPER_EXPERIMENT_NODE, experimentId), processId), ZkConstants.ZOOKEEPER_CANCEL_LISTENER_NODE);
        log.info("expId: {}, processId: {}, get process cancel data from zookeeper node {}", experimentId, processId, processCancelNodePath);
        byte[] bytes = curatorClient.getData().forPath(processCancelNodePath);
        if (bytes != null && new String(bytes).equalsIgnoreCase(ZkConstants.ZOOKEEPER_CANCEL_REQEUST)) {
            processContext.setRecoveryWithCancel(true);
        }
    }

    @Override
    public void executeProcess(ProcessContext processContext) throws GFacException {
        if (processContext.isInterrupted()) {
            GFacUtils.handleProcessInterrupt(processContext);
            return;
        }
        String taskDag = processContext.getTaskDag();
        List<String> taskIds = GFacUtils.parseTaskDag(taskDag);
        processContext.setTaskExecutionOrder(taskIds);
        executeTaskListFrom(processContext, taskIds.get(0));
    }

    private void executeTaskListFrom(ProcessContext processContext, String startingTaskId) throws GFacException {
        // checkpoint
        if (processContext.isInterrupted() && processContext.getProcessState() != ProcessState.MONITORING) {
            GFacUtils.handleProcessInterrupt(processContext);
            return;
        }
        List<TaskModel> taskList = processContext.getTaskList();
        Map<String, TaskModel> taskMap = processContext.getTaskMap();
        boolean fastForward = true;
        for (String taskId : processContext.getTaskExecutionOrder()) {
            if (fastForward) {
                if (taskId.equalsIgnoreCase(startingTaskId)) {
                    fastForward = false;
                } else {
                    continue;
                }
            }

            TaskModel taskModel = taskMap.get(taskId);
            processContext.setCurrentExecutingTaskModel(taskModel);
            TaskTypes taskType = taskModel.getTaskType();
            TaskContext taskContext = getTaskContext(processContext);
            taskContext.setTaskModel(taskModel);
            ProcessStatus status = null;
            switch (taskType) {
                case ENV_SETUP:
                    status = new ProcessStatus(ProcessState.CONFIGURING_WORKSPACE);
                    status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                    processContext.setProcessStatus(status);
                    GFacUtils.saveAndPublishProcessStatus(processContext);
                    // checkpoint
                    if (processContext.isInterrupted()) {
                        GFacUtils.handleProcessInterrupt(processContext);
                        return;
                    }
                    configureWorkspace(taskContext, processContext.isRecovery());
                    // checkpoint
                    if (processContext.isInterrupted()) {
                        GFacUtils.handleProcessInterrupt(processContext);
                        return;
                    }
                    break;
                case DATA_STAGING:
                    try {
                        // checkpoint
                        if (processContext.isInterrupted()) {
                            GFacUtils.handleProcessInterrupt(processContext);
                            return;
                        }
                        DataStagingTaskModel subTaskModel = (DataStagingTaskModel) taskContext.getSubTaskModel();
                        DataStageType type = subTaskModel.getType();
                        switch (type) {
                            case INPUT:
                                status = new ProcessStatus(ProcessState.INPUT_DATA_STAGING);
                                status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                                processContext.setProcessStatus(status);
                                GFacUtils.saveAndPublishProcessStatus(processContext);
                                taskContext.setProcessInput(subTaskModel.getProcessInput());
                                inputDataStaging(taskContext, processContext.isRecovery());
                                break;
                            case OUPUT:
                                status = new ProcessStatus(ProcessState.OUTPUT_DATA_STAGING);
                                status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                                processContext.setProcessStatus(status);
                                GFacUtils.saveAndPublishProcessStatus(processContext);
                                taskContext.setProcessOutput(subTaskModel.getProcessOutput());
                                outputDataStaging(taskContext, processContext.isRecovery(), false);
                                break;
                            case ARCHIVE_OUTPUT:
                                status = new ProcessStatus(ProcessState.OUTPUT_DATA_STAGING);
                                status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                                processContext.setProcessStatus(status);
                                GFacUtils.saveAndPublishProcessStatus(processContext);
                                outputDataStaging(taskContext, processContext.isRecovery(), true);
                                break;

                        }
                        // checkpoint
                        if (processContext.isInterrupted()) {
                            GFacUtils.handleProcessInterrupt(processContext);
                            return;
                        }
                    } catch (TException e) {
                        throw new GFacException(e);
                    }
                    break;

                case JOB_SUBMISSION:
                    // checkpoint
                    if (processContext.isInterrupted()) {
                        GFacUtils.handleProcessInterrupt(processContext);
                        return;
                    }
                    status = new ProcessStatus(ProcessState.EXECUTING);
                    status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                    processContext.setProcessStatus(status);
                    GFacUtils.saveAndPublishProcessStatus(processContext);
                    executeJobSubmission(taskContext, processContext.isRecovery());
                    // Don't put any checkpoint in between JobSubmission and Monitoring tasks

                    JobStatus jobStatus = processContext.getJobModel().getJobStatuses().get(0);
                    if (jobStatus != null && (jobStatus.getJobState() == JobState.SUBMITTED
                            || jobStatus.getJobState() == JobState.QUEUED || jobStatus.getJobState() == JobState.ACTIVE)) {

                        List<OutputDataObjectType> processOutputs = processContext.getProcessModel().getProcessOutputs();
                        if (processOutputs != null && !processOutputs.isEmpty()){
                            for (OutputDataObjectType output : processOutputs){
                                try {
                                    if (output.isOutputStreaming()){
                                        TaskModel streamingTaskModel = new TaskModel();
                                        streamingTaskModel.setTaskType(TaskTypes.OUTPUT_FETCHING);
                                        streamingTaskModel.setTaskStatuses(Arrays.asList(new TaskStatus(TaskState.CREATED)));
                                        streamingTaskModel.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());
                                        streamingTaskModel.setParentProcessId(processContext.getProcessId());
                                        TaskContext streamingTaskContext = getTaskContext(processContext);
                                        DataStagingTaskModel submodel = new DataStagingTaskModel();
                                        submodel.setType(DataStageType.OUPUT);
                                        submodel.setProcessOutput(output);
                                        URI source = new URI(processContext.getDataMovementProtocol().name(),
                                                processContext.getComputeResourceLoginUserName(),
                                                processContext.getComputeResourceDescription().getHostName(),
                                                22,
                                                processContext.getWorkingDir() + output.getValue(), null, null);
                                        submodel.setSource(source.getPath());
                                        submodel.setDestination("dummy://temp/file/location");
                                        streamingTaskModel.setSubTaskModel(ThriftUtils.serializeThriftObject(submodel));
                                        String streamTaskId = (String) processContext.getExperimentCatalog()
                                                .add(ExpCatChildDataType.TASK, streamingTaskModel, processContext.getProcessId());
                                        streamingTaskModel.setTaskId(streamTaskId);
                                        streamingTaskContext.setTaskModel(streamingTaskModel);
                                        executeDataStreaming(streamingTaskContext, processContext.isRecovery());
                                    }
                                } catch (URISyntaxException | TException | RegistryException e) {
                                    log.error("Error while streaming output " + output.getValue());
                                }
                            }
                        }
                    }
                    break;

                case MONITORING:
                    status = new ProcessStatus(ProcessState.MONITORING);
                    status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                    processContext.setProcessStatus(status);
                    GFacUtils.saveAndPublishProcessStatus(processContext);
                    executeJobMonitoring(taskContext, processContext.isRecovery());
                    break;

                case ENV_CLEANUP:
                    // TODO implement environment clean up task logic
                    break;

                default:
                    throw new GFacException("Unsupported Task type");

            }


            if (processContext.isPauseTaskExecution()) {
                return;   // If any task put processContext to wait, the same task must continue processContext execution.
            }

        }
        processContext.setComplete(true);
    }

    private void executeJobMonitoring(TaskContext taskContext, boolean recovery) throws GFacException {
        ProcessContext processContext = taskContext.getParentProcessContext();
        TaskStatus taskStatus;
        JobMonitor monitorService = null;
        try {
            taskStatus = new TaskStatus(TaskState.EXECUTING);
            taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            taskContext.setTaskStatus(taskStatus);
            GFacUtils.saveAndPublishTaskStatus(taskContext);

            MonitorTaskModel monitorTaskModel = ((MonitorTaskModel) taskContext.getSubTaskModel());
            monitorService = Factory.getMonitorService(monitorTaskModel.getMonitorMode());
            if (!monitorService.isMonitoring(processContext.getJobModel().getJobId())) {
                monitorService.monitor(processContext.getJobModel().getJobId(), taskContext);
            } else {
                log.warn("Jobid: {}, already in monitoring map", processContext.getJobModel().getJobId());
            }
        } catch (AiravataException | TException e) {
            taskStatus = new TaskStatus(TaskState.FAILED);
            taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            taskStatus.setReason("Couldn't handover jobId {} to monitor service, monitor service type {}");
            taskContext.setTaskStatus(taskStatus);
            GFacUtils.saveAndPublishTaskStatus(taskContext);

            String errorMsg = new StringBuilder("expId: ").append(processContext.getExperimentId()).append(", processId: ")
                    .append(processContext.getProcessId()).append(", taskId: ").append(taskContext.getTaskId())
                    .append(", type: ").append(taskContext.getTaskType().name()).append(" :- Input staging failed. Reason: ")
                    .append(taskStatus.getReason()).toString();
            ErrorModel errorModel = new ErrorModel();
            errorModel.setUserFriendlyMessage("Error while staging output data");
            errorModel.setActualErrorMessage(errorMsg);
            GFacUtils.saveTaskError(taskContext, errorModel);
            throw new GFacException(e);
        }
        if (processContext.isPauseTaskExecution()) {
            // we won't update task status to complete, job monitor will update task status to complete after it complete monitoring for this job id.
            return;
        }
        taskStatus = new TaskStatus(TaskState.COMPLETED);
        taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        taskStatus.setReason("Successfully handed over job id to job monitor service.");
        taskContext.setTaskStatus(taskStatus);
        GFacUtils.saveAndPublishTaskStatus(taskContext);
    }

    private boolean executeJobSubmission(TaskContext taskContext, boolean recovery) throws GFacException {
        TaskStatus taskStatus = new TaskStatus(TaskState.EXECUTING);
        taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        taskContext.setTaskStatus(taskStatus);
        GFacUtils.saveAndPublishTaskStatus(taskContext);
        try {
            JobSubmissionTaskModel jobSubmissionTaskModel = ((JobSubmissionTaskModel) taskContext.getSubTaskModel());
            JobSubmissionTask jobSubmissionTask = Factory.getJobSubmissionTask(jobSubmissionTaskModel.getJobSubmissionProtocol());

            ProcessContext processContext = taskContext.getParentProcessContext();
            taskStatus = executeTask(taskContext, jobSubmissionTask, recovery);
            taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            taskContext.setTaskStatus(taskStatus);
            GFacUtils.saveAndPublishTaskStatus(taskContext);
            checkFailures(taskContext, taskStatus, jobSubmissionTask);
            return false;
        } catch (TException e) {
            throw new GFacException(e);
        }
    }

    private void executeDataStreaming(TaskContext taskContext, boolean recovery) throws GFacException {
        TaskStatus taskStatus = new TaskStatus(TaskState.EXECUTING);
        taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        taskContext.setTaskStatus(taskStatus);
        GFacUtils.saveAndPublishTaskStatus(taskContext);
        try {
            DataStreamingTask dataStreamingTask = new DataStreamingTask();
            taskStatus = executeTask(taskContext, dataStreamingTask, recovery);
            taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            taskContext.setTaskStatus(taskStatus);
            GFacUtils.saveAndPublishTaskStatus(taskContext);
        } catch (Exception e) {
            throw new GFacException(e);
        }
    }

    private boolean configureWorkspace(TaskContext taskContext, boolean recover) throws GFacException {

        try {
            EnvironmentSetupTaskModel subTaskModel = (EnvironmentSetupTaskModel) taskContext.getSubTaskModel();
            Task envSetupTask = null;
            if (subTaskModel.getProtocol() == SecurityProtocol.SSH_KEYS ||
                    subTaskModel.getProtocol() == SecurityProtocol.LOCAL) {
                envSetupTask = new EnvironmentSetupTask();
            } else {
                throw new GFacException("Unsupported security protocol, Airavata doesn't support " +
                        subTaskModel.getProtocol().name() + " protocol yet.");
            }
            TaskStatus status = new TaskStatus(TaskState.EXECUTING);
            status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            taskContext.setTaskStatus(status);
            GFacUtils.saveAndPublishTaskStatus(taskContext);
            TaskStatus taskStatus = executeTask(taskContext, envSetupTask, recover);
            taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            taskContext.setTaskStatus(taskStatus);
            GFacUtils.saveAndPublishTaskStatus(taskContext);

            if (taskStatus.getState() == TaskState.FAILED) {
                log.error("expId: {}, processId: {}, taskId: {} type: {},:- Input staging failed, " +
                        "reason:" + " {}", taskContext.getParentProcessContext().getExperimentId(), taskContext
                        .getParentProcessContext().getProcessId(), taskContext.getTaskId(), envSetupTask.getType
                        ().name(), taskStatus.getReason());
                ProcessContext processContext = taskContext.getParentProcessContext();
                String errorMsg = new StringBuilder("expId: ").append(processContext.getExperimentId()).append(", processId: ")
                        .append(processContext.getProcessId()).append(", taskId: ").append(taskContext.getTaskId())
                        .append(", type: ").append(taskContext.getTaskType().name()).append(" :- Environment Setup failed. Reason: ")
                        .append(taskStatus.getReason()).toString();
                ErrorModel errorModel = new ErrorModel();
                errorModel.setUserFriendlyMessage("Error while environment setup");
                errorModel.setActualErrorMessage(errorMsg);
                GFacUtils.saveTaskError(taskContext, errorModel);
                throw new GFacException("Error while environment setup");
            }
        } catch (TException e) {
            throw new GFacException("Couldn't get environment setup task model", e);
        }
        return false;
    }

    private boolean inputDataStaging(TaskContext taskContext, boolean recover) throws GFacException, TException {
        TaskStatus taskStatus = new TaskStatus(TaskState.EXECUTING);
        taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        taskContext.setTaskStatus(taskStatus);
        GFacUtils.saveAndPublishTaskStatus(taskContext);

        ProcessContext processContext = taskContext.getParentProcessContext();
        // handle URI_COLLECTION input data type
        Task dMoveTask = Factory.getDataMovementTask(processContext.getDataMovementProtocol());
        if(null == dMoveTask){
            throw new GFacException("Unsupported security protocol, Airavata doesn't support " +
                    processContext.getDataMovementProtocol() + " protocol yet.");
        }
        if (taskContext.getProcessInput().getType() == DataType.URI_COLLECTION) {
            String values = taskContext.getProcessInput().getValue();
            String[] multiple_inputs = values.split(GFacConstants.MULTIPLE_INPUTS_SPLITTER);
            DataStagingTaskModel subTaskModel = (DataStagingTaskModel) taskContext.getSubTaskModel();
            for (String input : multiple_inputs) {
                taskContext.getProcessInput().setValue(input);
                subTaskModel.setSource(input);
                taskStatus = executeTask(taskContext, dMoveTask, false);
            }
            taskContext.getProcessInput().setValue(values);
        } else {
            taskStatus = executeTask(taskContext, dMoveTask, false);
        }
        taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        taskContext.setTaskStatus(taskStatus);
        GFacUtils.saveAndPublishTaskStatus(taskContext);
        checkFailures(taskContext, taskStatus, dMoveTask);
        return false;
    }

    private void checkFailures(TaskContext taskContext, TaskStatus taskStatus, Task task) throws GFacException {
        if (taskStatus.getState() == TaskState.FAILED) {
            log.error("expId: {}, processId: {}, taskId: {} type: {},:- " + task.getType().toString() + " failed, " +
                    "reason:" + " {}", taskContext.getParentProcessContext().getExperimentId(), taskContext
                    .getParentProcessContext().getProcessId(), taskContext.getTaskId(), task.getType
                    ().name(), taskStatus.getReason());
            String errorMsg = new StringBuilder("expId: ").append(taskContext.getParentProcessContext().getExperimentId()).append(", processId: ")
                    .append(taskContext.getParentProcessContext().getProcessId()).append(", taskId: ").append(taskContext.getTaskId())
                    .append(", type: ").append(taskContext.getTaskType().name()).append(" :- " + task.getType().toString() + " failed. Reason: ")
                    .append(taskStatus.getReason()).toString();
            ErrorModel errorModel = new ErrorModel();
            errorModel.setUserFriendlyMessage("Error while executing " + task.getType() + " task" );
            errorModel.setActualErrorMessage(errorMsg);
            GFacUtils.saveTaskError(taskContext, errorModel);
            throw new GFacException("Error: userFriendly msg :" + errorModel.getUserFriendlyMessage() + ", actual msg :"
                    + errorModel.getActualErrorMessage());
        }
    }

    @Override
    public void recoverProcess(ProcessContext processContext) throws GFacException {
        processContext.setRecovery(true);
        String taskDag = processContext.getProcessModel().getTaskDag();
        List<String> taskExecutionOrder = GFacUtils.parseTaskDag(taskDag);
        processContext.setTaskExecutionOrder(taskExecutionOrder);
        Map<String, TaskModel> taskMap = processContext.getTaskMap();
        String recoverTaskId = null;
        String previousTaskId = null;
        TaskModel taskModel = null;
        for (String taskId : taskExecutionOrder) {
            taskModel = taskMap.get(taskId);
            TaskState state = taskModel.getTaskStatuses().get(0).getState();
            if (state == TaskState.CREATED || state == TaskState.EXECUTING) {
                recoverTaskId = taskId;
                break;
            }
            previousTaskId = taskId;
        }
        final String rTaskId = recoverTaskId;
        final String pTaskId = previousTaskId;
        if (recoverTaskId != null) {
            if (processContext.isRecoveryWithCancel()) {
                cancelJobSubmission(processContext, rTaskId, pTaskId);
            }
            continueProcess(processContext, recoverTaskId);
        } else {
            log.error("expId: {}, processId: {}, couldn't find recovery task, mark this as complete ",
                    processContext.getExperimentId(), processContext.getProcessId());
            processContext.setComplete(true);
        }
    }

    private void cancelJobSubmission(ProcessContext processContext, String rTaskId, String pTaskId) {
        new Thread(() -> {
            try {
                processContext.setCancel(true);
                ProcessState processState = processContext.getProcessState();
                List<Object> jobModels = null;
                switch (processState) {
                    case EXECUTING:
                        jobModels = processContext.getExperimentCatalog().get(
                                ExperimentCatalogModelType.JOB, Constants.FieldConstants.TaskConstants.TASK_ID,
                                rTaskId);
                        break;
                    case MONITORING:
                        if (pTaskId != null) {
                            jobModels = processContext.getExperimentCatalog().get(
                                    ExperimentCatalogModelType.JOB, Constants.FieldConstants.TaskConstants.TASK_ID,
                                    pTaskId);
                        }
                }

                if (jobModels != null && !jobModels.isEmpty()) {
                    JobModel jobModel = (JobModel) jobModels.get(jobModels.size() - 1);
                    if (jobModel.getJobId() != null) {
                        processContext.setJobModel(jobModel);
                        log.info("expId: {}, processId: {}, Canceling jobId {}", processContext.getExperimentId(),
                                processContext.getProcessId(), jobModel.getJobId());
                        cancelProcess(processContext);
                        log.info("expId: {}, processId: {}, Canceled jobId {}", processContext.getExperimentId(),
                                processContext.getProcessId(), jobModel.getJobId());
                    } else {
                        log.error("expId: {}, processId: {}, Couldn't find jobId in jobModel, aborting process recovery",
                                processContext.getExperimentId(), processContext.getProcessId());
                    }
                }
            } catch (GFacException e) {
                log.error("expId: {}, processId: {}, Error while canceling process which is in recovery mode",
                        processContext.getExperimentId(), processContext.getProcessId());
            } catch (RegistryException e) {
                log.error("expId: {}, processId: {}, Error while getting job model for taskId {}, " +
                                "couldn't cancel process which is in recovery mode", processContext.getExperimentId(),
                        processContext.getProcessId(), rTaskId);
            }
        }).start();
    }

    private JobModel getJobModel(ProcessContext processContext) {
        try {
            return GFacUtils.getJobModel(processContext);
        } catch (RegistryException e) {
            log.error("Error while retrieving jobId,", e);
            return null;
        }
    }

    @Override
    public void continueProcess(ProcessContext processContext, String taskId) throws GFacException {
        executeTaskListFrom(processContext, taskId);
    }

    /**
     * @param processContext
     * @param recovery
     * @return <code>true</code> if you need to interrupt processing <code>false</code> otherwise.
     * @throws GFacException
     */
    private boolean postProcessing(ProcessContext processContext, boolean recovery) throws GFacException {
        ProcessStatus status = new ProcessStatus(ProcessState.POST_PROCESSING);
        status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        processContext.setProcessStatus(status);
        GFacUtils.saveAndPublishProcessStatus(processContext);
//		taskCtx = getEnvCleanupTaskContext(processContext);
        if (processContext.isInterrupted()) {
            GFacUtils.handleProcessInterrupt(processContext);
            return true;
        }
        return false;
    }

    /**
     * @param taskContext
     * @param recovery
     * @return <code>true</code> if process execution interrupted , <code>false</code> otherwise.
     * @throws GFacException
     */
    private boolean outputDataStaging(TaskContext taskContext, boolean recovery, boolean isArchive) throws GFacException {
        TaskStatus taskStatus = new TaskStatus(TaskState.EXECUTING);
        taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        taskContext.setTaskStatus(taskStatus);
        GFacUtils.saveAndPublishTaskStatus(taskContext);

        ProcessContext processContext = taskContext.getParentProcessContext();
        Task dMoveTask = null;
        if (isArchive) {
            dMoveTask = Factory.getArchiveTask();
        } else {
            dMoveTask = Factory.getDataMovementTask(processContext.getDataMovementProtocol());
        }
        if(null == dMoveTask){
            throw new GFacException("Unsupported security protocol, Airavata doesn't support " +
                    processContext.getDataMovementProtocol() + " protocol yet.");
        }
        taskStatus = executeTask(taskContext, dMoveTask, recovery);
        taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        taskContext.setTaskStatus(taskStatus);
        GFacUtils.saveAndPublishTaskStatus(taskContext);

        if (taskStatus.getState() == TaskState.FAILED) {
            log.error("expId: {}, processId: {}, taskId: {} type: {},:- output staging failed, " +
                    "reason:" + " {}", taskContext.getParentProcessContext().getExperimentId(), taskContext
                    .getParentProcessContext().getProcessId(), taskContext.getTaskId(), dMoveTask.getType
                    ().name(), taskStatus.getReason());

            String errorMsg = new StringBuilder("expId: ").append(processContext.getExperimentId()).append(", processId: ")
                    .append(processContext.getProcessId()).append(", taskId: ").append(taskContext.getTaskId())
                    .append(", type: ").append(taskContext.getTaskType().name()).append(" :- Output staging failed. Reason: ")
                    .append(taskStatus.getReason()).toString();
            ErrorModel errorModel = new ErrorModel();
            errorModel.setUserFriendlyMessage("Error while staging output data");
            errorModel.setActualErrorMessage(errorMsg);
            GFacUtils.saveTaskError(taskContext, errorModel);
        }
        return false;
    }


    @Override
    public void cancelProcess(ProcessContext processContext) throws GFacException {
        if (processContext != null) {
            switch (processContext.getProcessState()) {
                case MONITORING: case EXECUTING:
                    // get job submission task and invoke cancel
                    JobSubmissionTask jobSubmissionTask = Factory.getJobSubmissionTask(processContext.getJobSubmissionProtocol());
                    TaskContext taskCtx = getJobSubmissionTaskContext(processContext);
                    executeCancel(taskCtx, jobSubmissionTask);
                    break;
                case COMPLETED: case FAILED: case CANCELED : case CANCELLING:
                    log.warn("Process cancel trigger for already {} process", processContext.getProcessState().name());
                    break;
                default:
                    break;
            }
        }
    }

    private TaskStatus executeTask(TaskContext taskCtx, Task task, boolean recover) throws GFacException {
        TaskStatus taskStatus = null;
        if (recover) {
            taskStatus = task.recover(taskCtx);
        } else {
            taskStatus = task.execute(taskCtx);
        }
        return taskStatus;
    }

    private void executeCancel(TaskContext taskContext, JobSubmissionTask jSTask) throws GFacException {
        try {
            JobStatus oldJobStatus = jSTask.cancel(taskContext);
            // If Job was in Queued state when cancel command runs, then there won't be any email from this job.
            ProcessContext pc = taskContext.getParentProcessContext();
            JobMonitor monitorService = Factory.getMonitorService(pc.getMonitorMode());
            monitorService.canceledJob(pc.getJobModel().getJobId());

        } catch (TaskException e) {
            throw new GFacException("Error while cancelling job");
        } catch (AiravataException e) {
            throw new GFacException("Error wile getting monitoring service");
        }
    }

    private TaskContext getJobSubmissionTaskContext(ProcessContext processContext) throws GFacException {
        TaskContext taskCtx = new TaskContext();
        taskCtx.setParentProcessContext(processContext);

        TaskModel taskModel = new TaskModel();
        taskModel.setParentProcessId(processContext.getProcessId());
        taskModel.setCreationTime(new Date().getTime());
        taskModel.setLastUpdateTime(taskModel.getCreationTime());
        TaskStatus taskStatus = new TaskStatus(TaskState.CREATED);
        taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        taskModel.setTaskStatuses(Arrays.asList(taskStatus));
        taskModel.setTaskType(TaskTypes.JOB_SUBMISSION);
        taskCtx.setTaskModel(taskModel);
        return taskCtx;
    }

    private TaskContext getDataStagingTaskContext(ProcessContext processContext, OutputDataObjectType processOutput)
            throws TException, TaskException, GFacException {
        TaskContext taskCtx = new TaskContext();
        taskCtx.setParentProcessContext(processContext);
        // create new task model for this task
        TaskModel taskModel = new TaskModel();
        taskModel.setParentProcessId(processContext.getProcessId());
        taskModel.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());
        taskModel.setLastUpdateTime(taskModel.getCreationTime());
        TaskStatus taskStatus = new TaskStatus(TaskState.CREATED);
        taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        taskModel.setTaskStatuses(Arrays.asList(taskStatus));
        taskModel.setTaskType(TaskTypes.DATA_STAGING);
        // create data staging sub task model
        String remoteOutputDir = processContext.getOutputDir();
        remoteOutputDir = remoteOutputDir.endsWith("/") ? remoteOutputDir : remoteOutputDir + "/";
        DataStagingTaskModel submodel = new DataStagingTaskModel();
        ServerInfo serverInfo = processContext.getComputeResourceServerInfo();
        URI source = null;
        try {
            source = new URI(processContext.getDataMovementProtocol().name(), serverInfo.getHost(),
                    serverInfo.getUserName(), serverInfo.getPort(), remoteOutputDir + processOutput.getValue(), null, null);
        } catch (URISyntaxException e) {
            throw new TaskException("Error while constructing source file URI");
        }
        submodel.setSource(source.toString());
        // We don't know destination location at this time, data staging task will set this.
        // because destination is required field we set dummy destination
        submodel.setDestination("dummy://temp/file/location");
        taskModel.setSubTaskModel(ThriftUtils.serializeThriftObject(submodel));
        taskCtx.setTaskModel(taskModel);
        taskCtx.setProcessOutput(processOutput);
        return taskCtx;
    }

    /**
     * Persist task model
     */
    private void saveTaskModel(TaskContext taskContext) throws GFacException {
        try {
            TaskModel taskModel = taskContext.getTaskModel();
            taskContext.getParentProcessContext().getExperimentCatalog().add(ExpCatChildDataType.TASK, taskModel,
                    taskModel.getParentProcessId());
        } catch (RegistryException e) {
            throw new GFacException("Error while saving task model", e);
        }
    }

    private TaskContext getTaskContext(ProcessContext processContext) {
        TaskContext taskCtx = new TaskContext();
        taskCtx.setParentProcessContext(processContext);
        return taskCtx;
    }


    /**
     * Sort input data type by input order.
     */
    private void sortByInputOrder(List<InputDataObjectType> processInputs) {
        Collections.sort(processInputs, new Comparator<InputDataObjectType>() {
            @Override
            public int compare(InputDataObjectType inputDT_1, InputDataObjectType inputDT_2) {
                return inputDT_1.getInputOrder() - inputDT_2.getInputOrder();
            }
        });
    }

    private void updateProcessFailure(ProcessContext pc, String reason) throws GFacException {
        if (pc == null) {
            throw new GFacException("Can't update process failure, process context is null");
        }
        ProcessStatus status = new ProcessStatus(ProcessState.FAILED);
        status.setReason(reason);
        pc.setProcessStatus(status);
        try {
            GFacUtils.saveAndPublishProcessStatus(pc);
        } catch (GFacException e) {
            log.error("Error while save and publishing process failed status event");
        }
    }

    private void saveErrorModel(ProcessContext pc, Exception e, String userFriendlyMsg) throws GFacException {
        if(pc == null){
            throw new GFacException("Can't save error process context is null", e);
        }
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        ErrorModel errorModel = new ErrorModel();
        errorModel.setUserFriendlyMessage(userFriendlyMsg);
        errorModel.setActualErrorMessage(errors.toString());
        errorModel.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());
        try {
            GFacUtils.saveProcessError(pc, errorModel);
            GFacUtils.saveExperimentError(pc, errorModel);
        } catch (GFacException e1) {
            log.error("Error while updating error model for process:" + pc.getProcessId());
        }
    }

    public static ResourceJobManager getResourceJobManager(ProcessContext processCtx) throws AppCatalogException, GFacException {
        List<JobSubmissionInterface> jobSubmissionInterfaces = Factory.getDefaultAppCatalog().getComputeResource()
                .getComputeResource(processCtx.getComputeResourceId()).getJobSubmissionInterfaces();

        ResourceJobManager resourceJobManager = null;
        JobSubmissionInterface jsInterface = null;
        for (JobSubmissionInterface jobSubmissionInterface : jobSubmissionInterfaces) {
            if (jobSubmissionInterface.getJobSubmissionProtocol() == processCtx.getJobSubmissionProtocol()) {
                jsInterface = jobSubmissionInterface;
                break;
            }
        }
        if (jsInterface == null) {
            throw new GFacException("Job Submission interface cannot be empty at this point");
        } else if (jsInterface.getJobSubmissionProtocol() == JobSubmissionProtocol.SSH) {
            SSHJobSubmission sshJobSubmission = Factory.getDefaultAppCatalog().getComputeResource().getSSHJobSubmission
                    (jsInterface.getJobSubmissionInterfaceId());
            processCtx.setMonitorMode(sshJobSubmission.getMonitorMode()); // fixme - Move this to populate process
            // context method.
            resourceJobManager = sshJobSubmission.getResourceJobManager();
        } else if (jsInterface.getJobSubmissionProtocol() == JobSubmissionProtocol.LOCAL) {
            LOCALSubmission localSubmission = Factory.getDefaultAppCatalog().getComputeResource().getLocalJobSubmission
                    (jsInterface.getJobSubmissionInterfaceId());
            resourceJobManager = localSubmission.getResourceJobManager();
        } else if (jsInterface.getJobSubmissionProtocol() == JobSubmissionProtocol.SSH_FORK) {
            SSHJobSubmission sshJobSubmission = Factory.getDefaultAppCatalog().getComputeResource().getSSHJobSubmission
                    (jsInterface.getJobSubmissionInterfaceId());
            processCtx.setMonitorMode(sshJobSubmission.getMonitorMode()); // fixme - Move this to populate process
            resourceJobManager = sshJobSubmission.getResourceJobManager();
        } else if (jsInterface.getJobSubmissionProtocol() == JobSubmissionProtocol.CLOUD) {
            return null;
        } else {
            throw new GFacException("Unsupported JobSubmissionProtocol - " + jsInterface.getJobSubmissionProtocol()
                    .name());
        }

        if (resourceJobManager == null) {
            throw new GFacException("Resource Job Manager is empty.");
        }
        return resourceJobManager;
    }
}
