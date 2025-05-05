package org.apache.airavata.agent.connection.service.models;

public class AgentTunnelCreateResponse {

    private String executionId;
    private String tunnelId;
    private int poxyPort;
    private String proxyHost;
    private String status;

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getTunnelId() {
        return tunnelId;
    }

    public void setTunnelId(String tunnelId) {
        this.tunnelId = tunnelId;
    }

    public int getPoxyPort() {
        return poxyPort;
    }

    public void setPoxyPort(int poxyPort) {
        this.poxyPort = poxyPort;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
