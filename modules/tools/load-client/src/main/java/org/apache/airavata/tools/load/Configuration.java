package org.apache.airavata.tools.load;

import org.apache.airavata.model.security.AuthzToken;

import java.util.ArrayList;
import java.util.List;

public class Configuration {

    private String userId;

    private String gatewayId;
    private String projectId;
    private String applicationInterfaceId;
    private String computeResourceId;
    private String storageResourceId;
    private String keycloakUrl;
    private String keycloakClientId;
    private String keycloakClientSecret;

    private String experimentBaseName;

    private String queue;
    private int wallTime;
    private int cpuCount;
    private int nodeCount;
    private int physicalMemory;

    private int concurrentUsers;
    private int iterationsPerUser;
    private int randomMSDelayWithinSubmissions;


    private AuthzToken authzToken;

    public AuthzToken getAuthzToken() throws Exception {

        if (authzToken == null) {
            System.out.print("Enter password for user " + getUserId() + " in gateway " + getGatewayId() + " : ");
            String pw = new String(System.console().readPassword());
            authzToken = Authenticator.getAuthzToken(getUserId(), pw, getGatewayId(),
                    getKeycloakUrl(), getKeycloakClientId(), getKeycloakClientSecret());
        }
        return authzToken;
    }

    private List<Input> inputs = new ArrayList<>();

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getApplicationInterfaceId() {
        return applicationInterfaceId;
    }

    public void setApplicationInterfaceId(String applicationInterfaceId) {
        this.applicationInterfaceId = applicationInterfaceId;
    }

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    public String getStorageResourceId() {
        return storageResourceId;
    }

    public void setStorageResourceId(String storageResourceId) {
        this.storageResourceId = storageResourceId;
    }

    public String getExperimentBaseName() {
        return experimentBaseName;
    }

    public void setExperimentBaseName(String experimentBaseName) {
        this.experimentBaseName = experimentBaseName;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public int getWallTime() {
        return wallTime;
    }

    public void setWallTime(int wallTime) {
        this.wallTime = wallTime;
    }

    public int getCpuCount() {
        return cpuCount;
    }

    public void setCpuCount(int cpuCount) {
        this.cpuCount = cpuCount;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }

    public int getPhysicalMemory() {
        return physicalMemory;
    }

    public void setPhysicalMemory(int physicalMemory) {
        this.physicalMemory = physicalMemory;
    }

    public int getConcurrentUsers() {
        return concurrentUsers;
    }

    public void setConcurrentUsers(int concurrentUsers) {
        this.concurrentUsers = concurrentUsers;
    }

    public int getIterationsPerUser() {
        return iterationsPerUser;
    }

    public void setIterationsPerUser(int iterationsPerUser) {
        this.iterationsPerUser = iterationsPerUser;
    }

    public int getRandomMSDelayWithinSubmissions() {
        return randomMSDelayWithinSubmissions;
    }

    public void setRandomMSDelayWithinSubmissions(int randomMSDelayWithinSubmissions) {
        this.randomMSDelayWithinSubmissions = randomMSDelayWithinSubmissions;
    }

    public List<Input> getInputs() {
        return inputs;
    }

    public void setInputs(List<Input> inputs) {
        this.inputs = inputs;
    }

    public static class Input {
        private String name;
        private String value;

        public Input() {
        }

        public Input(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public String getKeycloakUrl() {
        return keycloakUrl;
    }

    public void setKeycloakUrl(String keycloakUrl) {
        this.keycloakUrl = keycloakUrl;
    }

    public String getKeycloakClientId() {
        return keycloakClientId;
    }

    public void setKeycloakClientId(String keycloakClientId) {
        this.keycloakClientId = keycloakClientId;
    }

    public String getKeycloakClientSecret() {
        return keycloakClientSecret;
    }

    public void setKeycloakClientSecret(String keycloakClientSecret) {
        this.keycloakClientSecret = keycloakClientSecret;
    }
}
