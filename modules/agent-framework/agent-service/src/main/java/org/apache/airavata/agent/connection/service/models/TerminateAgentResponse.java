package org.apache.airavata.agent.connection.service.models;

public class TerminateAgentResponse {
    private String experimentId;
    private boolean terminated;

    public TerminateAgentResponse(String experimentId, boolean terminated) {
        this.experimentId = experimentId;
        this.terminated = terminated;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public boolean isTerminated() {
        return terminated;
    }

    public void setTerminated(boolean terminated) {
        this.terminated = terminated;
    }
}
