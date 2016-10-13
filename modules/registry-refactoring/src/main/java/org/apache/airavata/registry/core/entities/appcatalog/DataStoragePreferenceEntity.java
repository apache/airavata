package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The persistent class for the data_storage_preference database table.
 */
@Entity
@Table(name = "data_storage_preference")
public class DataStoragePreference implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private DataStoragePreferencePK id;

    @Column(name = "FS_ROOT_LOCATION")
    private String fsRootLocation;

    @Column(name = "LOGIN_USERNAME")
    private String loginUsername;

    @Column(name = "RESOURCE_CS_TOKEN")
    private String resourceCsToken;

    public DataStoragePreference() {
    }

    public DataStoragePreferencePK getId() {
        return id;
    }

    public void setId(DataStoragePreferencePK id) {
        this.id = id;
    }

    public String getFsRootLocation() {
        return fsRootLocation;
    }

    public void setFsRootLocation(String fsRootLocation) {
        this.fsRootLocation = fsRootLocation;
    }

    public String getLoginUsername() {
        return loginUsername;
    }

    public void setLoginUsername(String loginUsername) {
        this.loginUsername = loginUsername;
    }

    public String getResourceCsToken() {
        return resourceCsToken;
    }

    public void setResourceCsToken(String resourceCsToken) {
        this.resourceCsToken = resourceCsToken;
    }
}