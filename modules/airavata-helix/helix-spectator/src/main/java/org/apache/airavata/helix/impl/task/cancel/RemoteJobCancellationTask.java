package org.apache.airavata.helix.impl.task.cancel;

import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.helix.task.TaskResult;

@TaskDef(name = "Remote Job Cancellation Task")
public class RemoteJobCancellationTask extends AbstractTask {

    @Override
    public TaskResult onRun(TaskHelper helper) {
        return null;
    }

    @Override
    public void onCancel() {

    }
}
