package org.apache.airavata.k8s.api.resources.process;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class ProcessStatusResource {

    private long id;
    private int state;
    private long timeOfStateChange;
    private String reason;
    private long processId;

    public long getId() {
        return id;
    }

    public ProcessStatusResource setId(long id) {
        this.id = id;
        return this;
    }

    public int getState() {
        return state;
    }

    public ProcessStatusResource setState(int state) {
        this.state = state;
        return this;
    }

    public long getTimeOfStateChange() {
        return timeOfStateChange;
    }

    public ProcessStatusResource setTimeOfStateChange(long timeOfStateChange) {
        this.timeOfStateChange = timeOfStateChange;
        return this;
    }

    public String getReason() {
        return reason;
    }

    public ProcessStatusResource setReason(String reason) {
        this.reason = reason;
        return this;
    }

    public long getProcessId() {
        return processId;
    }

    public ProcessStatusResource setProcessId(long processId) {
        this.processId = processId;
        return this;
    }

    public static final class State {
        public static final int CREATED = 0;
        public static final int VALIDATED = 1;
        public static final int STARTED = 2;
        public static final int PRE_PROCESSING = 3;
        public static final int CONFIGURING_WORKSPACE = 4;
        public static final int INPUT_DATA_STAGING = 5;
        public static final int EXECUTING = 6;
        public static final int MONITORING = 7;
        public static final int OUTPUT_DATA_STAGING = 8;
        public static final int POST_PROCESSING = 9;
        public static final int COMPLETED = 10;
        public static final int FAILED = 11;
        public static final int CANCELLING = 12;
        public static final int CANCELED = 13;
    }
}
