package org.apache.airavata.compute.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Create/update payload for a per-user SSH credential binding on a Slurm cluster.
 *
 * <p>There is no {@code userId} field: ownership is derived from the caller's access
 * token, never from client input, so a caller cannot bind a credential on another user's
 * behalf.
 */
public class ClusterCredentialRequestDto {

    @NotBlank(message = "Cluster id cannot be blank")
    private String clusterId;

    @NotBlank(message = "SSH credential id cannot be blank")
    private String sshCredentialId;

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
}
