package org.apache.airavata.helix.impl.task;

import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.helix.task.TaskResult;

@TaskDef(name = "Data Staging Task")
public class DataStagingTask extends AiravataTask {

    @Override
    public TaskResult onRun(TaskHelper taskHelper) {
        return null;
    }

    @Override
    public void onCancel() {

    }
}
