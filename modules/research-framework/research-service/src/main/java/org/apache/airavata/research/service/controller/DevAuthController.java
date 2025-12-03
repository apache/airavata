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

package org.apache.airavata.research.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dev")
@Profile("dev")
@Tag(name = "Development Authentication", description = "Development-only endpoints for testing authentication")
public class DevAuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevAuthController.class);

    @Operation(summary = "Generate test JWT token for development")
    @PostMapping("/auth/token")
    public ResponseEntity<Map<String, Object>> generateTestToken(
            @RequestParam(defaultValue = "test@example.com") String email,
            @RequestParam(defaultValue = "default") String gatewayId) {
        
        LOGGER.info("Generating test token for email: {}, gatewayId: {}", email, gatewayId);
        
        try {
            // For development, we'll create a simple token-like response
            // In a real implementation, this would use JwtEncoder to create actual JWTs
            
            Instant now = Instant.now();
            String fakeToken = "dev-jwt-token-" + now.getEpochSecond() + "-" + email.hashCode();
            
            Map<String, Object> response = new HashMap<>();
            response.put("access_token", fakeToken);
            response.put("token_type", "Bearer");
            response.put("expires_in", 3600);
            response.put("email", email);
            response.put("gatewayID", gatewayId);
            response.put("roles", Arrays.asList("USER"));
            response.put("note", "This is a development-only token for testing purposes");

            LOGGER.info("Generated test token for user: {}", email);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            LOGGER.error("Error generating test token: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "token_generation_failed");
            errorResponse.put("error_description", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @Operation(summary = "Get development API key information")
    @PostMapping("/auth/api-key-info") 
    public ResponseEntity<Map<String, Object>> getApiKeyInfo() {
        LOGGER.info("Providing development API key information");
        
        Map<String, Object> response = new HashMap<>();
        response.put("api_key_header", "X-API-Key");
        response.put("api_key_value", "dev-research-api-key-12345");
        response.put("note", "Use this API key in the X-API-Key header for development testing");
        response.put("example_curl", "curl -H \"X-API-Key: dev-research-api-key-12345\" http://localhost:8080/api/v2/rf/compute-resources/");
        
        return ResponseEntity.ok(response);
    }
}