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
package org.apache.airavata.credential.model;

import java.util.Objects;

/**
 * SSH key-based credential for authenticating to resources.
 * Login username is not stored here; it is set per resource via RESOURCE_ACCESS (credential assignment).
 */
public final class SSHCredential implements Credential {
    private static final long serialVersionUID = 2L;

    // Base credential fields
    private String userId;
    private long createdAt;
    private String token;
    private String gatewayId;
    private String name;
    private String description;

    // SSH-specific fields
    private String passphrase;
    private String publicKey;
    private String privateKey;

    public SSHCredential() {}

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public long getCreatedAt() {
        return createdAt;
    }

    @Override
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String getGatewayId() {
        return gatewayId;
    }

    @Override
    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SSHCredential that = (SSHCredential) o;
        return Objects.equals(gatewayId, that.gatewayId)
                && Objects.equals(userId, that.userId)
                && Objects.equals(passphrase, that.passphrase)
                && Objects.equals(publicKey, that.publicKey)
                && Objects.equals(privateKey, that.privateKey)
                && createdAt == that.createdAt
                && Objects.equals(token, that.token)
                && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gatewayId, userId, passphrase, publicKey, privateKey, createdAt, token, description);
    }

    @Override
    public String toString() {
        return "SSHCredential{"
                + "gatewayId='" + gatewayId + '\''
                + ", userId='" + userId + '\''
                + ", passphrase='" + (passphrase != null ? "***" : null) + '\''
                + ", publicKey='" + (publicKey != null ? "***" : null) + '\''
                + ", privateKey='" + (privateKey != null ? "***" : null) + '\''
                + ", createdAt=" + createdAt
                + ", token='" + token + '\''
                + ", description='" + description + '\''
                + '}';
    }
}
