package org.apache.airavata.helix.impl.task.cancel;

import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.airavata.helix.task.api.annotation.TaskParam;
import org.apache.helix.HelixManager;
import org.apache.helix.HelixManagerFactory;
import org.apache.helix.InstanceType;
import org.apache.helix.task.TaskDriver;
import org.apache.helix.task.TaskResult;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

@TaskDef(name = "Workflow Cancellation Task")
public class WorkflowCancellationTask extends AbstractTask {

    private static final Logger logger = LogManager.getLogger(WorkflowCancellationTask.class);

    private TaskDriver taskDriver;

    @TaskParam(name = "Cancelling Workflow")
    private String cancellingWorkflowName;

    @Override
    public void init(HelixManager manager, String workflowName, String jobName, String taskName) {
        super.init(manager, workflowName, jobName, taskName);

        try {

            HelixManager helixManager = HelixManagerFactory.getZKHelixManager(ServerSettings.getSetting("helix.cluster.name"), taskName,
                    InstanceType.SPECTATOR, ServerSettings.getZookeeperConnection());
            helixManager.connect();
            Runtime.getRuntime().addShutdownHook(
                    new Thread() {
                        @Override
                        public void run() {
                            helixManager.disconnect();
                        }
                    }
            );
            taskDriver = new TaskDriver(helixManager);
        } catch (Exception e) {
            logger.error("Failed to build Helix Task driver in " + taskName, e);
            throw new RuntimeException("Failed to build Helix Task driver in " + taskName, e);
        }
    }

    @Override
    public TaskResult onRun(TaskHelper helper) {
        logger.info("Cancelling workflow " + cancellingWorkflowName);
        try {
            taskDriver.stop(cancellingWorkflowName);
            logger.info("Workflow " + cancellingWorkflowName + " cancelled");
            return onSuccess("Successfully cancelled workflow " + cancellingWorkflowName);
        } catch (Exception e) {
            logger.error("Failed to stop workflow " + cancellingWorkflowName, e);
            return onFail("Failed to stop workflow " + cancellingWorkflowName + ": " + e.getMessage(), true);
        }
    }

    @Override
    public void onCancel() {

    }

    public String getCancellingWorkflowName() {
        return cancellingWorkflowName;
    }

    public void setCancellingWorkflowName(String cancellingWorkflowName) {
        this.cancellingWorkflowName = cancellingWorkflowName;
    }
}
