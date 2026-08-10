package org.apache.airavata.compute.model;

import org.apache.airavata.credentials.model.SSHUserCredential;
import org.apache.airavata.iam.model.UserEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class SlurmClusterCredentialEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String slurmClusterCredentialId;

    @ManyToOne
    @JoinColumn(name = "cluster_id", foreignKey = @ForeignKey(name = "fk_cluster_credential_cluster"))
    private SlurmClusterEntity slurmCluster;

    @ManyToOne
    @JoinColumn(name = "ssh_credential_id", foreignKey = @ForeignKey(name = "fk_cluster_credential_ssh_credential"))
    private SSHUserCredential sshUserCredential;

    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_cluster_credential_user"))
    private UserEntity user;

    public String getSlurmClusterCredentialId() {
        return slurmClusterCredentialId;
    }

    public void setSlurmClusterCredentialId(String slurmClusterCredentialId) {
        this.slurmClusterCredentialId = slurmClusterCredentialId;
    }

    public SlurmClusterEntity getSlurmCluster() {
        return slurmCluster;
    }

    public void setSlurmCluster(SlurmClusterEntity slurmCluster) {
        this.slurmCluster = slurmCluster;
    }

    public SSHUserCredential getSshUserCredential() {
        return sshUserCredential;
    }

    public void setSshUserCredential(SSHUserCredential sshUserCredential) {
        this.sshUserCredential = sshUserCredential;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }
}
