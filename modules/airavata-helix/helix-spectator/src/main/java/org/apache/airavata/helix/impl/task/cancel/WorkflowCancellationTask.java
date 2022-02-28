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
import org.apache.helix.task.TaskState;
import org.apache.helix.task.WorkflowContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TaskDef(name = "Workflow Cancellation Task")
public class WorkflowCancellationTask extends AbstractTask {

    private final static Logger logger = LoggerFactory.getLogger(WorkflowCancellationTask.class);

    private TaskDriver taskDriver;
    private HelixManager helixManager;

    @TaskParam(name = "Cancelling Workflow")
    private String cancellingWorkflowName;

    @TaskParam(name = "Waiting time to monitor status (s)")
    private int waitTime = 20;

    @Override
    public void init(HelixManager manager, String workflowName, String jobName, String taskName) {
        super.init(manager, workflowName, jobName, taskName);

        try {
            helixManager = HelixManagerFactory.getZKHelixManager(ServerSettings.getSetting("helix.cluster.name"), taskName,
                    InstanceType.SPECTATOR, ServerSettings.getZookeeperConnection());
            helixManager.connect();
            Runtime.getRuntime().addShutdownHook(
                    new Thread(() -> {
                        if (helixManager.isConnected()) {
                            helixManager.disconnect();
                        }
                    })
            );
            taskDriver = new TaskDriver(helixManager);
        } catch (Exception e) {
            try {
                if (helixManager != null) {
                    if (helixManager.isConnected()) {
                        helixManager.disconnect();
                    }
                }
            } catch (Exception ex) {
                logger.warn("Failed to disconnect helix manager", ex);
            }

            logger.error("Failed to build Helix Task driver in " + taskName, e);
            throw new RuntimeException("Failed to build Helix Task driver in " + taskName, e);
        }
    }

    @Override
    public TaskResult onRun(TaskHelper helper) {
        logger.info("Cancelling workflow " + cancellingWorkflowName);

        try {
            if (taskDriver.getWorkflowConfig(cancellingWorkflowName) == null) {
                // Workflow could be already deleted by cleanup agents
                logger.warn("Can not find a workflow with name " + cancellingWorkflowName + " but continuing");
                return onSuccess("Can not find a workflow with name " + cancellingWorkflowName + " but continuing");
            }

            try {
                WorkflowContext workflowContext = taskDriver.getWorkflowContext(cancellingWorkflowName);

                // if the workflow can not be found, ignore it
                if (workflowContext == null) {
                    logger.warn("Can not find a workflow with id " + cancellingWorkflowName + ". So ignoring");
                    return onSuccess("Can not find a workflow with id " + cancellingWorkflowName + ". So ignoring");
                }

                TaskState workflowState = workflowContext.getWorkflowState();
                logger.info("Current state of workflow " + cancellingWorkflowName + " : " + workflowState.name());

                taskDriver.stop(cancellingWorkflowName);

            } catch (Exception e) {
                logger.error("Failed to stop workflow " + cancellingWorkflowName, e);
                // in case of an error, retry
                return onFail("Failed to stop workflow " + cancellingWorkflowName + ": " + e.getMessage(), false);
            }

            try {
                logger.info("Waiting maximum " + waitTime + "s for workflow " + cancellingWorkflowName + " state to change");
                TaskState newWorkflowState = taskDriver.pollForWorkflowState(cancellingWorkflowName, waitTime * 1000,
                        TaskState.COMPLETED, TaskState.FAILED, TaskState.STOPPED, TaskState.ABORTED, TaskState.NOT_STARTED);

                logger.info("Workflow " + cancellingWorkflowName + " state changed to " + newWorkflowState.name());
                return onSuccess("Successfully cancelled workflow " + cancellingWorkflowName);

            } catch (Exception e) {
                logger.warn("Failed while watching workflow to stop " + cancellingWorkflowName, e);
                return onSuccess("Failed while watching workflow to stop " + cancellingWorkflowName + ". But continuing");

            }

        } finally {

            try {
                if (helixManager != null) {
                    if (helixManager.isConnected()) {
                        helixManager.disconnect();
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to disconnect helix manager", e);
            }
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

    public int getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }
}
