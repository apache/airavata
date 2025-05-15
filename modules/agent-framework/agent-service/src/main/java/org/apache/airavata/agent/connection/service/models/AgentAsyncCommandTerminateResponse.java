package org.apache.airavata.agent.connection.service.models;

public class AgentAsyncCommandTerminateResponse {

    private String executionId;
    private String status;

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
}
