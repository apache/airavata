package org.apache.airavata.process.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Create/update payload for a batch job process.
 *
 * <p>There is no {@code userId} field: ownership is derived from the caller's access
 * token, never from client input, so a caller cannot submit a process on another user's
 * behalf.
 */
public class BatchJobProcessRequestDto {

    @NotBlank(message = "Deployment id cannot be blank")
    private String deploymentId;

    /** Optional; defaults to the deployment's default submission credential when omitted. */
    private String sshCredentialId;

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getSshCredentialId() {
        return sshCredentialId;
    }

    public void setSshCredentialId(String sshCredentialId) {
        this.sshCredentialId = sshCredentialId;
    }
}
