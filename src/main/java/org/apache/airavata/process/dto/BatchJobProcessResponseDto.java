package org.apache.airavata.process.dto;

import org.apache.airavata.application.dto.deployment.BatchJobConfigResponseDto;

/**
 * Read view of a batch job process. The batch job config is inlined as a snapshot of
 * what the process actually ran with, since the deployment's own config can change
 * after the process is created.
 */
public class BatchJobProcessResponseDto {

    private String processId;
    private String deploymentId;
    private String userId;
    private String sshCredentialId;
    private BatchJobConfigResponseDto batchJobConfig;

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSshCredentialId() {
        return sshCredentialId;
    }

    public void setSshCredentialId(String sshCredentialId) {
        this.sshCredentialId = sshCredentialId;
    }

    public BatchJobConfigResponseDto getBatchJobConfig() {
        return batchJobConfig;
    }

    public void setBatchJobConfig(BatchJobConfigResponseDto batchJobConfig) {
        this.batchJobConfig = batchJobConfig;
    }
}
