/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
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
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.research.service.handlers.UserHandler;
import org.apache.airavata.research.service.model.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
@Profile("!dev")
public class AuthzTokenFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthzTokenFilter.class);
    private static final String USERNAME_CLAIM = "userName";

    private final UserHandler userHandler;

    @Value("${cybershuttle.hub.url}")
    private String csHubUrl;

    public AuthzTokenFilter(UserHandler userHandler) {
        this.userHandler = userHandler;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");
        String xClaimsHeader = request.getHeader("X-Claims");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ") || xClaimsHeader == null) {
            LOGGER.error("Missing or invalid Authorization header");
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.setHeader("Location", csHubUrl);
            return;
        }

        try {
            String accessToken = authorizationHeader.substring(7); // Remove "Bearer " prefix
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> claimsMap = objectMapper.readValue(xClaimsHeader, new TypeReference<>() {
            });

            AuthzToken authzToken = new AuthzToken();
            authzToken.setAccessToken(accessToken);
            authzToken.setClaimsMap(claimsMap);

            UserContext.setAuthzToken(authzToken);
            UserContext.setUser(userHandler.initializeOrGetUser(claimsMap.get(USERNAME_CLAIM)));

        } catch (Exception e) {
            LOGGER.error("Invalid authorization data", e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid authorization data");
            return;
        }

        filterChain.doFilter(request, response);
    }
}

