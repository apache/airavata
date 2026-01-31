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
 * Password-based credential for authenticating to resources or IAM/AWS.
 * For compute/storage resource login, use RESOURCE_ACCESS.loginUsername (per assignment).
 * This field is used for IAM/Keycloak admin and AWS access key ID when stored in gateway config.
 */
public class PasswordCredential extends Credential {
    private static final long serialVersionUID = 1L;

    /** Used for IAM/Keycloak username or AWS access key ID; not for resource login (use RESOURCE_ACCESS). */
    private String loginUserName;

    /**
     * The password for authentication (or AWS secret key).
     */
    private String password;

    public PasswordCredential() {}

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
        return Objects.equals(getGatewayId(), that.getGatewayId())
                && Objects.equals(getUserId(), that.getUserId())
                && Objects.equals(password, that.password)
                && Objects.equals(getDescription(), that.getDescription())
                && Objects.equals(getPersistedTime(), that.getPersistedTime())
                && Objects.equals(getToken(), that.getToken());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGatewayId(), getUserId(), loginUserName, password,
                getDescription(), getPersistedTime(), getToken());
    }

    @Override
    public String toString() {
        return "PasswordCredential{"
                + "gatewayId='" + getGatewayId() + '\''
                + ", userId='" + getUserId() + '\''
                + ", loginUserName='" + loginUserName + '\''
                + ", password='" + (password != null ? "***" : null) + '\''
                + ", description='" + getDescription() + '\''
                + ", persistedTime=" + getPersistedTime()
                + ", token='" + getToken() + '\''
                + '}';
    }
}
