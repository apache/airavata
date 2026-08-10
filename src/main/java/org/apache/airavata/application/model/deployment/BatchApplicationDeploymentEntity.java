package org.apache.airavata.application.model.deployment;

import org.apache.airavata.application.model.template.ApplicationTemplateEntity;
import org.apache.airavata.compute.model.SlurmClusterEntity;
import org.apache.airavata.credentials.model.SSHUserCredential;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
public class BatchApplicationDeploymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String deploymentId;

    @ManyToOne
    @JoinColumn(name = "cluster_id", foreignKey = @ForeignKey(name = "fk_dep_slurm_cluster"))
    private SlurmClusterEntity slurmCluster;

    @ManyToOne
    @JoinColumn(name = "template_id", foreignKey = @ForeignKey(name = "fk_dep_app_template"))
    private ApplicationTemplateEntity applicationTemplate;

    /*
     * All execution commands go into this. This includes module loads, clean up
     * commands, actual run command. Script can be parameterized using jinja
     * template
     */
    @Lob
    @Column(nullable = false)
    private String slurmRunSection;

    // Batch-scheduler resource request. Owned by this deployment: created, replaced and
    // deleted with it, so cascades fully and has no life of its own.
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(
            name = "batch_job_config_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_dep_batch_job_config"))
    private BatchJobConfigs batchJobConfig;

    @ManyToOne
    @JoinColumn(
            name = "default_submission_credential_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_dep_submission_credential"))
    private SSHUserCredential defaultSubmissionCredential;

    @Column(nullable = true)
    private String workDir; // Additional subdir for execution will be created inside this
    @Column(nullable = true)
    private String partition;

    // Getters and Setters
    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public SlurmClusterEntity getSlurmCluster() {
        return slurmCluster;
    }

    public void setSlurmCluster(SlurmClusterEntity slurmCluster) {
        this.slurmCluster = slurmCluster;
    }

    public ApplicationTemplateEntity getApplicationTemplate() {
        return applicationTemplate;
    }

    public void setApplicationTemplate(ApplicationTemplateEntity applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
    }

    public String getSlurmRunSection() {
        return slurmRunSection;
    }

    public void setSlurmRunSection(String slurmRunSection) {
        this.slurmRunSection = slurmRunSection;
    }

    public BatchJobConfigs getBatchJobConfig() {
        return batchJobConfig;
    }

    public void setBatchJobConfig(BatchJobConfigs batchJobConfig) {
        this.batchJobConfig = batchJobConfig;
    }

    public SSHUserCredential getDefaultSubmissionCredential() {
        return defaultSubmissionCredential;
    }

    public void setDefaultSubmissionCredential(SSHUserCredential defaultSubmissionCredential) {
        this.defaultSubmissionCredential = defaultSubmissionCredential;
    }

    public String getWorkDir() {
        return workDir;
    }

    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }

    public String getPartition() {
        return partition;
    }

    public void setPartition(String partition) {
        this.partition = partition;
    }
}
