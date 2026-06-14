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
package org.apache.airavata.server.grpc.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.apache.airavata.config.Constants;
import org.apache.airavata.model.security.proto.AuthzToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared parsing of the Bearer access token and the {@code x-claims} header used by both the gRPC
 * ({@link GrpcAuthInterceptor}) and HTTP ({@link HttpAuthDecorator}) authentication paths. Transport-specific
 * concerns (rejecting missing tokens, per-transport header fallbacks, UserContext lifecycle) stay in the callers.
 */
public final class AuthTokenExtractor {

    private static final Logger log = LoggerFactory.getLogger(AuthTokenExtractor.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private AuthTokenExtractor() {}

    /** Returns the bearer access token, or {@code null} if the header is absent or not a Bearer token. */
    public static String stripBearer(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /** Parses the {@code x-claims} JSON header into a mutable claims map; empty on absence or parse failure. */
    public static Map<String, String> parseClaims(String claimsHeader) {
        if (claimsHeader != null && !claimsHeader.isBlank()) {
            try {
                return objectMapper.readValue(claimsHeader, new TypeReference<Map<String, String>>() {});
            } catch (Exception e) {
                log.warn("Failed to parse x-claims: {}", e.getMessage());
            }
        }
        return new HashMap<>();
    }

    /**
     * Builds an AuthzToken from the access token and (possibly augmented) claims map. The caller's realm roles
     * are derived from the verified access token (not the client-asserted {@code x-claims}) and written into the
     * claims map under {@link Constants#REALM_ROLES} as a CSV, overwriting any client-supplied value.
     */
    public static AuthzToken buildAuthzToken(String accessToken, Map<String, String> claimsMap) {
        claimsMap.put(Constants.REALM_ROLES, String.join(",", JwtVerifier.verifyAndExtractRoles(accessToken)));
        return AuthzToken.newBuilder()
                .setAccessToken(accessToken)
                .putAllClaimsMap(claimsMap)
                .build();
    }
}
