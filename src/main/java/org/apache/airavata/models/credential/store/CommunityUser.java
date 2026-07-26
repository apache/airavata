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
 * {@code org.apache.airavata.model.credential.store.proto.CommunityUser}.
 *
 * <p>Implements {@link Serializable} because instances are persisted at rest via Java
 * serialization inside a {@code StoredCredential} (see {@code CredentialEncryptionUtil}).
 */
public record CommunityUser(String gatewayName, String username, String userEmail) implements Serializable {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String gatewayName = "";
        private String username = "";
        private String userEmail = "";

        private Builder() {}

        private Builder(CommunityUser source) {
            this.gatewayName = source.gatewayName;
            this.username = source.username;
            this.userEmail = source.userEmail;
        }

        public Builder setGatewayName(String gatewayName) {
            this.gatewayName = gatewayName;
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setUserEmail(String userEmail) {
            this.userEmail = userEmail;
            return this;
        }

        public CommunityUser build() {
            return new CommunityUser(gatewayName, username, userEmail);
        }
    }
}
