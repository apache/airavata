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

package org.apache.airavata.gfac.impl;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftUtils;
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
import org.apache.airavata.gfac.impl.task.EnvironmentSetupTask;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.model.status.TaskStatus;
import org.apache.airavata.model.task.*;
import org.apache.airavata.registry.cpi.AppCatalog;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.ExpCatChildDataType;
import org.apache.airavata.registry.cpi.ExperimentCatalog;
import org.apache.airavata.registry.cpi.ExperimentCatalogModelType;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class GFacEngineImpl implements GFacEngine {

    private static final Logger log = LoggerFactory.getLogger(GFacEngineImpl.class);

    public GFacEngineImpl() throws GFacException {

    }

    @Override
    public ProcessContext populateProcessContext(String processId, String gatewayId, String
            tokenId) throws GFacException {
        try {
            ProcessContext processContext = new ProcessContext(processId, gatewayId, tokenId);
            AppCatalog appCatalog = Factory.getDefaultAppCatalog();
            processContext.setAppCatalog(appCatalog);
            ExperimentCatalog expCatalog = Factory.getDefaultExpCatalog();
            processContext.setExperimentCatalog(expCatalog);
            processContext.setCuratorClient(Factory.getCuratorClient());
            processContext.setStatusPublisher(Factory.getStatusPublisher());

            ProcessModel processModel = (ProcessModel) expCatalog.get(ExperimentCatalogModelType.PROCESS, processId);
            processContext.setProcessModel(processModel);
            GatewayResourceProfile gatewayProfile = appCatalog.getGatewayProfile().getGatewayProfile(gatewayId);
            processContext.setGatewayResourceProfile(gatewayProfile);
            processContext.setComputeResourcePreference(appCatalog.getGatewayProfile().getComputeResourcePreference
                    (gatewayId, processModel.getComputeResourceId()));
            processContext.setComputeResourceDescription(appCatalog.getComputeResource().getComputeResource
                    (processContext.getComputeResourcePreference().getComputeResourceId()));
            processContext.setApplicationDeploymentDescription(appCatalog.getApplicationDeployment()
                    .getApplicationDeployement(processModel.getApplicationDeploymentId()));
            ApplicationInterfaceDescription applicationInterface = appCatalog.getApplicationInterface()
                    .getApplicationInterface(processModel.getApplicationInterfaceId());
            processContext.setApplicationInterfaceDescription(applicationInterface);
            String computeResourceId = processContext.getComputeResourceDescription().getComputeResourceId();
            String hostName = Factory.getDefaultAppCatalog().getComputeResource().getComputeResource(computeResourceId).getHostName();
            ServerInfo serverInfo = new ServerInfo(processContext.getComputeResourcePreference().getLoginUserName(), hostName);
            processContext.setServerInfo(serverInfo);
            List<OutputDataObjectType> applicationOutputs = applicationInterface.getApplicationOutputs();
            if (applicationOutputs != null && !applicationOutputs.isEmpty()) {
                for (OutputDataObjectType outputDataObjectType : applicationOutputs) {
                    if (outputDataObjectType.getType().equals(DataType.STDOUT)) {
                        if (outputDataObjectType.getValue() == null || outputDataObjectType.getValue().equals("")) {
                            outputDataObjectType.setValue(applicationInterface.getApplicationName() + ".stdout");
                            processContext.setStdoutLocation(applicationInterface.getApplicationName() + ".stdout");
                        } else {
                            processContext.setStdoutLocation(outputDataObjectType.getValue());
                        }
                    }
                    if (outputDataObjectType.getType().equals(DataType.STDERR)) {
                        if (outputDataObjectType.getValue() == null || outputDataObjectType.getValue().equals("")) {
                            String stderrLocation = applicationInterface.getApplicationName() + ".stderr";
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
            processContext.setResourceJobManager(getResourceJobManager(processContext));
            processContext.setJobSubmissionRemoteCluster(Factory.getJobSubmissionRemoteCluster(processContext));
            processContext.setDataMovementRemoteCluster(Factory.getDataMovementRemoteCluster(processContext));

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
            throw new GFacException("App catalog access exception ", e);
        } catch (RegistryException e) {
            throw new GFacException("Registry access exception", e);
        } catch (AiravataException e) {
            throw new GFacException("Remote cluster initialization error", e);
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
        if (processContext.isInterrupted()) {
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
                        if (subTaskModel.getType() == DataStageType.INPUT) {
                            status = new ProcessStatus(ProcessState.INPUT_DATA_STAGING);
                            status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                            processContext.setProcessStatus(status);
                            GFacUtils.saveAndPublishProcessStatus(processContext);
                            taskContext.setProcessInput(subTaskModel.getProcessInput());
                            inputDataStaging(taskContext, processContext.isRecovery());
                        } else {
                            status = new ProcessStatus(ProcessState.OUTPUT_DATA_STAGING);
                            status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                            processContext.setProcessStatus(status);
                            GFacUtils.saveAndPublishProcessStatus(processContext);
                            taskContext.setProcessOutput(subTaskModel.getProcessOutput());
                            outputDataStaging(taskContext, processContext.isRecovery());
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
                    // checkpoint
                    if (processContext.isInterrupted()) {
                        GFacUtils.handleProcessInterrupt(processContext);
                        return;
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

    private boolean configureWorkspace(TaskContext taskContext, boolean recover) throws GFacException {

        try {
            EnvironmentSetupTaskModel subTaskModel = (EnvironmentSetupTaskModel) taskContext.getSubTaskModel();
            Task envSetupTask = null;
            if (subTaskModel.getProtocol() == SecurityProtocol.SSH_KEYS) {
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

    private boolean inputDataStaging(TaskContext taskContext, boolean recover) throws GFacException {
        TaskStatus taskStatus = new TaskStatus(TaskState.EXECUTING);
        taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        taskContext.setTaskStatus(taskStatus);
        GFacUtils.saveAndPublishTaskStatus(taskContext);

        ProcessContext processContext = taskContext.getParentProcessContext();
        Task dMoveTask = Factory.getDataMovementTask(processContext.getDataMovementProtocol());
        taskStatus = executeTask(taskContext, dMoveTask, false);
        taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        taskContext.setTaskStatus(taskStatus);
        GFacUtils.saveAndPublishTaskStatus(taskContext);

        checkFailures(taskContext, taskStatus, dMoveTask);
        return false;
    }

    private void checkFailures(TaskContext taskContext, TaskStatus taskStatus, Task dMoveTask) throws GFacException {
        if (taskStatus.getState() == TaskState.FAILED) {
            log.error("expId: {}, processId: {}, taskId: {} type: {},:- Input statging failed, " +
                    "reason:" + " {}", taskContext.getParentProcessContext().getExperimentId(), taskContext
                    .getParentProcessContext().getProcessId(), taskContext.getTaskId(), dMoveTask.getType
                    ().name(), taskStatus.getReason());
            String errorMsg = new StringBuilder("expId: ").append(taskContext.getParentProcessContext().getExperimentId()).append(", processId: ")
                    .append(taskContext.getParentProcessContext().getProcessId()).append(", taskId: ").append(taskContext.getTaskId())
                    .append(", type: ").append(taskContext.getTaskType().name()).append(" :- Input staging failed. Reason: ")
                    .append(taskStatus.getReason()).toString();
            ErrorModel errorModel = new ErrorModel();
            errorModel.setUserFriendlyMessage("Error while staging input data");
            errorModel.setActualErrorMessage(errorMsg);
            GFacUtils.saveTaskError(taskContext, errorModel);
            throw new GFacException("Error while staging input data");
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
        for (String taskId : taskExecutionOrder) {
            TaskModel taskModel = taskMap.get(taskId);
            TaskState state = taskModel.getTaskStatus().getState();
            if (state == TaskState.CREATED || state == TaskState.EXECUTING) {
                recoverTaskId = taskId;
                break;
            }
        }

        continueProcess(processContext, recoverTaskId);
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
        if (processContext.isInterrupted()) {
            GFacUtils.handleProcessInterrupt(processContext);
            return;
        }
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
    private boolean outputDataStaging(TaskContext taskContext, boolean recovery) throws GFacException {
        TaskStatus taskStatus = new TaskStatus(TaskState.EXECUTING);
        taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        taskContext.setTaskStatus(taskStatus);
        GFacUtils.saveAndPublishTaskStatus(taskContext);

        ProcessContext processContext = taskContext.getParentProcessContext();
        Task dMoveTask = Factory.getDataMovementTask(processContext.getDataMovementProtocol());
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
        if (processContext.getProcessState() == ProcessState.MONITORING) {
            // get job submission task and invoke cancel
            JobSubmissionTask jobSubmissionTask = Factory.getJobSubmissionTask(processContext.getJobSubmissionProtocol());
            TaskContext taskCtx = getJobSubmissionTaskContext(processContext);
            executeCancel(taskCtx, jobSubmissionTask);
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

            if (oldJobStatus != null && oldJobStatus.getJobState() == JobState.QUEUED) {
                JobMonitor monitorService = Factory.getMonitorService(taskContext.getParentProcessContext().getMonitorMode());
                monitorService.stopMonitor(taskContext.getParentProcessContext().getJobModel().getJobId(), true);
                JobStatus newJobStatus = new JobStatus(JobState.CANCELED);
                newJobStatus.setReason("Job cancelled");
                newJobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                taskContext.getParentProcessContext().getJobModel().setJobStatus(newJobStatus);
                GFacUtils.saveJobStatus(taskContext.getParentProcessContext(), taskContext.getParentProcessContext()
                        .getJobModel());
            }
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
        taskModel.setTaskStatus(taskStatus);
        taskModel.setTaskType(TaskTypes.JOB_SUBMISSION);
        taskCtx.setTaskModel(taskModel);
        return taskCtx;
    }

    private TaskContext getDataStagingTaskContext(ProcessContext processContext, OutputDataObjectType processOutput)
            throws TException, TaskException {
        TaskContext taskCtx = new TaskContext();
        taskCtx.setParentProcessContext(processContext);
        // create new task model for this task
        TaskModel taskModel = new TaskModel();
        taskModel.setParentProcessId(processContext.getProcessId());
        taskModel.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());
        taskModel.setLastUpdateTime(taskModel.getCreationTime());
        TaskStatus taskStatus = new TaskStatus(TaskState.CREATED);
        taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        taskModel.setTaskStatus(taskStatus);
        taskModel.setTaskType(TaskTypes.DATA_STAGING);
        // create data staging sub task model
        String remoteOutputDir = processContext.getOutputDir();
        remoteOutputDir = remoteOutputDir.endsWith("/") ? remoteOutputDir : remoteOutputDir + "/";
        DataStagingTaskModel submodel = new DataStagingTaskModel();
        ServerInfo serverInfo = processContext.getServerInfo();
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
