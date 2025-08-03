package org.apache.airavata.research.service.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "STORAGE_RESOURCE")
public class StorageResourceEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "STORAGE_RESOURCE_ID")
    private String storageResourceId;

    @Column(name = "HOST_NAME", nullable = false)
    private String hostName;
    

    @Column(name = "DESCRIPTION", length = 2048)
    private String description;

    @Column(name = "ENABLED")
    private Short enabled;

    @Column(name = "CREATION_TIME", nullable = false)
    private Timestamp creationTime;

    @Column(name = "UPDATE_TIME", nullable = false)
    private Timestamp updateTime;

    public String getStorageResourceId() {
        return storageResourceId;
    }

    public void setStorageResourceId(String storageResourceId) {
        this.storageResourceId = storageResourceId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
    

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Short getEnabled() {
        return enabled;
    }

    public void setEnabled(Short enabled) {
        this.enabled = enabled;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}