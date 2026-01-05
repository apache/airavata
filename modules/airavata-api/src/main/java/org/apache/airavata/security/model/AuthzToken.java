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
package org.apache.airavata.security.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Domain model: AuthzToken
 * Represents an authorization token with access token and claims map.
 */
public class AuthzToken {
    private String accessToken;
    private Map<String, String> claimsMap;

    public AuthzToken() {
        this.claimsMap = new HashMap<>();
    }

    public AuthzToken(String accessToken) {
        this();
        this.accessToken = accessToken;
    }

    public AuthzToken(String accessToken, Map<String, String> claimsMap) {
        this.accessToken = accessToken;
        this.claimsMap = claimsMap != null ? claimsMap : new HashMap<>();
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Map<String, String> getClaimsMap() {
        if (claimsMap == null) {
            claimsMap = new HashMap<>();
        }
        return claimsMap;
    }

    public void setClaimsMap(Map<String, String> claimsMap) {
        this.claimsMap = claimsMap != null ? claimsMap : new HashMap<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthzToken that = (AuthzToken) o;
        return Objects.equals(accessToken, that.accessToken) && Objects.equals(claimsMap, that.claimsMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessToken, claimsMap);
    }

    @Override
    public String toString() {
        return "AuthzToken{" + "accessToken='" + accessToken + '\'' + ", claimsMap=" + claimsMap + '}';
    }
}
