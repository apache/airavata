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
package org.apache.airavata.restapi.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.registry.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.restapi.security.AuthorizationService;
import org.apache.airavata.security.model.AuthzToken;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gateways")
@ConditionalOnProperty(name = "services.rest.enabled", havingValue = "true", matchIfMissing = false)
public class GatewayController {
    private final GatewayService gatewayService;
    private final AuthorizationService authorizationService;

    public GatewayController(GatewayService gatewayService, AuthorizationService authorizationService) {
        this.gatewayService = gatewayService;
        this.authorizationService = authorizationService;
    }

    private AuthzToken getAuthzToken(HttpServletRequest request) {
        return (AuthzToken) request.getAttribute("authzToken");
    }

    @GetMapping
    public ResponseEntity<?> getAllGateways(HttpServletRequest request) {
        try {
            var authzToken = getAuthzToken(request);
            
            // Get all gateways
            var allGateways = gatewayService.getAllGateways();
            
            // Filter based on user access
            if (authorizationService.isRootUser(authzToken)) {
                // Root user sees all gateways
                return ResponseEntity.ok(allGateways);
            } else {
                // Regular users only see gateways they have access to
                var accessibleGateways = authorizationService.getAccessibleGateways(authzToken);
                var filteredGateways = allGateways.stream()
                    .filter(g -> accessibleGateways.contains(g.getGatewayId()))
                    .collect(Collectors.toList());
                return ResponseEntity.ok(filteredGateways);
            }
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{gatewayId}")
    public ResponseEntity<?> getGateway(@PathVariable String gatewayId, HttpServletRequest request) {
        try {
            var authzToken = getAuthzToken(request);
            
            // Check gateway access
            authorizationService.requireGatewayAccess(authzToken, gatewayId);
            
            var gateway = gatewayService.getGateway(gatewayId);
            if (gateway == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(gateway);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createGateway(@RequestBody Gateway gateway, HttpServletRequest request) {
        try {
            // Note: In production, uncomment the following to restrict gateway creation to root users
            // var authzToken = getAuthzToken(request);
            // authorizationService.requireRootUser(authzToken);
            
            var gatewayId = gatewayService.addGateway(gateway);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("gatewayId", gatewayId));
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{gatewayId}")
    public ResponseEntity<?> updateGateway(@PathVariable String gatewayId, @RequestBody Gateway gateway, HttpServletRequest request) {
        try {
            // Note: In production, uncomment the following to restrict gateway updates to root users
            // var authzToken = getAuthzToken(request);
            // authorizationService.requireRootUser(authzToken);
            
            gateway.setGatewayId(gatewayId);
            gatewayService.updateGateway(gatewayId, gateway);
            return ResponseEntity.ok().build();
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{gatewayId}")
    public ResponseEntity<?> deleteGateway(@PathVariable String gatewayId, HttpServletRequest request) {
        try {
            // Note: In production, uncomment the following to restrict gateway deletion to root users
            // var authzToken = getAuthzToken(request);
            // authorizationService.requireRootUser(authzToken);
            
            gatewayService.removeGateway(gatewayId);
            return ResponseEntity.ok().build();
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
