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

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.airavata.gateway.model.Gateway;
import org.apache.airavata.gateway.service.GatewayService;
import org.apache.airavata.core.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.restapi.exception.ResourceNotFoundException;
import org.apache.airavata.restapi.security.AuthorizationService;
import org.apache.airavata.iam.model.AuthzToken;
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
@Tag(name = "Gateways")
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
    public List<Gateway> getAllGateways(HttpServletRequest request) throws RegistryException {
        var authzToken = getAuthzToken(request);
        var allGateways = gatewayService.getAllGateways();
        if (authorizationService.isRootUser(authzToken)) {
            return allGateways;
        } else {
            var accessibleGateways = authorizationService.getAccessibleGateways(authzToken);
            return allGateways.stream()
                    .filter(g -> accessibleGateways.contains(g.getGatewayId()))
                    .collect(Collectors.toList());
        }
    }

    @GetMapping("/{gatewayId}")
    public Gateway getGateway(@PathVariable String gatewayId, HttpServletRequest request) throws RegistryException {
        var authzToken = getAuthzToken(request);
        authorizationService.requireGatewayAccess(authzToken, gatewayId);
        var gateway = gatewayService.getGateway(gatewayId);
        if (gateway == null) {
            throw new ResourceNotFoundException("Gateway", gatewayId);
        }
        return gateway;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createGateway(
            @Valid @RequestBody Gateway gateway, HttpServletRequest request) throws RegistryException {
        var gatewayId = gatewayService.createGateway(gateway);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("gatewayId", gatewayId));
    }

    @PutMapping("/{gatewayId}")
    public ResponseEntity<Void> updateGateway(
            @PathVariable String gatewayId, @Valid @RequestBody Gateway gateway, HttpServletRequest request)
            throws RegistryException {
        gateway.setGatewayId(gatewayId);
        gatewayService.updateGateway(gatewayId, gateway);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{gatewayId}")
    public ResponseEntity<Void> deleteGateway(@PathVariable String gatewayId, HttpServletRequest request)
            throws RegistryException {
        gatewayService.deleteGateway(gatewayId);
        return ResponseEntity.ok().build();
    }
}
