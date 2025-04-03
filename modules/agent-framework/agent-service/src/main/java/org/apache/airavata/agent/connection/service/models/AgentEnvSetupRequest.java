package org.apache.airavata.agent.connection.service.models;

import java.util.ArrayList;
import java.util.List;

public class AgentEnvSetupRequest {

    private String agentId;
    private String envName;
    private List<String> libraries = new ArrayList<>();
    private List<String> pip = new ArrayList<>();

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public List<String> getLibraries() {
        return libraries;
    }

    public void setLibraries(List<String> libraries) {
        this.libraries = libraries;
    }

    public List<String> getPip() {
        return pip;
    }

    public void setPip(List<String> pip) {
        this.pip = pip;
    }

}
