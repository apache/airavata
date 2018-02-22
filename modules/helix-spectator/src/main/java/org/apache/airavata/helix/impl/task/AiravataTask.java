package org.apache.airavata.helix.impl.task;

import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.core.OutPort;
import org.apache.airavata.helix.task.api.annotation.TaskOutPort;
import org.apache.airavata.helix.task.api.annotation.TaskParam;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserResourceProfile;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.messaging.event.TaskIdentifier;
import org.apache.airavata.model.messaging.event.TaskStatusChangeEvent;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.model.status.TaskStatus;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.*;
import org.apache.helix.HelixManager;
import org.apache.helix.task.TaskResult;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.*;

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

    @TaskOutPort(name = "Success Port")
    private OutPort onSuccess;


    protected TaskResult onSuccess(String message) {
        String successMessage = "Task " + getTaskId() + " completed." + message != null ? " Message : " + message : "";
        logger.info(successMessage);
        return onSuccess.invoke(new TaskResult(TaskResult.Status.COMPLETED, message));
    }

    protected TaskResult onFail(String reason, boolean fatal, Throwable error) {
        String errorMessage;

        if (error == null) {
            errorMessage = "Task " + getTaskId() + " failed due to " + reason;
            logger.error(errorMessage);
        } else {
            errorMessage = "Task " + getTaskId() + " failed due to " + reason + ", " + error.getMessage();
            logger.error(errorMessage, error);
        }
        return new TaskResult(fatal ? TaskResult.Status.FATAL_FAILED : TaskResult.Status.FAILED, errorMessage);

    }

    @Override
    public void init(HelixManager manager, String workflowName, String jobName, String taskName) {
        super.init(manager, workflowName, jobName, taskName);
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
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

    public Publisher getStatusPublisher() {
        return statusPublisher;
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

}
