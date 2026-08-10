package org.apache.airavata.process.model;

import org.apache.airavata.iam.model.UserEntity;
import org.apache.airavata.application.model.deployment.BatchJobConfigs;
import org.apache.airavata.application.model.deployment.BatchApplicationDeploymentEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

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

    // Batch-scheduler resource request. Owned by this process: created, replaced and
    // deleted with it, so cascades fully and has no life of its own.
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(
            name = "batch_job_config_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_process_batch_job_config"))
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

    public BatchJobConfigs getBatchJobConfigs() {
        return batchJobConfigs;
    }

    public void setBatchJobConfigs(BatchJobConfigs batchJobConfigs) {
        this.batchJobConfigs = batchJobConfigs;
    }
}
