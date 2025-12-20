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
package org.apache.airavata.restproxy.controller;

import java.util.List;
import java.util.Map;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.utils.DBConstants;
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

@RestController
@RequestMapping("/api/v1/experiments")
@ConditionalOnProperty(name = "services.rest.enabled", havingValue = "true", matchIfMissing = false)
public class ExperimentController {
    private final ExperimentService experimentService;

    public ExperimentController(ExperimentService experimentService) {
        this.experimentService = experimentService;
    }

    @GetMapping("/{experimentId}")
    public ResponseEntity<?> getExperiment(@PathVariable String experimentId) {
        try {
            ExperimentModel experiment = experimentService.getExperiment(experimentId);
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
            String experimentId = experimentService.addExperiment(experiment);
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
            @RequestParam(required = false) String gatewayId,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String projectId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        try {
            List<ExperimentModel> experiments;
            if (gatewayId != null && userName != null) {
                experiments = experimentService.getExperimentList(
                        gatewayId, DBConstants.Experiment.USER_NAME, userName, limit, offset, null, null);
            } else if (gatewayId != null && projectId != null) {
                experiments = experimentService.getExperimentList(
                        gatewayId, DBConstants.Experiment.PROJECT_ID, projectId, limit, offset, null, null);
            } else {
                return ResponseEntity.badRequest()
                        .body("Either gatewayId+userName or gatewayId+projectId must be provided");
            }
            return ResponseEntity.ok(experiments);
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
