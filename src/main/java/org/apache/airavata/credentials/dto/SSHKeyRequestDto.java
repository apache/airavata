package org.apache.airavata.credentials.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Create/update payload for an SSH key pair.
 *
 * <p>{@code privateKey} and {@code passphrase} are write-only: they are accepted here but
 * never returned by any endpoint. On update, leaving either blank keeps the stored value
 * rather than clearing it — a client cannot echo back a secret it was never given.
 */
public class SSHKeyRequestDto {

    @NotBlank(message = "SSH key name cannot be blank")
    private String sshKeyName;

    @NotBlank(message = "Public key cannot be blank")
    private String publicKey;

    /** Required on create; omit on update to keep the stored key. */
    private String privateKey;

    /** Omit on update to keep the stored passphrase. */
    private String passphrase;

    public String getSshKeyName() {
        return sshKeyName;
    }

    public void setSshKeyName(String sshKeyName) {
        this.sshKeyName = sshKeyName;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }
}
