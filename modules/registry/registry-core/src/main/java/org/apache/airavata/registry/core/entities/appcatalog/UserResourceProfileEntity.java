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

import java.util.*;
import javax.persistence.*;
import java.sql.Timestamp;

/**
 * The persistent class for the user_resource_profile database table.
 */
@Entity
@Table(name = "USER_RESOURCE_PROFILE")
@IdClass(UserResourceProfilePK.class)
public class UserResourceProfileEntity {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "USER_ID")
    private String userId;

    @Id
    @Column(name = "GATEWAY_ID")
    private String gatewayId;

    @Column(name = "CS_TOKEN")
    private String credentialStoreToken;

    @Column(name = "IDENTITY_SERVER_PWD_CRED_TOKEN")
    private String identityServerPwdCredToken;

    @Column(name = "IDENTITY_SERVER_TENANT")
    private String identityServerTenant;

    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;

    @Column(name = "UPDATE_TIME")
    private Timestamp updateTime;

    @OneToMany (targetEntity = UserComputeResourcePreferenceEntity.class, cascade = CascadeType.ALL,
            mappedBy = "userResourceProfile", fetch = FetchType.EAGER)
    private List<UserComputeResourcePreferenceEntity> userComputeResourcePreferences;

    @OneToMany (targetEntity = UserStoragePreferenceEntity.class, cascade = CascadeType.ALL,
            mappedBy = "userResourceProfile", fetch = FetchType.EAGER)
    private List<UserStoragePreferenceEntity> userStoragePreferences;

    public UserResourceProfileEntity() {
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

    public String getCredentialStoreToken() {
        return credentialStoreToken;
    }

    public void setCredentialStoreToken(String credentialStoreToken) {
        this.credentialStoreToken = credentialStoreToken;
    }

    public String getIdentityServerPwdCredToken() {
        return identityServerPwdCredToken;
    }

    public void setIdentityServerPwdCredToken(String identityServerPwdCredToken) {
        this.identityServerPwdCredToken = identityServerPwdCredToken;
    }

    public String getIdentityServerTenant() {
        return identityServerTenant;
    }

    public void setIdentityServerTenant(String identityServerTenant) {
        this.identityServerTenant = identityServerTenant;
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

    public List<UserComputeResourcePreferenceEntity> getUserComputeResourcePreferences() {
        return userComputeResourcePreferences;
    }

    public void setUserComputeResourcePreferences(List<UserComputeResourcePreferenceEntity> userComputeResourcePreferences) {
        this.userComputeResourcePreferences = userComputeResourcePreferences;
    }

    public List<UserStoragePreferenceEntity> getUserStoragePreferences() {
        return userStoragePreferences;
    }

    public void setUserStoragePreferences(List<UserStoragePreferenceEntity> userStoragePreferences) {
        this.userStoragePreferences = userStoragePreferences;
    }

}
