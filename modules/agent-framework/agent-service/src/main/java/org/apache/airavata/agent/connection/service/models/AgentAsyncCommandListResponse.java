package org.apache.airavata.agent.connection.service.models;

import java.util.List;

public class AgentAsyncCommandListResponse {
    private String executionId;
    private List<AsyncCommand> commands;
    private String error;

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public List<AsyncCommand> getCommands() {
        return commands;
    }

    public void setCommands(List<AsyncCommand> commands) {
        this.commands = commands;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
