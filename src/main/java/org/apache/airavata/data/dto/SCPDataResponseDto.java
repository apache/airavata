package org.apache.airavata.data.dto;

import org.apache.airavata.data.model.DataProvisionStatus;

/** Read view of an SCP data registration. */
public class SCPDataResponseDto {

    private String dataId;
    private String dataName;
    private String dataDescription;
    private String isFile;
    private String path;
    private String slurmClusterCredentialId;
    private DataProvisionStatus provisionStatus;
    private String ownerId;

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

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

    public DataProvisionStatus getProvisionStatus() {
        return provisionStatus;
    }

    public void setProvisionStatus(DataProvisionStatus provisionStatus) {
        this.provisionStatus = provisionStatus;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}
