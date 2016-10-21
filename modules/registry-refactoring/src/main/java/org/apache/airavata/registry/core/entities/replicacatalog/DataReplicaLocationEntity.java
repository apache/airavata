package org.apache.airavata.registry.core.entities.replicacatalog;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by abhij on 10/13/2016.
 */
@Entity
@Table(name = "data_replica_location", schema = "airavata_catalog", catalog = "")
public class DataReplicaLocationEntity {
    private String replicaId;
    private String replicaName;
    private String replicaDescription;
    private String storageResourceId;
    private String filePath;
    private Timestamp creationTime;
    private Timestamp lastModifiedTime;
    private Timestamp validUntilTime;

    @Id
    @Column(name = "REPLICA_ID")
    public String getReplicaId() {
        return replicaId;
    }

    public void setReplicaId(String replicaId) {
        this.replicaId = replicaId;
    }

    @Basic
    @Column(name = "REPLICA_NAME")
    public String getReplicaName() {
        return replicaName;
    }

    public void setReplicaName(String replicaName) {
        this.replicaName = replicaName;
    }

    @Basic
    @Column(name = "REPLICA_DESCRIPTION")
    public String getReplicaDescription() {
        return replicaDescription;
    }

    public void setReplicaDescription(String replicaDescription) {
        this.replicaDescription = replicaDescription;
    }

    @Basic
    @Column(name = "STORAGE_RESOURCE_ID")
    public String getStorageResourceId() {
        return storageResourceId;
    }

    public void setStorageResourceId(String storageResourceId) {
        this.storageResourceId = storageResourceId;
    }

    @Basic
    @Column(name = "FILE_PATH")
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Basic
    @Column(name = "CREATION_TIME")
    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    @Basic
    @Column(name = "LAST_MODIFIED_TIME")
    public Timestamp getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(Timestamp lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    @Basic
    @Column(name = "VALID_UNTIL_TIME")
    public Timestamp getValidUntilTime() {
        return validUntilTime;
    }

    public void setValidUntilTime(Timestamp validUntilTime) {
        this.validUntilTime = validUntilTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataReplicaLocationEntity that = (DataReplicaLocationEntity) o;

        if (replicaId != null ? !replicaId.equals(that.replicaId) : that.replicaId != null) return false;
        if (replicaName != null ? !replicaName.equals(that.replicaName) : that.replicaName != null) return false;
        if (replicaDescription != null ? !replicaDescription.equals(that.replicaDescription) : that.replicaDescription != null)
            return false;
        if (storageResourceId != null ? !storageResourceId.equals(that.storageResourceId) : that.storageResourceId != null)
            return false;
        if (filePath != null ? !filePath.equals(that.filePath) : that.filePath != null) return false;
        if (creationTime != null ? !creationTime.equals(that.creationTime) : that.creationTime != null) return false;
        if (lastModifiedTime != null ? !lastModifiedTime.equals(that.lastModifiedTime) : that.lastModifiedTime != null)
            return false;
        if (validUntilTime != null ? !validUntilTime.equals(that.validUntilTime) : that.validUntilTime != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = replicaId != null ? replicaId.hashCode() : 0;
        result = 31 * result + (replicaName != null ? replicaName.hashCode() : 0);
        result = 31 * result + (replicaDescription != null ? replicaDescription.hashCode() : 0);
        result = 31 * result + (storageResourceId != null ? storageResourceId.hashCode() : 0);
        result = 31 * result + (filePath != null ? filePath.hashCode() : 0);
        result = 31 * result + (creationTime != null ? creationTime.hashCode() : 0);
        result = 31 * result + (lastModifiedTime != null ? lastModifiedTime.hashCode() : 0);
        result = 31 * result + (validUntilTime != null ? validUntilTime.hashCode() : 0);
        return result;
    }
}
