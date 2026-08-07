package org.apache.airavata.credentials.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class SSHUserCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String sshCredentialId;

    @Column(nullable = false)
    private String username;

    // Many-to-one rather than one-to-one: the same registered key can back
    // credentials
    // for different usernames, and deleting a credential must not take the key with
    // it.
    @ManyToOne
    @JoinColumn(name = "ssh_key_id", foreignKey = @ForeignKey(name = "fk_credential_ssh_key"))
    private SSHKeyEntity sshKey;

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

    public SSHKeyEntity getSshKey() {
        return sshKey;
    }

    public void setSshKey(SSHKeyEntity sshKey) {
        this.sshKey = sshKey;
    }
}