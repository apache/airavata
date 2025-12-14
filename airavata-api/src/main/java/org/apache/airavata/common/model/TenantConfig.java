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
package org.apache.airavata.common.model;

import java.util.Objects;

/**
 * Domain model: TenantConfig
 */
public class TenantConfig {
    private String oauthClientId;
    private String oauthClientSecret;
    private String identityServerUserName;
    private String identityServerPasswordToken;

    public TenantConfig() {}

    public String getOauthClientId() {
        return oauthClientId;
    }

    public void setOauthClientId(String oauthClientId) {
        this.oauthClientId = oauthClientId;
    }

    public String getOauthClientSecret() {
        return oauthClientSecret;
    }

    public void setOauthClientSecret(String oauthClientSecret) {
        this.oauthClientSecret = oauthClientSecret;
    }

    public String getIdentityServerUserName() {
        return identityServerUserName;
    }

    public void setIdentityServerUserName(String identityServerUserName) {
        this.identityServerUserName = identityServerUserName;
    }

    public String getIdentityServerPasswordToken() {
        return identityServerPasswordToken;
    }

    public void setIdentityServerPasswordToken(String identityServerPasswordToken) {
        this.identityServerPasswordToken = identityServerPasswordToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TenantConfig that = (TenantConfig) o;
        return Objects.equals(oauthClientId, that.oauthClientId)
                && Objects.equals(oauthClientSecret, that.oauthClientSecret)
                && Objects.equals(identityServerUserName, that.identityServerUserName)
                && Objects.equals(identityServerPasswordToken, that.identityServerPasswordToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oauthClientId, oauthClientSecret, identityServerUserName, identityServerPasswordToken);
    }

    @Override
    public String toString() {
        return "TenantConfig{" + "oauthClientId=" + oauthClientId + ", oauthClientSecret=" + oauthClientSecret
                + ", identityServerUserName=" + identityServerUserName + ", identityServerPasswordToken="
                + identityServerPasswordToken + "}";
    }
}
