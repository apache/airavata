package org.apache.airavata.agent.connection.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "airavata.agent")
public class AgentProperties {
    private String storageResourceId;
    private String storagePath;
    private Tunnel tunnel = new Tunnel();

    public String getStorageResourceId() { return storageResourceId; }
    public void setStorageResourceId(String s) { this.storageResourceId = s; }
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String s) { this.storagePath = s; }
    public Tunnel getTunnel() { return tunnel; }
    public void setTunnel(Tunnel t) { this.tunnel = t; }

    public static class Tunnel {
        private String serverHost;
        private int serverPort;
        private String serverToken;
        private String serverApiUrl;

        public String getServerHost() { return serverHost; }
        public void setServerHost(String s) { this.serverHost = s; }
        public int getServerPort() { return serverPort; }
        public void setServerPort(int p) { this.serverPort = p; }
        public String getServerToken() { return serverToken; }
        public void setServerToken(String s) { this.serverToken = s; }
        public String getServerApiUrl() { return serverApiUrl; }
        public void setServerApiUrl(String s) { this.serverApiUrl = s; }
    }
}
