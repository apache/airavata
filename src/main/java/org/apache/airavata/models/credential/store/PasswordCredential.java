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
package org.apache.airavata.models.credential.store;

import java.io.Serializable;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.credential.store.proto.PasswordCredential}.
 *
 * <p>Implements {@link Serializable} because instances are persisted at rest via Java
 * serialization inside a {@code StoredCredential} (see {@code CredentialEncryptionUtil}).
 */
public record PasswordCredential(
        String gatewayId,
        String portalUserName,
        String loginUserName,
        String password,
        String description,
        long persistedTime,
        String token)
        implements Serializable {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String gatewayId = "";
        private String portalUserName = "";
        private String loginUserName = "";
        private String password = "";
        private String description = "";
        private long persistedTime;
        private String token = "";

        private Builder() {}

        private Builder(PasswordCredential source) {
            this.gatewayId = source.gatewayId;
            this.portalUserName = source.portalUserName;
            this.loginUserName = source.loginUserName;
            this.password = source.password;
            this.description = source.description;
            this.persistedTime = source.persistedTime;
            this.token = source.token;
        }

        public Builder setGatewayId(String gatewayId) {
            this.gatewayId = gatewayId;
            return this;
        }

        public Builder setPortalUserName(String portalUserName) {
            this.portalUserName = portalUserName;
            return this;
        }

        public Builder setLoginUserName(String loginUserName) {
            this.loginUserName = loginUserName;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setPersistedTime(long persistedTime) {
            this.persistedTime = persistedTime;
            return this;
        }

        public Builder setToken(String token) {
            this.token = token;
            return this;
        }

        public PasswordCredential build() {
            return new PasswordCredential(
                    gatewayId, portalUserName, loginUserName, password, description, persistedTime, token);
        }
    }
}
