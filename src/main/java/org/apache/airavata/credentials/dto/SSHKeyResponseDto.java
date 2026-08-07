package org.apache.airavata.credentials.dto;

/**
 * Read view of an SSH key.
 *
 * <p>Deliberately carries no {@code privateKey} and no {@code passphrase}. The absence of
 * those fields is the mechanism that keeps them from leaking — do not add them here.
 */
public class SSHKeyResponseDto {

    private String sshKeyId;
    private String sshKeyName;
    private String publicKey;

    public String getSshKeyId() {
        return sshKeyId;
    }

    public void setSshKeyId(String sshKeyId) {
        this.sshKeyId = sshKeyId;
    }

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
}
