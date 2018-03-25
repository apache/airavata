package org.apache.airavata.helix.impl.task.cancel;

import org.apache.airavata.helix.impl.task.AiravataTask;
import org.apache.airavata.helix.impl.task.TaskContext;
import org.apache.airavata.helix.impl.task.completing.CompletingTask;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.airavata.model.status.ProcessState;
import org.apache.helix.task.TaskResult;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

@TaskDef(name = "Cancel Completing Task")
public class CancelCompletingTask extends AiravataTask {
    private static final Logger logger = LogManager.getLogger(CancelCompletingTask.class);

    @Override
    public TaskResult onRun(TaskHelper helper, TaskContext taskContext) {
        logger.info("Starting cancel completing task for task " + getTaskId() + ", experiment id " + getExperimentId());
        logger.info("Process " + getProcessId() + " successfully cancelled");
        saveAndPublishProcessStatus(ProcessState.CANCELED);
        return onSuccess("Process " + getProcessId() + " successfully cancelled");
    }

    @Override
    public void onCancel(TaskContext taskContext) {

    }
}
