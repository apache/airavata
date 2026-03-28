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
package org.apache.airavata.server.rest.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.airavata.common.config.Constants;
import org.apache.airavata.common.security.UserContext;
import org.apache.airavata.model.security.AuthzToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Set<String> SKIP_PREFIXES = Set.of("/swagger-ui", "/v3/api-docs", "/actuator");

    @Bean
    public FilterRegistrationBean<Filter> authFilter() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new AuthTokenFilter());
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);
        return registration;
    }

    private static class AuthTokenFilter implements Filter {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            HttpServletRequest httpReq = (HttpServletRequest) request;
            HttpServletResponse httpResp = (HttpServletResponse) response;

            // Skip OPTIONS (CORS preflight)
            if ("OPTIONS".equalsIgnoreCase(httpReq.getMethod())) {
                chain.doFilter(request, response);
                return;
            }

            // Skip swagger / api-docs / actuator
            String path = httpReq.getRequestURI();
            for (String prefix : SKIP_PREFIXES) {
                if (path.startsWith(prefix)) {
                    chain.doFilter(request, response);
                    return;
                }
            }

            try {
                String authHeader = httpReq.getHeader("Authorization");
                String accessToken = null;
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    accessToken = authHeader.substring(7);
                }

                if (accessToken == null) {
                    httpResp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing Authorization header");
                    return;
                }

                AuthzToken authzToken = new AuthzToken(accessToken);

                // Parse X-Claims header if present (JSON map of claim key-values)
                String claimsHeader = httpReq.getHeader("X-Claims");
                Map<String, String> claimsMap = new HashMap<>();
                if (claimsHeader != null && !claimsHeader.isBlank()) {
                    try {
                        claimsMap = objectMapper.readValue(claimsHeader, new TypeReference<Map<String, String>>() {});
                    } catch (Exception e) {
                        log.warn("Failed to parse X-Claims header: {}", e.getMessage());
                    }
                }

                // Also accept individual headers as fallback
                if (!claimsMap.containsKey(Constants.USER_NAME)) {
                    String userName = httpReq.getHeader("X-User-Name");
                    if (userName != null) {
                        claimsMap.put(Constants.USER_NAME, userName);
                    }
                }
                if (!claimsMap.containsKey(Constants.GATEWAY_ID)) {
                    String gatewayId = httpReq.getHeader("X-Gateway-Id");
                    if (gatewayId != null) {
                        claimsMap.put(Constants.GATEWAY_ID, gatewayId);
                    }
                }

                authzToken.setClaimsMap(claimsMap);
                UserContext.setAuthzToken(authzToken);

                chain.doFilter(request, response);
            } finally {
                UserContext.clear();
            }
        }
    }
}
