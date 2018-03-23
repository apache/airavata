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

/**
 * The persistent class for the user_storage_preference database table.
 */
@Entity
@Table(name = "USER_STORAGE_PREFERENCE")
@IdClass(UserStoragePreferencePK.class)
public class UserStoragePreferenceEntity {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "STORAGE_RESOURCE_ID")
    private String storageResourceId;

    @Id
    @Column(name = "USER_ID")
    private String userId;

    @Id
    @Column(name = "GATEWAY_ID")
    private String gatewayId;

    @Column(name = "RESOURCE_CS_TOKEN")
    private String resourceSpecificCredentialStoreToken;

    @Column(name = "FS_ROOT_LOCATION")
    private String fileSystemRootLocation;

    @Column(name = "LOGIN_USERNAME")
    private String loginUserName;

    @ManyToOne(targetEntity = UserResourceProfileEntity.class, cascade = CascadeType.MERGE)
    @JoinColumns({
            @JoinColumn(name = "USER_ID"),
            @JoinColumn(name="GATEWAY_ID")
    })
    private UserResourceProfileEntity userResourceProfile;

    public UserStoragePreferenceEntity() {
    }

    public String getStorageResourceId() {
        return storageResourceId;
    }

    public void setStorageResourceId(String storageResourceId) {
        this.storageResourceId = storageResourceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getResourceSpecificCredentialStoreToken() {
        return resourceSpecificCredentialStoreToken;
    }

    public void setResourceSpecificCredentialStoreToken(String resourceSpecificCredentialStoreToken) {
        this.resourceSpecificCredentialStoreToken = resourceSpecificCredentialStoreToken;
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

    public UserResourceProfileEntity getUserResourceProfile() {
        return userResourceProfile;
    }

    public void setUserResourceProfile(UserResourceProfileEntity userResourceProfile) {
        this.userResourceProfile = userResourceProfile;
    }

}
