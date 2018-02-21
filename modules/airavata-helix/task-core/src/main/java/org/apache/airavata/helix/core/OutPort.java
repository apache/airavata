package org.apache.airavata.helix.core;

import org.apache.helix.task.TaskResult;
import org.apache.helix.task.UserContentStore;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class OutPort {

    private String nextJobId;
    private AbstractTask task;

    public OutPort(String nextJobId, AbstractTask task) {
        this.nextJobId = nextJobId;
        this.task = task;
    }

    public TaskResult invoke(TaskResult taskResult) {
        task.sendNextJob(nextJobId);
        return taskResult;
    }

    public String getNextJobId() {
        return nextJobId;
    }

    public OutPort setNextJobId(String nextJobId) {
        this.nextJobId = nextJobId;
        return this;
    }

    public AbstractTask getTask() {
        return task;
    }

    public OutPort setTask(AbstractTask task) {
        this.task = task;
        return this;
    }
}
