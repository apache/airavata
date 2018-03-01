package org.apache.airavata.agents.api;

public interface StorageResourceAdaptor {
    public void init(String storageResourceId, String gatewayId, String loginUser, String token) throws AgentException;
    public void uploadFile(String sourceFile, String destFile) throws AgentException;
    public void downloadFile(String sourceFile, String destFile) throws AgentException;
}
