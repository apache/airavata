package org.apache.airavata.agent.connection.service.models;

import java.util.ArrayList;
import java.util.List;

public class AgentPythonRunRequest {
    private List<String> libraries = new ArrayList<>();
    private String code;
    private String pythonVersion;
    private String parentExperimentId;
    private boolean keepAlive;
    private String agentId;

    public List<String> getLibraries() {
        return libraries;
    }

    public void setLibraries(List<String> libraries) {
        this.libraries = libraries;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPythonVersion() {
        return pythonVersion;
    }

    public void setPythonVersion(String pythonVersion) {
        this.pythonVersion = pythonVersion;
    }

    public String getParentExperimentId() {
        return parentExperimentId;
    }

    public void setParentExperimentId(String parentExperimentId) {
        this.parentExperimentId = parentExperimentId;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }
}
