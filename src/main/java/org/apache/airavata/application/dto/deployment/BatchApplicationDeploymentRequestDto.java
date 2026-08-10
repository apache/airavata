package org.apache.airavata.application.dto.deployment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Create/update payload for a batch application deployment of an application template.
 * Required fields mirror the {@code nullable = false} columns on the entity.
 */
public class BatchApplicationDeploymentRequestDto {

    @NotBlank(message = "Template id cannot be blank")
    private String templateId;

    private String slurmClusterId;

    @NotBlank(message = "Slurm run section cannot be blank")
    private String slurmRunSection;

    @NotNull(message = "Batch job config cannot be null")
    @Valid
    private BatchJobConfigRequestDto batchJobConfig;

    @NotBlank(message = "Default submission credential id cannot be blank")
    private String defaultSubmissionCredentialId;

    private String workDir;
    private String partition;

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getSlurmClusterId() {
        return slurmClusterId;
    }

    public void setSlurmClusterId(String slurmClusterId) {
        this.slurmClusterId = slurmClusterId;
    }

    public String getSlurmRunSection() {
        return slurmRunSection;
    }

    public void setSlurmRunSection(String slurmRunSection) {
        this.slurmRunSection = slurmRunSection;
    }

    public BatchJobConfigRequestDto getBatchJobConfig() {
        return batchJobConfig;
    }

    public void setBatchJobConfig(BatchJobConfigRequestDto batchJobConfig) {
        this.batchJobConfig = batchJobConfig;
    }

    public String getDefaultSubmissionCredentialId() {
        return defaultSubmissionCredentialId;
    }

    public void setDefaultSubmissionCredentialId(String defaultSubmissionCredentialId) {
        this.defaultSubmissionCredentialId = defaultSubmissionCredentialId;
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
