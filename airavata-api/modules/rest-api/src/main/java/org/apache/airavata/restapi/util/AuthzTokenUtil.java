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
package org.apache.airavata.restapi.util;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.apache.airavata.core.util.Constants;
import org.apache.airavata.iam.model.AuthzToken;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

/**
 * Utility for extracting AuthzToken from Spring Security's SecurityContext.
 * The JWT has already been validated by Spring Security's OAuth2 Resource Server filter.
 */
public class AuthzTokenUtil {

    private AuthzTokenUtil() {}

    /**
     * Extract AuthzToken from the SecurityContext's validated JWT.
     * Falls back to the request attribute set by AuthenticationInterceptor.
     */
    public static AuthzToken extractAuthzToken(HttpServletRequest request) {
        // Try SecurityContext first (JWT validated by Spring Security)
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return buildFromJwt(jwtAuth.getToken());
        }

        // Fallback to request attribute (set by AuthenticationInterceptor)
        var fromAttribute = request.getAttribute("authzToken");
        if (fromAttribute instanceof AuthzToken authzToken) {
            return authzToken;
        }

        // No authentication available — return empty token
        return new AuthzToken();
    }

    /**
     * Require authentication — throws 401 if no valid JWT is present.
     */
    public static AuthzToken requireAuthzToken(HttpServletRequest request) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return buildFromJwt(jwtAuth.getToken());
        }

        var fromAttribute = request.getAttribute("authzToken");
        if (fromAttribute instanceof AuthzToken authzToken) {
            return authzToken;
        }

        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Authentication required. Please provide a valid Bearer token.");
    }

    private static AuthzToken buildFromJwt(Jwt jwt) {
        AuthzToken authzToken = new AuthzToken();
        authzToken.setAccessToken(jwt.getTokenValue());

        Map<String, String> claims = new HashMap<>();
        String userId = jwt.getClaimAsString("preferred_username");
        if (userId == null) {
            userId = jwt.getSubject();
        }
        claims.put("userId", userId);
        claims.put("userName", userId);

        String gatewayId = jwt.getClaimAsString(Constants.GATEWAY_ID);
        if (gatewayId == null) {
            gatewayId = jwt.getClaimAsString("gateway_id");
        }
        if (gatewayId == null) {
            gatewayId = "default";
        }
        claims.put(Constants.GATEWAY_ID, gatewayId);

        authzToken.setClaimsMap(claims);
        return authzToken;
    }
}
