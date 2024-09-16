package org.apache.airavata.agent.connection.service.models;

import java.util.ArrayList;
import java.util.List;

public class AgentCommandRequest {
    private List<String> arguments = new ArrayList<>();
    private String workingDir;
    private String agentId;

    public List<String> getArguments() {
        return arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }
}
