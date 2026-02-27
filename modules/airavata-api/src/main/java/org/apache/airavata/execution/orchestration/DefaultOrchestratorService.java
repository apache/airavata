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

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import org.apache.airavata.config.ServerProperties;
import org.apache.airavata.core.exception.CoreExceptions.AiravataSystemException;
import org.apache.airavata.core.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.core.exception.ValidationExceptions.LaunchValidationException;
import org.apache.airavata.core.model.ProcessState;
import org.apache.airavata.core.model.StatusModel;
import org.apache.airavata.core.util.IdGenerator;
import org.apache.airavata.core.util.LoggingUtil;
import org.apache.airavata.execution.activity.ProcessActivityManager;
import org.apache.airavata.execution.process.ProcessModel;
import org.apache.airavata.execution.process.ProcessService;
import org.apache.airavata.research.experiment.exception.ExperimentExceptions.ExperimentNotFoundException;
import org.apache.airavata.research.experiment.model.Experiment;
import org.apache.airavata.research.experiment.model.ExperimentState;
import org.apache.airavata.research.experiment.service.ExperimentService;
import org.apache.airavata.research.experiment.util.ExperimentUtil;
import org.apache.airavata.status.model.ErrorModel;
import org.apache.airavata.status.model.ExperimentDequeueEvent;
import org.apache.airavata.status.model.ProcessStatusChangedEvent;
import org.apache.airavata.status.service.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link OrchestratorService}.
 *
 * <p>Owns the top-level launch pipeline for single-application and workflow experiments.
 * Status transitions are delegated to {@link ExperimentStatusManager}; credential and deployment
 * resolution is delegated to {@link ProcessResourceResolver}.
 */
