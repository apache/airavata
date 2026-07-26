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
 * {@code org.apache.airavata.model.credential.store.proto.SSHCredential}.
 *
 * <p>Implements {@link Serializable} because instances are persisted at rest via Java
 * serialization inside a {@code StoredCredential} (see {@code CredentialEncryptionUtil}).
 */
public record SSHCredential(
        String gatewayId,
        String username,
        String passphrase,
        String publicKey,
        String privateKey,
        long persistedTime,
        String token,
        String description)
        implements Serializable {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String gatewayId = "";
        private String username = "";
        private String passphrase = "";
        private String publicKey = "";
        private String privateKey = "";
        private long persistedTime;
        private String token = "";
        private String description = "";

        private Builder() {}

        private Builder(SSHCredential source) {
            this.gatewayId = source.gatewayId;
            this.username = source.username;
            this.passphrase = source.passphrase;
            this.publicKey = source.publicKey;
            this.privateKey = source.privateKey;
            this.persistedTime = source.persistedTime;
            this.token = source.token;
            this.description = source.description;
        }

        public Builder setGatewayId(String gatewayId) {
            this.gatewayId = gatewayId;
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setPassphrase(String passphrase) {
            this.passphrase = passphrase;
            return this;
        }

        public Builder setPublicKey(String publicKey) {
            this.publicKey = publicKey;
            return this;
        }

        public Builder setPrivateKey(String privateKey) {
            this.privateKey = privateKey;
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

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public SSHCredential build() {
            return new SSHCredential(
                    gatewayId, username, passphrase, publicKey, privateKey, persistedTime, token, description);
        }
    }
}
