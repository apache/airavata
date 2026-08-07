package org.apache.airavata.credentials.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

@Entity
public class SSHKeyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String sshKeyId;
    
    @Column(nullable = false)
    private String sshKeyName;

    @Lob
    @Column(nullable = false)
    private String publicKey;

    @Lob
    @Column(nullable = false)
    private String privateKey;

    @Column(nullable = true)
    private String passphrase;

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
