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
package org.apache.airavata.execution.orchestration;

import org.apache.airavata.research.experiment.exception.ExperimentExceptions.ExperimentNotFoundException;
import org.apache.airavata.core.exception.ValidationExceptions.LaunchValidationException;
import org.apache.airavata.status.model.ProcessStatusChangedEvent;
import org.apache.airavata.core.model.StatusModel;
import org.apache.airavata.research.experiment.entity.ExperimentEntity;
import org.apache.airavata.research.experiment.model.ExperimentState;
import org.apache.airavata.research.experiment.repository.ExperimentRepository;
import org.apache.airavata.core.model.ResourceIdentifier;
import org.apache.airavata.execution.service.ProcessService;
import org.apache.airavata.core.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.execution.model.TaskTypes;
import org.apache.airavata.execution.state.StateValidators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Manages experiment status transitions and event handling for the orchestrator.
 *
 * <p>Responsible for the experiment state machine: translating process-level status change events
 * into valid experiment state transitions and persisting state updates.
 */
@Service
public class ExperimentStatusManager {

    private static final Logger logger = LoggerFactory.getLogger(ExperimentStatusManager.class);

    private final ProcessService processService;
    private final ExperimentRepository experimentRepository;

    /**
     * Back-reference to OrchestratorService for operations that require the full launch pipeline
     * (e.g. re-queued experiments, fetch intermediate outputs). Lazy to break the circular
     * dependency between OrchestratorService and ExperimentStatusManager.
     */
    private final OrchestratorService orchestratorService;

    public ExperimentStatusManager(
            ProcessService processService,
            ExperimentRepository experimentRepository,
            @Lazy OrchestratorService orchestratorService) {
        this.processService = processService;
        this.experimentRepository = experimentRepository;
        this.orchestratorService = orchestratorService;
    }

    // -------------------------------------------------------------------------
    // Status update
    // -------------------------------------------------------------------------

    /**
     * Updates experiment status in the registry only. No pub/sub.
     *
     * <p>Idempotent updates (e.g. FAILED {@literal ->} FAILED) are allowed via
     * {@code StateValidators} terminal self-transitions.
     */
    public void updateExperimentStatus(String experimentId, StatusModel<ExperimentState> status, String gatewayId) {
        try {
            ExperimentEntity experimentEntity = experimentRepository.findById(experimentId).orElse(null);
            ExperimentState currentState = null;
            if (experimentEntity != null && experimentEntity.getState() != null) {
                try {
                    currentState = ExperimentState.valueOf(experimentEntity.getState());
                } catch (IllegalArgumentException e) {
                    // Unknown stored state — treat as null so any transition is allowed.
                    logger.debug("Unknown stored experiment state '{}' for experimentId={}; treating as null",
                            experimentEntity.getState(), experimentId, e);
                }
            }

            if (!StateValidators.StateTransitionService.validateAndLog(
                    StateValidators.ExperimentStateValidator.INSTANCE,
                    currentState,
                    status.getState(),
                    experimentId,
                    "experiment")) {
                logger.warn(
                        "Invalid experiment state transition rejected: experimentId={}, {} -> {}",
                        experimentId,
                        currentState != null ? currentState.name() : "(initial)",
                        status.getState().name());
                return;
            }

            if (experimentEntity != null) {
                experimentEntity.setState(status.getState().name());
                experimentRepository.save(experimentEntity);
            } else {
                logger.error("expId : {} Cannot update state — experiment entity not found", experimentId);
            }
        } catch (Exception e) {
            logger.error("expId : " + experimentId + " Error updating experiment status to " + status.toString(), e);
        }
    }

    /**
     * Records a FAILED experiment status and returns an {@link OrchestratorException} wrapping the
     * cause. The caller is expected to throw the returned exception.
     */
    public OrchestratorException failExperiment(String experimentId, String gatewayId, String reason, Exception cause) {
        StatusModel<ExperimentState> status = StatusModel.of(ExperimentState.FAILED, reason);
        updateExperimentStatus(experimentId, status, gatewayId);
        return new OrchestratorException("Experiment '" + experimentId + "' launch failed. " + reason, cause);
    }

