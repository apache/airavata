package org.apache.airavata.gfac.context;

public class AmazonSecurityContext extends SecurityContext {
    private String userName;
    private String accessKey;
    private String secretKey;
    private String amiId;
    private String instanceType;
    private String instanceId;
    private boolean isRunningInstance = false;

    public AmazonSecurityContext(String userName, String accessKey, String secretKey, String amiId, String instanceType) {
        this.userName = userName;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.amiId = amiId;
        this.instanceType = instanceType;
    }

    public AmazonSecurityContext(String userName, String accessKey, String secretKey, String instanceId) {
        this.userName = userName;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.instanceId = instanceId;
        this.isRunningInstance = true;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public String getAmiId() {
        return amiId;
    }

    public boolean isRunningInstance() {
        return isRunningInstance;
    }

    public String getUserName() {
        return userName;
    }
}
