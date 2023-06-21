package org.apache.airavata.apis.workflow.task.ec2;

import org.apache.airavata.apis.workflow.task.common.BaseTask;
import org.apache.airavata.apis.workflow.task.common.annotation.TaskDef;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TaskDef(name = "CreateEC2InstanceTask")
public class CreateEC2InstanceTask extends BaseTask {

    private final static Logger logger = LoggerFactory.getLogger(CreateEC2InstanceTask.class);

    @Override
    public TaskResult onRun() throws Exception {
        logger.info("Starting Create EC2 Instance Task {}", getTaskId());

        return new TaskResult(TaskResult.Status.COMPLETED, "Completed");
    }

    @Override
    public void onCancel() throws Exception {

    }
}
