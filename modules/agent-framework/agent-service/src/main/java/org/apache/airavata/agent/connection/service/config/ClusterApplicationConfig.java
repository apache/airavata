package org.apache.airavata.agent.connection.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "airavata.cluster")
public class ClusterApplicationConfig {

    private Map<String, String> applicationInterfaceId;

    public Map<String, String> getApplicationInterfaceId() {
        return applicationInterfaceId;
    }

    public void setApplicationInterfaceId(Map<String, String> applicationInterfaceId) {
        this.applicationInterfaceId = applicationInterfaceId;
    }

    public String getApplicationInterfaceIdByCluster(String clusterName) {
        return applicationInterfaceId.get(clusterName);
    }

}
