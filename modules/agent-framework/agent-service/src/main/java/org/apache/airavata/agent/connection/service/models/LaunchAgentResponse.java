package org.apache.airavata.agent.connection.service.models;

public class LaunchAgentResponse {
    private String agentId;
    private String experimentId;
    private String processId;

    public LaunchAgentResponse(String agentId, String experimentId) {
        this.agentId = agentId;
        this.experimentId = experimentId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }
}
