package org.apache.airavata.apis.workflow.task.data;

import org.apache.airavata.apis.workflow.task.common.BaseTask;
import org.apache.airavata.apis.workflow.task.common.annotation.TaskDef;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TaskDef(name = "DataMovementTask")
public class DataMovementTask extends BaseTask {

    private final static Logger logger = LoggerFactory.getLogger(DataMovementTask.class);

    @Override
    public TaskResult onRun() throws Exception {
        logger.info("Starting Data Movement task {}", getTaskId());

        return new TaskResult(TaskResult.Status.COMPLETED, "Completed");
    }

    @Override
    public void onCancel() throws Exception {

    }
}
