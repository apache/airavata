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
package org.apache.airavata.restapi.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import org.apache.airavata.core.util.Constants;
import org.apache.airavata.iam.model.AuthzToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor that populates request attributes (authzToken, userId, gatewayId)
 * from the JWT already validated by Spring Security's OAuth2 Resource Server filter.
 *
 * <p>Spring Security handles authentication (401 for missing/invalid tokens).
 * This interceptor bridges the validated JWT to the existing AuthzToken model
 * used by controllers and services.
 */
@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            AuthzToken authzToken = buildAuthzToken(jwt);
            request.setAttribute("authzToken", authzToken);
            request.setAttribute("userId", authzToken.getClaimsMap().get("userId"));
            request.setAttribute("gatewayId", authzToken.getClaimsMap().get(Constants.GATEWAY_ID));
        }

        return true;
    }

    private AuthzToken buildAuthzToken(Jwt jwt) {
        AuthzToken authzToken = new AuthzToken();
        authzToken.setAccessToken(jwt.getTokenValue());

        Map<String, String> claims = new HashMap<>();
        // Extract userId: try preferred_username first (Keycloak standard), then sub
        String userId = jwt.getClaimAsString("preferred_username");
        if (userId == null) {
            userId = jwt.getSubject();
        }
        claims.put("userId", userId);
        claims.put("userName", userId);

        // Extract gateway ID from custom claim, fallback to "default"
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
