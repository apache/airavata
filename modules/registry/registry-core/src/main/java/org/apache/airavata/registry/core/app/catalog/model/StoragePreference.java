/**
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
 */
package org.apache.airavata.registry.core.app.catalog.model;


import javax.persistence.*;

@Entity
@Table(name = "STORAGE_PREFERENCE")
@IdClass(StoragePreferencePK.class)
public class StoragePreference {
    @Id
    @Column(name = "GATEWAY_ID")
    private String gatewayId;
    @Id
    @Column(name = "STORAGE_RESOURCE_ID")
    private String storageResourceId;
    @Column(name = "LOGIN_USERNAME")
    private String loginUserName;
    @Column(name = "FS_ROOT_LOCATION")
    private String fsRootLocation;
    @Column(name = "RESOURCE_CS_TOKEN")
    private String computeResourceCSToken;

    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "GATEWAY_ID")
    private GatewayProfile gatewayProfile;

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public GatewayProfile getGatewayProfile() {
        return gatewayProfile;
    }

    public void setGatewayProfile(GatewayProfile gatewayProfile) {
        this.gatewayProfile = gatewayProfile;
    }

    public String getLoginUserName() {
        return loginUserName;
    }

    public void setLoginUserName(String loginUserName) {
        this.loginUserName = loginUserName;
    }

    public String getComputeResourceCSToken() {
        return computeResourceCSToken;
    }

    public void setComputeResourceCSToken(String computeResourceCSToken) {
        this.computeResourceCSToken = computeResourceCSToken;
    }

    public String getFsRootLocation() {
        return fsRootLocation;
    }

    public void setFsRootLocation(String fsRootLocation) {
        this.fsRootLocation = fsRootLocation;
    }

    public String getStorageResourceId() {
        return storageResourceId;
    }

    public void setStorageResourceId(String storageResourceId) {
        this.storageResourceId = storageResourceId;
    }
}
