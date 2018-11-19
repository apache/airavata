package org.apache.airavata.helix.impl.task.mock;

import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.helix.task.TaskResult;

@TaskDef(name = "Mock Task")
public class MockTask extends AbstractTask {

    @Override
    public TaskResult onRun(TaskHelper helper) {
        return onSuccess("Successfully executed Mock Task");
    }

    @Override
    public void onCancel() {

    }
}