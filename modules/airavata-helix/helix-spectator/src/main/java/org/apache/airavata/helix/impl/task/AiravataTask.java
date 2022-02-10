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

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.core.util.MonitoringUtil;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskParam;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessagingFactory;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.messaging.core.Type;
import org.apache.airavata.messaging.core.impl.RabbitMQPublisher;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.data.replica.*;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.messaging.event.*;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.status.*;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.client.RegistryServiceClientFactory;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.service.profile.client.ProfileServiceClientFactory;
import org.apache.airavata.service.profile.user.cpi.UserProfileService;
import org.apache.airavata.service.profile.user.cpi.exception.UserProfileServiceException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.helix.HelixManager;
import org.apache.helix.task.TaskResult;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public abstract class AiravataTask extends AbstractTask {

    private final static Logger logger = LoggerFactory.getLogger(AiravataTask.class);

    private static Publisher statusPublisher;

    private ProcessModel processModel;
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

    @TaskParam(name ="Force Run Task")
    private boolean forceRunTask = false;

    protected TaskResult onSuccess(String message) {
        logger.info(message);
        if (!skipAllStatusPublish) {
            publishTaskState(TaskState.COMPLETED);
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

        logger.warn("Task failed with fatal = " + fatal + ".  Current retry count " + currentRetryCount + " total retry count " + getRetryCount());

        if (currentRetryCount < getRetryCount() && !fatal) {
            try {
                markNewRetry(currentRetryCount);
            } catch (Exception e) {
                logger.error("Failed to mark retry. So failing the task permanently", e);
                fatal = true;
            }
        }

        if (currentRetryCount >= getRetryCount() || fatal) {
            ProcessStatus status = new ProcessStatus(ProcessState.FAILED);
            StringWriter errors = new StringWriter();

            String errorCode = UUID.randomUUID().toString();
            String errorMessage = "Error Code : " + errorCode + ", Task " + getTaskId() + " failed due to " + reason +
                    (error == null ? "" : ", " + error.getMessage());

            // wrapping from new error object with error code
            error = new TaskOnFailException(errorMessage, true, error);

            status.setReason(errorMessage);
            errors.write(ExceptionUtils.getStackTrace(error));
            logger.error(errorMessage, error);

            status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            if (getTaskContext() != null) { // task context could be null if the initialization failed
                getTaskContext().setProcessStatus(status);
            } else {
                logger.warn("Task context is null. So can not store the process status in the context");
            }

            ErrorModel errorModel = new ErrorModel();
            errorModel.setUserFriendlyMessage(reason);
            errorModel.setActualErrorMessage(errors.toString());
            errorModel.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());

            if (!skipAllStatusPublish) {
                publishTaskState(TaskState.FAILED);
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
            ProcessStatus processStatus = new ProcessStatus(state);
            processStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
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
                    status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                } else {
                    status.setTimeOfStateChange(status.getTimeOfStateChange());
                }
                getRegistryServiceClient().addProcessStatus(status, getProcessId());
                ProcessIdentifier identifier = new ProcessIdentifier(getProcessId(), getExperimentId(), getGatewayId());
                ProcessStatusChangeEvent processStatusChangeEvent = new ProcessStatusChangeEvent(status.getState(), identifier);
                MessageContext msgCtx = new MessageContext(processStatusChangeEvent, MessageType.PROCESS,
                        AiravataUtils.getId(MessageType.PROCESS.name()), getGatewayId());
                msgCtx.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
                getStatusPublisher().publish(msgCtx);
            }
        } catch (Exception e) {
            logger.error("Failed to save process status of process " + getProcessId(), e);
        }
    }


    public void saveAndPublishJobStatus(String jobId, String taskId, String processId, String experimentId, String gateway,
                                         JobState jobState) throws Exception {
        try {

            JobStatus jobStatus = new JobStatus();
            jobStatus.setReason(jobState.name());
            jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            jobStatus.setJobState(jobState);

            if (jobStatus.getTimeOfStateChange() == 0 || jobStatus.getTimeOfStateChange() > 0 ) {
                jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            } else {
                jobStatus.setTimeOfStateChange(jobStatus.getTimeOfStateChange());
            }

            getRegistryServiceClient().addJobStatus(jobStatus, taskId, jobId);

            JobIdentifier identifier = new JobIdentifier(jobId, taskId, processId, experimentId, gateway);

            JobStatusChangeEvent jobStatusChangeEvent = new JobStatusChangeEvent(jobStatus.getJobState(), identifier);
            MessageContext msgCtx = new MessageContext(jobStatusChangeEvent, MessageType.JOB, AiravataUtils.getId
                    (MessageType.JOB.name()), gateway);
            msgCtx.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            getStatusPublisher().publish(msgCtx);

        } catch (Exception e) {
            logger.error("Error persisting job status " + e.getLocalizedMessage(), e);
        }
    }

    public void saveExperimentOutput(String outputName, String outputVal) throws TaskOnFailException {
        try {
            ExperimentModel experiment = getRegistryServiceClient().getExperiment(experimentId);
            List<OutputDataObjectType> experimentOutputs = experiment.getExperimentOutputs();
            if (experimentOutputs != null && !experimentOutputs.isEmpty()) {
                for (OutputDataObjectType expOutput : experimentOutputs) {
                    if (expOutput.getName().equals(outputName)) {
                        String productUri = saveDataProduct(outputName, outputVal, expOutput.getMetaData());
                        expOutput.setValue(productUri);

                        if (!skipExperimentStatusPublish) {
                            getRegistryServiceClient().addExperimentProcessOutputs("EXPERIMENT_OUTPUT",
                                    Collections.singletonList(expOutput), experimentId);
                        }

                        if (!skipProcessStatusPublish) {
                            getRegistryServiceClient().addExperimentProcessOutputs("PROCESS_OUTPUT",
                                    Collections.singletonList(expOutput), processId);
                        }
                    }
                }
            }

        } catch (Exception e) {
            String msg = "expId: " + getExperimentId() + " processId: " + getProcessId() + " : - Error while updating experiment outputs";
            throw new TaskOnFailException(msg, true, e);
        }
    }
    public void saveExperimentOutputCollection(String outputName, List<String> outputVals) throws TaskOnFailException {
        try {
            ExperimentModel experiment = getRegistryServiceClient().getExperiment(experimentId);
            List<OutputDataObjectType> experimentOutputs = experiment.getExperimentOutputs();
            if (experimentOutputs != null && !experimentOutputs.isEmpty()) {
                for (OutputDataObjectType expOutput : experimentOutputs) {
                    if (expOutput.getName().equals(outputName)) {
                        List<String> productUris = new ArrayList<String>();
                        for (String outputVal : outputVals) {
                            String productUri = saveDataProduct(outputName, outputVal, expOutput.getMetaData());
                            productUris.add(productUri);
                        }
                        expOutput.setValue(String.join(",", productUris));
                        if (!skipExperimentStatusPublish) {
                            getRegistryServiceClient().addExperimentProcessOutputs("EXPERIMENT_OUTPUT",
                                    Collections.singletonList(expOutput), experimentId);
                        }

                        if (!skipProcessStatusPublish) {
                            getRegistryServiceClient().addExperimentProcessOutputs("PROCESS_OUTPUT",
                                    Collections.singletonList(expOutput), processId);
                        }
                    }
                }
            }

        } catch (Exception e) {
            String msg = "expId: " + getExperimentId() + " processId: " + getProcessId() + " : - Error while updating experiment outputs";
            throw new TaskOnFailException(msg, true, e);
        }
    }

    private String saveDataProduct(String outputName, String outputVal, String outputMetadata) throws Exception {

        DataProductModel dataProductModel = new DataProductModel();
        dataProductModel.setGatewayId(getGatewayId());
        dataProductModel.setOwnerName(getProcessModel().getUserName());
        dataProductModel.setProductName(outputName);
        dataProductModel.setDataProductType(DataProductType.FILE);
        // Copy experiment output's file-metadata to data product's metadata
        if (outputMetadata != null) {
            try {
                JSONObject outputMetadataJSON = new JSONObject(outputMetadata);
                if (outputMetadataJSON.has("file-metadata")) {
                    JSONObject fileMetadata = outputMetadataJSON.getJSONObject("file-metadata");
                    for (Object key : fileMetadata.keySet()) {
                        String k = key.toString();
                        dataProductModel.putToProductMetadata(k, fileMetadata.getString(k));
                    }
                }
            } catch (JSONException e) {
                logger.warn("Failed to parse output metadata: [" + outputMetadata + "]", e);
            }
        }

        DataReplicaLocationModel replicaLocationModel = new DataReplicaLocationModel();
        replicaLocationModel.setStorageResourceId(getTaskContext().getStorageResourceDescription().getStorageResourceId());
        replicaLocationModel.setReplicaName(outputName + " gateway data store copy");
        replicaLocationModel.setReplicaLocationCategory(ReplicaLocationCategory.GATEWAY_DATA_STORE);
        replicaLocationModel.setReplicaPersistentType(ReplicaPersistentType.TRANSIENT);
        replicaLocationModel.setFilePath(outputVal);
        dataProductModel.addToReplicaLocations(replicaLocationModel);

        return getRegistryServiceClient().registerDataProduct(dataProductModel);
    }

    @SuppressWarnings("WeakerAccess")
    private void saveExperimentError(ErrorModel errorModel) {
        try {
            errorModel.setErrorId(AiravataUtils.getId("EXP_ERROR"));
            getRegistryServiceClient().addErrors("EXPERIMENT_ERROR", errorModel, experimentId);
        } catch (Exception e) {
            String msg = "expId: " + getExperimentId() + " processId: " + getProcessId() + " : - Error while updating experiment errors";
            logger.error(msg, e);
        }
    }

    @SuppressWarnings("WeakerAccess")
    private void saveProcessError(ErrorModel errorModel) {
        try {
            errorModel.setErrorId(AiravataUtils.getId("PROCESS_ERROR"));
            getRegistryServiceClient().addErrors("PROCESS_ERROR", errorModel, getProcessId());
        } catch (Exception e) {
            logger.error("expId: " + getExperimentId() + " processId: " + getProcessId() + " : - Error while updating process errors", e);
        }
    }

    @SuppressWarnings("WeakerAccess")
    private void saveTaskError(ErrorModel errorModel) {
        try {
            errorModel.setErrorId(AiravataUtils.getId("TASK_ERROR"));
            getRegistryServiceClient().addErrors("TASK_ERROR", errorModel, getTaskId());
        } catch (Exception e) {
            logger.error("expId: " + getExperimentId() + " processId: " + getProcessId() + " taskId: " + getTaskId()
                    + " : - Error while updating task errors", e);
        }
    }

    protected Publisher getStatusPublisher() throws AiravataException {
        if (statusPublisher == null) {
            synchronized (RabbitMQPublisher.class) {
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
                    if (taskState != null && taskState != TaskState.CREATED) {
                        logger.warn("Task " + getTaskId() + " is not in CREATED state. So skipping execution");
                        skipAllStatusPublish = false;
                        return onSuccess("Task " + getTaskId() + " is not in CREATED state. So skipping execution");
                    }
                }
            }
            if (!skipAllStatusPublish) {
                publishTaskState(TaskState.EXECUTING);
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
                publishTaskState(TaskState.CANCELED);
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

    private void loadContext() throws TaskOnFailException {
        try {
            logger.info("Loading context for task " + getTaskId());
            processModel = getRegistryServiceClient().getProcess(processId);

            this.computeResourceDescription = getRegistryServiceClient().getComputeResource(this.processModel.getComputeResourceId());

            TaskContext.TaskContextBuilder taskContextBuilder = new TaskContext.TaskContextBuilder(getProcessId(), getGatewayId(), getTaskId())
                    .setRegistryClient(getRegistryServiceClient())
                    .setProfileClient(getUserProfileClient())
                    .setProcessModel(getProcessModel());

            this.taskContext = taskContextBuilder.build();
            logger.info("Task " + this.taskName + " initialized");

        } catch (Exception e) {
            logger.error("Error occurred while initializing the task " + getTaskId() + " of experiment " + getExperimentId(), e);
            throw new TaskOnFailException("Error occurred while initializing the task " + getTaskId() + " of experiment " + getExperimentId(), false, e);
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected void publishTaskState(TaskState ts) {

        try {
            TaskStatus taskStatus = new TaskStatus();
            taskStatus.setState(ts);
            taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            getRegistryServiceClient().addTaskStatus(taskStatus, getTaskId());
            TaskIdentifier identifier = new TaskIdentifier(getTaskId(),
                    getProcessId(), getExperimentId(), getGatewayId());
            TaskStatusChangeEvent taskStatusChangeEvent = new TaskStatusChangeEvent(ts,
                    identifier);
            MessageContext msgCtx = new MessageContext(taskStatusChangeEvent, MessageType.TASK, AiravataUtils.getId
                    (MessageType.TASK.name()), getGatewayId());
            msgCtx.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            statusPublisher.publish(msgCtx);
        } catch (Exception e) {
            logger.error("Failed to publish task status " + (ts != null ? ts.name(): "null") +" of task " + getTaskId());
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

    // TODO this is inefficient. Try to use a connection pool
    public static RegistryService.Client getRegistryServiceClient() {
        try {
            final int serverPort = Integer.parseInt(ServerSettings.getRegistryServerPort());
            final String serverHost = ServerSettings.getRegistryServerHost();
            return RegistryServiceClientFactory.createRegistryClient(serverHost, serverPort);
        } catch (RegistryServiceException|ApplicationSettingsException e) {
            throw new RuntimeException("Unable to create registry client...", e);
        }
    }

    public static UserProfileService.Client getUserProfileClient() {
        try {
            final int serverPort = Integer.parseInt(ServerSettings.getProfileServiceServerPort());
            final String serverHost = ServerSettings.getProfileServiceServerHost();
            return ProfileServiceClientFactory.createUserProfileServiceClient(serverHost, serverPort);
        } catch (UserProfileServiceException | ApplicationSettingsException e) {
            throw new RuntimeException("Unable to create profile service client...", e);
        }
    }
}
