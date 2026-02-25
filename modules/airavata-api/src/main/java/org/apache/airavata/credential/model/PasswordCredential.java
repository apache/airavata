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

import java.util.Date;
import java.util.Objects;

/**
 * Password-based credential for authenticating to resources or IAM/AWS.
 * For compute/storage resource login, use RESOURCE_ACCESS.loginUsername (per assignment).
 * This field is used for IAM/Keycloak admin and AWS access key ID when stored in gateway config.
 */
public final class PasswordCredential implements Credential {
    private static final long serialVersionUID = 2L;

    // Base credential fields
    private String userId;
    private Date persistedTime;
    private String token;
    private String gatewayId;
    private String name;
    private String description;

    // Password-specific fields
    /** Used for IAM/Keycloak username or AWS access key ID; not for resource login (use RESOURCE_ACCESS). */
    private String loginUserName;

    private String password;

    public PasswordCredential() {}

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public Date getPersistedTime() {
        return persistedTime;
    }

    @Override
    public void setPersistedTime(Date persistedTime) {
        this.persistedTime = persistedTime;
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

    public String getLoginUserName() {
        return loginUserName;
    }

    public void setLoginUserName(String loginUserName) {
        this.loginUserName = loginUserName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PasswordCredential that = (PasswordCredential) o;
        return Objects.equals(gatewayId, that.gatewayId)
                && Objects.equals(userId, that.userId)
                && Objects.equals(password, that.password)
                && Objects.equals(description, that.description)
                && Objects.equals(persistedTime, that.persistedTime)
                && Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gatewayId, userId, loginUserName, password, description, persistedTime, token);
    }

    @Override
    public String toString() {
        return "PasswordCredential{"
                + "gatewayId='" + gatewayId + '\''
                + ", userId='" + userId + '\''
                + ", loginUserName='" + loginUserName + '\''
                + ", password='" + (password != null ? "***" : null) + '\''
                + ", description='" + description + '\''
                + ", persistedTime=" + persistedTime
                + ", token='" + token + '\''
                + '}';
    }
}
