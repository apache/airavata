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
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.helix.HelixManager;
import org.apache.helix.task.TaskResult;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.StringWriter;
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

    @TaskParam(name = "Skip Status Publish")
    private boolean skipTaskStatusPublish = false;

    protected TaskResult onSuccess(String message) {
        logger.info(message);
        if (!skipTaskStatusPublish) {
            publishTaskState(TaskState.COMPLETED);
        }
        return super.onSuccess(message);
    }

    protected TaskResult onFail(String reason, boolean fatal, Throwable error) {

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

        if (!skipTaskStatusPublish) {
            publishTaskState(TaskState.FAILED);
            saveAndPublishProcessStatus(taskContext != null ? taskContext.getProcessStatus() : status);
            saveExperimentError(errorModel);
            saveProcessError(errorModel);
            saveTaskError(errorModel);
        }

        return onFail(errorMessage, fatal);
    }

    protected void saveAndPublishProcessStatus(ProcessState state) {
        ProcessStatus processStatus = new ProcessStatus(state);
        processStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        if (getTaskContext() != null) {
            getTaskContext().setProcessStatus(processStatus);
        } else {
            logger.warn("Task context is null. So can not store the process status in the context");
        }
        saveAndPublishProcessStatus((taskContext != null ? taskContext.getProcessStatus() : processStatus));
    }

    @SuppressWarnings("WeakerAccess")
    protected void saveAndPublishProcessStatus(ProcessStatus status) {
        try {
            if (status.getTimeOfStateChange() == 0 || status.getTimeOfStateChange() > 0 ){
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
        } catch (Exception e) {
            logger.error("Failed to save process status of process " + getProcessId(), e);
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected void saveAndPublishTaskStatus() {
        try {
            TaskState state = getTaskContext().getTaskState();
            // first we save job jobModel to the registry for sa and then save the job status.
            TaskStatus status = getTaskContext().getTaskStatus();
            if (status.getTimeOfStateChange() == 0 || status.getTimeOfStateChange() > 0 ){
                status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            }else {
                status.setTimeOfStateChange(status.getTimeOfStateChange());
            }
            getRegistryServiceClient().addTaskStatus(status, getTaskId());
            TaskIdentifier identifier = new TaskIdentifier(getTaskId(), getProcessId(), getExperimentId(), getGatewayId());
            TaskStatusChangeEvent taskStatusChangeEvent = new TaskStatusChangeEvent(state,
                    identifier);
            MessageContext msgCtx = new MessageContext(taskStatusChangeEvent, MessageType.TASK, AiravataUtils.getId
                    (MessageType.TASK.name()), getGatewayId());
            msgCtx.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            getStatusPublisher().publish(msgCtx);
        } catch (Exception e) {
            logger.error("Failed to publist task status of task " + getTaskId());
        }
    }

    public void saveExperimentOutput(String outputName, String outputVal) throws TaskOnFailException {
        try {
            ExperimentModel experiment = getRegistryServiceClient().getExperiment(experimentId);
            List<OutputDataObjectType> experimentOutputs = experiment.getExperimentOutputs();
            if (experimentOutputs != null && !experimentOutputs.isEmpty()) {
                for (OutputDataObjectType expOutput : experimentOutputs) {
                    if (expOutput.getName().equals(outputName)) {
                        DataProductModel dataProductModel = new DataProductModel();
                        dataProductModel.setGatewayId(getGatewayId());
                        dataProductModel.setOwnerName(getProcessModel().getUserName());
                        dataProductModel.setProductName(outputName);
                        dataProductModel.setDataProductType(DataProductType.FILE);

                        DataReplicaLocationModel replicaLocationModel = new DataReplicaLocationModel();
                        replicaLocationModel.setStorageResourceId(getTaskContext().getStorageResource().getStorageResourceId());
                        replicaLocationModel.setReplicaName(outputName + " gateway data store copy");
                        replicaLocationModel.setReplicaLocationCategory(ReplicaLocationCategory.GATEWAY_DATA_STORE);
                        replicaLocationModel.setReplicaPersistentType(ReplicaPersistentType.TRANSIENT);
                        replicaLocationModel.setFilePath(outputVal);
                        dataProductModel.addToReplicaLocations(replicaLocationModel);

                        String productUri = getRegistryServiceClient().registerDataProduct(dataProductModel);
                        expOutput.setValue(productUri);
                        getRegistryServiceClient().addExperimentProcessOutputs("EXPERIMENT_OUTPUT",
                                Collections.singletonList(expOutput), experimentId);
                    }
                }
            }

        } catch (TException e) {
            String msg = "expId: " + getExperimentId() + " processId: " + getProcessId() + " : - Error while updating experiment outputs";
            throw new TaskOnFailException(msg, true, e);
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected void saveExperimentError(ErrorModel errorModel) {
        try {
            errorModel.setErrorId(AiravataUtils.getId("EXP_ERROR"));
            getRegistryServiceClient().addErrors("EXPERIMENT_ERROR", errorModel, experimentId);
        } catch (Exception e) {
            String msg = "expId: " + getExperimentId() + " processId: " + getProcessId() + " : - Error while updating experiment errors";
            logger.error(msg, e);
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected void saveProcessError(ErrorModel errorModel) {
        try {
            errorModel.setErrorId(AiravataUtils.getId("PROCESS_ERROR"));
            getRegistryServiceClient().addErrors("PROCESS_ERROR", errorModel, getProcessId());
        } catch (Exception e) {
            logger.error("expId: " + getExperimentId() + " processId: " + getProcessId() + " : - Error while updating process errors", e);
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected void saveTaskError(ErrorModel errorModel) {
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
            if (!skipTaskStatusPublish) {
                publishTaskState(TaskState.EXECUTING);
            }
            return onRun(helper, getTaskContext());
        } catch (Exception e) {
            return onFail("Unknown error while running task " + getTaskId(), true, e);
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
            if (!skipTaskStatusPublish) {
                publishTaskState(TaskState.CANCELED);
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
                    .setProcessModel(getProcessModel())
                    .setStatusPublisher(getStatusPublisher())
                    .setGatewayResourceProfile(getRegistryServiceClient().getGatewayResourceProfile(gatewayId))
                    .setGatewayComputeResourcePreference(
                            getRegistryServiceClient().getGatewayComputeResourcePreference(gatewayId,
                                    processModel.getComputeResourceId()))
                    .setGatewayStorageResourcePreference(
                            getRegistryServiceClient().getGatewayStoragePreference(gatewayId,
                                    processModel.getStorageResourceId()));

            this.taskContext = taskContextBuilder.build();
            logger.info("Task " + this.taskName + " initialized");

        } catch (Exception e) {
            logger.error("Error occurred while initializing the task " + getTaskId() + " of experiment " + getExperimentId(), e);
            throw new TaskOnFailException("Error occurred while initializing the task " + getTaskId() + " of experiment " + getExperimentId(), true, e);
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

    public void setSkipTaskStatusPublish(boolean skipTaskStatusPublish) {
        this.skipTaskStatusPublish = skipTaskStatusPublish;
    }

    public boolean isSkipTaskStatusPublish() {
        return skipTaskStatusPublish;
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
}
