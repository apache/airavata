package org.apache.airavata.helix.impl.task;

import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.helix.task.TaskResult;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

@TaskDef(name = "Environment Setup Task")
public class EnvSetupTask extends AiravataTask {

    private static final Logger logger = LogManager.getLogger(EnvSetupTask.class);

    @Override
    public TaskResult onRun(TaskHelper taskHelper) {
        try {
            publishTaskState(TaskState.EXECUTING);
            AgentAdaptor adaptor = taskHelper.getAdaptorSupport().fetchAdaptor(
                    getTaskContext().getGatewayId(),
                    getTaskContext().getComputeResourceId(),
                    getTaskContext().getJobSubmissionProtocol().name(),
                    getTaskContext().getComputeResourceCredentialToken(),
                    getTaskContext().getComputeResourceLoginUserName());

            logger.info("Creating directory " + getTaskContext().getWorkingDir() + " on compute resource " + getTaskContext().getComputeResourceId());
            adaptor.createDirectory(getTaskContext().getWorkingDir());
            publishTaskState(TaskState.COMPLETED);
            return onSuccess("Successfully completed");
        } catch (Exception e) {
            try {
                publishTaskState(TaskState.FAILED);
            } catch (RegistryException e1) {
                logger.error("Task failed to publish task status", e1);

                // ignore silently
            }
            return onFail("Failed to setup environment of task " + getTaskId(), true, e);
        }
    }

    @Override
    public void onCancel() {

    }

}
