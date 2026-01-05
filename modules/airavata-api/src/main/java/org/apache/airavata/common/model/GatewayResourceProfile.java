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
 * Domain model: GatewayResourceProfile
 */
public class GatewayResourceProfile {
    private String gatewayID;
    private String credentialStoreToken;
    private List<ComputeResourcePreference> computeResourcePreferences;
    private List<StoragePreference> storagePreferences;
    private String identityServerTenant;
    private String identityServerPwdCredToken;

    public GatewayResourceProfile() {}

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

    public List<ComputeResourcePreference> getComputeResourcePreferences() {
        return computeResourcePreferences;
    }

    public void setComputeResourcePreferences(List<ComputeResourcePreference> computeResourcePreferences) {
        this.computeResourcePreferences = computeResourcePreferences;
    }

    public List<StoragePreference> getStoragePreferences() {
        return storagePreferences;
    }

    public void setStoragePreferences(List<StoragePreference> storagePreferences) {
        this.storagePreferences = storagePreferences;
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
        GatewayResourceProfile that = (GatewayResourceProfile) o;
        return Objects.equals(gatewayID, that.gatewayID)
                && Objects.equals(credentialStoreToken, that.credentialStoreToken)
                && Objects.equals(computeResourcePreferences, that.computeResourcePreferences)
                && Objects.equals(storagePreferences, that.storagePreferences)
                && Objects.equals(identityServerTenant, that.identityServerTenant)
                && Objects.equals(identityServerPwdCredToken, that.identityServerPwdCredToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                gatewayID,
                credentialStoreToken,
                computeResourcePreferences,
                storagePreferences,
                identityServerTenant,
                identityServerPwdCredToken);
    }

    @Override
    public String toString() {
        return "GatewayResourceProfile{" + "gatewayID=" + gatewayID + ", credentialStoreToken=" + credentialStoreToken
                + ", computeResourcePreferences=" + computeResourcePreferences + ", storagePreferences="
                + storagePreferences + ", identityServerTenant=" + identityServerTenant
                + ", identityServerPwdCredToken=" + identityServerPwdCredToken + "}";
    }
}
