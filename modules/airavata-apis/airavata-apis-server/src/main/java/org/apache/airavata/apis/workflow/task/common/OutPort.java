package org.apache.airavata.apis.workflow.task.common;

public class OutPort {
    private String nextTaskId;

    public String getNextTaskId() {
        return nextTaskId;
    }

    public OutPort setNextTaskId(String nextTaskId) {
        this.nextTaskId = nextTaskId;
        return this;
    }
}
