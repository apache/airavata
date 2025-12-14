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
 * Domain model: PasswordCredential
 */
public class PasswordCredential {
    private String gatewayId;
    private String portalUserName;
    private String loginUserName;
    private String password;
    private String description;
    private Long persistedTime;
    private String token;

    public PasswordCredential() {}

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getPortalUserName() {
        return portalUserName;
    }

    public void setPortalUserName(String portalUserName) {
        this.portalUserName = portalUserName;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PasswordCredential that = (PasswordCredential) o;
        return Objects.equals(gatewayId, that.gatewayId)
                && Objects.equals(portalUserName, that.portalUserName)
                && Objects.equals(loginUserName, that.loginUserName)
                && Objects.equals(password, that.password)
                && Objects.equals(description, that.description)
                && Objects.equals(persistedTime, that.persistedTime)
                && Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gatewayId, portalUserName, loginUserName, password, description, persistedTime, token);
    }

    @Override
    public String toString() {
        return "PasswordCredential{" + "gatewayId='"
                + gatewayId + '\'' + ", portalUserName='"
                + portalUserName + '\'' + ", loginUserName='"
                + loginUserName + '\'' + ", password='"
                + (password != null ? "***" : null) + '\'' + ", description='"
                + description + '\'' + ", persistedTime="
                + persistedTime + ", token='"
                + token + '\'' + '}';
    }
}
