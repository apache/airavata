package org.apache.airavata.k8s.api.resources.experiment;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class ExperimentStatusResource {
    private long id;
    private int state;
    private String stateStr;
    private long timeOfStateChange;
    private String reason;

    public long getId() {
        return id;
    }

    public ExperimentStatusResource setId(long id) {
        this.id = id;
        return this;
    }

    public int getState() {
        return state;
    }

    public ExperimentStatusResource setState(int state) {
        this.state = state;
        return this;
    }

    public String getStateStr() {
        return stateStr;
    }

    public ExperimentStatusResource setStateStr(String stateStr) {
        this.stateStr = stateStr;
        return this;
    }

    public long getTimeOfStateChange() {
        return timeOfStateChange;
    }

    public ExperimentStatusResource setTimeOfStateChange(long timeOfStateChange) {
        this.timeOfStateChange = timeOfStateChange;
        return this;
    }

    public String getReason() {
        return reason;
    }

    public ExperimentStatusResource setReason(String reason) {
        this.reason = reason;
        return this;
    }
}
