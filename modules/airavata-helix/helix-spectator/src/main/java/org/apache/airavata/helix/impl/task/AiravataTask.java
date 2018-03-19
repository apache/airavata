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
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.core.OutPort;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskOutPort;
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
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.*;
import org.apache.helix.HelixManager;
import org.apache.helix.task.TaskResult;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public abstract class AiravataTask extends AbstractTask {

    private static final Logger logger = LogManager.getLogger(AiravataTask.class);

    private AppCatalog appCatalog;
    private ExperimentCatalog experimentCatalog;
    private Publisher statusPublisher;
    private ProcessModel processModel;
    private ComputeResourceDescription computeResourceDescription;

    private TaskContext taskContext;

    @TaskParam(name = "Process Id")
    private String processId;

    @TaskParam(name = "experimentId")
    private String experimentId;

    @TaskParam(name = "gatewayId")
    private String gatewayId;

    @TaskParam(name = "Skip Status Publish")
    private boolean skipTaskStatusPublish = false;

    @TaskOutPort(name = "Next Task")
    private OutPort nextTask;

    protected TaskResult onSuccess(String message) {
        if (!skipTaskStatusPublish) {
            publishTaskState(TaskState.COMPLETED);
        }
        String successMessage = "Task " + getTaskId() + " completed." + (message != null ? " Message : " + message : "");
        logger.info(successMessage);
        return nextTask.invoke(new TaskResult(TaskResult.Status.COMPLETED, message));
    }

    protected TaskResult onFail(String reason, boolean fatal, Throwable error) {

        String errorMessage;
        ProcessStatus status = new ProcessStatus(ProcessState.FAILED);
        StringWriter errors = new StringWriter();

        if (error == null) {
            errorMessage = "Task " + getTaskId() + " failed due to " + reason;
            errors.write(errorMessage);
            status.setReason(errorMessage);
            logger.error(errorMessage);

        } else {
            errorMessage = "Task " + getTaskId() + " failed due to " + reason + ", " + error.getMessage();
            status.setReason(errorMessage);
            error.printStackTrace(new PrintWriter(errors));
            logger.error(errorMessage, error);
        }
        status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        getTaskContext().setProcessStatus(status);

        ErrorModel errorModel = new ErrorModel();
        errorModel.setUserFriendlyMessage(reason);
        errorModel.setActualErrorMessage(errors.toString());
        errorModel.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());

        if (!skipTaskStatusPublish) {
            publishTaskState(TaskState.FAILED);
            saveAndPublishProcessStatus();
            saveExperimentError(errorModel);
            saveProcessError(errorModel);
            saveTaskError(errorModel);
        }
        return new TaskResult(fatal ? TaskResult.Status.FATAL_FAILED : TaskResult.Status.FAILED, errorMessage);
    }

    protected void saveAndPublishProcessStatus(ProcessState state) {
        ProcessStatus processStatus = new ProcessStatus(state);
        processStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        getTaskContext().setProcessStatus(processStatus);
        saveAndPublishProcessStatus();
    }

    @SuppressWarnings("WeakerAccess")
    protected void saveAndPublishProcessStatus() {
        try {
            ProcessStatus status = taskContext.getProcessStatus();
            if (status.getTimeOfStateChange() == 0 || status.getTimeOfStateChange() > 0 ){
                status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            }else {
                status.setTimeOfStateChange(status.getTimeOfStateChange());
            }
            experimentCatalog.add(ExpCatChildDataType.PROCESS_STATUS, status, getProcessId());
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
            experimentCatalog.add(ExpCatChildDataType.TASK_STATUS, status, getTaskId());
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
            ExperimentModel experiment = (ExperimentModel)experimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT, experimentId);
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

                        ReplicaCatalog replicaCatalog = RegistryFactory.getReplicaCatalog();
                        String productUri = replicaCatalog.registerDataProduct(dataProductModel);
                        expOutput.setValue(productUri);
                    }
                }
            }
            experimentCatalog.update(ExperimentCatalogModelType.EXPERIMENT, experiment, experimentId);

        } catch (RegistryException | AppCatalogException e) {
            String msg = "expId: " + getExperimentId() + " processId: " + getProcessId() + " : - Error while updating experiment outputs";
            throw new TaskOnFailException(msg, true, e);
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected void saveExperimentError(ErrorModel errorModel) {
        try {
            errorModel.setErrorId(AiravataUtils.getId("EXP_ERROR"));
            getExperimentCatalog().add(ExpCatChildDataType.EXPERIMENT_ERROR, errorModel, experimentId);
        } catch (RegistryException e) {
            String msg = "expId: " + getExperimentId() + " processId: " + getProcessId() + " : - Error while updating experiment errors";
            logger.error(msg, e);
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected void saveProcessError(ErrorModel errorModel) {
        try {
            errorModel.setErrorId(AiravataUtils.getId("PROCESS_ERROR"));
            experimentCatalog.add(ExpCatChildDataType.PROCESS_ERROR, errorModel, getProcessId());
        } catch (RegistryException e) {
            String msg = "expId: " + getExperimentId() + " processId: " + getProcessId()
                    + " : - Error while updating process errors";
            logger.error(msg, e);
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected void saveTaskError(ErrorModel errorModel) {
        try {
            errorModel.setErrorId(AiravataUtils.getId("TASK_ERROR"));
            getExperimentCatalog().add(ExpCatChildDataType.TASK_ERROR, errorModel, getTaskId());
        } catch (RegistryException e) {
            String msg = "expId: " + getExperimentId() + " processId: " + getProcessId() + " taskId: " + getTaskId()
                    + " : - Error while updating task errors";
            logger.error(msg, e);
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
            if (!skipTaskStatusPublish) {
                publishTaskState(TaskState.EXECUTING);
            }
            return onRun(helper, getTaskContext());
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
        super.init(manager, workflowName, jobName, taskName);
        MDC.put("experiment", getExperimentId());
        MDC.put("process", getProcessId());
        MDC.put("gateway", getGatewayId());
        MDC.put("task", getTaskId());
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            //logger.info("Gateway id is " + getGatewayId());
            experimentCatalog = RegistryFactory.getExperimentCatalog(getGatewayId());
            processModel = (ProcessModel) experimentCatalog.get(ExperimentCatalogModelType.PROCESS, processId);

            this.computeResourceDescription = getAppCatalog().getComputeResource().getComputeResource(getProcessModel()
                    .getComputeResourceId());

            TaskContext.TaskContextBuilder taskContextBuilder = new TaskContext.TaskContextBuilder(getProcessId(), getGatewayId(), getTaskId())
                    .setAppCatalog(getAppCatalog())
                    .setExperimentCatalog(getExperimentCatalog())
                    .setProcessModel(getProcessModel())
                    .setStatusPublisher(getStatusPublisher())
                    .setGatewayResourceProfile(appCatalog.getGatewayProfile().getGatewayProfile(gatewayId))
                    .setGatewayComputeResourcePreference(
                            appCatalog.getGatewayProfile()
                                    .getComputeResourcePreference(gatewayId, processModel.getComputeResourceId()))
                    .setGatewayStorageResourcePreference(
                            appCatalog.getGatewayProfile()
                                    .getStoragePreference(gatewayId, processModel.getStorageResourceId()));

            this.taskContext = taskContextBuilder.build();
            logger.info("Task " + taskName + " initialized");
        } catch (Exception e) {
            logger.error("Error occurred while initializing the task " + getTaskId() + " of experiment " + getExperimentId(), e);
           throw new RuntimeException("Error occurred while initializing the task " + getTaskId() + " of experiment " + getExperimentId(), e);
        } finally {
            MDC.clear();
        }
    }

    protected AppCatalog getAppCatalog() {
        return appCatalog;
    }

    @SuppressWarnings("WeakerAccess")
    protected void publishTaskState(TaskState ts) {

        try {
            TaskStatus taskStatus = new TaskStatus();
            taskStatus.setState(ts);
            taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            experimentCatalog.add(ExpCatChildDataType.TASK_STATUS, taskStatus, getTaskId());
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

    protected ExperimentCatalog getExperimentCatalog() {
        return experimentCatalog;
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

    public void setNextTask(OutPort nextTask) {
        this.nextTask = nextTask;
    }

    public void setSkipTaskStatusPublish(boolean skipTaskStatusPublish) {
        this.skipTaskStatusPublish = skipTaskStatusPublish;
    }

    public boolean isSkipTaskStatusPublish() {
        return skipTaskStatusPublish;
    }
}
