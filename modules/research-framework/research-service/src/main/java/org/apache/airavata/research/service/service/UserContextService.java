/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.airavata.research.service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.airavata.research.service.config.ApiKeyAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class UserContextService {

    public String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) auth;
            return jwtAuth.getToken().getClaimAsString("email");
        } else if (auth instanceof ApiKeyAuthenticationToken) {
            return "api-key-user";
        }
        
        return "anonymous";
    }

    public String getCurrentGatewayId() {
        // Extract from X-Claims header or JWT
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        String claims = attr.getRequest().getHeader("X-Claims");
        
        if (claims != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(claims);
                JsonNode gatewayNode = node.get("gatewayID");
                if (gatewayNode != null) {
                    return gatewayNode.asText();
                }
            } catch (Exception e) {
                // Fall back to default
            }
        }
        
        return "default";
    }

    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !"anonymous".equals(auth.getName());
    }

    public String getCurrentUserName() {
        // Extract from X-Claims header first
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        String claims = attr.getRequest().getHeader("X-Claims");
        
        if (claims != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(claims);
                JsonNode userNameNode = node.get("userName");
                if (userNameNode != null) {
                    return userNameNode.asText();
                }
            } catch (Exception e) {
                // Fall back to authentication context
            }
        }
        
        // Fall back to current user ID
        return getCurrentUserId();
    }
}