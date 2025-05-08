package org.apache.airavata.agent.connection.service.models;

public class AgentTerminateResponse {
    private String experimentId;
    private boolean terminated;

    public AgentTerminateResponse(String experimentId, boolean terminated) {
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
