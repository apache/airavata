package org.apache.airavata.apis.workflow.task.ec2;

import org.apache.airavata.api.execution.stubs.EC2Backend;
import org.apache.airavata.apis.workflow.task.common.BaseTask;
import org.apache.airavata.apis.workflow.task.common.annotation.TaskDef;
import org.apache.airavata.apis.workflow.task.common.annotation.TaskParam;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TaskDef(name = "CreateEC2InstanceTask")
public class CreateEC2InstanceTask extends BaseTask {

    private final static Logger logger = LoggerFactory.getLogger(CreateEC2InstanceTask.class);

    @TaskParam(name = "ec2Backend")
    private ThreadLocal<EC2Backend> ec2Backend = new ThreadLocal<>();

    @Override
    public TaskResult onRun() throws Exception {
        logger.info("Starting Create EC2 Instance Task {}", getTaskId());
        logger.info("EC2 Backend {}", getEc2Backend().toString());
        return new TaskResult(TaskResult.Status.COMPLETED, "Completed");
    }

    @Override
    public void onCancel() throws Exception {

    }

    public EC2Backend getEc2Backend() {
        return ec2Backend.get();
    }

    public void setEc2Backend(EC2Backend ec2Backend) {
        this.ec2Backend.set(ec2Backend);
    }
}
