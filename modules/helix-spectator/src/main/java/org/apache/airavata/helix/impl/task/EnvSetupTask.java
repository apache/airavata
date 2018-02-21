package org.apache.airavata.helix.impl.task;

import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.helix.core.OutPort;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.airavata.helix.task.api.annotation.TaskOutPort;
import org.apache.airavata.helix.task.api.annotation.TaskParam;
import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.helix.task.TaskResult;

@TaskDef(name = "Environment Setup Task")
public class EnvSetupTask extends AiravataTask {

    @TaskParam(name = "Working Directory")
    private String workingDirectory;

    @TaskOutPort(name = "Success Out Port")
    private OutPort successPort;

    @Override
    public TaskResult onRun(TaskHelper taskHelper) {
        try {
            publishTaskState(TaskState.EXECUTING);
            AgentAdaptor adaptor = taskHelper.getAdaptorSupport().fetchAdaptor(getComputeResourceId(),
                    getJobSubmissionProtocol().name(), getComputeResourceCredentialToken());

            adaptor.createDirectory(workingDirectory);
            publishTaskState(TaskState.COMPLETED);
            return successPort.invoke(new TaskResult(TaskResult.Status.COMPLETED, "Successfully completed"));
        } catch (Exception e) {
            try {
                publishTaskState(TaskState.FAILED);
            } catch (RegistryException e1) {
                publishErrors(e1);
                // ignore silently
            }
            publishErrors(e);
            return new TaskResult(TaskResult.Status.FAILED, "Failed the task");
        }
    }

    @Override
    public void onCancel() {

    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public OutPort getSuccessPort() {
        return successPort;
    }

    public void setSuccessPort(OutPort successPort) {
        this.successPort = successPort;
    }
}
