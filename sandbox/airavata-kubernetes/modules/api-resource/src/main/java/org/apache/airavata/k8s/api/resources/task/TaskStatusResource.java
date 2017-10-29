package org.apache.airavata.k8s.api.resources.task;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class TaskStatusResource {

    private long id;
    private int state;
    private String stateStr;
    private long timeOfStateChange;
    private String reason;
    private long taskId;

    public long getId() {
        return id;
    }

    public TaskStatusResource setId(long id) {
        this.id = id;
        return this;
    }

    public int getState() {
        return state;
    }

    public TaskStatusResource setState(int state) {
        this.state = state;
        return this;
    }

    public long getTimeOfStateChange() {
        return timeOfStateChange;
    }

    public TaskStatusResource setTimeOfStateChange(long timeOfStateChange) {
        this.timeOfStateChange = timeOfStateChange;
        return this;
    }

    public String getReason() {
        return reason;
    }

    public TaskStatusResource setReason(String reason) {
        this.reason = reason;
        return this;
    }

    public long getTaskId() {
        return taskId;
    }

    public TaskStatusResource setTaskId(long taskId) {
        this.taskId = taskId;
        return this;
    }

    public String getStateStr() {
        return stateStr;
    }

    public TaskStatusResource setStateStr(String stateStr) {
        this.stateStr = stateStr;
        return this;
    }

    public static final class State {
        public static final int CREATED = 0;
        public static final int SCHEDULED = 1;
        public static final int EXECUTING = 2;
        public static final int COMPLETED = 3;
        public static final int FAILED = 4;
        public static final int CANCELED = 5;
    }
}
