package org.apache.airavata.data.model;

import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import org.apache.airavata.compute.model.ClusterCredentialEntity;
import org.apache.airavata.iam.model.UserEntity;

import jakarta.persistence.Entity;

@Entity
public class SCPDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String dataId;

    private String dataName;
    private String dataDescription;
    private String isFile;
    private String path;

    @ManyToOne
    @JoinColumn(
            name = "slurm_cluster_credential_id",
            foreignKey = @ForeignKey(name = "fk_scp_data_slurm_cluster_credential"))
    private ClusterCredentialEntity slurmClusterCredential;

    private DataProvisionStatus provisionStatus;

    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_scp_data_user"))
    private UserEntity owner;

    public ClusterCredentialEntity getSlurmClusterCredential() {
        return slurmClusterCredential;
    }

    public void setSlurmClusterCredential(ClusterCredentialEntity slurmClusterCredential) {
        this.slurmClusterCredential = slurmClusterCredential;
    }

    public UserEntity getOwner() {
        return owner;
    }

    public void setOwner(UserEntity owner) {
        this.owner = owner;
    }

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

    public DataProvisionStatus getProvisionStatus() {
        return provisionStatus;
    }

    public void setProvisionStatus(DataProvisionStatus provisionStatus) {
        this.provisionStatus = provisionStatus;
    }
}
