package org.apache.airavata.agent.connection.service.models;

public class AgentTunnelResponse {

    private String executionId;
    private boolean tunneled;

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public boolean isTunneled() {
        return tunneled;
    }

    public void setTunneled(boolean tunneled) {
        this.tunneled = tunneled;
    }
}
