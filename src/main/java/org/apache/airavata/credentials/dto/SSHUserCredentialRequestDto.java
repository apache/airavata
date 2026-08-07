package org.apache.airavata.credentials.dto;

import jakarta.validation.constraints.NotBlank;

/** Create/update payload for an SSH user credential: a username paired with a stored key. */
public class SSHUserCredentialRequestDto {

    @NotBlank(message = "Username cannot be blank")
    private String username;

    @NotBlank(message = "SSH key id cannot be blank")
    private String sshKeyId;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSshKeyId() {
        return sshKeyId;
    }

    public void setSshKeyId(String sshKeyId) {
        this.sshKeyId = sshKeyId;
    }
}
