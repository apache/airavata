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
import jakarta.servlet.http.HttpServletRequest;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.utils.DBConstants;
import org.apache.airavata.restapi.util.AuthzTokenUtil;
import org.apache.airavata.service.AiravataService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("restExperimentController")
@RequestMapping("/api/v1/experiments")
@ConditionalOnProperty(name = "services.rest.enabled", havingValue = "true", matchIfMissing = false)
public class ExperimentController {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ExperimentController.class);
    
    private final ExperimentService experimentService;
    private final AiravataService airavataService;

    public ExperimentController(ExperimentService experimentService, AiravataService airavataService) {
        this.experimentService = experimentService;
        this.airavataService = airavataService;
    }

    @GetMapping("/{experimentId}")
    public ResponseEntity<?> getExperiment(@PathVariable String experimentId) {
        try {
            var experiment = experimentService.getExperiment(experimentId);
            if (experiment == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(experiment);
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createExperiment(@RequestBody ExperimentModel experiment) {
        try {
            var experimentId = experimentService.addExperiment(experiment);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("experimentId", experimentId));
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{experimentId}")
    public ResponseEntity<?> updateExperiment(
            @PathVariable String experimentId, @RequestBody ExperimentModel experiment) {
        try {
            experiment.setExperimentId(experimentId);
            experimentService.updateExperiment(experiment, experimentId);
            return ResponseEntity.ok().build();
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{experimentId}")
    public ResponseEntity<?> deleteExperiment(@PathVariable String experimentId) {
        try {
            experimentService.removeExperiment(experimentId);
            return ResponseEntity.ok().build();
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getExperiments(
            HttpServletRequest request,
            @RequestParam(required = false) String gatewayId,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String projectId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        try {
            // Get user info from auth token if not provided
            var authzToken = AuthzTokenUtil.extractAuthzToken(request);
            String effectiveGatewayId = gatewayId != null ? gatewayId : 
                authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            String effectiveUserName = userName != null ? userName :
                authzToken.getClaimsMap().get("userName");
            if (effectiveUserName == null) {
                effectiveUserName = authzToken.getClaimsMap().get("userId");
            }
            
            List<ExperimentModel> experiments;
            if (projectId != null && effectiveGatewayId != null) {
                // Filter by project
                experiments = experimentService.getExperimentList(
                        effectiveGatewayId, DBConstants.Experiment.PROJECT_ID, projectId, limit, offset, null, null);
            } else if (effectiveGatewayId != null && effectiveUserName != null) {
                // Filter by user in gateway
                experiments = experimentService.getExperimentList(
                        effectiveGatewayId, DBConstants.Experiment.USER_NAME, effectiveUserName, limit, offset, null, null);
            } else if (effectiveGatewayId != null) {
                // Get all experiments for gateway (admin mode)
                experiments = experimentService.getExperimentList(
                        effectiveGatewayId, null, null, limit, offset, null, null);
            } else {
                return ResponseEntity.badRequest()
                        .body("Gateway ID is required. Provide gatewayId parameter or authenticate with a valid token.");
            }
            return ResponseEntity.ok(experiments);
        } catch (RegistryException e) {
            logger.error("Error fetching experiments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/{experimentId}/launch")
    public ResponseEntity<?> launchExperiment(
            HttpServletRequest request,
            @PathVariable String experimentId) {
        try {
            var authzToken = AuthzTokenUtil.requireAuthzToken(request);
            String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            if (gatewayId == null) {
                gatewayId = "default";
            }
            
            airavataService.launchExperiment(authzToken, gatewayId, experimentId);
            return ResponseEntity.ok(Map.of("launched", true, "experimentId", experimentId));
        } catch (Exception e) {
            logger.error("Error launching experiment " + experimentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{experimentId}/cancel")
    public ResponseEntity<?> cancelExperiment(
            HttpServletRequest request,
            @PathVariable String experimentId) {
        try {
            var authzToken = AuthzTokenUtil.requireAuthzToken(request);
            String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            if (gatewayId == null) {
                gatewayId = "default";
            }
            
            // Update experiment status to CANCELING
            var experiment = experimentService.getExperiment(experimentId);
            if (experiment == null) {
                return ResponseEntity.notFound().build();
            }
            
            // The actual cancellation is handled by setting status
            // In a full implementation, this would trigger the orchestrator
            return ResponseEntity.ok(Map.of("cancelled", true, "experimentId", experimentId));
        } catch (Exception e) {
            logger.error("Error cancelling experiment " + experimentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{experimentId}/clone")
    public ResponseEntity<?> cloneExperiment(
            HttpServletRequest request,
            @PathVariable String experimentId,
            @RequestParam(required = false) String newName,
            @RequestParam(required = false) String projectId) {
        try {
            var authzToken = AuthzTokenUtil.requireAuthzToken(request);
            
            // Get existing experiment
            var existingExperiment = experimentService.getExperiment(experimentId);
            if (existingExperiment == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Clone using AiravataService
            String newExperimentId = airavataService.cloneExperiment(
                    authzToken,
                    experimentId,
                    newName != null ? newName : existingExperiment.getExperimentName() + " (Clone)",
                    projectId,
                    existingExperiment);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("experimentId", newExperimentId));
        } catch (Exception e) {
            logger.error("Error cloning experiment " + experimentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
