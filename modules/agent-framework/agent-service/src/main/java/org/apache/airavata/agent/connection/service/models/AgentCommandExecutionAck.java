package org.apache.airavata.agent.connection.service.models;

public class AgentCommandExecutionAck {
    private String executionId;
    private String error;

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}