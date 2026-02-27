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
import java.util.List;
import org.apache.airavata.restapi.exception.ResourceNotFoundException;
import org.apache.airavata.workflow.model.Workflow;
import org.apache.airavata.workflow.model.WorkflowRun;
import org.apache.airavata.workflow.service.WorkflowService;
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

/**
 * REST controller for workflow DAG definitions and their execution runs.
 *
 * <p>Workflow definitions live under {@code /api/v1/workflows}. Workflow run
 * resources that require a top-level identifier (not scoped to a workflow) are
 * served by {@link WorkflowRunController} at {@code /api/v1/workflow-runs}.
 */
@RestController
@RequestMapping("/api/v1/workflows")
@Tag(name = "Workflows")
public class WorkflowController {
    private static final Logger logger = LoggerFactory.getLogger(WorkflowController.class);

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    // -------------------------------------------------------------------------
    // Workflow CRUD
    // -------------------------------------------------------------------------

    /**
     * Lists all workflows belonging to a project within a gateway.
     *
     * @param projectId  the project identifier
     * @param gatewayId  the gateway identifier
     * @return list of workflows; empty list when none match
     */
    @GetMapping
    public List<Workflow> getWorkflowsByProject(@RequestParam String projectId, @RequestParam String gatewayId) {
        logger.debug("Listing workflows: projectId={}, gatewayId={}", projectId, gatewayId);
        return workflowService.getWorkflowsByProject(projectId, gatewayId);
    }

    /**
     * Returns a single workflow by its identifier.
     *
     * @param workflowId the workflow identifier
     * @return the workflow definition
     * @throws ResourceNotFoundException when no workflow exists for the given id
     */
    @GetMapping("/{workflowId}")
    public Workflow getWorkflow(@PathVariable("workflowId") String workflowId) {
        logger.debug("Fetching workflow: {}", workflowId);
        Workflow workflow = workflowService.getWorkflow(workflowId);
        if (workflow == null) {
            throw new ResourceNotFoundException("Workflow", workflowId);
        }
        return workflow;
    }

    /**
     * Creates a new workflow DAG definition.
     *
     * @param workflow the workflow definition to persist
     * @return 201 Created with the persisted workflow (including generated id)
     */
    @PostMapping
    public ResponseEntity<Workflow> createWorkflow(@RequestBody Workflow workflow) {
        logger.debug("Creating workflow: name={}", workflow.getWorkflowName());
        Workflow created = workflowService.createWorkflow(workflow);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Replaces an existing workflow definition.
     *
     * @param workflowId the workflow identifier
     * @param workflow   the replacement definition
     * @return the updated workflow definition
     * @throws ResourceNotFoundException when no workflow exists for the given id
     */
    @PutMapping("/{workflowId}")
    public Workflow updateWorkflow(@PathVariable("workflowId") String workflowId, @RequestBody Workflow workflow) {
        logger.debug("Updating workflow: {}", workflowId);
        if (workflowService.getWorkflow(workflowId) == null) {
            throw new ResourceNotFoundException("Workflow", workflowId);
        }
        return workflowService.updateWorkflow(workflowId, workflow);
    }

    /**
     * Deletes a workflow definition and all associated data.
     *
     * @param workflowId the workflow identifier
     * @return 200 OK on success
     * @throws ResourceNotFoundException when no workflow exists for the given id
     */
    @DeleteMapping("/{workflowId}")
    public ResponseEntity<Void> deleteWorkflow(@PathVariable("workflowId") String workflowId) {
        logger.debug("Deleting workflow: {}", workflowId);
        if (workflowService.getWorkflow(workflowId) == null) {
            throw new ResourceNotFoundException("Workflow", workflowId);
        }
        workflowService.deleteWorkflow(workflowId);
        return ResponseEntity.ok().build();
    }

    // -------------------------------------------------------------------------
    // Workflow Runs (scoped to a workflow: /workflows/{workflowId}/runs)
    // -------------------------------------------------------------------------

    /**
     * Starts a new execution run for the specified workflow.
     *
     * @param workflowId the workflow to execute
     * @param userName   the user initiating the run
     * @return 201 Created with the newly created {@link WorkflowRun}
     * @throws ResourceNotFoundException when no workflow exists for the given id
     */
    @PostMapping("/{workflowId}/runs")
    public ResponseEntity<WorkflowRun> startRun(
            @PathVariable("workflowId") String workflowId, @RequestParam String userName) {
        logger.debug("Starting run: workflowId={}, userName={}", workflowId, userName);
        if (workflowService.getWorkflow(workflowId) == null) {
            throw new ResourceNotFoundException("Workflow", workflowId);
        }
        WorkflowRun run = workflowService.createRun(workflowId, userName);
        return ResponseEntity.status(HttpStatus.CREATED).body(run);
    }

    /**
     * Lists all execution runs for the specified workflow.
     *
     * @param workflowId the workflow identifier
     * @return list of runs; empty list when none exist
     * @throws ResourceNotFoundException when no workflow exists for the given id
     */
    @GetMapping("/{workflowId}/runs")
    public List<WorkflowRun> getRunsByWorkflow(@PathVariable("workflowId") String workflowId) {
        logger.debug("Listing runs for workflowId={}", workflowId);
        if (workflowService.getWorkflow(workflowId) == null) {
            throw new ResourceNotFoundException("Workflow", workflowId);
        }
        return workflowService.getRunsByWorkflow(workflowId);
    }
}
