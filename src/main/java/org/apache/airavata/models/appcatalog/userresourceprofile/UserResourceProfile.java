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
package org.apache.airavata.models.appcatalog.userresourceprofile;

import java.util.ArrayList;
import java.util.List;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserResourceProfile}.
 */
public record UserResourceProfile(
        String userId,
        String gatewayId,
        String credentialStoreToken,
        List<UserComputeResourcePreference> userComputeResourcePreferences,
        List<UserStoragePreference> userStoragePreferences,
        String identityServerTenant,
        String identityServerPwdCredToken) {

    public UserResourceProfile {
        userComputeResourcePreferences =
                userComputeResourcePreferences == null ? List.of() : List.copyOf(userComputeResourcePreferences);
        userStoragePreferences = userStoragePreferences == null ? List.of() : List.copyOf(userStoragePreferences);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String userId = "";
        private String gatewayId = "";
        private String credentialStoreToken = "";
        private List<UserComputeResourcePreference> userComputeResourcePreferences = new ArrayList<>();
        private List<UserStoragePreference> userStoragePreferences = new ArrayList<>();
        private String identityServerTenant = "";
        private String identityServerPwdCredToken = "";

        private Builder() {}

        private Builder(UserResourceProfile source) {
            this.userId = source.userId;
            this.gatewayId = source.gatewayId;
            this.credentialStoreToken = source.credentialStoreToken;
            this.userComputeResourcePreferences = new ArrayList<>(source.userComputeResourcePreferences);
            this.userStoragePreferences = new ArrayList<>(source.userStoragePreferences);
            this.identityServerTenant = source.identityServerTenant;
            this.identityServerPwdCredToken = source.identityServerPwdCredToken;
        }

        public Builder setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder setGatewayId(String gatewayId) {
            this.gatewayId = gatewayId;
            return this;
        }

        public Builder setCredentialStoreToken(String credentialStoreToken) {
            this.credentialStoreToken = credentialStoreToken;
            return this;
        }

        public Builder addUserComputeResourcePreferences(UserComputeResourcePreference value) {
            this.userComputeResourcePreferences.add(value);
            return this;
        }

        public Builder addAllUserComputeResourcePreferences(Iterable<UserComputeResourcePreference> values) {
            values.forEach(this.userComputeResourcePreferences::add);
            return this;
        }

        public Builder clearUserComputeResourcePreferences() {
            this.userComputeResourcePreferences.clear();
            return this;
        }

        public Builder removeUserComputeResourcePreferences(int index) {
            this.userComputeResourcePreferences.remove(index);
            return this;
        }

        public Builder addUserStoragePreferences(UserStoragePreference value) {
            this.userStoragePreferences.add(value);
            return this;
        }

        public Builder addAllUserStoragePreferences(Iterable<UserStoragePreference> values) {
            values.forEach(this.userStoragePreferences::add);
            return this;
        }

        public Builder clearUserStoragePreferences() {
            this.userStoragePreferences.clear();
            return this;
        }

        public Builder removeUserStoragePreferences(int index) {
            this.userStoragePreferences.remove(index);
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

        public UserResourceProfile build() {
            return new UserResourceProfile(
                    userId, gatewayId, credentialStoreToken, userComputeResourcePreferences,
                    userStoragePreferences, identityServerTenant, identityServerPwdCredToken);
        }
    }
}