@Service
@Lazy(false)
@Profile({"!test", "orchestrator-integration"})
public class DefaultOrchestratorService implements OrchestratorService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultOrchestratorService.class);

    private final ExperimentService experimentService;
    private final ProcessService processService;
    private final StatusService statusService;
    private final ExperimentStatusManager experimentStatusManager;
    private final ProcessResourceResolver processResourceResolver;
    private final ServerProperties properties;
    private final ProcessActivityManager processActivityManager;

    public DefaultOrchestratorService(
            ExperimentService experimentService,
            ProcessService processService,
            StatusService statusService,
            ExperimentStatusManager experimentStatusManager,
            ProcessResourceResolver processResourceResolver,
            ServerProperties properties,
            @org.springframework.lang.Nullable ProcessActivityManager processActivityManager) {
        this.experimentService = experimentService;
        this.processService = processService;
        this.statusService = statusService;
        this.experimentStatusManager = experimentStatusManager;
        this.processResourceResolver = processResourceResolver;
        this.properties = properties;
        this.processActivityManager = processActivityManager;
    }

    @PostConstruct
    public void postConstruct() {
        logger.info("[BEAN-INIT] DefaultOrchestratorService initialized");
    }

    /**
     * Handle process status changes published as Spring application events.
     */
    @EventListener
    public void onProcessStatusChanged(LocalStatusEvent<ProcessStatusChangedEvent> event) {
        var processEvent = event.getStatusEvent();
        var identity = processEvent.getProcessIdentity();
        logger.info(
                "Received ProcessStatusChangedEvent: processId={}, state={}",
                identity.getProcessId(),
                processEvent.getState());
        try {
            experimentStatusManager.handleProcessStatusChange(processEvent, identity);
        } catch (Exception e) {
            logger.error("Error handling process status change for process {}", identity.getProcessId(), e);
        }
    }

    @EventListener
    public void onExperimentDequeue(ExperimentDequeueEvent event) {
        try {
            launchQueuedExperiment(event.experimentId());
        } catch (Exception e) {
            logger.error("Error re-launching dequeued experiment {}", event.experimentId(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Package-private helpers
    // -------------------------------------------------------------------------

    Experiment getExperimentOrThrow(String experimentId) throws RegistryException {
        try {
            return experimentService.getExperiment(experimentId);
        } catch (AiravataSystemException e) {
            throw new RegistryException("Error retrieving experiment " + experimentId + ": " + e.getMessage(), e);
        }
    }

    boolean launchExperimentInternal(String experimentId, String gatewayId)
            throws ExperimentNotFoundException, OrchestratorException, RegistryException, LaunchValidationException {
        Experiment experiment = getExperimentOrThrow(experimentId);
        prepareProcesses(experiment, gatewayId);

        if (isReadyToLaunch(experiment)) {
            createAndValidateTasks(experiment, false);
            return true;
        } else {
            scheduleExperiment(experimentId, gatewayId);
            return false;
        }
    }

    private boolean validateProcess(Experiment experiment, List<ProcessModel> processes)
            throws LaunchValidationException, OrchestratorException {
        for (ProcessModel processModel : processes) {
            if (!validateExperimentState(experiment, processModel.getProcessId())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Validates that the experiment is in the CREATED state (required before launch).
     * If validation is disabled via config, always returns true. On failure, records an
     * error against the given entity ID and throws {@link LaunchValidationException}.
     */
    private boolean validateExperimentState(Experiment experiment, String errorEntityId)
            throws OrchestratorException, LaunchValidationException {
        if (!properties.validationEnabled()) {
            return true;
        }

        if (ExperimentState.CREATED.equals(experiment.getState())) {
            logger.info("Validation of experiment status is SUCCESSFUL");
            return true;
        }

        String error = "During the validation step experiment status should be CREATED, "
                + "But this experiment status is : " + experiment.getState();
        logger.error(error);
        logger.error(
                "Validation of experiment status for {} is FAILED:[error]. Validation Errors : {}",
                errorEntityId,
                error);

        var errorModel = new ErrorModel();
        errorModel.setActualErrorMessage("Validation Errors : " + error + " ");
        errorModel.setCreatedAt(IdGenerator.getUniqueTimestamp().toEpochMilli());
        try {
            statusService.addProcessError(errorModel, errorEntityId);
        } catch (RegistryException e) {
            throw new OrchestratorException("Error while saving error details to database", e);
        }

        var launchException = new LaunchValidationException();
        launchException.setErrorMessage("Validation failed: " + error);
        throw launchException;
    }

    @Override
    public boolean terminateExperiment(String experimentId, String gatewayId)
            throws RegistryException, OrchestratorException {
        logger.info("Experiment: {} is cancelling", experimentId);
        var experimentStatus = experimentStatusManager.getExperimentStatus(experimentId);
        return switch (experimentStatus.getState()) {
            case COMPLETED, CANCELED, FAILED, CANCELING -> {
                logger.warn(
                        "Can't terminate already {} experiment",
                        experimentStatus.getState().name());
                yield false;
            }
            case CREATED -> {
                logger.warn("Experiment termination is only allowed for launched experiments.");
                yield false;
            }
            default -> {
                var experimentModel = getExperimentOrThrow(experimentId);
                cancelExperimentProcesses(experimentModel, gatewayId);
                StatusModel<ExperimentState> status =
                        StatusModel.of(ExperimentState.CANCELING, "Experiment cancel request processed");
                experimentStatusManager.updateExperimentStatus(experimentId, status, gatewayId);
                logger.info("expId : {} :- Experiment status updated to {}", experimentId, status.getState());
                yield true;
            }
        };
    }

    @Override
    public void fetchIntermediateOutputs(String experimentId, String gatewayId, List<String> outputNames)
            throws RegistryException, OrchestratorException {
        Experiment experimentModel = getExperimentOrThrow(experimentId);
        ProcessModel processModel = ExperimentUtil.cloneProcessFromExperiment(experimentModel);
        processModel.setExperimentDataDir(processModel.getExperimentDataDir() + "/intermediates");

        String processId = processService.addProcess(processModel, experimentId);
        processModel.setProcessId(processId);

        try {
            processService.updateProcess(processModel, processModel.getProcessId());
            launchProcessWorkflow(processModel);
        } catch (RegistryException | OrchestratorException e) {
            logger.error("Failed to launch process for intermediate output fetching", e);

            StatusModel<ProcessState> status = StatusModel.of(
                    ProcessState.FAILED, "Intermediate output fetching process failed to launch: " + e.getMessage());
            statusService.addProcessStatus(status, processId);

            throw e;
        }
    }

    private boolean launchProcess(String processId, String gatewayId) throws RegistryException, OrchestratorException {
        var processStatus = statusService.getLatestProcessStatus(processId);

        return switch (processStatus.getState()) {
            case CREATED, VALIDATED, DEQUEUING -> {
                var processModel = processService.getProcess(processId);
                var applicationId = processModel.getApplicationInterfaceId();
                if (applicationId == null) {
                    logger.error("Application interface id shouldn't be null for process {}", processId);
                    throw new OrchestratorException(
                            "Error executing the job, application interface id shouldn't be null.");
                }
                var applicationDeploymentDescription =
                        processResourceResolver.getAppDeployment(processModel, applicationId);
                if (applicationDeploymentDescription == null) {
                    logger.error(
                            "Could not find an application deployment for {} and application {}",
                            processModel.getComputeResourceId(),
                            applicationId);
                    throw new OrchestratorException("Could not find an application deployment for "
                            + processModel.getComputeResourceId() + " and application " + applicationId);
                }
                var resourceSchedule = processModel.getResourceSchedule();
                if (resourceSchedule != null && resourceSchedule.get("resourceHostId") != null) {
                    processModel.setComputeResourceId(
                            resourceSchedule.get("resourceHostId").toString());
                }
                processService.updateProcess(processModel, processModel.getProcessId());
                yield launchProcessWorkflow(processModel);
            }
            default -> {
                logger.warn("Process {} is already launched. So it can not be relaunched", processId);
                yield false;
            }
        };
    }

    // -------------------------------------------------------------------------
    // Shared launch pipeline helpers
    // -------------------------------------------------------------------------

    private List<ProcessModel> prepareProcesses(Experiment experiment, String gatewayId)
            throws OrchestratorException, RegistryException, LaunchValidationException {
        String experimentId = experiment.getExperimentId();
        List<ProcessModel> processes = createProcesses(experiment);

        for (ProcessModel processModel : processes) {
            if (!experiment.getUserConfigurationData().getAiravataAutoSchedule()) {
                requireResourceHostId(processModel);
            }
            processService.updateProcess(processModel, processModel.getProcessId());
        }

        if (!experiment.getUserConfigurationData().getAiravataAutoSchedule()
                && !validateProcess(experiment, processes)) {
            throw validationFailure(experimentId);
        }

        return processes;
    }

    private boolean isReadyToLaunch(Experiment experiment) {
        return !experiment.getUserConfigurationData().getAiravataAutoSchedule()
                || canLaunchProcesses(experiment.getExperimentId());
    }

    private boolean canLaunchProcesses(String experimentId) {
        try {
            var processModels = processService.getProcessList(experimentId);
            boolean allProcessesScheduled = true;
            for (var processModel : processModels) {
                var processStatus = statusService.getLatestProcessStatus(processModel.getProcessId());
                if (processStatus.getState().equals(ProcessState.CREATED)
                        || processStatus.getState().equals(ProcessState.VALIDATED)) {
                    StatusModel<ProcessState> newProcessStatus = StatusModel.of(ProcessState.QUEUED);
                    statusService.addProcessStatus(newProcessStatus, processModel.getProcessId());
                    allProcessesScheduled = false;
                }
            }
            return allProcessesScheduled;
        } catch (Exception e) {
            logger.error("Exception while scheduling experiment {}", experimentId, e);
        }
        return false;
    }

    private void scheduleExperiment(String experimentId, String gatewayId) throws RegistryException {
        StatusModel<ExperimentState> status =
                StatusModel.of(ExperimentState.SCHEDULED, "Compute resources are not ready");
        experimentStatusManager.updateExperimentStatus(experimentId, status, gatewayId);
        logger.info("expId: {}, Scheduled experiment", experimentId);
    }

    private void createAndValidateTasks(Experiment experiment, boolean recreateTaskDag)
            throws OrchestratorException, RegistryException, LaunchValidationException {
        if (experiment.getUserConfigurationData().getAiravataAutoSchedule()) {
            List<ProcessModel> processModels = processService.getProcessList(experiment.getExperimentId());
            for (ProcessModel processModel : processModels) {
                if (recreateTaskDag) {
                    requireResourceHostId(processModel);
                    processService.updateProcess(processModel, processModel.getProcessId());
                }
            }
            if (!validateProcess(experiment, processModels)) {
                throw validationFailure(experiment.getExperimentId());
            }
        }
    }

    private boolean launchSingleAppExperimentInternal(String experimentId, String gatewayId)
            throws RegistryException, OrchestratorException {
        try {
            List<String> processIds = processService.getProcessIds(experimentId);
            for (String processId : processIds) {
                launchProcess(processId, gatewayId);
            }
            return true;
        } catch (RegistryException | OrchestratorException e) {
            StatusModel<ExperimentState> status =
                    StatusModel.of(ExperimentState.FAILED, "Error while launching processes: " + e.getMessage());
            experimentStatusManager.updateExperimentStatus(experimentId, status, gatewayId);
            logger.error("expId: {}, Error while launching processes", experimentId, e);
            throw e;
        }
    }

    @Override
    public void launchQueuedExperiment(String experimentId)
            throws ExperimentNotFoundException, OrchestratorException, RegistryException, LaunchValidationException {
        Experiment experiment = getExperimentOrThrow(experimentId);
        createAndValidateTasks(experiment, true);

        StatusModel<ExperimentState> status = StatusModel.of(ExperimentState.LAUNCHED, "submitted all processes");
        experimentStatusManager.updateExperimentStatus(experimentId, status, experiment.getGatewayId());
        logger.info("expId: {}, Launched experiment ", experimentId);

        launchSingleAppExperimentInternal(experimentId, experiment.getGatewayId());
    }

    @Override
    public boolean launchExperiment(String experimentId, String gatewayId, ExecutorService executorService)
            throws OrchestratorException {
        try {
            boolean result = launchExperimentInternal(experimentId, gatewayId);
            if (result) {
                Experiment experiment = getExperimentOrThrow(experimentId);
                StatusModel<ExperimentState> status =
                        StatusModel.of(ExperimentState.LAUNCHED, "submitted all processes");
                experimentStatusManager.updateExperimentStatus(experimentId, status, gatewayId);
                logger.info("expId: {}, Launched experiment ", experimentId);

                if (executorService != null) {
                    Runnable runner = () -> {
                        try {
                            launchSingleAppExperimentInternal(experimentId, gatewayId);
                        } catch (RegistryException | OrchestratorException e) {
                            logger.error("expId: {}, Error while launching single app experiment", experimentId, e);
                        }
                    };
                    executorService.execute(LoggingUtil.withMDC(runner));
                }
            }
            return result;
        } catch (LaunchValidationException e) {
            throw experimentStatusManager.failExperiment(
                    experimentId, gatewayId, "Validation failed: " + e.getErrorMessage(), e);
        } catch (Exception e) {
            throw experimentStatusManager.failExperiment(
                    experimentId, gatewayId, "Error launching experiment: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Process lifecycle (inlined from former SimpleOrchestrator)
    // -------------------------------------------------------------------------

    private boolean launchProcessWorkflow(ProcessModel processModel) throws OrchestratorException {
        if (processActivityManager == null) {
            throw new OrchestratorException(
                    "Process launch requires ProcessActivityManager (airavata.services.controller.enabled=true)");
        }
        try {
            String processId = processModel.getProcessId();
            processActivityManager.launchPreWorkflow(processId, false);
            logger.info(
                    "Started ProcessPreWorkflow for process {} of experiment {}",
                    processId,
                    processModel.getExperimentId());
            return true;
        } catch (Exception e) {
            logger.error("Error launching the process", e);
            throw new OrchestratorException("Error launching the process", e);
        }
    }

    private void cancelExperimentProcesses(Experiment experiment, String gatewayId) throws OrchestratorException {
        logger.info("Terminating experiment {}", experiment.getExperimentId());

        try {
            if (processActivityManager == null) {
                throw new OrchestratorException(
                        "Cancel requires ProcessActivityManager" + " (airavata.services.controller.enabled=true)");
            }

            var processIds = processService.getProcessIds(experiment.getExperimentId());
            if (processIds != null && !processIds.isEmpty()) {
                for (String processId : processIds) {
                    logger.info("Terminating process {} of experiment {}", processId, experiment.getExperimentId());
                    processActivityManager.launchCancelWorkflow(processId, gatewayId);
                }
            } else {
                logger.warn("No processes found for experiment {} to cancel", experiment.getExperimentId());
            }
        } catch (RegistryException e) {
            logger.error("Failed to fetch process ids for experiment {}", experiment.getExperimentId(), e);
            throw new OrchestratorException(
                    "Failed to fetch process ids for experiment " + experiment.getExperimentId(), e);
        } catch (Exception e) {
            logger.error("Failed to schedule cancel workflow", e);
            throw new OrchestratorException("Failed to terminate experiment", e);
        }
    }

    // -------------------------------------------------------------------------
    // Process and task DAG creation (inlined from former SimpleOrchestrator)
    // -------------------------------------------------------------------------

    private static void requireResourceHostId(ProcessModel processModel) throws OrchestratorException {
        var schedule = processModel.getResourceSchedule();
        var hostId = schedule != null ? (String) schedule.get("resourceHostId") : null;
        if (hostId == null) {
            throw new OrchestratorException("Compute Resource Id cannot be null at this point");
        }
    }

    private static LaunchValidationException validationFailure(String experimentId) {
        var exception = new LaunchValidationException();
        exception.setErrorMessage("Validating process fails for given experiment Id : " + experimentId);
        return exception;
    }

    private List<ProcessModel> createProcesses(Experiment experiment) throws OrchestratorException {
        try {
            String experimentId = experiment.getExperimentId();
            var processModels = processService.getProcessList(experimentId);
            if (processModels == null || processModels.isEmpty()) {
                var processModel = ExperimentUtil.cloneProcessFromExperiment(experiment);
                var processId = processService.addProcess(processModel, experimentId);
                processModel.setProcessId(processId);
                processModels = new ArrayList<>();
                processModels.add(processModel);
            }
            return processModels;
        } catch (Exception e) {
            throw new OrchestratorException("Error during creating process", e);
        }
    }
}
