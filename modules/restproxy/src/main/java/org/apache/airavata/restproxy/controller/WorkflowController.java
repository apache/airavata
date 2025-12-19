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

import java.util.Map;
import org.apache.airavata.common.model.AiravataWorkflow;
import org.apache.airavata.registry.exception.WorkflowCatalogException;
import org.apache.airavata.registry.services.WorkflowService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workflows")
public class WorkflowController {
    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping("/{workflowId}")
    public ResponseEntity<?> getWorkflow(@PathVariable String workflowId) {
        try {
            AiravataWorkflow workflow = workflowService.getWorkflow(workflowId);
            if (workflow == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(workflow);
        } catch (WorkflowCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> registerWorkflow(
            @RequestParam String experimentId, @RequestBody AiravataWorkflow workflow) {
        try {
            workflowService.registerWorkflow(workflow, experimentId);
            String workflowId = workflowService.getWorkflowId(experimentId);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("workflowId", workflowId));
        } catch (WorkflowCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/experiment/{experimentId}")
    public ResponseEntity<?> getWorkflowByExperiment(@PathVariable String experimentId) {
        try {
            String workflowId = workflowService.getWorkflowId(experimentId);
            if (workflowId == null) {
                return ResponseEntity.notFound().build();
            }
            AiravataWorkflow workflow = workflowService.getWorkflow(workflowId);
            return ResponseEntity.ok(workflow);
        } catch (WorkflowCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}


