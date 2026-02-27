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
import org.apache.airavata.restapi.exception.ResourceNotFoundException;
import org.apache.airavata.workflow.model.WorkflowRun;
import org.apache.airavata.workflow.model.WorkflowRunStatus;
import org.apache.airavata.workflow.service.WorkflowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for top-level workflow run operations (not scoped to a
 * specific workflow). Endpoints that are scoped to a workflow parent (e.g.
 * starting or listing runs) are handled by {@link WorkflowController}.
 */
@RestController
@RequestMapping("/api/v1/workflow-runs")
@Tag(name = "Workflows")
public class WorkflowRunController {
    private static final Logger logger = LoggerFactory.getLogger(WorkflowRunController.class);

    private final WorkflowService workflowService;

    public WorkflowRunController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    /**
     * Returns a single workflow run by its identifier.
     *
     * @param runId the run identifier
     * @return the workflow run
     * @throws ResourceNotFoundException when no run exists for the given id
     */
    @GetMapping("/{runId}")
    public WorkflowRun getRun(@PathVariable("runId") String runId) {
        logger.debug("Fetching workflow run: {}", runId);
        WorkflowRun run = workflowService.getRun(runId);
        if (run == null) {
            throw new ResourceNotFoundException("WorkflowRun", runId);
        }
        return run;
    }

    /**
     * Updates the state of a single step within a run, recording the experiment
     * that corresponds to that step and its current status.
     *
     * @param runId        the run identifier
     * @param stepId       the step identifier within the workflow DAG
     * @param experimentId the Airavata experiment driving this step
     * @param status       the new status string for the step
     * @return the updated workflow run
     * @throws ResourceNotFoundException when no run exists for the given id
     */
    @PutMapping("/{runId}/steps/{stepId}")
    public WorkflowRun updateRunStepState(
            @PathVariable("runId") String runId,
            @PathVariable("stepId") String stepId,
            @RequestParam String experimentId,
            @RequestParam WorkflowRunStatus status) {
        logger.debug(
                "Updating step state: runId={}, stepId={}, experimentId={}, status={}",
                runId,
                stepId,
                experimentId,
                status);
        if (workflowService.getRun(runId) == null) {
            throw new ResourceNotFoundException("WorkflowRun", runId);
        }
        return workflowService.updateRunStepState(runId, stepId, experimentId, status);
    }

    /**
     * Updates the overall status of a workflow run (e.g. RUNNING, COMPLETED,
     * FAILED).
     *
     * @param runId  the run identifier
     * @param status the new status string for the run
     * @return the updated workflow run
     * @throws ResourceNotFoundException when no run exists for the given id
     */
    @PutMapping("/{runId}/status")
    public WorkflowRun updateRunStatus(@PathVariable("runId") String runId, @RequestParam WorkflowRunStatus status) {
        logger.debug("Updating run status: runId={}, status={}", runId, status);
        if (workflowService.getRun(runId) == null) {
            throw new ResourceNotFoundException("WorkflowRun", runId);
        }
        return workflowService.updateRunStatus(runId, status);
    }
}
