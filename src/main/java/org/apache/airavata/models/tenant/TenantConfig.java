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
package org.apache.airavata.models.tenant;

/**
 * Plain-POJO replacement for the generated {@code org.apache.airavata.model.tenant.proto.TenantConfig}.
 */
public record TenantConfig(
        String oauthClientId, String oauthClientSecret, String identityServerUserName,
        String identityServerPasswordToken) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String oauthClientId = "";
        private String oauthClientSecret = "";
        private String identityServerUserName = "";
        private String identityServerPasswordToken = "";

        private Builder() {}

        private Builder(TenantConfig source) {
            this.oauthClientId = source.oauthClientId;
            this.oauthClientSecret = source.oauthClientSecret;
            this.identityServerUserName = source.identityServerUserName;
            this.identityServerPasswordToken = source.identityServerPasswordToken;
        }

        public Builder setOauthClientId(String oauthClientId) {
            this.oauthClientId = oauthClientId;
            return this;
        }

        public Builder setOauthClientSecret(String oauthClientSecret) {
            this.oauthClientSecret = oauthClientSecret;
            return this;
        }

        public Builder setIdentityServerUserName(String identityServerUserName) {
            this.identityServerUserName = identityServerUserName;
            return this;
        }

        public Builder setIdentityServerPasswordToken(String identityServerPasswordToken) {
            this.identityServerPasswordToken = identityServerPasswordToken;
            return this;
        }

        public TenantConfig build() {
            return new TenantConfig(
                    oauthClientId, oauthClientSecret, identityServerUserName, identityServerPasswordToken);
        }
    }
}
