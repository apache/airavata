package org.apache.airavata.agent.connection.service.models;

public class AgentInfoResponse {
    private String agentId;
    private boolean isAgentUp;

    public AgentInfoResponse(String agentId, boolean isAgentUp) {
        this.agentId = agentId;
        this.isAgentUp = isAgentUp;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public boolean isAgentUp() {
        return isAgentUp;
    }

    public void setAgentUp(boolean agentUp) {
        isAgentUp = agentUp;
    }
}