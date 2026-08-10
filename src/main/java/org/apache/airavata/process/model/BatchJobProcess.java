package org.apache.airavata.process.model;

import org.apache.airavata.iam.model.UserEntity;
import org.apache.airavata.application.model.deployment.BatchJobConfigs;
import org.apache.airavata.application.model.deployment.BatchApplicationDeploymentEntity;
import org.apache.airavata.credentials.model.SSHUserCredential;

import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class BatchJobProcess {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String processId;

    @ManyToOne
    @JoinColumn(name = "deployment_id", foreignKey = @ForeignKey(name = "fk_process_deployment"))
    private BatchApplicationDeploymentEntity batchApplicationDeployment;

    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_process_user"))
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "ssh_credential_id", foreignKey = @ForeignKey(name = "fk_process_ssh_credential"))
    private SSHUserCredential sshUserCredential;

    @ManyToOne
    @JoinColumn(name = "batch_job_config_id", foreignKey = @ForeignKey(name = "fk_process_batch_job_config"))
    private BatchJobConfigs batchJobConfigs;

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public BatchApplicationDeploymentEntity getBatchApplicationDeployment() {
        return batchApplicationDeployment;
    }

    public void setBatchApplicationDeployment(BatchApplicationDeploymentEntity batchApplicationDeployment) {
        this.batchApplicationDeployment = batchApplicationDeployment;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public SSHUserCredential getSshUserCredential() {
        return sshUserCredential;
    }

    public void setSshUserCredential(SSHUserCredential sshUserCredential) {
        this.sshUserCredential = sshUserCredential;
    }

    public BatchJobConfigs getBatchJobConfigs() {
        return batchJobConfigs;
    }

    public void setBatchJobConfigs(BatchJobConfigs batchJobConfigs) {
        this.batchJobConfigs = batchJobConfigs;
    }
}
