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

import java.util.HashMap;
import java.util.Map;
import org.apache.airavata.common.model.ApplicationDeploymentDescription;
import org.apache.airavata.registry.exception.AppCatalogException;
import org.apache.airavata.registry.services.ApplicationDeploymentService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/application-deployments")
@ConditionalOnProperty(name = "services.rest.enabled", havingValue = "true", matchIfMissing = false)
public class ApplicationDeploymentController {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ApplicationDeploymentController.class);
    
    private final ApplicationDeploymentService applicationDeploymentService;
    private final org.apache.airavata.registry.repositories.ResourceAccessRepository resourceAccessRepository;

    public ApplicationDeploymentController(
            ApplicationDeploymentService applicationDeploymentService,
            org.apache.airavata.registry.repositories.ResourceAccessRepository resourceAccessRepository) {
        this.applicationDeploymentService = applicationDeploymentService;
        this.resourceAccessRepository = resourceAccessRepository;
    }

    @GetMapping("/{deploymentId}")
    public ResponseEntity<?> getApplicationDeployment(@PathVariable String deploymentId) {
        try {
            logger.debug("Getting application deployment: {}", deploymentId);
            var deployment = applicationDeploymentService.getApplicationDeployement(deploymentId);
            if (deployment == null) {
                logger.warn("Application deployment not found: {}", deploymentId);
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(deployment);
        } catch (AppCatalogException e) {
            logger.error("Error getting application deployment {}: {}", deploymentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error getting application deployment {}: {}", deploymentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping
    public ResponseEntity<?> createApplicationDeployment(
            @RequestParam String gatewayId, @RequestBody ApplicationDeploymentDescription deployment) {
        try {
            logger.info("Creating application deployment: appModuleId={}, computeHostId={}, gatewayId={}", 
                deployment.getAppModuleId(), deployment.getComputeHostId(), gatewayId);
            
            // Validate required fields
            if (deployment.getAppModuleId() == null || deployment.getAppModuleId().trim().isEmpty()) {
                logger.warn("Missing appModuleId in deployment request");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "appModuleId is required"));
            }
            if (deployment.getComputeHostId() == null || deployment.getComputeHostId().trim().isEmpty()) {
                logger.warn("Missing computeHostId in deployment request");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "computeHostId is required"));
            }
            if (deployment.getExecutablePath() == null || deployment.getExecutablePath().trim().isEmpty()) {
                logger.warn("Missing executablePath in deployment request");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "executablePath is required"));
            }
            
            // Ensure appDeploymentId is null or empty so it gets auto-generated
            if (deployment.getAppDeploymentId() == null || deployment.getAppDeploymentId().trim().isEmpty()) {
                deployment.setAppDeploymentId(null);
            }
            
            var deploymentId = applicationDeploymentService.addApplicationDeployment(deployment, gatewayId);
            logger.info("Application deployment created successfully: {}", deploymentId);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("deploymentId", deploymentId));
        } catch (AppCatalogException e) {
            logger.error("Error creating application deployment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error creating application deployment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    @PutMapping("/{deploymentId}")
    public ResponseEntity<?> updateApplicationDeployment(
            @PathVariable String deploymentId, @RequestBody ApplicationDeploymentDescription deployment) {
        try {
            logger.info("Updating application deployment: {}", deploymentId);
            deployment.setAppDeploymentId(deploymentId);
            applicationDeploymentService.updateApplicationDeployment(deploymentId, deployment);
            logger.info("Application deployment updated successfully: {}", deploymentId);
            return ResponseEntity.ok().build();
        } catch (AppCatalogException e) {
            logger.error("Error updating application deployment {}: {}", deploymentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error updating application deployment {}: {}", deploymentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping
    public ResponseEntity<?> getApplicationDeployments(
            @RequestParam(required = false) String appModuleId, 
            @RequestParam(required = false) String computeHostId,
            @RequestParam(required = false) String credentialToken) {
        try {
            logger.debug("Getting application deployments: appModuleId={}, computeHostId={}, credentialToken={}", 
                appModuleId, computeHostId, credentialToken != null ? "***" : null);
            
            // If credentialToken is provided, find deployments via resource access
            if (credentialToken != null && !credentialToken.trim().isEmpty()) {
                // Get all resource access entries for this credential token (COMPUTE resources)
                var resourceAccesses = resourceAccessRepository.findByCredentialToken(credentialToken.trim());
                var computeResourceIds = resourceAccesses.stream()
                    .filter(ra -> ra.getResourceType() == org.apache.airavata.common.model.PreferenceResourceType.COMPUTE)
                    .map(ra -> ra.getResourceId())
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());
                
                logger.debug("Found {} compute resources for credential token", computeResourceIds.size());
                
                // Get deployments for each compute resource
                var allDeployments = new java.util.ArrayList<ApplicationDeploymentDescription>();
                for (String computeId : computeResourceIds) {
                    var filters = new HashMap<String, String>();
                    filters.put("computeHostId", computeId);
                    var deployments = applicationDeploymentService.getApplicationDeployments(filters);
                    if (deployments != null) {
                        allDeployments.addAll(deployments);
                    }
                }
                
                logger.debug("Found {} application deployments for credential token", allDeployments.size());
                return ResponseEntity.ok(allDeployments);
            }
            
            // Original logic for appModuleId and computeHostId filters
            var filters = new HashMap<String, String>();
            if (appModuleId != null) {
                filters.put("appModuleId", appModuleId);
            }
            if (computeHostId != null) {
                filters.put("computeHostId", computeHostId);
            }
            var deployments = applicationDeploymentService.getApplicationDeployments(filters);
            logger.debug("Found {} application deployments", deployments != null ? deployments.size() : 0);
            return ResponseEntity.ok(deployments != null ? deployments : java.util.Collections.emptyList());
        } catch (AppCatalogException e) {
            logger.error("Error getting application deployments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error getting application deployments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }
}
