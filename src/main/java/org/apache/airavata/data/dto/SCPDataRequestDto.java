package org.apache.airavata.data.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Create/update payload for an SCP data registration.
 *
 * <p>There is no {@code ownerId} field: ownership is derived from the caller's access
 * token, never from client input, so a caller cannot register data on another user's
 * behalf. There is no {@code provisionStatus} field either: that is lifecycle state
 * managed by {@code SCPDataService}, not something a client sets directly.
 */
public class SCPDataRequestDto {

    @NotBlank(message = "Data name cannot be blank")
    private String dataName;

    private String dataDescription;

    @NotBlank(message = "isFile cannot be blank")
    private String isFile;

    @NotBlank(message = "Path cannot be blank")
    private String path;

    @NotBlank(message = "Slurm cluster credential id cannot be blank")
    private String slurmClusterCredentialId;

    public String getDataName() {
        return dataName;
    }

    public void setDataName(String dataName) {
        this.dataName = dataName;
    }

    public String getDataDescription() {
        return dataDescription;
    }

    public void setDataDescription(String dataDescription) {
        this.dataDescription = dataDescription;
    }

    public String getIsFile() {
        return isFile;
    }

    public void setIsFile(String isFile) {
        this.isFile = isFile;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSlurmClusterCredentialId() {
        return slurmClusterCredentialId;
    }

    public void setSlurmClusterCredentialId(String slurmClusterCredentialId) {
        this.slurmClusterCredentialId = slurmClusterCredentialId;
    }
}
