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
package org.apache.airavata.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RequestContext {

    private final String userId;
    private final String gatewayId;
    private final String accessToken;
    private final Map<String, String> claims;
    private final List<String> roles;

    public RequestContext(String userId, String gatewayId, String accessToken, Map<String, String> claims) {
        this(userId, gatewayId, accessToken, claims, List.of());
    }

    public RequestContext(
            String userId, String gatewayId, String accessToken, Map<String, String> claims, List<String> roles) {
        this.userId = userId;
        this.gatewayId = gatewayId;
        this.accessToken = accessToken;
        this.claims = Collections.unmodifiableMap(claims);
        this.roles = roles == null ? List.of() : List.copyOf(roles);
    }

    public String getUserId() {
        return userId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Map<String, String> getClaims() {
        return claims;
    }

    /** Realm roles from the verified access token. */
    public List<String> getRoles() {
        return roles;
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    /** True when the caller holds the read-write gateway-admin role ({@code admin-rw}). */
    public boolean isGatewayAdmin() {
        return hasRole(Constants.ROLE_GATEWAY_ADMIN);
    }

    /** True when the caller holds the read-only gateway-admin role ({@code admin-ro}). */
    public boolean isReadOnlyGatewayAdmin() {
        return hasRole(Constants.ROLE_READ_ONLY_ADMIN);
    }
}
