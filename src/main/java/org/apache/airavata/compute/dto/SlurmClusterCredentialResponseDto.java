package org.apache.airavata.compute.dto;

/** Read view of a per-user SSH credential binding on a Slurm cluster. */
public class SlurmClusterCredentialResponseDto {

    private String slurmClusterCredentialId;
    private String clusterId;
    private String sshCredentialId;
    private String userId;

    public String getSlurmClusterCredentialId() {
        return slurmClusterCredentialId;
    }

    public void setSlurmClusterCredentialId(String slurmClusterCredentialId) {
        this.slurmClusterCredentialId = slurmClusterCredentialId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getSshCredentialId() {
        return sshCredentialId;
    }

    public void setSshCredentialId(String sshCredentialId) {
        this.sshCredentialId = sshCredentialId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
