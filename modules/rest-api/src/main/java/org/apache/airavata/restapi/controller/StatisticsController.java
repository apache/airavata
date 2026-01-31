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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.airavata.common.model.ExperimentStatistics;
import org.apache.airavata.common.model.ExperimentSummaryModel;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.registry.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.restapi.util.AuthzTokenUtil;
import org.apache.airavata.security.model.AuthzToken;
import org.apache.airavata.service.registry.RegistryService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/statistics")
@ConditionalOnProperty(name = "services.rest.enabled", havingValue = "true", matchIfMissing = false)
public class StatisticsController {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(StatisticsController.class);

    private final RegistryService registryService;

    public StatisticsController(RegistryService registryService) {
        this.registryService = registryService;
    }

    /**
     * Get experiment statistics for a gateway.
     * Returns counts and lists of experiments grouped by status (completed, failed, running, created, cancelled).
     */
    @GetMapping("/experiments")
    public ResponseEntity<?> getExperimentStatistics(
            HttpServletRequest request,
            @RequestParam(required = false) String gatewayId,
            @RequestParam(required = false) Long fromTime,
            @RequestParam(required = false) Long toTime,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String applicationName,
            @RequestParam(required = false) String resourceHostName,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        try {
            // Get gateway from auth token if not provided
            AuthzToken authzToken = AuthzTokenUtil.extractAuthzToken(request);
            String effectiveGatewayId = gatewayId;
            if (effectiveGatewayId == null && authzToken != null && authzToken.getClaimsMap() != null) {
                effectiveGatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            }

            if (effectiveGatewayId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Gateway ID is required. Provide gatewayId parameter or authenticate with a valid token."));
            }

            // Set default time range if not provided (last year to now)
            long now = System.currentTimeMillis();
            long effectiveFromTime = fromTime != null ? fromTime : now - (365L * 24 * 60 * 60 * 1000); // 1 year ago
            long effectiveToTime = toTime != null ? toTime : now;

            // For now, we don't restrict by accessible experiment IDs (admin view)
            List<String> accessibleExpIds = Collections.emptyList();

            ExperimentStatistics stats = registryService.getExperimentStatistics(
                    effectiveGatewayId,
                    effectiveFromTime,
                    effectiveToTime,
                    userName,
                    applicationName,
                    resourceHostName,
                    accessibleExpIds,
                    limit,
                    offset);

            // Transform to a more frontend-friendly format
            Map<String, Object> response = transformExperimentStatistics(stats);
            return ResponseEntity.ok(response);

        } catch (RegistryException e) {
            logger.error("Error fetching experiment statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching experiment statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get system-wide statistics including counts of gateways, compute resources,
     * storage resources, applications, and projects.
     */
    @GetMapping("/system")
    public ResponseEntity<?> getSystemStatistics(
            HttpServletRequest request,
            @RequestParam(required = false) String gatewayId) {
        try {
            AuthzToken authzToken = AuthzTokenUtil.extractAuthzToken(request);
            String effectiveGatewayId = gatewayId;
            if (effectiveGatewayId == null && authzToken != null && authzToken.getClaimsMap() != null) {
                effectiveGatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            }

            Map<String, Object> stats = new HashMap<>();

            // Count gateways
            try {
                var gateways = registryService.getAllGateways();
                stats.put("totalGateways", gateways != null ? gateways.size() : 0);
            } catch (Exception e) {
                logger.warn("Could not fetch gateways count", e);
                stats.put("totalGateways", 0);
            }

            // Count compute resources
            try {
                Map<String, String> computeResources = registryService.getAllComputeResourceNames();
                stats.put("totalComputeResources", computeResources != null ? computeResources.size() : 0);
            } catch (Exception e) {
                logger.warn("Could not fetch compute resources count", e);
                stats.put("totalComputeResources", 0);
            }

            // Count storage resources
            try {
                Map<String, String> storageResources = registryService.getAllStorageResourceNames();
                stats.put("totalStorageResources", storageResources != null ? storageResources.size() : 0);
            } catch (Exception e) {
                logger.warn("Could not fetch storage resources count", e);
                stats.put("totalStorageResources", 0);
            }

            // Count applications (if gateway is specified)
            if (effectiveGatewayId != null) {
                try {
                    Map<String, String> applications = registryService.getAllApplicationInterfaceNames(effectiveGatewayId);
                    stats.put("totalApplications", applications != null ? applications.size() : 0);
                } catch (Exception e) {
                    logger.warn("Could not fetch applications count", e);
                    stats.put("totalApplications", 0);
                }

                // Count users for gateway
                try {
                    List<String> users = registryService.getAllUsersInGateway(effectiveGatewayId);
                    stats.put("totalUsers", users != null ? users.size() : 0);
                } catch (Exception e) {
                    logger.warn("Could not fetch users count", e);
                    stats.put("totalUsers", 0);
                }
            } else {
                stats.put("totalApplications", 0);
                stats.put("totalUsers", 0);
            }

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            logger.error("Error fetching system statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Transform the backend ExperimentStatistics model to a frontend-friendly format.
     */
    private Map<String, Object> transformExperimentStatistics(ExperimentStatistics stats) {
        Map<String, Object> result = new HashMap<>();

        // Total count
        result.put("total", stats.getAllExperimentCount());

        // Counts by status
        Map<String, Integer> byStatus = new HashMap<>();
        byStatus.put("COMPLETED", stats.getCompletedExperimentCount());
        byStatus.put("FAILED", stats.getFailedExperimentCount());
        byStatus.put("RUNNING", stats.getRunningExperimentCount());
        byStatus.put("CREATED", stats.getCreatedExperimentCount());
        byStatus.put("CANCELLED", stats.getCancelledExperimentCount());
        result.put("byStatus", byStatus);

        // Aggregate by user from all experiments
        Map<String, Integer> byUser = new HashMap<>();
        if (stats.getAllExperiments() != null) {
            for (ExperimentSummaryModel exp : stats.getAllExperiments()) {
                String user = exp.getUserName();
                if (user != null) {
                    byUser.put(user, byUser.getOrDefault(user, 0) + 1);
                }
            }
        }
        result.put("byUser", byUser);

        // Aggregate by gateway from all experiments
        Map<String, Integer> byGateway = new HashMap<>();
        if (stats.getAllExperiments() != null) {
            for (ExperimentSummaryModel exp : stats.getAllExperiments()) {
                String gateway = exp.getGatewayId();
                if (gateway != null) {
                    byGateway.put(gateway, byGateway.getOrDefault(gateway, 0) + 1);
                }
            }
        }
        result.put("byGateway", byGateway);

        // Recent experiments (sorted by creation time, descending)
        List<Map<String, Object>> recent = Collections.emptyList();
        if (stats.getAllExperiments() != null && !stats.getAllExperiments().isEmpty()) {
            recent = stats.getAllExperiments().stream()
                    .sorted((a, b) -> Long.compare(b.getCreationTime(), a.getCreationTime()))
                    .limit(10)
                    .map(this::transformExperimentSummary)
                    .collect(Collectors.toList());
        }
        result.put("recent", recent);

        // Also include the raw counts for more detailed views
        result.put("completedExperimentCount", stats.getCompletedExperimentCount());
        result.put("failedExperimentCount", stats.getFailedExperimentCount());
        result.put("runningExperimentCount", stats.getRunningExperimentCount());
        result.put("createdExperimentCount", stats.getCreatedExperimentCount());
        result.put("cancelledExperimentCount", stats.getCancelledExperimentCount());

        return result;
    }

    /**
     * Transform an ExperimentSummaryModel to a map for JSON serialization.
     */
    private Map<String, Object> transformExperimentSummary(ExperimentSummaryModel exp) {
        Map<String, Object> result = new HashMap<>();
        result.put("experimentId", exp.getExperimentId());
        result.put("experimentName", exp.getName());
        result.put("projectId", exp.getProjectId());
        result.put("gatewayId", exp.getGatewayId());
        result.put("userName", exp.getUserName());
        result.put("creationTime", exp.getCreationTime());
        result.put("experimentStatus", exp.getExperimentStatus());
        result.put("statusUpdateTime", exp.getStatusUpdateTime());
        result.put("description", exp.getDescription());
        result.put("executionId", exp.getExecutionId());
        result.put("resourceHostId", exp.getResourceHostId());
        return result;
    }
}
