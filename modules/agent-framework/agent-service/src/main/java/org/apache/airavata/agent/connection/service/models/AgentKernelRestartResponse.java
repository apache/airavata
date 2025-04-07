package org.apache.airavata.agent.connection.service.models;

public class AgentKernelRestartResponse {

    private String executionId;
    private String status;
    private boolean restarted;

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean getRestarted() {
        return restarted;
    }

    public void setRestarted(boolean restarted) {
        this.restarted = restarted;
    }
}
