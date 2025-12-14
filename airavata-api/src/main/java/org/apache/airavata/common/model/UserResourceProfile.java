/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.common.model;

import java.util.List;
import java.util.Objects;

/**
 * Domain model: UserResourceProfile
 */
public class UserResourceProfile {
    private String userId;
    private String gatewayID;
    private String credentialStoreToken;
    private List<UserComputeResourcePreference> userComputeResourcePreferences;
    private List<UserStoragePreference> userStoragePreferences;
    private String identityServerTenant;
    private String identityServerPwdCredToken;

    public UserResourceProfile() {}

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGatewayID() {
        return gatewayID;
    }

    public void setGatewayID(String gatewayID) {
        this.gatewayID = gatewayID;
    }

    public String getCredentialStoreToken() {
        return credentialStoreToken;
    }

    public void setCredentialStoreToken(String credentialStoreToken) {
        this.credentialStoreToken = credentialStoreToken;
    }

    public List<UserComputeResourcePreference> getUserComputeResourcePreferences() {
        return userComputeResourcePreferences;
    }

    public void setUserComputeResourcePreferences(List<UserComputeResourcePreference> userComputeResourcePreferences) {
        this.userComputeResourcePreferences = userComputeResourcePreferences;
    }

    public List<UserStoragePreference> getUserStoragePreferences() {
        return userStoragePreferences;
    }

    public void setUserStoragePreferences(List<UserStoragePreference> userStoragePreferences) {
        this.userStoragePreferences = userStoragePreferences;
    }

    public String getIdentityServerTenant() {
        return identityServerTenant;
    }

    public void setIdentityServerTenant(String identityServerTenant) {
        this.identityServerTenant = identityServerTenant;
    }

    public String getIdentityServerPwdCredToken() {
        return identityServerPwdCredToken;
    }

    public void setIdentityServerPwdCredToken(String identityServerPwdCredToken) {
        this.identityServerPwdCredToken = identityServerPwdCredToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserResourceProfile that = (UserResourceProfile) o;
        return Objects.equals(userId, that.userId)
                && Objects.equals(gatewayID, that.gatewayID)
                && Objects.equals(credentialStoreToken, that.credentialStoreToken)
                && Objects.equals(userComputeResourcePreferences, that.userComputeResourcePreferences)
                && Objects.equals(userStoragePreferences, that.userStoragePreferences)
                && Objects.equals(identityServerTenant, that.identityServerTenant)
                && Objects.equals(identityServerPwdCredToken, that.identityServerPwdCredToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                userId,
                gatewayID,
                credentialStoreToken,
                userComputeResourcePreferences,
                userStoragePreferences,
                identityServerTenant,
                identityServerPwdCredToken);
    }

    @Override
    public String toString() {
        return "UserResourceProfile{" + "userId=" + userId + ", gatewayID=" + gatewayID + ", credentialStoreToken="
                + credentialStoreToken + ", userComputeResourcePreferences=" + userComputeResourcePreferences
                + ", userStoragePreferences=" + userStoragePreferences + ", identityServerTenant="
                + identityServerTenant + ", identityServerPwdCredToken=" + identityServerPwdCredToken + "}";
    }
}
