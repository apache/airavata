package org.apache.airavata.credentials.dto;

/**
 * Read view of an SSH user credential. The key is inlined as its safe summary — no
 * private key or passphrase — so callers can show which key backs the credential without
 * a second request.
 */
public class SSHUserCredentialResponseDto {

    private String sshCredentialId;
    private String username;
    private SSHKeyResponseDto sshKey;

    public String getSshCredentialId() {
        return sshCredentialId;
    }

    public void setSshCredentialId(String sshCredentialId) {
        this.sshCredentialId = sshCredentialId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public SSHKeyResponseDto getSshKey() {
        return sshKey;
    }

    public void setSshKey(SSHKeyResponseDto sshKey) {
        this.sshKey = sshKey;
    }
}
