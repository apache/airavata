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
package org.apache.airavata.restapi.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.apache.airavata.common.model.ClusterInfo;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.restapi.security.AuthorizationService;
import org.apache.airavata.security.model.AuthzToken;
import org.apache.airavata.service.cluster.ClusterInfoService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for on-demand SLURM cluster information (partitions, accounts).
 * Fetches and caches cluster info per credential and compute resource.
 */
@RestController
@RequestMapping("/api/v1/cluster-info")
@ConditionalOnProperty(name = "services.rest.enabled", havingValue = "true", matchIfMissing = false)
public class ClusterInfoController {

    private final ClusterInfoService clusterInfoService;
    private final AuthorizationService authorizationService;

    public ClusterInfoController(ClusterInfoService clusterInfoService, AuthorizationService authorizationService) {
        this.clusterInfoService = clusterInfoService;
        this.authorizationService = authorizationService;
    }

    private AuthzToken getAuthzToken(HttpServletRequest request) {
        return (AuthzToken) request.getAttribute("authzToken");
    }

    private String resolveGatewayId(AuthzToken authzToken, String bodyGatewayId) {
        if (bodyGatewayId != null && !bodyGatewayId.isBlank()) {
            return bodyGatewayId.trim();
        }
        if (authzToken != null && authzToken.getClaimsMap() != null) {
            Object g = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            if (g != null) {
                return g.toString();
            }
        }
        return null;
    }

    /**
     * Fetch cluster info by running slurminfo.sh on the host using the given credential.
     * Results are cached. Request body: credentialToken, computeResourceId, hostname, port (optional, default 22), gatewayId (optional, from auth).
     */
    @PostMapping("/fetch")
    public ResponseEntity<?> fetchClusterInfo(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        AuthzToken authzToken = getAuthzToken(httpRequest);
        String gatewayId = resolveGatewayId(authzToken, request.get("gatewayId") != null ? request.get("gatewayId").toString() : null);
        if (gatewayId == null || gatewayId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "gatewayId is required"));
        }
        authorizationService.requireGatewayAccess(authzToken, gatewayId);

        String credentialToken = request.get("credentialToken") != null ? request.get("credentialToken").toString() : null;
        String computeResourceId = request.get("computeResourceId") != null ? request.get("computeResourceId").toString().trim() : null;
        if (computeResourceId == null || computeResourceId.isBlank()) {
            computeResourceId = "form-draft";
        }
        String hostname = request.get("hostname") != null ? request.get("hostname").toString() : null;
        String loginUsername = request.get("loginUsername") != null ? request.get("loginUsername").toString().trim() : null;
        if (loginUsername != null && loginUsername.isEmpty()) {
            loginUsername = null;
        }
        int port = 22;
        if (request.get("port") != null) {
            if (request.get("port") instanceof Number n) {
                port = n.intValue();
            } else {
                try {
                    port = Integer.parseInt(request.get("port").toString());
                } catch (NumberFormatException e) {
                    port = 22;
                }
            }
        }

        if (credentialToken == null || credentialToken.isBlank()
                || hostname == null || hostname.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "credentialToken and hostname are required"));
        }

        try {
            ClusterInfo info = clusterInfoService.fetchClusterInfo(
                    credentialToken, gatewayId, computeResourceId, hostname, port, loginUsername);
            return ResponseEntity.ok(toResponse(info));
        } catch (CredentialStoreException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get cached cluster info for the given credential and compute resource.
     */
    @GetMapping("/{credentialToken}/{computeResourceId}")
    public ResponseEntity<?> getCached(
            @PathVariable String credentialToken,
            @PathVariable String computeResourceId,
            HttpServletRequest httpRequest) {
        AuthzToken authzToken = getAuthzToken(httpRequest);
        String gatewayId = resolveGatewayId(authzToken, null);
        if (gatewayId == null || gatewayId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "gatewayId is required"));
        }
        authorizationService.requireGatewayAccess(authzToken, gatewayId);

        return clusterInfoService.getCached(gatewayId, credentialToken, computeResourceId)
                .map(info -> ResponseEntity.ok(toResponse(info)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Invalidate cached cluster info for the given credential and compute resource.
     */
    @DeleteMapping("/{credentialToken}/{computeResourceId}")
    public ResponseEntity<?> deleteCached(
            @PathVariable String credentialToken,
            @PathVariable String computeResourceId,
            HttpServletRequest httpRequest) {
        AuthzToken authzToken = getAuthzToken(httpRequest);
        String gatewayId = resolveGatewayId(authzToken, null);
        if (gatewayId == null || gatewayId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "gatewayId is required"));
        }
        authorizationService.requireGatewayAccess(authzToken, gatewayId);

        clusterInfoService.deleteCached(gatewayId, credentialToken, computeResourceId);
        return ResponseEntity.noContent().build();
    }

    private static Map<String, Object> toResponse(ClusterInfo info) {
        return Map.of(
                "partitions", info.getPartitions(),
                "fetchedAt", info.getFetchedAt() != null ? info.getFetchedAt().toString() : null,
                "accounts", info.getAccountsList());
    }
}
