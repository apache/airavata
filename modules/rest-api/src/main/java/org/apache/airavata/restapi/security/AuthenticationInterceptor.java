/**
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
import org.apache.airavata.restapi.util.AuthzTokenUtil;
import org.apache.airavata.security.model.AuthzToken;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor that enforces authentication on all REST API endpoints.
 * Extracts and validates the Authorization token from the request.
 */
@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Skip authentication for actuator endpoints and swagger
        String path = request.getRequestURI();
        if (path.startsWith("/actuator") || path.startsWith("/swagger") || path.startsWith("/v3/api-docs")) {
            return true;
        }

        // Skip authentication for auth endpoints (logout must be publicly accessible)
        if (path.startsWith("/api/v1/auth/")) {
            return true;
        }

        // Skip authentication for health endpoint
        if (path.equals("/api/v1/health")) {
            return true;
        }

        // Skip authentication for public catalog endpoints
        if (path.startsWith("/api/v1/rf/resources/public")) {
            return true;
        }

        // Skip authentication for catalog search (public)
        if (path.equals("/api/v1/rf/resources/search")) {
            return true;
        }

        // Allow CORS preflight (OPTIONS) without auth so browser can complete actual request
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // Require authentication for all other /api/v1 endpoints
        if (path.startsWith("/api/v1")) {
            try {
                AuthzToken authzToken = AuthzTokenUtil.requireAuthzToken(request);
                // Store token in request attribute for use in controllers
                request.setAttribute("authzToken", authzToken);
                request.setAttribute("userId", authzToken.getClaimsMap().get("userId"));
                request.setAttribute("gatewayId", authzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.GATEWAY_ID));
                return true;
            } catch (Exception e) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Authentication required\",\"message\":\"" + 
                    e.getMessage() + "\"}");
                return false;
            }
        }

        return true;
    }
}
