package org.apache.airavata.agent.connection.service.models;

import java.util.List;

public class AsyncCommand {
    private int processId;
    private List<String> arguments;

    public int getProcessId() {
        return processId;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }
}
