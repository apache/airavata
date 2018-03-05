package org.apache.airavata.helix.impl.task;

import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.airavata.model.status.ProcessState;
import org.apache.helix.task.TaskResult;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

@TaskDef(name = "Completing Task")
public class CompletingTask extends AiravataTask {

    private static final Logger logger = LogManager.getLogger(CompletingTask.class);

    @Override
    public TaskResult onRun(TaskHelper helper) {
        logger.info("Process " + getProcessId() + " successfully completed");
        saveAndPublishProcessStatus(ProcessState.COMPLETED);
        return onSuccess("Process " + getProcessId() + " successfully completed");
    }

    @Override
    public void onCancel() {

    }
}
