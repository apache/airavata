package org.apache.airavata.helix.impl.task.aws;

import org.apache.airavata.helix.impl.task.AiravataTask;
import org.apache.airavata.helix.impl.task.TaskContext;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.helix.task.TaskResult;

@TaskDef(name = "No Operation Task")
public class NoOperationTask extends AiravataTask  {

    @Override
    public TaskResult onRun(TaskHelper helper, TaskContext taskContext) {
        return new TaskResult(TaskResult.Status.COMPLETED, "OK");
    }

    @Override
    public void onCancel(TaskContext taskContext) {

    }
}
