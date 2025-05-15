package org.apache.airavata.agent.connection.service.models;

public class AgentAsyncCommandTerminateRequest {
    private String agentId;
    private int processId;

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public int getProcessId() {
        return processId;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }
}
