package org.apache.airavata.helix.impl.task;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.core.OutPort;
import org.apache.airavata.helix.task.api.annotation.TaskOutPort;
import org.apache.airavata.helix.task.api.annotation.TaskParam;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessagingFactory;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.messaging.core.Type;
import org.apache.airavata.messaging.core.impl.RabbitMQPublisher;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.messaging.event.*;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.status.*;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.*;
import org.apache.helix.HelixManager;
import org.apache.helix.task.TaskResult;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;

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

    @TaskOutPort(name = "Next Task")
    private OutPort nextTask;

    protected TaskResult onSuccess(String message) {
        String successMessage = "Task " + getTaskId() + " completed." + message != null ? " Message : " + message : "";
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
        errorModel.setUserFriendlyMessage("GFac Worker throws an exception");
        errorModel.setActualErrorMessage(errors.toString());
        errorModel.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());

        saveAndPublishProcessStatus();
        saveExperimentError(errorModel);
        saveProcessError(errorModel);
        return new TaskResult(fatal ? TaskResult.Status.FATAL_FAILED : TaskResult.Status.FAILED, errorMessage);
    }

    public void saveAndPublishProcessStatus(ProcessState state) {
        ProcessStatus processStatus = new ProcessStatus(state);
        processStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        getTaskContext().setProcessStatus(processStatus);
        saveAndPublishProcessStatus();
    }

    public void saveAndPublishProcessStatus() {
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

    public void saveAndPublishTaskStatus() {
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

    public void saveExperimentError(ErrorModel errorModel) {
        try {
            errorModel.setErrorId(AiravataUtils.getId("EXP_ERROR"));
            getExperimentCatalog().add(ExpCatChildDataType.EXPERIMENT_ERROR, errorModel, experimentId);
        } catch (RegistryException e) {
            String msg = "expId: " + getExperimentId() + " processId: " + getProcessId() + " : - Error while updating experiment errors";
            logger.error(msg, e);
        }
    }

    public void saveProcessError(ErrorModel errorModel) {
        try {
            errorModel.setErrorId(AiravataUtils.getId("PROCESS_ERROR"));
            experimentCatalog.add(ExpCatChildDataType.PROCESS_ERROR, errorModel, getProcessId());
        } catch (RegistryException e) {
            String msg = "expId: " + getExperimentId() + " processId: " + getProcessId()
                    + " : - Error while updating process errors";
            logger.error(msg, e);
        }
    }

    public void saveTaskError(ErrorModel errorModel) throws Exception {
        try {
            errorModel.setErrorId(AiravataUtils.getId("TASK_ERROR"));
            getExperimentCatalog().add(ExpCatChildDataType.TASK_ERROR, errorModel, getTaskId());
        } catch (RegistryException e) {
            String msg = "expId: " + getExperimentId() + " processId: " + getProcessId() + " taskId: " + getTaskId()
                    + " : - Error while updating task errors";
            throw new Exception(msg, e);
        }
    }

    public Publisher getStatusPublisher() throws AiravataException {
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
    public void init(HelixManager manager, String workflowName, String jobName, String taskName) {
        super.init(manager, workflowName, jobName, taskName);
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            experimentCatalog = RegistryFactory.getExperimentCatalog(getGatewayId());
            processModel = (ProcessModel) experimentCatalog.get(ExperimentCatalogModelType.PROCESS, processId);

            this.computeResourceDescription = getAppCatalog().getComputeResource().getComputeResource(getProcessModel()
                    .getComputeResourceId());

            TaskContext.TaskContextBuilder taskContextBuilder = new TaskContext.TaskContextBuilder(getProcessId(), getGatewayId(), getTaskId());
            taskContextBuilder.setAppCatalog(getAppCatalog());
            taskContextBuilder.setExperimentCatalog(getExperimentCatalog());
            taskContextBuilder.setProcessModel(getProcessModel());
            taskContextBuilder.setStatusPublisher(getStatusPublisher());

            taskContextBuilder.setGatewayResourceProfile(appCatalog.getGatewayProfile().getGatewayProfile(gatewayId));
            taskContextBuilder.setGatewayComputeResourcePreference(
                            appCatalog.getGatewayProfile()
                                    .getComputeResourcePreference(gatewayId, processModel.getComputeResourceId()));
            taskContextBuilder.setGatewayStorageResourcePreference(
                            appCatalog.getGatewayProfile()
                                    .getStoragePreference(gatewayId, processModel.getStorageResourceId()));

            this.taskContext = taskContextBuilder.build();
        } catch (AppCatalogException e) {
            e.printStackTrace();
        } catch (RegistryException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected AppCatalog getAppCatalog() {
        return appCatalog;
    }

    protected void publishTaskState(TaskState ts) throws RegistryException {

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
    }

    //////////////////////////

    public ComputeResourceDescription getComputeResourceDescription() {
        return computeResourceDescription;
    }

    ////////////////////////


    public TaskContext getTaskContext() {
        return taskContext;
    }

    public ExperimentCatalog getExperimentCatalog() {
        return experimentCatalog;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public ProcessModel getProcessModel() {
        return processModel;
    }

    public void setNextTask(OutPort nextTask) {
        this.nextTask = nextTask;
    }
}
