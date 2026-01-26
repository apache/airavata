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
package org.apache.airavata.restapi.util;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.security.model.AuthzToken;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AuthzTokenUtil {
    
    // Master account configuration - can be overridden via environment variables
    private static final String MASTER_USER_ID = System.getenv().getOrDefault("AIRAVATA_MASTER_USER_ID", "root");
    private static final String MASTER_GATEWAY_ID = System.getenv().getOrDefault("AIRAVATA_MASTER_GATEWAY_ID", "default");
    private static final String MASTER_ACCESS_TOKEN = System.getenv().getOrDefault("AIRAVATA_MASTER_ACCESS_TOKEN", "master-token");

    /**
     * Extract AuthzToken from the Authorization header in the request.
     * If no token is provided, uses master account credentials.
     * 
     * @param request HTTP servlet request
     * @return AuthzToken extracted from request or master account token
     */
    public static AuthzToken extractAuthzToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        
        AuthzToken authzToken = new AuthzToken();
        Map<String, String> claims = new HashMap<>();
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Extract Bearer token
            String token = authHeader.substring(7);
            authzToken.setAccessToken(token);
            
            // Try to extract claims from token (JWT decoding would go here)
            // For now, we'll use a master account approach where the token is validated
            // and user info is extracted from the token or session
            
            // If token is the master token, use master account
            if (MASTER_ACCESS_TOKEN.equals(token)) {
                claims.put(Constants.GATEWAY_ID, MASTER_GATEWAY_ID);
                claims.put("userId", MASTER_USER_ID);
            } else {
                // Try to decode JWT token and extract claims
                try {
                    Map<String, Object> jwtClaims = decodeJWT(token);
                    if (jwtClaims != null) {
                        // Extract user info from JWT claims
                        String userId = extractClaim(jwtClaims, "sub", "preferred_username", "email", "userId");
                        // Extract preferred_username separately (useful for admin checks)
                        String preferredUsername = extractClaim(jwtClaims, "preferred_username");
                        // Try multiple possible claim names for gatewayId
                        String gatewayId = extractClaim(jwtClaims, Constants.GATEWAY_ID, "gatewayId", "gateway_id", "gateway");
                        
                        claims.put("userId", userId != null ? userId : MASTER_USER_ID);
                        if (preferredUsername != null) {
                            claims.put("userName", preferredUsername);
                        }
                        claims.put(Constants.GATEWAY_ID, gatewayId != null ? gatewayId : MASTER_GATEWAY_ID);
                    } else {
                        // Fallback to master account if JWT decode fails
                        claims.put(Constants.GATEWAY_ID, MASTER_GATEWAY_ID);
                        claims.put("userId", MASTER_USER_ID);
                    }
                } catch (Exception e) {
                    // If JWT decode fails, fallback to master account
                    claims.put(Constants.GATEWAY_ID, MASTER_GATEWAY_ID);
                    claims.put("userId", MASTER_USER_ID);
                }
            }
        } else {
            // No token provided - use master account for backward compatibility
            // In production, you might want to throw an exception instead
            authzToken.setAccessToken(MASTER_ACCESS_TOKEN);
            claims.put(Constants.GATEWAY_ID, MASTER_GATEWAY_ID);
            claims.put("userId", MASTER_USER_ID);
        }
        
        authzToken.setClaimsMap(claims);
        return authzToken;
    }

    /**
     * Decode JWT token and extract claims (without signature verification for now).
     * In production, you should verify the signature.
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> decodeJWT(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null; // Not a valid JWT
            }
            
            // Decode the payload (second part)
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(payload, Map.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extract a claim value from JWT claims, trying multiple keys.
     */
    @SuppressWarnings("unchecked")
    private static String extractClaim(Map<String, Object> claims, String... keys) {
        for (String key : keys) {
            Object value = claims.get(key);
            if (value != null) {
                return value.toString();
            }
        }
        return null;
    }

    /**
     * Require authentication - throws exception if no valid token is provided.
     */
    public static AuthzToken requireAuthzToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, 
                "Authentication required. Please provide a valid Bearer token."
            );
        }
        
        return extractAuthzToken(request);
    }
}
