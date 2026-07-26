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
package org.apache.airavata.models.security;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Encapsulates the information that needs to be passed to the API methods in order to
 * authenticate and authorize the users.
 *
 * <p>Plain-POJO replacement for the generated {@code org.apache.airavata.model.security.proto.AuthzToken}.
 */
public record AuthzToken(String accessToken, Map<String, String> claimsMap) {

    public AuthzToken {
        claimsMap = claimsMap == null ? Map.of() : Map.copyOf(claimsMap);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String accessToken = "";
        private Map<String, String> claimsMap = new LinkedHashMap<>();

        private Builder() {}

        private Builder(AuthzToken source) {
            this.accessToken = source.accessToken;
            this.claimsMap = new LinkedHashMap<>(source.claimsMap);
        }

        public Builder setAccessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder putClaimsMap(String key, String value) {
            this.claimsMap.put(key, value);
            return this;
        }

        public Builder putAllClaimsMap(Map<String, String> values) {
            this.claimsMap.putAll(values);
            return this;
        }

        public Builder clearClaimsMap() {
            this.claimsMap.clear();
            return this;
        }

        public AuthzToken build() {
            return new AuthzToken(accessToken, claimsMap);
        }
    }
}
