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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import org.apache.airavata.config.ServerProperties;
import org.apache.airavata.core.exception.CoreExceptions.AiravataSystemException;
import org.apache.airavata.core.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.core.exception.ValidationExceptions.LaunchValidationException;
import org.apache.airavata.core.exception.ValidationExceptions.ValidationResults;
import org.apache.airavata.core.model.ProcessState;
import org.apache.airavata.core.model.StatusModel;
import org.apache.airavata.core.util.LoggingUtil;
import org.apache.airavata.execution.activity.ProcessActivityManager;
import org.apache.airavata.execution.event.LocalStatusEvent;
import org.apache.airavata.execution.model.ProcessModel;
import org.apache.airavata.execution.model.TaskTypes;
import org.apache.airavata.execution.service.ProcessService;
import org.apache.airavata.research.application.adapter.ApplicationAdapter;
import org.apache.airavata.research.application.model.ApplicationOutput;
import org.apache.airavata.research.experiment.exception.ExperimentExceptions.ExperimentNotFoundException;
import org.apache.airavata.research.experiment.model.ExperimentModel;
import org.apache.airavata.research.experiment.model.ExperimentState;
import org.apache.airavata.research.experiment.model.UserConfigurationDataModel;
import org.apache.airavata.research.experiment.service.ExperimentService;
import org.apache.airavata.research.experiment.util.ExperimentModelUtil;
import org.apache.airavata.status.model.ErrorModel;
import org.apache.airavata.status.model.ProcessStatusChangedEvent;
import org.apache.airavata.status.service.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final ApplicationAdapter applicationAdapter;
    private final ExperimentStatusManager experimentStatusManager;
    private final ProcessResourceResolver processResourceResolver;
    private final ServerProperties properties;
    private final ValidationService validationService;

    @Autowired(required = false)
    private ProcessActivityManager processActivityManager;

    public DefaultOrchestratorService(
            ExperimentService experimentService,
            ProcessService processService,
            StatusService statusService,
            ApplicationAdapter applicationAdapter,
            ExperimentStatusManager experimentStatusManager,
            ProcessResourceResolver processResourceResolver,
            ServerProperties properties,
            ValidationService validationService) {
        this.experimentService = experimentService;
        this.processService = processService;
        this.statusService = statusService;
        this.applicationAdapter = applicationAdapter;
        this.experimentStatusManager = experimentStatusManager;
        this.processResourceResolver = processResourceResolver;
        this.properties = properties;
        this.validationService = validationService;
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

    // -------------------------------------------------------------------------
    // Package-private helpers used by ExperimentStatusManager (back-reference)
    // -------------------------------------------------------------------------

    ExperimentModel getExperimentOrThrow(String experimentId) throws RegistryException {
        try {
            return experimentService.getExperiment(experimentId);
        } catch (AiravataSystemException e) {
            throw new RegistryException("Error retrieving experiment " + experimentId + ": " + e.getMessage(), e);
        }
    }

    boolean launchExperimentInternal(String experimentId, String gatewayId)
            throws ExperimentNotFoundException, OrchestratorException, RegistryException, LaunchValidationException {
        ExperimentModel experiment = getExperimentOrThrow(experimentId);

        UserConfigurationDataModel userConfigurationData = experiment.getUserConfigurationData();
        String token = processResourceResolver.getCredentialToken(experiment, userConfigurationData);

        return launchSingleAppExperiment(experiment, experimentId, gatewayId, token);
    }

    private boolean launchSingleAppExperiment(
            ExperimentModel experiment, String experimentId, String gatewayId, String token)
            throws OrchestratorException, RegistryException, LaunchValidationException {
        prepareProcesses(experiment, experimentId, gatewayId);

        if (isReadyToLaunch(experiment, experimentId)) {
            createAndValidateTasks(experiment, false);
            return true;
        } else {
            scheduleExperiment(experimentId, gatewayId);
            return false;
        }
    }

    @Override
    public boolean validateExperiment(String experimentId)
            throws LaunchValidationException, RegistryException, OrchestratorException {
        ExperimentModel experimentModel = getExperimentOrThrow(experimentId);
        return validationService.validateExperiment(experimentModel).getValidationState();
    }

    @Override
    public boolean validateProcess(String experimentId, List<ProcessModel> processes)
            throws LaunchValidationException, RegistryException, OrchestratorException {
        ExperimentModel experimentModel = getExperimentOrThrow(experimentId);
        for (ProcessModel processModel : processes) {
            boolean state = validationService
                    .validateProcess(experimentModel, processModel)
                    .getValidationState();
            if (!state) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean terminateExperiment(String experimentId, String gatewayId)
            throws RegistryException, OrchestratorException {
        logger.info("Experiment: {} is cancelling  !!!!!", experimentId);
        return validateStatesAndCancel(experimentId, gatewayId);
    }

    private boolean validateStatesAndCancel(String experimentId, String gatewayId)
            throws RegistryException, OrchestratorException {
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
                var token = processResourceResolver.getCredentialToken(
                        experimentModel, experimentModel.getUserConfigurationData());

                cancelExperimentProcesses(experimentModel, token);
                StatusModel<ExperimentState> status =
                        StatusModel.of(ExperimentState.CANCELING, "Experiment cancel request processed");
                experimentStatusManager.updateExperimentStatus(experimentId, status, gatewayId);
                logger.info("expId : " + experimentId + " :- Experiment status updated to " + status.getState());
                yield true;
            }
        };
    }

    @Override
    public void fetchIntermediateOutputs(String experimentId, String gatewayId, List<String> outputNames)
            throws RegistryException, OrchestratorException {
        submitIntermediateOutputsProcess(experimentId, gatewayId, outputNames);
    }

    private void submitIntermediateOutputsProcess(String experimentId, String gatewayId, List<String> outputNames)
            throws RegistryException, OrchestratorException {

        ExperimentModel experimentModel = getExperimentOrThrow(experimentId);
        ProcessModel processModel = ExperimentModelUtil.cloneProcessFromExperiment(experimentModel);
        processModel.setExperimentDataDir(processModel.getExperimentDataDir() + "/intermediates");

        List<ApplicationOutput> applicationOutputs =
                applicationAdapter.getApplicationOutputs(experimentModel.getApplicationId());
        List<ApplicationOutput> requestedOutputs = new ArrayList<>();

        for (ApplicationOutput output : applicationOutputs) {
            if (outputNames.contains(output.getName())) {
                requestedOutputs.add(output);
            }
        }
        processModel.setProcessOutputs(requestedOutputs);
        String processId = processService.addProcess(processModel, experimentId);
        processModel.setProcessId(processId);

        try {
            Optional<ProcessModel> jobSubmissionProcess = experimentModel.getProcesses().stream()
                    .filter(p -> p.getTasks().stream().anyMatch(t -> t.getTaskType() == TaskTypes.JOB_SUBMISSION))
                    .findFirst();
            if (!jobSubmissionProcess.isPresent()) {
                throw new OrchestratorException(MessageFormat.format(
                        "Could not find job submission process for experiment {0}, unable to fetch intermediate outputs {1}",
                        experimentId, outputNames));
            }
            processService.updateProcess(processModel, processModel.getProcessId());

            String token = processResourceResolver.getCredentialToken(
                    experimentModel, experimentModel.getUserConfigurationData());
            launchProcessWorkflow(processModel, token);
        } catch (RegistryException | OrchestratorException e) {
            logger.error("Failed to launch process for intermediate output fetching", e);

            StatusModel<ProcessState> status = StatusModel.of(
                    ProcessState.FAILED, "Intermediate output fetching process failed to launch: " + e.getMessage());
            statusService.addProcessStatus(status, processId);

            throw e;
        }
    }

    @Override
    public boolean launchProcess(String processId, String airavataCredStoreToken, String gatewayId)
            throws RegistryException, OrchestratorException {
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
                    logger.error("Could not find an application deployment for " + processModel.getComputeResourceId()
                            + " and application " + applicationId);
                    throw new OrchestratorException("Could not find an application deployment for "
                            + processModel.getComputeResourceId() + " and application " + applicationId);
                }
                var resourceSchedule = processModel.getProcessResourceSchedule();
                if (resourceSchedule != null && resourceSchedule.get("resourceHostId") != null) {
                    processModel.setComputeResourceId(
                            resourceSchedule.get("resourceHostId").toString());
                }
                processService.updateProcess(processModel, processModel.getProcessId());
                yield launchProcessWorkflow(processModel, airavataCredStoreToken);
            }
            default -> {
                logger.warn("Process " + processId + " is already launched. So it can not be relaunched");
                yield false;
            }
        };
    }

    // -------------------------------------------------------------------------
    // Shared launch pipeline helpers
    // -------------------------------------------------------------------------

    private List<ProcessModel> prepareProcesses(ExperimentModel experiment, String experimentId, String gatewayId)
            throws OrchestratorException, RegistryException, LaunchValidationException {
        List<ProcessModel> processes = createProcesses(experimentId, gatewayId);

        for (ProcessModel processModel : processes) {
            if (!experiment.getUserConfigurationData().getAiravataAutoSchedule()) {
                var resourceSchedule = processModel.getProcessResourceSchedule();
                var resourceHostId = resourceSchedule != null ? (String) resourceSchedule.get("resourceHostId") : null;
                if (resourceHostId == null) {
                    throw new OrchestratorException("Compute Resource Id cannot be null at this point");
                }
            }
            processService.updateProcess(processModel, processModel.getProcessId());
        }

        if (!experiment.getUserConfigurationData().getAiravataAutoSchedule()
                && !validateProcess(experimentId, processes)) {
            LaunchValidationException exception = new LaunchValidationException();
            ValidationResults validationResults = new ValidationResults();
            validationResults.setValidationState(false);
            validationResults.setValidationResultList(new ArrayList<>());
            exception.setValidationResult(validationResults);
            exception.setErrorMessage("Validating process fails for given experiment Id : " + experimentId);
            throw exception;
        }

        return processes;
    }

    private boolean isReadyToLaunch(ExperimentModel experiment, String experimentId) {
        return !experiment.getUserConfigurationData().getAiravataAutoSchedule() || canLaunchProcesses(experimentId);
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

    @Override
    public void createAndValidateTasks(ExperimentModel experiment, boolean recreateTaskDag)
            throws OrchestratorException, RegistryException, LaunchValidationException {
        if (experiment.getUserConfigurationData().getAiravataAutoSchedule()) {
            List<ProcessModel> processModels = processService.getProcessList(experiment.getExperimentId());
            for (ProcessModel processModel : processModels) {
                if (recreateTaskDag) {
                    var resourceSchedule = processModel.getProcessResourceSchedule();
                    var resourceHostId =
                            resourceSchedule != null ? (String) resourceSchedule.get("resourceHostId") : null;
                    if (resourceHostId == null) {
                        throw new OrchestratorException("Compute Resource Id cannot be null at this point");
                    }
                    processService.updateProcess(processModel, processModel.getProcessId());
                }
            }
            if (!validateProcess(experiment.getExperimentId(), processModels)) {
                LaunchValidationException exception = new LaunchValidationException();
                ValidationResults validationResults = new ValidationResults();
                validationResults.setValidationState(false);
                validationResults.setValidationResultList(new ArrayList<>());
                exception.setValidationResult(validationResults);
                exception.setErrorMessage(
                        "Validating process fails for given experiment Id : " + experiment.getExperimentId());
                throw exception;
            }
        }
    }

    @Override
    public void addProcessValidationErrors(String processId, ErrorModel details) throws RegistryException {
        statusService.addProcessError(details, processId);
    }

    @Override
    public boolean launchSingleAppExperimentInternal(
            String experimentId, String airavataCredStoreToken, String gatewayId)
            throws RegistryException, OrchestratorException {
        try {
            List<String> processIds = processService.getProcessIds(experimentId);
            for (String processId : processIds) {
                launchProcess(processId, airavataCredStoreToken, gatewayId);
            }
            return true;
        } catch (RegistryException e) {
            StatusModel<ExperimentState> status =
                    StatusModel.of(ExperimentState.FAILED, "Error while retrieving process IDs");
            experimentStatusManager.updateExperimentStatus(experimentId, status, gatewayId);
            logger.error("expId: " + experimentId + ", Error while retrieving process IDs", e);
            throw e;
        } catch (OrchestratorException e) {
            StatusModel<ExperimentState> status =
                    StatusModel.of(ExperimentState.FAILED, "Error while launching processes");
            experimentStatusManager.updateExperimentStatus(experimentId, status, gatewayId);
            logger.error("expId: " + experimentId + ", Error while launching processes", e);
            throw e;
        }
    }

    @Override
    public void launchQueuedExperiment(String experimentId)
            throws ExperimentNotFoundException, OrchestratorException, RegistryException, LaunchValidationException {
        ExperimentModel experiment = getExperimentOrThrow(experimentId);

        UserConfigurationDataModel userConfigurationData = experiment.getUserConfigurationData();
        String token = processResourceResolver.getCredentialToken(experiment, userConfigurationData);
        createAndValidateTasks(experiment, true);

        StatusModel<ExperimentState> status = StatusModel.of(ExperimentState.LAUNCHED, "submitted all processes");
        experimentStatusManager.updateExperimentStatus(experimentId, status, experiment.getGatewayId());
        logger.info("expId: {}, Launched experiment ", experimentId);

        launchSingleAppExperimentInternal(experimentId, token, experiment.getGatewayId());
    }

    @Override
    public StatusModel<ExperimentState> getExperimentStatus(String experimentId) throws RegistryException {
        return experimentStatusManager.getExperimentStatus(experimentId);
    }

    @Override
    public ProcessModel getProcess(String processId) throws RegistryException {
        return processService.getProcess(processId);
    }

    @Override
    public boolean launchExperiment(String experimentId, String gatewayId, ExecutorService executorService)
            throws OrchestratorException {
        try {
            boolean result = launchExperimentInternal(experimentId, gatewayId);
            if (result) {
                ExperimentModel experiment = getExperimentOrThrow(experimentId);
                String token =
                        processResourceResolver.getCredentialToken(experiment, experiment.getUserConfigurationData());
                StatusModel<ExperimentState> status =
                        StatusModel.of(ExperimentState.LAUNCHED, "submitted all processes");
                experimentStatusManager.updateExperimentStatus(experimentId, status, gatewayId);
                logger.info("expId: {}, Launched experiment ", experimentId);

                if (executorService != null) {
                    Runnable runner = () -> {
                        try {
                            launchSingleAppExperimentInternal(experimentId, token, gatewayId);
                        } catch (RegistryException | OrchestratorException e) {
                            logger.error("expId: " + experimentId + ", Error while launching single app experiment", e);
                        }
                    };
                    executorService.execute(LoggingUtil.withMDC(runner));
                }
            }
            return result;
        } catch (LaunchValidationException e) {
            throw experimentStatusManager.failExperiment(
                    experimentId, gatewayId, "Validation failed: " + e.getErrorMessage(), e);
        } catch (RegistryException e) {
            throw experimentStatusManager.failExperiment(
                    experimentId, gatewayId, "Database error: " + e.getMessage(), e);
        } catch (ExperimentNotFoundException e) {
            throw experimentStatusManager.failExperiment(
                    experimentId, gatewayId, "Unexpected error occurred: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            throw experimentStatusManager.failExperiment(
                    experimentId, gatewayId, "Unexpected runtime error occurred: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Process lifecycle (inlined from former SimpleOrchestrator)
    // -------------------------------------------------------------------------

    private boolean launchProcessWorkflow(ProcessModel processModel, String tokenId) throws OrchestratorException {
        if (processActivityManager == null) {
            throw new OrchestratorException(
                    "Process launch requires ProcessActivityManager" + " (airavata.services.controller.enabled=true)");
        }
        try {
            String processId = processModel.getProcessId();
            processActivityManager.launchPreWorkflow(processId, false);
            logger.info(
                    "Started ProcessPreWorkflow for process {} of experiment {}",
                    processId,
                    processModel.getExperimentId());
            return true;
        } catch (OrchestratorException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error launching the process", e);
            throw new OrchestratorException("Error launching the process", e);
        }
    }

    private void cancelExperimentProcesses(ExperimentModel experiment, String tokenId) throws OrchestratorException {
        logger.info("Terminating experiment {}", experiment.getExperimentId());

        try {
            if (processActivityManager == null) {
                throw new OrchestratorException(
                        "Cancel requires ProcessActivityManager" + " (airavata.services.controller.enabled=true)");
            }

            var processIds = processService.getProcessIds(experiment.getExperimentId());
            if (processIds != null && !processIds.isEmpty()) {
                String gatewayId = null;
                var credentialService =
                        processResourceResolver != null ? processResourceResolver.getCredentialEntityService() : null;
                if (credentialService != null) {
                    try {
                        gatewayId = credentialService.getGatewayId(tokenId);
                    } catch (Exception e) {
                        logger.error(e.getLocalizedMessage());
                    }
                }
                if (gatewayId == null || gatewayId.isEmpty()) {
                    gatewayId = properties != null ? properties.defaultGateway() : "default";
                }

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

    private List<ProcessModel> createProcesses(String experimentId, String gatewayId) throws OrchestratorException {
        try {
            var experimentModel = experimentService.getExperiment(experimentId);
            var processModels = processService.getProcessList(experimentId);
            if (processModels == null || processModels.isEmpty()) {
                var processModel = ExperimentModelUtil.cloneProcessFromExperiment(experimentModel);
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
