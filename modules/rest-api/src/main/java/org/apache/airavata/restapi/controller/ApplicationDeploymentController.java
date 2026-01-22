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
    private final ApplicationDeploymentService applicationDeploymentService;

    public ApplicationDeploymentController(ApplicationDeploymentService applicationDeploymentService) {
        this.applicationDeploymentService = applicationDeploymentService;
    }

    @GetMapping("/{deploymentId}")
    public ResponseEntity<?> getApplicationDeployment(@PathVariable String deploymentId) {
        try {
            var deployment = applicationDeploymentService.getApplicationDeployement(deploymentId);
            if (deployment == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(deployment);
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createApplicationDeployment(
            @RequestParam String gatewayId, @RequestBody ApplicationDeploymentDescription deployment) {
        try {
            var deploymentId = applicationDeploymentService.addApplicationDeployment(deployment, gatewayId);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("deploymentId", deploymentId));
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{deploymentId}")
    public ResponseEntity<?> updateApplicationDeployment(
            @PathVariable String deploymentId, @RequestBody ApplicationDeploymentDescription deployment) {
        try {
            deployment.setAppDeploymentId(deploymentId);
            applicationDeploymentService.updateApplicationDeployment(deploymentId, deployment);
            return ResponseEntity.ok().build();
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getApplicationDeployments(
            @RequestParam(required = false) String appModuleId, @RequestParam(required = false) String computeHostId) {
        try {
            var filters = new HashMap<String, String>();
            if (appModuleId != null) {
                filters.put("APPLICATION_MODULE_ID", appModuleId);
            }
            if (computeHostId != null) {
                filters.put("COMPUTE_HOST_ID", computeHostId);
            }
            var deployments = applicationDeploymentService.getApplicationDeployments(filters);
            return ResponseEntity.ok(deployments);
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
