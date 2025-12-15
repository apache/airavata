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

import org.apache.airavata.credential.Credential;

/**
 * Domain model: SSHCredential
 */
public class SSHCredential extends Credential {
    private String gatewayId;
    private String username;
    private String passphrase;
    private String publicKey;
    private String privateKey;
    private Long persistedTime;
    private String token;
    private String description;

    public SSHCredential() {}

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public Long getPersistedTime() {
        return persistedTime;
    }

    public void setPersistedTime(Long persistedTime) {
        this.persistedTime = persistedTime;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SSHCredential that = (SSHCredential) o;
        return Objects.equals(gatewayId, that.gatewayId)
                && Objects.equals(username, that.username)
                && Objects.equals(passphrase, that.passphrase)
                && Objects.equals(publicKey, that.publicKey)
                && Objects.equals(privateKey, that.privateKey)
                && Objects.equals(persistedTime, that.persistedTime)
                && Objects.equals(token, that.token)
                && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gatewayId, username, passphrase, publicKey, privateKey, persistedTime, token, description);
    }

    @Override
    public String toString() {
        return "SSHCredential{" + "gatewayId='"
                + gatewayId + '\'' + ", username='"
                + username + '\'' + ", passphrase='"
                + (passphrase != null ? "***" : null) + '\'' + ", publicKey='"
                + (publicKey != null ? "***" : null) + '\'' + ", privateKey='"
                + (privateKey != null ? "***" : null) + '\'' + ", persistedTime="
                + persistedTime + ", token='"
                + token + '\'' + ", description='"
                + description + '\'' + '}';
    }
}
