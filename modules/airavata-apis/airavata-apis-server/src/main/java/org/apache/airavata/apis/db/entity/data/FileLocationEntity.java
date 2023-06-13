package org.apache.airavata.apis.db.entity.data;

import org.apache.airavata.apis.db.entity.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class FileLocationEntity extends BaseEntity {

    @Column
    private String storageId;
    @Column
    private String path;
    @Column
    private String storageCredentialId;


    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getStorageCredentialId() {
        return storageCredentialId;
    }

    public void setStorageCredentialId(String storageCredentialId) {
        this.storageCredentialId = storageCredentialId;
    }

}
