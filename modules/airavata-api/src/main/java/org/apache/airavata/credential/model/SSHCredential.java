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
 * SSH key-based credential for authenticating to resources.
 */
public class SSHCredential extends Credential {
    private static final long serialVersionUID = 1L;

    /**
     * The login username for the target resource.
     */
    private String username;

    /**
     * The passphrase for the private key.
     */
    private String passphrase;

    /**
     * The public key in OpenSSH format.
     */
    private String publicKey;

    /**
     * The private key in PEM format.
     */
    private String privateKey;

    public SSHCredential() {}

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SSHCredential that = (SSHCredential) o;
        return Objects.equals(getGatewayId(), that.getGatewayId())
                && Objects.equals(getUserId(), that.getUserId())
                && Objects.equals(username, that.username)
                && Objects.equals(passphrase, that.passphrase)
                && Objects.equals(publicKey, that.publicKey)
                && Objects.equals(privateKey, that.privateKey)
                && Objects.equals(getPersistedTime(), that.getPersistedTime())
                && Objects.equals(getToken(), that.getToken())
                && Objects.equals(getDescription(), that.getDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGatewayId(), getUserId(), username, passphrase, publicKey,
                privateKey, getPersistedTime(), getToken(), getDescription());
    }

    @Override
    public String toString() {
        return "SSHCredential{"
                + "gatewayId='" + getGatewayId() + '\''
                + ", userId='" + getUserId() + '\''
                + ", username='" + username + '\''
                + ", passphrase='" + (passphrase != null ? "***" : null) + '\''
                + ", publicKey='" + (publicKey != null ? "***" : null) + '\''
                + ", privateKey='" + (privateKey != null ? "***" : null) + '\''
                + ", persistedTime=" + getPersistedTime()
                + ", token='" + getToken() + '\''
                + ", description='" + getDescription() + '\''
                + '}';
    }
}
