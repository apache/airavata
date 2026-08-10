package org.apache.airavata.application.dto.deployment;

/** Read view of a batch application deployment; associations are referenced by id only. */
public class BatchApplicationDeploymentResponseDto {

    private String deploymentId;
    private String templateId;
    private String slurmClusterId;
    private String slurmRunSection;
    private BatchJobConfigResponseDto batchJobConfig;
    private String defaultSubmissionCredentialId;
    private String workDir;
    private String partition;

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

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

    public BatchJobConfigResponseDto getBatchJobConfig() {
        return batchJobConfig;
    }

    public void setBatchJobConfig(BatchJobConfigResponseDto batchJobConfig) {
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
