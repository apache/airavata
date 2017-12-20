/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package org.apache.airavata.registry.core.entities.appcatalog;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * The persistent class for the data_storage_preference database table.
 */
@Entity
@Table(name = "STORAGE_PREFERENCE")
public class StoragePreferenceEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private StoragePreferencePK id;

    @Column(name = "FS_ROOT_LOCATION")
    private String fsRootLocation;

    @Column(name = "LOGIN_USERNAME")
    private String loginUsername;

    @Column(name = "RESOURCE_CS_TOKEN")
    private String resourceCsToken;

    public StoragePreferenceEntity() {
    }

    public StoragePreferencePK getId() {
        return id;
    }

    public void setId(StoragePreferencePK id) {
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