    /**
     * Returns the current state of the experiment as a {@link StatusModel}, read directly from
     * the experiment entity's {@code state} column.
     */
    public StatusModel<ExperimentState> getExperimentStatus(String experimentId) throws RegistryException {
        ExperimentEntity experimentEntity = experimentRepository.findById(experimentId).orElse(null);
        if (experimentEntity == null || experimentEntity.getState() == null) {
            return null;
        }
        StatusModel<ExperimentState> model = new StatusModel<>();
        try {
            model.setState(ExperimentState.valueOf(experimentEntity.getState()));
        } catch (IllegalArgumentException e) {
            logger.warn("Unknown experiment state '{}' for experimentId={}", experimentEntity.getState(), experimentId);
            return null;
        }
        return model;
    }

    // -------------------------------------------------------------------------
    // Process status event handling
    // -------------------------------------------------------------------------

    /**
     * Translates a {@link ProcessStatusChangedEvent} into the corresponding experiment state
     * transition and persists it.
     */
    public void handleProcessStatusChange(
            ProcessStatusChangedEvent processStatusChangeEvent, ResourceIdentifier processIdentity)
            throws ExperimentNotFoundException, OrchestratorException, RegistryException, LaunchValidationException {
        StatusModel<ExperimentState> status = null;

        // Intermediate output-fetching processes must not drive experiment status.
        var process = processService.getProcess(processIdentity.getProcessId());
        boolean isIntermediateOutputFetchingProcess =
                process.getTasks().stream().anyMatch(t -> t.getTaskType() == TaskTypes.OUTPUT_FETCHING);
        if (isIntermediateOutputFetchingProcess) {
            logger.info("Not updating experiment status because process is an intermediate output fetching one");
            return;
        }

        ExperimentState currentExpState = experimentRepository
                .findById(processIdentity.getExperimentId())
                .map(e -> {
                    try {
                        return ExperimentState.valueOf(e.getState());
                    } catch (IllegalArgumentException | NullPointerException ex) {
                        // Unknown or null stored state for experiment; default to CREATED.
                        logger.debug("Unknown stored experiment state '{}' for experimentId={}; defaulting to CREATED",
                                e.getState(), processIdentity.getExperimentId(), ex);
                        return ExperimentState.CREATED;
                    }
                })
                .orElse(ExperimentState.CREATED);

        switch (processStatusChangeEvent.getState()) {
            case LAUNCHED -> {
                if (currentExpState == ExperimentState.CANCELING) {
                    status = StatusModel.of(ExperimentState.CANCELING, "Process started but experiment cancelling is triggered");
                } else {
                    status = StatusModel.of(ExperimentState.EXECUTING, "process  started");
                }
            }
            case COMPLETED -> {
                if (currentExpState == ExperimentState.CANCELING) {
                    status = StatusModel.of(ExperimentState.CANCELED, "Process competed but experiment cancelling is triggered");
                } else {
                    // If experiment is still LAUNCHED (STARTED event was not delivered),
                    // transition to EXECUTING first so the COMPLETED transition is valid.
                    if (currentExpState == ExperimentState.LAUNCHED) {
                        StatusModel<ExperimentState> execStatus = StatusModel.of(ExperimentState.EXECUTING, "process started (inferred from completion)");
                        updateExperimentStatus(
                                processIdentity.getExperimentId(), execStatus, processIdentity.getGatewayId());
                    }
                    status = StatusModel.of(ExperimentState.COMPLETED, "process  completed");
                }
            }
            case FAILED -> {
                if (currentExpState == ExperimentState.CANCELING) {
                    status = StatusModel.of(ExperimentState.CANCELED, "Process failed but experiment cancelling is triggered");
                } else {
                    status = StatusModel.of(ExperimentState.FAILED, "process  failed");
                }
            }
            case CANCELED -> {
                status = StatusModel.of(ExperimentState.CANCELED, "process  cancelled");
            }
            case QUEUED -> {
                status = StatusModel.of(ExperimentState.SCHEDULED, "Process started but compute resource not avaialable");
            }
            case REQUEUED -> {
                status = StatusModel.of(ExperimentState.SCHEDULED, "Job submission failed,  requeued to resubmit");
            }
            case DEQUEUING -> {
                if (currentExpState == ExperimentState.CANCELING) {
                    status = StatusModel.of(ExperimentState.CANCELING, "Process started but experiment cancelling is triggered");
                } else {
                    orchestratorService.launchQueuedExperiment(processIdentity.getExperimentId());
                }
            }
            default -> {
                // ignore other status changes
                return;
            }
        }

        if (status != null) {
            updateExperimentStatus(processIdentity.getExperimentId(), status, processIdentity.getGatewayId());
            logger.info("expId : " + processIdentity.getExperimentId() + " :- Experiment status updated to "
                    + status.getState());
        }
    }

}
