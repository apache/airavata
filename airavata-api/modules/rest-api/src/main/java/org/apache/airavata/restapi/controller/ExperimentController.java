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
import org.apache.airavata.core.util.Constants;
import org.apache.airavata.research.experiment.model.Experiment;
import org.apache.airavata.research.experiment.service.ExperimentService;
import org.apache.airavata.restapi.exception.InvalidRequestException;
import org.apache.airavata.restapi.exception.ResourceNotFoundException;
import org.apache.airavata.restapi.util.AuthzTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Tag(name = "Experiments")
public class ExperimentController {
    private static final Logger logger = LoggerFactory.getLogger(ExperimentController.class);

    private final ExperimentService experimentService;

    public ExperimentController(ExperimentService experimentService) {
        this.experimentService = experimentService;
    }

    @GetMapping("/{experimentId}")
    public Experiment getExperiment(@PathVariable String experimentId) throws Exception {
        var experiment = experimentService.getExperiment(experimentId);
        if (experiment == null) {
            throw new ResourceNotFoundException("Experiment", experimentId);
        }
        return experiment;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createExperiment(@Valid @RequestBody Experiment experiment)
            throws Exception {
        String gatewayId = experiment.getGatewayId();
        if (gatewayId == null) {
            gatewayId = "default";
        }
        var experimentId = experimentService.createExperiment(gatewayId, experiment);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("experimentId", experimentId));
    }

    @PutMapping("/{experimentId}")
    public ResponseEntity<Void> updateExperiment(
            @PathVariable String experimentId, @Valid @RequestBody Experiment experiment) throws Exception {
        experiment.setExperimentId(experimentId);
        experimentService.updateExperiment(experimentId, experiment);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{experimentId}")
    public ResponseEntity<Void> deleteExperiment(@PathVariable String experimentId) throws Exception {
        experimentService.deleteExperiment(experimentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public List<Experiment> getExperiments(
            HttpServletRequest request,
            @RequestParam(required = false) String gatewayId,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String projectId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset)
            throws Exception {
        var authzToken = AuthzTokenUtil.extractAuthzToken(request);
        String effectiveGatewayId =
                gatewayId != null ? gatewayId : authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        String effectiveUserName =
                userName != null ? userName : authzToken.getClaimsMap().get("userName");
        if (effectiveUserName == null) {
            effectiveUserName = authzToken.getClaimsMap().get("userId");
        }

        if (effectiveGatewayId == null) {
            throw new InvalidRequestException(
                    "Gateway ID is required. Provide gatewayId parameter or authenticate with a valid token.");
        } else if (projectId != null) {
            return experimentService.getExperimentsInProject(effectiveGatewayId, projectId, limit, offset);
        } else if (effectiveUserName != null) {
            return experimentService.getUserExperiments(effectiveGatewayId, effectiveUserName, limit, offset);
        } else {
            return experimentService.getUserExperiments(effectiveGatewayId, "", limit, offset);
        }
    }

    @PostMapping("/{experimentId}/launch")
    public Map<String, Object> launchExperiment(HttpServletRequest request, @PathVariable String experimentId)
            throws Exception {
        var authzToken = AuthzTokenUtil.requireAuthzToken(request);
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        if (gatewayId == null) {
            gatewayId = "default";
        }
        experimentService.launchExperiment(authzToken, gatewayId, experimentId);
        return Map.of("launched", true, "experimentId", experimentId);
    }

    @PostMapping("/{experimentId}/cancel")
    public Map<String, Object> cancelExperiment(HttpServletRequest request, @PathVariable String experimentId)
            throws Exception {
        var authzToken = AuthzTokenUtil.requireAuthzToken(request);
        var experiment = experimentService.getExperiment(experimentId);
        if (experiment == null) {
            throw new ResourceNotFoundException("Experiment", experimentId);
        }
        return Map.of("cancelled", true, "experimentId", experimentId);
    }

    @PostMapping("/{experimentId}/clone")
    public ResponseEntity<Map<String, String>> cloneExperiment(
            HttpServletRequest request,
            @PathVariable String experimentId,
            @RequestParam(required = false) String newName,
            @RequestParam(required = false) String projectId)
            throws Exception {
        var authzToken = AuthzTokenUtil.requireAuthzToken(request);
        var existingExperiment = experimentService.getExperiment(experimentId);
        if (existingExperiment == null) {
            throw new ResourceNotFoundException("Experiment", experimentId);
        }
        String newExperimentId = experimentService.cloneExperiment(
                authzToken,
                experimentId,
                newName != null ? newName : existingExperiment.getExperimentName() + " (Clone)",
                projectId,
                existingExperiment);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("experimentId", newExperimentId));
    }
}
