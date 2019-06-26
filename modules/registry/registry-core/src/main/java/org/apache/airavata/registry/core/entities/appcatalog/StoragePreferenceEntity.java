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

import javax.persistence.*;
import java.io.Serializable;

/**
 * The persistent class for the storage_preference database table.
 */
@Entity
@Table(name = "STORAGE_PREFERENCE")
@IdClass(StoragePreferencePK.class)
public class StoragePreferenceEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "GATEWAY_ID")
    @Id
    private String gatewayId;

    @Column(name = "STORAGE_RESOURCE_ID")
    @Id
    private String storageResourceId;

    @Column(name = "FS_ROOT_LOCATION")
    private String fileSystemRootLocation;

    @Column(name = "LOGIN_USERNAME")
    private String loginUserName;

    @Column(name = "RESOURCE_CS_TOKEN")
    private String resourceSpecificCredentialStoreToken;

    @ManyToOne(targetEntity = GatewayProfileEntity.class, cascade = CascadeType.MERGE)
    @JoinColumn(name = "GATEWAY_ID")
    private GatewayProfileEntity gatewayProfileResource;

    public StoragePreferenceEntity() {
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getStorageResourceId() {
        return storageResourceId;
    }

    public void setStorageResourceId(String storageResourceId) {
        this.storageResourceId = storageResourceId;
    }

    public String getFileSystemRootLocation() {
        return fileSystemRootLocation;
    }

    public void setFileSystemRootLocation(String fileSystemRootLocation) {
        this.fileSystemRootLocation = fileSystemRootLocation;
    }

    public String getLoginUserName() {
        return loginUserName;
    }

    public void setLoginUserName(String loginUserName) {
        this.loginUserName = loginUserName;
    }

    public String getResourceSpecificCredentialStoreToken() {
        return resourceSpecificCredentialStoreToken;
    }

    public void setResourceSpecificCredentialStoreToken(String resourceSpecificCredentialStoreToken) {
        this.resourceSpecificCredentialStoreToken = resourceSpecificCredentialStoreToken;
    }

    public GatewayProfileEntity getGatewayProfileResource() {
        return gatewayProfileResource;
    }

    public void setGatewayProfileResource(GatewayProfileEntity gatewayProfileResource) {
        this.gatewayProfileResource = gatewayProfileResource;
    }
}
