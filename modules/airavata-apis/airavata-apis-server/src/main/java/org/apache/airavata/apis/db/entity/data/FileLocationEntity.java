package org.apache.airavata.apis.db.entity.data;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class FileLocationEntity {

    @Id
    @Column(name = "FILE_LOCATION_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String fileLocationId;
    @Column
    private String storageId;
    @Column
    private String path;
    @Column
    private String storageCredentialId;

    public String getFileLocationId() {
        return fileLocationId;
    }

    public void setFileLocationId(String fileLocationId) {
        this.fileLocationId = fileLocationId;
    }

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fileLocationId == null) ? 0 : fileLocationId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FileLocationEntity other = (FileLocationEntity) obj;
        if (fileLocationId == null) {
            if (other.fileLocationId != null)
                return false;
        } else if (!fileLocationId.equals(other.fileLocationId))
            return false;
        return true;
    }

}
