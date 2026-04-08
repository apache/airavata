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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.apache.airavata.config.ServerSettings;
import org.apache.airavata.exception.AiravataException;
import org.apache.airavata.interfaces.RegistryHandler;
import org.apache.airavata.interfaces.UserProfileProvider;
import org.apache.airavata.messaging.service.MessageContext;
import org.apache.airavata.messaging.service.MessagingFactory;
import org.apache.airavata.messaging.service.Publisher;
import org.apache.airavata.messaging.service.Type;
import org.apache.airavata.model.appcatalog.computeresource.proto.ComputeResourceDescription;
import org.apache.airavata.model.application.io.proto.OutputDataObjectType;
import org.apache.airavata.model.commons.proto.ErrorModel;
import org.apache.airavata.model.data.replica.proto.*;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.model.messaging.event.proto.*;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.model.status.proto.*;
import org.apache.airavata.util.AiravataUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.helix.HelixManager;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public abstract class AiravataTask extends AbstractTask {

    private static final Logger logger = LoggerFactory.getLogger(AiravataTask.class);

    private static Publisher statusPublisher;

    private ProcessModel processModel;
    private ExperimentModel experimentModel;
    private ComputeResourceDescription computeResourceDescription;
    private TaskContext taskContext;
    private String taskName;

    @TaskParam(name = "Process Id")
    private String processId;

    @TaskParam(name = "experimentId")
    private String experimentId;

    @TaskParam(name = "gatewayId")
    private String gatewayId;

    @TaskParam(name = "Skip All Status Publish")
    private boolean skipAllStatusPublish = false;

    @TaskParam(name = "Skip Process Status Publish")
    private boolean skipProcessStatusPublish = false;

    @TaskParam(name = "Skip Experiment Status Publish")
    private boolean skipExperimentStatusPublish = false;

    @TaskParam(name = "Force Run Task")
    private boolean forceRunTask = false;

    @TaskParam(name = "Auto Schedule")
    private boolean autoSchedule = false;

    protected TaskResult onSuccess(String message) {
        logger.info(message);
        if (!skipAllStatusPublish) {
            publishTaskState(TaskState.TASK_STATE_COMPLETED);
        }

        try {
            logger.info("Deleting task specific monitoring nodes");
            MonitoringUtil.deleteTaskSpecificNodes(getCuratorClient(), getTaskId());
        } catch (Exception e) {
            logger.error("Failed to delete task specific nodes but continuing", e);
        }

        return super.onSuccess(message);
    }

    protected TaskResult onFail(String reason, boolean fatal, Throwable error) {
        logger.error(reason, error);
        int currentRetryCount = 1;
        try {
            currentRetryCount = getCurrentRetryCount();
        } catch (Exception e) {
            logger.error("Failed to obtain current retry count. So failing the task permanently", e);
            fatal = true;
        }

        logger.warn("Task failed with fatal = " + fatal + ".  Current retry count " + currentRetryCount
                + " total retry count " + getRetryCount());

        if (currentRetryCount < getRetryCount() && !fatal) {
            try {
                markNewRetry(currentRetryCount);
            } catch (Exception e) {
                logger.error("Failed to mark retry. So failing the task permanently", e);
                fatal = true;
            }
        }

        if (currentRetryCount >= getRetryCount() || fatal) {
            ProcessStatus status = ProcessStatus.newBuilder()
                    .setState(ProcessState.PROCESS_STATE_FAILED)
                    .build();
            StringWriter errors = new StringWriter();

            String errorCode = UUID.randomUUID().toString();
            String errorMessage = "Error Code : " + errorCode + ", Task " + getTaskId() + " failed due to " + reason
                    + (error == null ? "" : ", " + error.getMessage());

            // wrapping from new error object with error code
            error = new TaskOnFailException(errorMessage, true, error);

            status = status.toBuilder()
                    .setReason(errorMessage)
                    .setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime())
                    .build();
            errors.write(ExceptionUtils.getStackTrace(error));
            logger.error(errorMessage, error);

            if (getTaskContext() != null) { // task context could be null if the initialization failed
                getTaskContext().setProcessStatus(status);
            } else {
                logger.warn("Task context is null. So can not store the process status in the context");
            }

            ErrorModel errorModel = ErrorModel.newBuilder()
                    .setUserFriendlyMessage(reason)
                    .setActualErrorMessage(errors.toString())
                    .setCreationTime(AiravataUtils.getCurrentTimestamp().getTime())
                    .build();

            if (!skipAllStatusPublish) {
                publishTaskState(TaskState.TASK_STATE_FAILED);
                saveTaskError(errorModel);

                if (!skipProcessStatusPublish) {
                    saveAndPublishProcessStatus(taskContext != null ? taskContext.getProcessStatus() : status);
                    saveProcessError(errorModel);
                }

                if (!skipExperimentStatusPublish) {
                    saveExperimentError(errorModel);
                }
            }

            try {
                logger.info("Deleting task specific monitoring nodes");
                MonitoringUtil.deleteTaskSpecificNodes(getCuratorClient(), getTaskId());
            } catch (Exception e) {
                logger.error("Failed to delete task specific nodes but continuing", e);
            }

            cleanup();

            if (autoSchedule) {
                ProcessStatus requeueStatus = ProcessStatus.newBuilder()
                        .setState(ProcessState.PROCESS_STATE_REQUEUED)
                        .build();
                saveAndPublishProcessStatus(requeueStatus);
            }

            return onFail(errorMessage, fatal);
        } else {
            return onFail("Handover back to helix engine to retry", fatal);
        }
    }

    protected void cleanup() {

        try {
            // cleaning up local data directory
            String localDataPath = ServerSettings.getLocalDataLocation();
            localDataPath = (localDataPath.endsWith(File.separator) ? localDataPath : localDataPath + File.separator);
            localDataPath = localDataPath + getProcessId();

            try {
                FileUtils.deleteDirectory(new File(localDataPath));
            } catch (IOException e) {
                logger.error("Failed to delete local data directory " + localDataPath, e);
            }
        } catch (Exception e) {
            logger.error("Failed to clean up", e);
        }
    }

    protected void saveAndPublishProcessStatus(ProcessState state) {

        if (!skipProcessStatusPublish) {
            ProcessStatus processStatus = ProcessStatus.newBuilder()
                    .setState(state)
                    .setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime())
                    .build();
            if (getTaskContext() != null) {
                getTaskContext().setProcessStatus(processStatus);
            } else {
                logger.warn("Task context is null. So can not store the process status in the context");
            }
            saveAndPublishProcessStatus((taskContext != null ? taskContext.getProcessStatus() : processStatus));
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected void saveAndPublishProcessStatus(ProcessStatus status) {
        try {
            if (!skipProcessStatusPublish) {
                if (status.getTimeOfStateChange() == 0 || status.getTimeOfStateChange() > 0) {
                    status = status.toBuilder()
                            .setTimeOfStateChange(
                                    AiravataUtils.getCurrentTimestamp().getTime())
                            .build();
                }
                getRegistryServiceClient().addProcessStatus(status, getProcessId());
                ProcessIdentifier identifier = ProcessIdentifier.newBuilder()
                        .setProcessId(getProcessId())
                        .setExperimentId(getExperimentId())
                        .setGatewayId(getGatewayId())
                        .build();
                ProcessStatusChangeEvent processStatusChangeEvent = ProcessStatusChangeEvent.newBuilder()
                        .setState(status.getState())
                        .setProcessIdentity(identifier)
                        .build();
                MessageContext msgCtx = new MessageContext(
                        processStatusChangeEvent,
                        MessageType.PROCESS,
                        AiravataUtils.getId(MessageType.PROCESS.name()),
                        getGatewayId());
                msgCtx.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
                getStatusPublisher().publish(msgCtx);
            }
        } catch (Exception e) {
            logger.error("Failed to save process status of process " + getProcessId(), e);
        }
    }

    public void saveAndPublishJobStatus(
            String jobId, String taskId, String processId, String experimentId, String gateway, JobState jobState)
            throws Exception {
        try {

            long now = AiravataUtils.getCurrentTimestamp().getTime();
            JobStatus jobStatus = JobStatus.newBuilder()
                    .setReason(jobState.name())
                    .setTimeOfStateChange(now)
                    .setJobState(jobState)
                    .build();

            getRegistryServiceClient().addJobStatus(jobStatus, taskId, jobId);

            JobIdentifier identifier = JobIdentifier.newBuilder()
                    .setJobId(jobId)
                    .setTaskId(taskId)
                    .setProcessId(processId)
                    .setExperimentId(experimentId)
                    .setGatewayId(gateway)
                    .build();

            JobStatusChangeEvent jobStatusChangeEvent = JobStatusChangeEvent.newBuilder()
                    .setState(jobStatus.getJobState())
                    .setJobIdentity(identifier)
                    .build();
            MessageContext msgCtx = new MessageContext(
                    jobStatusChangeEvent, MessageType.JOB, AiravataUtils.getId(MessageType.JOB.name()), gateway);
            msgCtx.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            getStatusPublisher().publish(msgCtx);

        } catch (Exception e) {
            logger.error("Error persisting job status " + e.getLocalizedMessage(), e);
        }
    }

    public void saveExperimentOutput(String outputName, String outputVal) throws TaskOnFailException {
        try {
            ExperimentModel experiment = getRegistryServiceClient().getExperiment(experimentId);
            List<OutputDataObjectType> experimentOutputs = experiment.getExperimentOutputsList();
            if (!experimentOutputs.isEmpty()) {
                for (OutputDataObjectType expOutput : experimentOutputs) {
                    if (expOutput.getName().equals(outputName)) {
                        String productUri = saveDataProduct(outputName, outputVal, expOutput.getMetaData());
                        OutputDataObjectType updatedOutput =
                                expOutput.toBuilder().setValue(productUri).build();

                        if (!skipExperimentStatusPublish) {
                            getRegistryServiceClient()
                                    .addExperimentProcessOutputs(
                                            "EXPERIMENT_OUTPUT",
                                            Collections.singletonList(updatedOutput),
                                            experimentId);
                        }

                        if (!skipProcessStatusPublish) {
                            getRegistryServiceClient()
                                    .addExperimentProcessOutputs(
                                            "PROCESS_OUTPUT", Collections.singletonList(updatedOutput), processId);
                        }
                    }
                }
            }

        } catch (Exception e) {
            String msg = "expId: " + getExperimentId() + " processId: " + getProcessId()
                    + " : - Error while updating experiment outputs";
            throw new TaskOnFailException(msg, true, e);
        }
    }

    public void saveExperimentOutputCollection(String outputName, List<String> outputVals) throws TaskOnFailException {
        try {
            ExperimentModel experiment = getRegistryServiceClient().getExperiment(experimentId);
            List<OutputDataObjectType> experimentOutputs = experiment.getExperimentOutputsList();
            if (!experimentOutputs.isEmpty()) {
                for (OutputDataObjectType expOutput : experimentOutputs) {
                    if (expOutput.getName().equals(outputName)) {
                        List<String> productUris = new ArrayList<String>();
                        for (String outputVal : outputVals) {
                            String productUri = saveDataProduct(outputName, outputVal, expOutput.getMetaData());
                            productUris.add(productUri);
                        }
                        OutputDataObjectType updatedOutput = expOutput.toBuilder()
                                .setValue(String.join(",", productUris))
                                .build();
                        if (!skipExperimentStatusPublish) {
                            getRegistryServiceClient()
                                    .addExperimentProcessOutputs(
                                            "EXPERIMENT_OUTPUT",
                                            Collections.singletonList(updatedOutput),
                                            experimentId);
                        }

                        if (!skipProcessStatusPublish) {
                            getRegistryServiceClient()
                                    .addExperimentProcessOutputs(
                                            "PROCESS_OUTPUT", Collections.singletonList(updatedOutput), processId);
                        }
                    }
                }
            }

        } catch (Exception e) {
            String msg = "expId: " + getExperimentId() + " processId: " + getProcessId()
                    + " : - Error while updating experiment outputs";
            throw new TaskOnFailException(msg, true, e);
        }
    }

    private String saveDataProduct(String outputName, String outputVal, String outputMetadata) throws Exception {

        DataProductModel.Builder dataProductBuilder = DataProductModel.newBuilder()
                .setGatewayId(getGatewayId())
                .setOwnerName(getProcessModel().getUserName())
                .setProductName(outputName)
                .setDataProductType(DataProductType.FILE);
        // Copy experiment output's file-metadata to data product's metadata
        if (outputMetadata != null) {
            try {
                JsonNode outputMetadataJSON = new ObjectMapper().readTree(outputMetadata);
                JsonNode fileMetadata = outputMetadataJSON.get("file-metadata");
                if (fileMetadata != null && fileMetadata.isObject()) {
                    fileMetadata
                            .fields()
                            .forEachRemaining(entry -> dataProductBuilder.putProductMetadata(
                                    entry.getKey(), entry.getValue().asText()));
                }
            } catch (Exception e) {
                logger.warn("Failed to parse output metadata: [" + outputMetadata + "]", e);
            }
        }

        DataReplicaLocationModel replicaLocationModel = DataReplicaLocationModel.newBuilder()
                .setStorageResourceId(
                        getTaskContext().getStorageResourceDescription().getStorageResourceId())
                .setReplicaName(outputName + " gateway data store copy")
                .setReplicaLocationCategory(ReplicaLocationCategory.GATEWAY_DATA_STORE)
                .setReplicaPersistentType(ReplicaPersistentType.TRANSIENT)
                .setFilePath(outputVal)
                .build();
        dataProductBuilder.addReplicaLocations(replicaLocationModel);

        return getRegistryServiceClient().registerDataProduct(dataProductBuilder.build());
    }

    @SuppressWarnings("WeakerAccess")
    private void saveExperimentError(ErrorModel errorModel) {
        try {
            errorModel = errorModel.toBuilder()
                    .setErrorId(AiravataUtils.getId("EXP_ERROR"))
                    .build();
            getRegistryServiceClient().addErrors("EXPERIMENT_ERROR", errorModel, experimentId);
        } catch (Exception e) {
            String msg = "expId: " + getExperimentId() + " processId: " + getProcessId()
                    + " : - Error while updating experiment errors";
            logger.error(msg, e);
        }
    }

    @SuppressWarnings("WeakerAccess")
    private void saveProcessError(ErrorModel errorModel) {
        try {
            errorModel = errorModel.toBuilder()
                    .setErrorId(AiravataUtils.getId("PROCESS_ERROR"))
                    .build();
            getRegistryServiceClient().addErrors("PROCESS_ERROR", errorModel, getProcessId());
        } catch (Exception e) {
            logger.error(
                    "expId: " + getExperimentId() + " processId: " + getProcessId()
                            + " : - Error while updating process errors",
                    e);
        }
    }

    @SuppressWarnings("WeakerAccess")
    private void saveTaskError(ErrorModel errorModel) {
        try {
            errorModel = errorModel.toBuilder()
                    .setErrorId(AiravataUtils.getId("TASK_ERROR"))
                    .build();
            getRegistryServiceClient().addErrors("TASK_ERROR", errorModel, getTaskId());
        } catch (Exception e) {
            logger.error(
                    "expId: " + getExperimentId() + " processId: " + getProcessId() + " taskId: " + getTaskId()
                            + " : - Error while updating task errors",
                    e);
        }
    }

    protected Publisher getStatusPublisher() throws AiravataException {
        if (statusPublisher == null) {
            synchronized (AiravataTask.class) {
                if (statusPublisher == null) {
                    statusPublisher = MessagingFactory.getPublisher(Type.STATUS);
                }
            }
        }
        return statusPublisher;
    }

    @Override
    public TaskResult onRun(TaskHelper helper) {

        try {
            MDC.put("experiment", getExperimentId());
            MDC.put("process", getProcessId());
            MDC.put("gateway", getGatewayId());
            MDC.put("task", getTaskId());
            loadContext();
            if (!forceRunTask) {
                if (this.taskContext != null) {
                    TaskState taskState = taskContext.getTaskState();
                    if (taskState != null && taskState != TaskState.TASK_STATE_CREATED) {
                        logger.warn("Task " + getTaskId() + " is not in CREATED state. So skipping execution");
                        skipAllStatusPublish = false;
                        return onSuccess("Task " + getTaskId() + " is not in CREATED state. So skipping execution");
                    }
                }
            }
            if (!skipAllStatusPublish) {
                publishTaskState(TaskState.TASK_STATE_EXECUTING);
            }
            return onRun(helper, getTaskContext());
        } catch (TaskOnFailException e) {
            return onFail("Captured a task fail : " + e.getReason(), e.isCritical(), e);
        } catch (Exception e) {
            return onFail("Unknown error while running task " + getTaskId(), false, e);
        } finally {
            MDC.clear();
        }
    }

    public abstract TaskResult onRun(TaskHelper helper, TaskContext taskContext);

    @Override
    public void onCancel() {
        try {
            MDC.put("experiment", getExperimentId());
            MDC.put("process", getProcessId());
            MDC.put("gateway", getGatewayId());
            MDC.put("task", getTaskId());
            if (!skipAllStatusPublish) {
                publishTaskState(TaskState.TASK_STATE_CANCELED);
            }

            try {
                logger.info("Deleting task specific monitoring nodes");
                MonitoringUtil.deleteTaskSpecificNodes(getCuratorClient(), getTaskId());
            } catch (Exception e) {
                logger.error("Failed to delete task specific nodes but continuing", e);
            }

            onCancel(getTaskContext());
        } finally {
            MDC.clear();
        }
    }

    public abstract void onCancel(TaskContext taskContext);

    @Override
    public void init(HelixManager manager, String workflowName, String jobName, String taskName) {

        try {
            super.init(manager, workflowName, jobName, taskName);
            MDC.put("experiment", getExperimentId());
            MDC.put("process", getProcessId());
            MDC.put("gateway", getGatewayId());
            MDC.put("task", getTaskId());
            this.taskName = taskName;
        } finally {
            MDC.clear();
        }
    }

    protected void loadContext() throws TaskOnFailException {
        try {
            logger.info("Loading context for task " + getTaskId());
            processModel = getRegistryServiceClient().getProcess(processId);
            experimentModel = getRegistryServiceClient().getExperiment(experimentId);

            this.computeResourceDescription =
                    getRegistryServiceClient().getComputeResource(this.processModel.getComputeResourceId());

            TaskContext.TaskContextBuilder taskContextBuilder = new TaskContext.TaskContextBuilder(
                            getProcessId(), getGatewayId(), getTaskId())
                    .setRegistryClient(getRegistryServiceClient())
                    .setUserProfileProvider(getUserProfileProvider())
                    .setExperimentModel(getExperimentModel())
                    .setProcessModel(getProcessModel());

            this.taskContext = taskContextBuilder.build();
            logger.info("Task " + this.taskName + " initialized");

        } catch (Exception e) {
            logger.error(
                    "Error occurred while initializing the task " + getTaskId() + " of experiment " + getExperimentId(),
                    e);
            throw new TaskOnFailException(
                    "Error occurred while initializing the task " + getTaskId() + " of experiment " + getExperimentId(),
                    false,
                    e);
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected void publishTaskState(TaskState ts) {

        try {
            TaskStatus taskStatus = TaskStatus.newBuilder()
                    .setState(ts)
                    .setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime())
                    .build();
            getRegistryServiceClient().addTaskStatus(taskStatus, getTaskId());
            TaskIdentifier identifier = TaskIdentifier.newBuilder()
                    .setTaskId(getTaskId())
                    .setProcessId(getProcessId())
                    .setExperimentId(getExperimentId())
                    .setGatewayId(getGatewayId())
                    .build();
            TaskStatusChangeEvent taskStatusChangeEvent = TaskStatusChangeEvent.newBuilder()
                    .setState(ts)
                    .setTaskIdentity(identifier)
                    .build();
            MessageContext msgCtx = new MessageContext(
                    taskStatusChangeEvent,
                    MessageType.TASK,
                    AiravataUtils.getId(MessageType.TASK.name()),
                    getGatewayId());
            msgCtx.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            statusPublisher.publish(msgCtx);
        } catch (Exception e) {
            logger.error(
                    "Failed to publish task status " + (ts != null ? ts.name() : "null") + " of task " + getTaskId());
        }
    }

    protected ComputeResourceDescription getComputeResourceDescription() {
        return computeResourceDescription;
    }

    protected TaskContext getTaskContext() {
        return taskContext;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    protected String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    protected String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    protected ProcessModel getProcessModel() {
        return processModel;
    }

    protected ExperimentModel getExperimentModel() {
        return experimentModel;
    }

    public void setSkipAllStatusPublish(boolean skipAllStatusPublish) {
        this.skipAllStatusPublish = skipAllStatusPublish;
    }

    public boolean isSkipAllStatusPublish() {
        return skipAllStatusPublish;
    }

    public boolean isSkipProcessStatusPublish() {
        return skipProcessStatusPublish;
    }

    public void setSkipProcessStatusPublish(boolean skipProcessStatusPublish) {
        this.skipProcessStatusPublish = skipProcessStatusPublish;
    }

    public boolean isSkipExperimentStatusPublish() {
        return skipExperimentStatusPublish;
    }

    public void setSkipExperimentStatusPublish(boolean skipExperimentStatusPublish) {
        this.skipExperimentStatusPublish = skipExperimentStatusPublish;
    }

    public boolean isForceRunTask() {
        return forceRunTask;
    }

    public void setForceRunTask(boolean forceRunTask) {
        this.forceRunTask = forceRunTask;
    }

    public boolean isAutoSchedule() {
        return autoSchedule;
    }

    public void setAutoSchedule(boolean autoSchedule) {
        this.autoSchedule = autoSchedule;
    }

    public static RegistryHandler getRegistryServiceClient() {
        return SchedulerUtils.getRegistryHandler();
    }

    private static UserProfileProvider userProfileProvider;

    public static synchronized void setUserProfileProvider(UserProfileProvider provider) {
        userProfileProvider = provider;
    }

    public static synchronized UserProfileProvider getUserProfileProvider() {
        if (userProfileProvider == null) {
            throw new IllegalStateException("UserProfileProvider has not been initialized");
        }
        return userProfileProvider;
    }
}
