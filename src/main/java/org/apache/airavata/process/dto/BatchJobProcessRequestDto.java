package org.apache.airavata.process.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.airavata.application.dto.deployment.BatchJobConfigRequestDto;

/**
 * Create/update payload for a batch job process.
 *
 * <p>There is no {@code userId} field: ownership is derived from the caller's access
 * token, never from client input, so a caller cannot submit a process on another user's
 * behalf.
 */
public class BatchJobProcessRequestDto {

    @NotBlank(message = "Deployment id cannot be blank")
    private String deploymentId;

    @NotNull(message = "Batch job config cannot be null")
    @Valid
    private BatchJobConfigRequestDto batchJobConfig;

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public BatchJobConfigRequestDto getBatchJobConfig() {
        return batchJobConfig;
    }

    public void setBatchJobConfig(BatchJobConfigRequestDto batchJobConfig) {
        this.batchJobConfig = batchJobConfig;
    }
}
