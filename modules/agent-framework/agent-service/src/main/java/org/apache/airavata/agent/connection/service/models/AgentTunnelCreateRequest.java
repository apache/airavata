package org.apache.airavata.agent.connection.service.models;

public class AgentTunnelCreateRequest {
    private String agentId;
    private int localPort;
    private String localBindHost;

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public String getLocalBindHost() {
        return localBindHost;
    }

    public void setLocalBindHost(String localBindHost) {
        this.localBindHost = localBindHost;
    }
}
