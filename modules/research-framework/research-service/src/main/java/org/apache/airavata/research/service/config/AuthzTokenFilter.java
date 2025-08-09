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
package org.apache.airavata.research.service.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.research.service.AiravataService;
import org.apache.airavata.research.service.model.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AuthzTokenFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthzTokenFilter.class);
    private static final String USERNAME_CLAIM = "userName";
    private static final String GATEWAY_CLAIM = "gatewayID";

    private final AiravataService airavataService;

    @Value("${airavata.research-hub.url}")
    private String csHubUrl;

    public AuthzTokenFilter(AiravataService airavataService) {
        this.airavataService = airavataService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        if (request.getMethod().equalsIgnoreCase("POST")
                || request.getMethod().equalsIgnoreCase("PUT")
                || request.getMethod().equalsIgnoreCase("PATCH")
                || request.getMethod().equalsIgnoreCase("DELETE")) {
            return false; // mutation requests should be authenticated
        }

        return path.startsWith("/swagger")
                || path.startsWith("/v2/api-docs")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/swagger-resources")
                || path.startsWith("/webjars/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String authorizationHeader = request.getHeader("Authorization");
            String xClaimsHeader = request.getHeader("X-Claims");

            if (request.getMethod().equals("OPTIONS")) {
                filterChain.doFilter(request, response);
                return;
            }

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                try {
                    String accessToken = authorizationHeader.substring(7); // Remove "Bearer " prefix
                    Map<String, String> claimsMap;
                    
                    // Primary: Use X-Claims header if available (frontend compatibility)
                    if (xClaimsHeader != null) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        claimsMap = objectMapper.readValue(xClaimsHeader, new TypeReference<>() {});
                        LOGGER.debug("Using claims from X-Claims header");
                    } else {
                        // Fallback: Extract claims from JWT payload for pure OAuth2/OIDC clients
                        claimsMap = extractClaimsFromJWT(accessToken);
                        LOGGER.debug("Using claims extracted from JWT payload");
                    }

                    AuthzToken authzToken = new AuthzToken();
                    authzToken.setAccessToken(accessToken);
                    authzToken.setClaimsMap(claimsMap);
                    UserContext.setAuthzToken(authzToken);

                    UserProfile userProfile = airavataService.getUserProfile(
                            authzToken, getClaim(authzToken, USERNAME_CLAIM), getClaim(authzToken, GATEWAY_CLAIM));
                    UserContext.setUser(userProfile);
                } catch (Exception e) {
                    LOGGER.error("Invalid authorization data", e);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid authorization data");
                    return;
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }

    /**
     * Extract claims from JWT payload as fallback when X-Claims header is not present
     * This enables pure OAuth2/OIDC clients to work without custom headers
     */
    private Map<String, String> extractClaimsFromJWT(String jwt) {
        try {
            // Split JWT into parts (header.payload.signature)
            String[] parts = jwt.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT format");
            }
            
            // Decode the payload (second part)
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            
            // Parse JSON payload
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> jwtClaims = objectMapper.readValue(payload, new TypeReference<>() {});
            
            // Convert to string map and extract required claims
            Map<String, String> claimsMap = new HashMap<>();
            
            // Map standard OIDC claims to Airavata claims
            String email = getClaimValue(jwtClaims, "email", "preferred_username", "sub");
            if (email != null) {
                claimsMap.put(USERNAME_CLAIM, email);
            }
            
            // Default gateway for JWT-only clients
            claimsMap.put(GATEWAY_CLAIM, "default");
            
            LOGGER.debug("Extracted claims from JWT: userName={}, gatewayID={}", 
                        claimsMap.get(USERNAME_CLAIM), claimsMap.get(GATEWAY_CLAIM));
            
            return claimsMap;
        } catch (Exception e) {
            LOGGER.error("Failed to extract claims from JWT", e);
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }
    
    /**
     * Get claim value from JWT payload, trying multiple possible claim names
     */
    private String getClaimValue(Map<String, Object> jwtClaims, String... claimNames) {
        for (String claimName : claimNames) {
            Object value = jwtClaims.get(claimName);
            if (value != null) {
                return value.toString();
            }
        }
        return null;
    }

    private static String getClaim(AuthzToken authzToken, String claimId) {
        return authzToken.getClaimsMap().entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(claimId))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Missing '" + claimId + "' claim in the authentication token"));
    }
}
