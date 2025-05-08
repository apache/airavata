package org.apache.airavata.agent.connection.service.models;

public class AgentTunnelTerminateRequest {
    private String agentId;
    private String tunnelId;

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getTunnelId() {
        return tunnelId;
    }

    public void setTunnelId(String tunnelId) {
        this.tunnelId = tunnelId;
    }
}
