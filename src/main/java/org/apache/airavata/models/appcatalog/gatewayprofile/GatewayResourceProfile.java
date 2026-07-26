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
package org.apache.airavata.models.appcatalog.gatewayprofile;

import java.util.ArrayList;
import java.util.List;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.appcatalog.gatewayprofile.proto.GatewayResourceProfile}.
 */
public record GatewayResourceProfile(
        String gatewayId,
        String credentialStoreToken,
        List<ComputeResourcePreference> computeResourcePreferences,
        List<StoragePreference> storagePreferences,
        String identityServerTenant,
        String identityServerPwdCredToken) {

    public GatewayResourceProfile {
        computeResourcePreferences =
                computeResourcePreferences == null ? List.of() : List.copyOf(computeResourcePreferences);
        storagePreferences = storagePreferences == null ? List.of() : List.copyOf(storagePreferences);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String gatewayId = "";
        private String credentialStoreToken = "";
        private List<ComputeResourcePreference> computeResourcePreferences = new ArrayList<>();
        private List<StoragePreference> storagePreferences = new ArrayList<>();
        private String identityServerTenant = "";
        private String identityServerPwdCredToken = "";

        private Builder() {}

        private Builder(GatewayResourceProfile source) {
            this.gatewayId = source.gatewayId;
            this.credentialStoreToken = source.credentialStoreToken;
            this.computeResourcePreferences = new ArrayList<>(source.computeResourcePreferences);
            this.storagePreferences = new ArrayList<>(source.storagePreferences);
            this.identityServerTenant = source.identityServerTenant;
            this.identityServerPwdCredToken = source.identityServerPwdCredToken;
        }

        public Builder setGatewayId(String gatewayId) {
            this.gatewayId = gatewayId;
            return this;
        }

        public Builder setCredentialStoreToken(String credentialStoreToken) {
            this.credentialStoreToken = credentialStoreToken;
            return this;
        }

        public Builder addComputeResourcePreferences(ComputeResourcePreference value) {
            this.computeResourcePreferences.add(value);
            return this;
        }

        public Builder addAllComputeResourcePreferences(Iterable<ComputeResourcePreference> values) {
            values.forEach(this.computeResourcePreferences::add);
            return this;
        }

        public Builder removeComputeResourcePreferences(int index) {
            this.computeResourcePreferences.remove(index);
            return this;
        }

        public Builder clearComputeResourcePreferences() {
            this.computeResourcePreferences.clear();
            return this;
        }

        public Builder addStoragePreferences(StoragePreference value) {
            this.storagePreferences.add(value);
            return this;
        }

        public Builder addAllStoragePreferences(Iterable<StoragePreference> values) {
            values.forEach(this.storagePreferences::add);
            return this;
        }

        public Builder clearStoragePreferences() {
            this.storagePreferences.clear();
            return this;
        }

        public Builder setIdentityServerTenant(String identityServerTenant) {
            this.identityServerTenant = identityServerTenant;
            return this;
        }

        public Builder setIdentityServerPwdCredToken(String identityServerPwdCredToken) {
            this.identityServerPwdCredToken = identityServerPwdCredToken;
            return this;
        }

        public GatewayResourceProfile build() {
            return new GatewayResourceProfile(
                    gatewayId, credentialStoreToken, computeResourcePreferences, storagePreferences,
                    identityServerTenant, identityServerPwdCredToken);
        }
    }
}
