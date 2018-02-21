package org.apache.airavata.helix.core;

import org.apache.airavata.helix.core.util.TaskUtil;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskParam;
import org.apache.helix.HelixManager;
import org.apache.helix.task.Task;
import org.apache.helix.task.TaskCallbackContext;
import org.apache.helix.task.TaskResult;
import org.apache.helix.task.UserContentStore;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public abstract class AbstractTask extends UserContentStore implements Task {

    private static final String NEXT_JOB = "next-job";
    private static final String WORKFLOW_STARTED = "workflow-started";

    @TaskParam(name = "taskId")
    private String taskId;

    private TaskCallbackContext callbackContext;
    private TaskHelper taskHelper;

    @Override
    public void init(HelixManager manager, String workflowName, String jobName, String taskName) {
        super.init(manager, workflowName, jobName, taskName);
        try {
            TaskUtil.deserializeTaskData(this, this.callbackContext.getTaskConfig().getConfigMap());
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public final TaskResult run() {
        boolean isThisNextJob = getUserContent(WORKFLOW_STARTED, Scope.WORKFLOW) == null ||
                this.callbackContext.getJobConfig().getJobId()
                        .equals(this.callbackContext.getJobConfig().getWorkflow() + "_" + getUserContent(NEXT_JOB, Scope.WORKFLOW));
        if (isThisNextJob) {
            return onRun(this.taskHelper);
        } else {
            return new TaskResult(TaskResult.Status.COMPLETED, "Not a target job");
        }
    }

    @Override
    public final void cancel() {
        onCancel();
    }

    public abstract TaskResult onRun(TaskHelper helper);

    public abstract void onCancel();

    protected void publishErrors(Throwable e) {
        // TODO Publish through kafka channel with task and workflow id
        e.printStackTrace();
    }

    public void sendNextJob(String jobId) {
        putUserContent(WORKFLOW_STARTED, "TRUE", Scope.WORKFLOW);
        if (jobId != null) {
            putUserContent(NEXT_JOB, jobId, Scope.WORKFLOW);
        }
    }

    protected void setContextVariable(String key, String value) {
        putUserContent(key, value, Scope.WORKFLOW);
    }

    protected String getContextVariable(String key) {
        return getUserContent(key, Scope.WORKFLOW);
    }

    // Getters and setters

    public String getTaskId() {
        return taskId;
    }

    public AbstractTask setTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public TaskCallbackContext getCallbackContext() {
        return callbackContext;
    }

    public AbstractTask setCallbackContext(TaskCallbackContext callbackContext) {
        this.callbackContext = callbackContext;
        return this;
    }

    public TaskHelper getTaskHelper() {
        return taskHelper;
    }

    public AbstractTask setTaskHelper(TaskHelper taskHelper) {
        this.taskHelper = taskHelper;
        return this;
    }
}
