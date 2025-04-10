package org.apache.airavata.agent.connection.service.models;

public class AgentLaunchResponse {
    private String agentId;
    private String experimentId;
    private String envName;
    private String processId;

    public AgentLaunchResponse(String agentId, String experimentId, String envName) {
        this.agentId = agentId;
        this.experimentId = experimentId;
        this.envName = envName;
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

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }
}
