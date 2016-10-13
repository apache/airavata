package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

/**
 * The persistent class for the storage_resource database table.
 */
@Entity
@Table(name = "storage_resource")
public class StorageResource implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "STORAGE_RESOURCE_ID")
    private String storageResourceId;

    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;

    private String description;

    private short enabled;

    @Column(name = "HOST_NAME")
    private String hostName;

    @Column(name = "UPDATE_TIME")
    private Timestamp updateTime;

    public StorageResource() {
    }

    public String getStorageResourceId() {
        return storageResourceId;
    }

    public void setStorageResourceId(String storageResourceId) {
        this.storageResourceId = storageResourceId;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public short getEnabled() {
        return enabled;
    }

    public void setEnabled(short enabled) {
        this.enabled = enabled;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}