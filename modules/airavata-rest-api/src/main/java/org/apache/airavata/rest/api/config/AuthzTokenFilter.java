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
package org.apache.airavata.rest.api.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.error.AuthorizationException;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.security.AiravataSecurityException;
import org.apache.airavata.service.security.AiravataSecurityManager;
import org.apache.airavata.service.security.IdentityContext;
import org.apache.airavata.service.security.SecurityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AuthzTokenFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthzTokenFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/swagger")
                || path.startsWith("/v2/api-docs")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/swagger-ui.html")
                || path.startsWith("/swagger-resources")
                || path.startsWith("/webjars/")
                || path.equals("/actuator/health")
                || path.equals("/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String authorizationHeader = request.getHeader("Authorization");
            String xClaimsHeader = request.getHeader("X-Claims");

            if (request.getMethod().equals("OPTIONS")) {
                filterChain.doFilter(request, response);
                return;
            }

            try {
                boolean isAPISecured = ServerSettings.isTLSEnabled();
                
                if (isAPISecured) {
                    // If API is secured, require authentication
                    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ") || xClaimsHeader == null) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
                        return;
                    }
                }
                
                if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ") && xClaimsHeader != null) {
                    String accessToken = authorizationHeader.substring(7); // Remove "Bearer " prefix
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, String> claimsMap = objectMapper.readValue(xClaimsHeader, new TypeReference<>() {});

                    AuthzToken authzToken = new AuthzToken();
                    authzToken.setAccessToken(accessToken);
                    authzToken.setClaimsMap(claimsMap);

                    // Validate authorization using SecurityInterceptor logic
                    authorize(authzToken, request.getMethod() + " " + request.getRequestURI());

                    // Set the user identity info in thread local for downstream execution
                    IdentityContext.set(authzToken);
                }
            } catch (AuthorizationException e) {
                LOGGER.error("Authorization failed", e);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User is not authenticated or authorized.");
                return;
            } catch (ApplicationSettingsException e) {
                LOGGER.error("Error checking security settings", e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
                return;
            } catch (Exception e) {
                LOGGER.error("Invalid authorization data", e);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid authorization data");
                return;
            }

            filterChain.doFilter(request, response);
        } finally {
            IdentityContext.unset();
        }
    }

    private void authorize(AuthzToken authzToken, String action) throws AuthorizationException {
        try {
            boolean isAPISecured = ServerSettings.isTLSEnabled();
            if (isAPISecured) {
                AiravataSecurityManager securityManager = SecurityManagerFactory.getSecurityManager();
                HashMap<String, String> metaDataMap = new HashMap<>();
                metaDataMap.put(Constants.API_METHOD_NAME, action);
                boolean isAuthz = securityManager.isUserAuthorized(authzToken, metaDataMap);
                if (!isAuthz) {
                    throw new AuthorizationException("User is not authenticated or authorized.");
                }
            }
        } catch (AiravataSecurityException e) {
            LOGGER.error(e.getMessage(), e);
            throw new AuthorizationException("Error in authenticating or authorizing user.");
        } catch (ApplicationSettingsException e) {
            LOGGER.error(e.getMessage(), e);
            throw new AuthorizationException("Internal error in authenticating or authorizing user.");
        }
    }
}

