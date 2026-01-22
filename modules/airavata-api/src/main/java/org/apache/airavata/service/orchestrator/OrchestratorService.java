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
package org.apache.airavata.service.orchestrator;

import jakarta.annotation.PostConstruct;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import org.apache.airavata.common.exception.ExperimentNotFoundException;
import org.apache.airavata.common.exception.LaunchValidationException;
import org.apache.airavata.common.exception.ValidationResults;
import org.apache.airavata.common.logging.LoggingUtil;
import org.apache.airavata.common.model.ApplicationDeploymentDescription;
import org.apache.airavata.common.model.ApplicationInterfaceDescription;
import org.apache.airavata.common.model.ComputationalResourceSchedulingModel;
import org.apache.airavata.common.model.ComputeResourceDescription;
import org.apache.airavata.common.model.DataProductModel;
import org.apache.airavata.common.model.DataReplicaLocationModel;
import org.apache.airavata.common.model.DataType;
import org.apache.airavata.common.model.ErrorModel;
import org.apache.airavata.common.model.ExperimentIntermediateOutputsEvent;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ExperimentState;
import org.apache.airavata.common.model.ExperimentStatus;
import org.apache.airavata.common.model.ExperimentSubmitEvent;
import org.apache.airavata.common.model.ExperimentType;
import org.apache.airavata.common.model.GroupComputeResourcePreference;
import org.apache.airavata.common.model.GroupResourceProfile;
import org.apache.airavata.common.model.OutputDataObjectType;
import org.apache.airavata.common.model.ProcessIdentifier;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.common.model.ProcessStatus;
import org.apache.airavata.common.model.ProcessStatusChangeEvent;
import org.apache.airavata.common.model.QueueStatusModel;
import org.apache.airavata.common.model.ReplicaLocationCategory;
import org.apache.airavata.common.model.TaskTypes;
import org.apache.airavata.common.model.UserConfigurationDataModel;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.config.conditional.ConditionalOnApiService;
import org.apache.airavata.orchestrator.HostScheduler;
import org.apache.airavata.orchestrator.OrchestratorConstants;
import org.apache.airavata.orchestrator.exception.OrchestratorException;
import org.apache.airavata.orchestrator.internal.messaging.MessageContext;
import org.apache.airavata.orchestrator.messaging.MessagingFactory;
import org.apache.airavata.orchestrator.state.StateKeys;
import org.apache.airavata.orchestrator.state.StateManager;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.util.ExperimentModelUtil;
import org.apache.airavata.workflow.orchestrator.SimpleOrchestratorImpl;
import org.apache.airavata.workflow.scheduling.ProcessScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnApiService
@ConditionalOnBean({org.apache.airavata.workflow.orchestrator.SimpleOrchestratorImpl.class})
public class OrchestratorService {
    private static final Logger logger = LoggerFactory.getLogger(OrchestratorService.class);

    private final RegistryService registryService;
    private final AiravataServerProperties properties;
    private final ProcessScheduler processScheduler;
    private final SimpleOrchestratorImpl orchestrator;
    private final MessagingFactory messagingFactory;
    private final HostScheduler hostScheduler;
    private final StateManager stateManager;

    public OrchestratorService(
            RegistryService registryService,
            AiravataServerProperties properties,
            SimpleOrchestratorImpl orchestrator,
            ProcessScheduler processScheduler,
            MessagingFactory messagingFactory,
            HostScheduler hostScheduler,
            @Autowired(required = false) StateManager stateManager) {
        this.registryService = registryService;
        this.properties = properties;
        this.orchestrator = orchestrator;
        this.processScheduler = processScheduler;
        this.messagingFactory = messagingFactory;
        this.hostScheduler = hostScheduler;
        this.stateManager = stateManager;
    }

    @PostConstruct
    public void postConstruct() {
        logger.info("[BEAN-INIT] OrchestratorService.postConstruct() called");
        try {
            initializeInternal();
            logger.info("[BEAN-INIT] OrchestratorService initialized successfully");
        } catch (OrchestratorException e) {
            logger.error("Failed to initialize OrchestratorService", e);
            throw new RuntimeException("Failed to initialize OrchestratorService", e);
        }
    }

    private void initializeInternal() throws OrchestratorException {
        try {
            this.orchestrator.initialize();
            // State for cancel is used when stateManager is available; no init needed
        } catch (Exception e) {
            logger.warn("Failed to initialize orchestrator: {}. Some features may be unavailable.", e.getMessage());
        }
    }

    private boolean launchExperimentInternal(String experimentId, String gatewayId)
            throws ExperimentNotFoundException, OrchestratorException, RegistryException, LaunchValidationException {
        if (stateManager != null && stateManager.isAvailable()) {
            try {
                stateManager.saveState(StateKeys.cancelExperiment(experimentId), "");
            } catch (Exception e) {
                logger.warn("Could not set cancel state for experiment {} at launch", experimentId, e);
            }
        }
        ExperimentModel experiment = registryService.getExperiment(experimentId);
        if (experiment == null) {
            throw new ExperimentNotFoundException(
                    "Error retrieving the Experiment by the given experimentID: " + experimentId);
        }

        UserConfigurationDataModel userConfigurationData = experiment.getUserConfigurationData();
        String token = getCredentialToken(experiment, userConfigurationData);

        ExperimentType executionType = experiment.getExperimentType();
        if (executionType == ExperimentType.SINGLE_APPLICATION) {
            return launchSingleAppExperiment(experiment, experimentId, gatewayId, token);
        } else if (executionType == ExperimentType.WORKFLOW) {
            logger.debug(experimentId, "Launching workflow experiment {}.", experimentId);
            launchWorkflowExperiment(experimentId, token, gatewayId);
            return true;
        } else {
            logger.error(
                    experimentId,
                    "Couldn't identify experiment type, experiment {} is neither single application nor workflow.",
                    experimentId);
            throw new OrchestratorException("Experiment '" + experimentId
                    + "' launch failed. Unable to figureout execution type for application "
                    + experiment.getExecutionId());
        }
    }

    private boolean launchSingleAppExperiment(
            ExperimentModel experiment, String experimentId, String gatewayId, String token)
            throws OrchestratorException, RegistryException, LaunchValidationException {
        List<ProcessModel> processes = orchestrator.createProcesses(experimentId, gatewayId);

        for (ProcessModel processModel : processes) {
            resolveInputReplicas(processModel);

            if (!experiment.getUserConfigurationData().getAiravataAutoSchedule()) {
                String taskDag = orchestrator.createAndSaveTasks(gatewayId, processModel);
                processModel.setTaskDag(taskDag);
            }
            registryService.updateProcess(processModel, processModel.getProcessId());
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

        if (!experiment.getUserConfigurationData().getAiravataAutoSchedule()
                || (processScheduler != null && processScheduler.canLaunch(experimentId))) {
            createAndValidateTasks(experiment, false);
            return true; // runExperimentLauncher will be called separately
        } else {
            logger.debug(experimentId, "Queuing single application experiment {}.", experimentId);
            ExperimentStatus status = new ExperimentStatus();
            status.setState(ExperimentState.SCHEDULED);
            status.setReason("Compute resources are not ready");
            status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            updateExperimentStatus(experimentId, status, gatewayId);
            logger.info("expId: {}, Scheduled experiment ", experimentId);
            return false;
        }
    }

    private void resolveInputReplicas(ProcessModel processModel) throws RegistryException {
        for (var pi : processModel.getProcessInputs()) {
            if (pi.getType().equals(DataType.URI)
                    && pi.getValue() != null
                    && pi.getValue().startsWith("airavata-dp://")) {
                DataProductModel dataProductModel = registryService.getDataProduct(pi.getValue());
                Optional<DataReplicaLocationModel> rpLocation = dataProductModel.getReplicaLocations().stream()
                        .filter(rpModel ->
                                rpModel.getReplicaLocationCategory().equals(ReplicaLocationCategory.GATEWAY_DATA_STORE))
                        .findFirst();
                if (rpLocation.isPresent()) {
                    pi.setValue(rpLocation.get().getFilePath());
                    pi.setStorageResourceId(rpLocation.get().getStorageResourceId());
                } else {
                    logger.error("Could not find a replica for the URI " + pi.getValue());
                }
            } else if (pi.getType().equals(DataType.URI_COLLECTION)
                    && pi.getValue() != null
                    && pi.getValue().contains("airavata-dp://")) {
                String[] uriList = pi.getValue().split(",");
                final ArrayList<String> filePathList = new ArrayList<>();
                for (String uri : uriList) {
                    if (uri.startsWith("airavata-dp://")) {
                        DataProductModel dataProductModel = registryService.getDataProduct(uri);
                        Optional<DataReplicaLocationModel> rpLocation = dataProductModel.getReplicaLocations().stream()
                                .filter(rpModel -> rpModel.getReplicaLocationCategory()
                                        .equals(ReplicaLocationCategory.GATEWAY_DATA_STORE))
                                .findFirst();
                        if (rpLocation.isPresent()) {
                            filePathList.add(rpLocation.get().getFilePath());
                        } else {
                            logger.error("Could not find a replica for the URI " + pi.getValue());
                        }
                    } else {
                        filePathList.add(uri);
                    }
                }
                pi.setValue(String.join(",", filePathList));
            }
        }
    }

    public String getCredentialToken(ExperimentModel experiment, UserConfigurationDataModel userConfigurationData)
            throws OrchestratorException, RegistryException {
        String token = null;
        final String groupResourceProfileId = userConfigurationData.getGroupResourceProfileId();
        if (groupResourceProfileId == null) {
            throw new OrchestratorException(
                    "Experiment not configured with a Group Resource Profile: " + experiment.getExperimentId());
        }

        if (userConfigurationData.getComputationalResourceScheduling() != null
                && userConfigurationData.getComputationalResourceScheduling().getResourceHostId() != null) {
            GroupComputeResourcePreference groupComputeResourcePreference =
                    registryService.getGroupComputeResourcePreference(
                            userConfigurationData
                                    .getComputationalResourceScheduling()
                                    .getResourceHostId(),
                            groupResourceProfileId);

            if (groupComputeResourcePreference.getResourceSpecificCredentialStoreToken() != null) {
                token = groupComputeResourcePreference.getResourceSpecificCredentialStoreToken();
            }
        }
        if (token == null || token.isEmpty()) {
            GroupResourceProfile groupResourceProfile = registryService.getGroupResourceProfile(groupResourceProfileId);
            token = groupResourceProfile.getDefaultCredentialStoreToken();
        }
        if (token == null || token.isEmpty()) {
            throw new OrchestratorException(
                    "You have not configured credential store token at group resource profile or compute resource preference."
                            + " Please provide the correct token at group resource profile or compute resource preference.");
        }
        return token;
    }

    public boolean validateExperiment(String experimentId)
            throws LaunchValidationException, RegistryException, OrchestratorException {
        ExperimentModel experimentModel = registryService.getExperiment(experimentId);
        return orchestrator.validateExperiment(experimentModel).getValidationState();
    }

    public boolean validateProcess(String experimentId, List<ProcessModel> processes)
            throws LaunchValidationException, RegistryException, OrchestratorException {
        ExperimentModel experimentModel = registryService.getExperiment(experimentId);
        for (ProcessModel processModel : processes) {
            boolean state =
                    orchestrator.validateProcess(experimentModel, processModel).getValidationState();
            if (!state) {
                return false;
            }
        }
        return true;
    }

    public boolean terminateExperiment(String experimentId, String gatewayId)
            throws RegistryException, OrchestratorException {
        logger.info(experimentId, "Experiment: {} is cancelling  !!!!!", experimentId);
        return validateStatesAndCancel(experimentId, gatewayId);
    }

    private boolean validateStatesAndCancel(String experimentId, String gatewayId)
            throws RegistryException, OrchestratorException {
        var experimentStatus = registryService.getExperimentStatus(experimentId);
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
                var experimentModel = registryService.getExperiment(experimentId);
                final var userConfigurationData = experimentModel.getUserConfigurationData();
                final var groupResourceProfileId = userConfigurationData.getGroupResourceProfileId();

                var groupComputeResourcePreference = registryService.getGroupComputeResourcePreference(
                        userConfigurationData
                                .getComputationalResourceScheduling()
                                .getResourceHostId(),
                        groupResourceProfileId);
                var token = groupComputeResourcePreference.getResourceSpecificCredentialStoreToken();
                if (token == null || token.isEmpty()) {
                    var groupResourceProfile = registryService.getGroupResourceProfile(groupResourceProfileId);
                    token = groupResourceProfile.getDefaultCredentialStoreToken();
                }
                if (token == null || token.isEmpty()) {
                    logger.error(
                            "You have not configured credential store token at group resource profile or compute resource preference."
                                    + " Please provide the correct token at group resource profile or compute resource preference.");
                    yield false;
                }

                orchestrator.cancelExperiment(experimentModel, token);
                if (stateManager != null && stateManager.isAvailable()) {
                    try {
                        stateManager.saveState(StateKeys.cancelExperiment(experimentId), "CANCEL_REQUEST");
                    } catch (Exception e) {
                        logger.error("Error setting Dapr cancel state for experiment {}", experimentId, e);
                        throw new OrchestratorException(
                                "Error setting Dapr cancel state for experiment " + experimentId, e);
                    }
                }
                var status = new ExperimentStatus();
                status.setState(ExperimentState.CANCELING);
                status.setReason("Experiment cancel request processed");
                status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                updateExperimentStatus(experimentId, status, gatewayId);
                logger.info("expId : " + experimentId + " :- Experiment status updated to " + status.getState());
                yield true;
            }
        };
    }

    public void fetchIntermediateOutputs(String experimentId, String gatewayId, List<String> outputNames)
            throws RegistryException, OrchestratorException {
        submitIntermediateOutputsProcess(experimentId, gatewayId, outputNames);
    }

    private void submitIntermediateOutputsProcess(String experimentId, String gatewayId, List<String> outputNames)
            throws RegistryException, OrchestratorException {

        ExperimentModel experimentModel = registryService.getExperiment(experimentId);
        ProcessModel processModel = ExperimentModelUtil.cloneProcessFromExperiment(experimentModel);
        processModel.setExperimentDataDir(processModel.getExperimentDataDir() + "/intermediates");

        List<OutputDataObjectType> applicationOutputs =
                registryService.getApplicationOutputs(experimentModel.getExecutionId());
        List<OutputDataObjectType> requestedOutputs = new ArrayList<>();

        for (OutputDataObjectType output : applicationOutputs) {
            if (outputNames.contains(output.getName())) {
                requestedOutputs.add(output);
            }
        }
        processModel.setProcessOutputs(requestedOutputs);
        String processId = registryService.addProcess(processModel, experimentId);
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
            String taskDag = orchestrator.createAndSaveIntermediateOutputFetchingTasks(
                    gatewayId, processModel, jobSubmissionProcess.get());
            processModel.setTaskDag(taskDag);

            registryService.updateProcess(processModel, processModel.getProcessId());

            String token = getCredentialToken(experimentModel, experimentModel.getUserConfigurationData());
            orchestrator.launchProcess(processModel, token);
        } catch (RegistryException | OrchestratorException e) {
            logger.error("Failed to launch process for intermediate output fetching", e);

            ProcessStatus status = new ProcessStatus(ProcessState.FAILED);
            status.setReason("Intermediate output fetching process failed to launch: " + e.getMessage());
            status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            registryService.addProcessStatus(status, processId);

            throw e;
        }
    }

    public boolean launchProcess(String processId, String airavataCredStoreToken, String gatewayId)
            throws RegistryException, OrchestratorException {
        var processStatus = registryService.getProcessStatus(processId);

        return switch (processStatus.getState()) {
            case CREATED, VALIDATED, DEQUEUING -> {
                var processModel = registryService.getProcess(processId);
                var applicationId = processModel.getApplicationInterfaceId();
                if (applicationId == null) {
                    logger.error(processId, "Application interface id shouldn't be null.");
                    throw new OrchestratorException(
                            "Error executing the job, application interface id shouldn't be null.");
                }
                var applicationDeploymentDescription = getAppDeployment(processModel, applicationId);
                if (applicationDeploymentDescription == null) {
                    logger.error("Could not find an application deployment for " + processModel.getComputeResourceId()
                            + " and application " + applicationId);
                    throw new OrchestratorException("Could not find an application deployment for "
                            + processModel.getComputeResourceId() + " and application " + applicationId);
                }
                processModel.setApplicationDeploymentId(applicationDeploymentDescription.getAppDeploymentId());
                processModel.setComputeResourceId(
                        processModel.getProcessResourceSchedule().getResourceHostId());
                registryService.updateProcess(processModel, processModel.getProcessId());
                yield orchestrator.launchProcess(processModel, airavataCredStoreToken);
            }
            default -> {
                logger.warn("Process " + processId + " is already launched. So it can not be relaunched");
                yield false;
            }
        };
    }

    private ApplicationDeploymentDescription getAppDeployment(ProcessModel processModel, String applicationId)
            throws OrchestratorException, RegistryException {
        String selectedModuleId = getModuleId(applicationId);
        return getAppDeploymentForModule(processModel, selectedModuleId);
    }

    private ApplicationDeploymentDescription getAppDeploymentForModule(
            ProcessModel processModel, String selectedModuleId) throws OrchestratorException, RegistryException {

        List<ApplicationDeploymentDescription> applicationDeployements =
                registryService.getApplicationDeployments(selectedModuleId);
        Map<ComputeResourceDescription, ApplicationDeploymentDescription> deploymentMap = new HashMap<>();

        for (ApplicationDeploymentDescription deploymentDescription : applicationDeployements) {
            if (processModel.getComputeResourceId().equals(deploymentDescription.getComputeHostId())) {
                deploymentMap.put(
                        registryService.getComputeResource(deploymentDescription.getComputeHostId()),
                        deploymentDescription);
            }
        }
        List<ComputeResourceDescription> computeHostList =
                Arrays.asList(deploymentMap.keySet().toArray(new ComputeResourceDescription[] {}));
        ComputeResourceDescription ComputeResourceDescription = hostScheduler.schedule(computeHostList);
        return deploymentMap.get(ComputeResourceDescription);
    }

    private String getModuleId(String applicationId) throws OrchestratorException, RegistryException {
        ApplicationInterfaceDescription applicationInterface = registryService.getApplicationInterface(applicationId);
        List<String> applicationModules = applicationInterface.getApplicationModules();
        if (applicationModules.size() == 0) {
            throw new OrchestratorException("No modules defined for application " + applicationId);
        }
        String selectedModuleId = applicationModules.get(0);
        return selectedModuleId;
    }

    private void launchWorkflowExperiment(String experimentId, String airavataCredStoreToken, String gatewayId)
            throws OrchestratorException {
        logger.info("Launching workflow experiment {}", experimentId);

        try {
            // Get the experiment model
            ExperimentModel experiment = registryService.getExperiment(experimentId);
            if (experiment == null) {
                throw new OrchestratorException("Experiment not found: " + experimentId);
            }

            // Get the workflow definition for this experiment
            // For workflow experiments, the workflow definition should be registered with the experiment
            // We'll proceed with creating processes based on the experiment configuration
            logger.info(
                    "Processing workflow experiment {} - creating processes for workflow applications", experimentId);

            // Create processes for each application in the workflow
            // For workflow experiments, we need to create processes based on the workflow definition
            // This is a simplified implementation - full implementation would handle workflow connections
            List<ProcessModel> processes = orchestrator.createProcesses(experimentId, gatewayId);

            // Resolve input replicas and create tasks for each process
            for (ProcessModel processModel : processes) {
                resolveInputReplicas(processModel);

                if (!experiment.getUserConfigurationData().getAiravataAutoSchedule()) {
                    String taskDag = orchestrator.createAndSaveTasks(gatewayId, processModel);
                    processModel.setTaskDag(taskDag);
                }
                registryService.updateProcess(processModel, processModel.getProcessId());
            }

            // Validate processes
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

            // Launch all processes (direct workflow start; no pub-sub)
            if (!experiment.getUserConfigurationData().getAiravataAutoSchedule()
                    || (processScheduler != null && processScheduler.canLaunch(experimentId))) {
                createAndValidateTasks(experiment, false);

                for (ProcessModel processModel : processes) {
                    orchestrator.launchProcess(processModel, airavataCredStoreToken);
                    logger.info(
                            "Started ProcessPreWorkflow for process {} of experiment {}",
                            processModel.getProcessId(),
                            experimentId);
                }

                // Update experiment status to LAUNCHED
                ExperimentStatus status = new ExperimentStatus();
                status.setState(ExperimentState.LAUNCHED);
                status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                updateExperimentStatus(experimentId, status, gatewayId);

                logger.info("Successfully launched workflow experiment {}", experimentId);

            } else {
                logger.debug(experimentId, "Queuing workflow experiment {}.", experimentId);
                ExperimentStatus status = new ExperimentStatus();
                status.setState(ExperimentState.SCHEDULED);
                status.setReason("Compute resources are not ready");
                status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                updateExperimentStatus(experimentId, status, gatewayId);
                logger.info("expId: {}, Scheduled workflow experiment ", experimentId);
            }

        } catch (RegistryException e) {
            logger.error("Failed to launch workflow experiment {}", experimentId, e);
            throw new OrchestratorException(
                    "Failed to launch workflow experiment " + experimentId + ": " + e.getMessage(), e);
        } catch (LaunchValidationException e) {
            logger.error("Validation failed for workflow experiment {}", experimentId, e);
            throw new OrchestratorException(
                    "Validation failed for workflow experiment " + experimentId + ": " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error launching workflow experiment {}", experimentId, e);
            throw new OrchestratorException(
                    "Unexpected error launching workflow experiment " + experimentId + ": " + e.getMessage(), e);
        }
    }

    public void createAndValidateTasks(ExperimentModel experiment, boolean recreateTaskDag)
            throws OrchestratorException, RegistryException, LaunchValidationException {
        if (experiment.getUserConfigurationData().getAiravataAutoSchedule()) {
            List<ProcessModel> processModels = registryService.getProcessList(experiment.getExperimentId());
            for (ProcessModel processModel : processModels) {
                if (processModel.getTaskDag() == null || recreateTaskDag) {
                    registryService.deleteTasks(processModel.getProcessId());
                    String taskDag = orchestrator.createAndSaveTasks(experiment.getGatewayId(), processModel);
                    processModel.setTaskDag(taskDag);
                    registryService.updateProcess(processModel, processModel.getProcessId());
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

    public void addProcessValidationErrors(String experimentId, ErrorModel details) throws RegistryException {
        registryService.addErrors(OrchestratorConstants.EXPERIMENT_ERROR, details, experimentId);
    }

    public boolean launchSingleAppExperimentInternal(
            String experimentId, String airavataCredStoreToken, String gatewayId)
            throws RegistryException, OrchestratorException {
        try {
            List<String> processIds = registryService.getProcessIds(experimentId);
            for (String processId : processIds) {
                launchProcess(processId, airavataCredStoreToken, gatewayId);
            }
            return true;
        } catch (RegistryException e) {
            ExperimentStatus status = new ExperimentStatus();
            status.setState(ExperimentState.FAILED);
            status.setReason("Error while retrieving process IDs");
            updateExperimentStatus(experimentId, status, gatewayId);
            logger.error("expId: " + experimentId + ", Error while retrieving process IDs", e);
            throw e;
        } catch (OrchestratorException e) {
            ExperimentStatus status = new ExperimentStatus();
            status.setState(ExperimentState.FAILED);
            status.setReason("Error while launching processes");
            updateExperimentStatus(experimentId, status, gatewayId);
            logger.error("expId: " + experimentId + ", Error while launching processes", e);
            throw e;
        }
    }

    public void launchQueuedExperiment(String experimentId)
            throws ExperimentNotFoundException, OrchestratorException, RegistryException, LaunchValidationException {
        ExperimentModel experiment = registryService.getExperiment(experimentId);
        if (experiment == null) {
            throw new ExperimentNotFoundException(
                    "Error retrieving the Experiment by the given experimentID: " + experimentId);
        }

        UserConfigurationDataModel userConfigurationData = experiment.getUserConfigurationData();
        String token = getCredentialToken(experiment, userConfigurationData);
        createAndValidateTasks(experiment, true);

        // Publish experiment launched status and run launcher
        ExperimentStatus status = new ExperimentStatus();
        status.setState(ExperimentState.LAUNCHED);
        status.setReason("submitted all processes");
        status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        updateExperimentStatus(experimentId, status, experiment.getGatewayId());
        logger.info("expId: {}, Launched experiment ", experimentId);

        // Launch processes
        launchSingleAppExperimentInternal(experimentId, token, experiment.getGatewayId());
    }

    public void handleProcessStatusChange(
            ProcessStatusChangeEvent processStatusChangeEvent, ProcessIdentifier processIdentity)
            throws ExperimentNotFoundException, OrchestratorException, RegistryException, LaunchValidationException {
        ExperimentStatus status = new ExperimentStatus();

        // Check if this is an intermediate output fetching process
        ProcessModel process = registryService.getProcess(processIdentity.getProcessId());
        boolean isIntermediateOutputFetchingProcess =
                process.getTasks().stream().anyMatch(t -> t.getTaskType() == TaskTypes.OUTPUT_FETCHING);
        if (isIntermediateOutputFetchingProcess) {
            logger.info("Not updating experiment status because process is an intermediate output fetching one");
            return;
        }

        switch (processStatusChangeEvent.getState()) {
            case STARTED -> {
                var stat = registryService.getExperimentStatus(processIdentity.getExperimentId());
                if (stat.getState() == ExperimentState.CANCELING) {
                    status.setState(ExperimentState.CANCELING);
                    status.setReason("Process started but experiment cancelling is triggered");
                } else {
                    status.setState(ExperimentState.EXECUTING);
                    status.setReason("process  started");
                }
            }
            case COMPLETED -> {
                var stat = registryService.getExperimentStatus(processIdentity.getExperimentId());
                if (stat.getState() == ExperimentState.CANCELING) {
                    status.setState(ExperimentState.CANCELED);
                    status.setReason("Process competed but experiment cancelling is triggered");
                } else {
                    status.setState(ExperimentState.COMPLETED);
                    status.setReason("process  completed");
                }
            }
            case FAILED -> {
                var stat = registryService.getExperimentStatus(processIdentity.getExperimentId());
                if (stat.getState() == ExperimentState.CANCELING) {
                    status.setState(ExperimentState.CANCELED);
                    status.setReason("Process failed but experiment cancelling is triggered");
                } else {
                    status.setState(ExperimentState.FAILED);
                    status.setReason("process  failed");
                }
            }
            case CANCELED -> {
                status.setState(ExperimentState.CANCELED);
                status.setReason("process  cancelled");
            }
            case QUEUED -> {
                status.setState(ExperimentState.SCHEDULED);
                status.setReason("Process started but compute resource not avaialable");
            }
            case REQUEUED -> {
                status.setState(ExperimentState.SCHEDULED);
                status.setReason("Job submission failed,  requeued to resubmit");
                registerQueueStatusForRequeue(processIdentity.getExperimentId());
            }
            case DEQUEUING -> {
                var stat = registryService.getExperimentStatus(processIdentity.getExperimentId());
                if (stat.getState() == ExperimentState.CANCELING) {
                    status.setState(ExperimentState.CANCELING);
                    status.setReason("Process started but experiment cancelling is triggered");
                } else {
                    launchQueuedExperiment(processIdentity.getExperimentId());
                }
            }
            default -> {
                // ignore other status changes
                return;
            }
        }

        if (status.getState() != null) {
            status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            updateExperimentStatus(processIdentity.getExperimentId(), status, processIdentity.getGatewayId());
            logger.info("expId : " + processIdentity.getExperimentId() + " :- Experiment status updated to "
                    + status.getState());
        }
    }

    private void registerQueueStatusForRequeue(String experimentId) {
        try {
            List<QueueStatusModel> queueStatusModels = new ArrayList<>();
            ExperimentModel experimentModel = registryService.getExperiment(experimentId);
            UserConfigurationDataModel userConfigurationDataModel = experimentModel.getUserConfigurationData();
            if (userConfigurationDataModel != null) {
                ComputationalResourceSchedulingModel computationalResourceSchedulingModel =
                        userConfigurationDataModel.getComputationalResourceScheduling();
                if (computationalResourceSchedulingModel != null) {
                    String queueName = computationalResourceSchedulingModel.getQueueName();
                    String resourceId = computationalResourceSchedulingModel.getResourceHostId();
                    ComputeResourceDescription comResourceDes = registryService.getComputeResource(resourceId);
                    QueueStatusModel queueStatusModel = new QueueStatusModel();
                    queueStatusModel.setHostName(comResourceDes.getHostName());
                    queueStatusModel.setQueueName(queueName);
                    queueStatusModel.setQueueUp(false);
                    queueStatusModel.setRunningJobs(0);
                    queueStatusModel.setQueuedJobs(0);
                    queueStatusModel.setTime(AiravataUtils.getUniqueTimestamp().getTime());
                    queueStatusModels.add(queueStatusModel);
                    registryService.registerQueueStatuses(queueStatusModels);
                }
            }
        } catch (RegistryException e) {
            logger.error("Error while registering queue statuses", e);
        }
    }

    public void handleLaunchExperiment(ExperimentSubmitEvent expEvent)
            throws ExperimentNotFoundException, OrchestratorException, RegistryException, LaunchValidationException {
        ExperimentModel experimentModel = registryService.getExperiment(expEvent.getExperimentId());
        if (experimentModel.getExperimentStatus().get(0).getState() == ExperimentState.CREATED) {
            launchExperimentInternal(expEvent.getExperimentId(), expEvent.getGatewayId());
        }
    }

    /**
     * Handle launch experiment from message context with deserialization and redelivery checks
     */
    public void handleLaunchExperimentFromMessage(MessageContext messageContext)
            throws ExperimentNotFoundException, OrchestratorException, RegistryException, LaunchValidationException {
        ExperimentSubmitEvent expEvent = (ExperimentSubmitEvent) messageContext.getEvent();

        if (messageContext.isRedeliver()) {
            try {
                ExperimentModel experimentModel = registryService.getExperiment(expEvent.getExperimentId());
                if (experimentModel != null
                        && experimentModel.getExperimentStatus().get(0).getState() == ExperimentState.CREATED) {
                    handleLaunchExperiment(expEvent);
                }
            } catch (RegistryException e) {
                String msg = String.format(
                        "Error getting experiment for redelivery: experimentId=%s, gatewayId=%s, isRedeliver=true. Reason: %s",
                        expEvent.getExperimentId(), expEvent.getGatewayId(), e.getMessage());
                logger.error(msg, e);
                throw e;
            }
        } else {
            handleLaunchExperiment(expEvent);
        }
    }

    public void handleCancelExperiment(ExperimentSubmitEvent expEvent) throws RegistryException, OrchestratorException {
        terminateExperiment(expEvent.getExperimentId(), expEvent.getGatewayId());
    }

    public void handleIntermediateOutputsEvent(ExperimentIntermediateOutputsEvent event) throws OrchestratorException {
        try {
            fetchIntermediateOutputs(event.getExperimentId(), event.getGatewayId(), event.getOutputNames());
        } catch (RegistryException e) {
            String msg = String.format(
                    "Error handling intermediate outputs event: experimentId=%s, gatewayId=%s, outputNames=%s. Reason: %s",
                    event.getExperimentId(), event.getGatewayId(), event.getOutputNames(), e.getMessage());
            logger.error(msg, e);
            throw new OrchestratorException(msg, e);
        }
    }

    /**
     * Updates experiment status in registry only. No pub-sub.
     */
    private void updateExperimentStatus(String experimentId, ExperimentStatus status, String gatewayId) {
        try {
            ExperimentStatus currentStatus = registryService.getExperimentStatus(experimentId);
            org.apache.airavata.common.model.ExperimentState currentState =
                    currentStatus != null ? currentStatus.getState() : null;

            if (!org.apache.airavata.orchestrator.state.StateTransitionService.validateAndLog(
                    org.apache.airavata.orchestrator.state.ExperimentStateValidator.INSTANCE,
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

            registryService.updateExperimentStatus(status, experimentId);
        } catch (RegistryException e) {
            logger.error("expId : " + experimentId + " Error updating experiment status to " + status.toString(), e);
        }
    }

    public ExperimentStatus getExperimentStatus(String experimentId) throws RegistryException {
        return registryService.getExperimentStatus(experimentId);
    }

    public ProcessModel getProcess(String processId) throws RegistryException {
        return registryService.getProcess(processId);
    }

    public boolean launchExperiment(String experimentId, String gatewayId, ExecutorService executorService)
            throws OrchestratorException {
        try {
            boolean result = launchExperimentInternal(experimentId, gatewayId);
            if (result) {
                ExperimentModel experiment = registryService.getExperiment(experimentId);
                String token = getCredentialToken(experiment, experiment.getUserConfigurationData());
                ExperimentStatus status = new ExperimentStatus();
                status.setState(ExperimentState.LAUNCHED);
                status.setReason("submitted all processes");
                status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                updateExperimentStatus(experimentId, status, gatewayId);
                logger.info("expId: {}, Launched experiment ", experimentId);

                // Execute the single app experiment runner in the provided thread pool
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
        } catch (LaunchValidationException launchValidationException) {
            ExperimentStatus status = new ExperimentStatus();
            status.setState(ExperimentState.FAILED);
            status.setReason("Validation failed: " + launchValidationException.getErrorMessage());
            status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            updateExperimentStatus(experimentId, status, gatewayId);
            throw new OrchestratorException(
                    "Experiment '" + experimentId + "' launch failed. Experiment failed to validate: "
                            + launchValidationException.getErrorMessage(),
                    launchValidationException);
        } catch (RegistryException e) {
            ExperimentStatus status = new ExperimentStatus();
            status.setState(ExperimentState.FAILED);
            status.setReason("Registry error: " + e.getMessage());
            status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            updateExperimentStatus(experimentId, status, gatewayId);
            throw new OrchestratorException("Experiment '" + experimentId + "' launch failed.", e);
        } catch (ExperimentNotFoundException e) {
            ExperimentStatus status = new ExperimentStatus();
            status.setState(ExperimentState.FAILED);
            status.setReason("Unexpected error occurred: " + e.getMessage());
            status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            updateExperimentStatus(experimentId, status, gatewayId);
            throw new OrchestratorException("Experiment '" + experimentId + "' launch failed.", e);
        } catch (RuntimeException e) {
            ExperimentStatus status = new ExperimentStatus();
            status.setState(ExperimentState.FAILED);
            status.setReason("Unexpected runtime error occurred: " + e.getMessage());
            status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            updateExperimentStatus(experimentId, status, gatewayId);
            throw new OrchestratorException("Experiment '" + experimentId + "' launch failed.", e);
        }
    }
}
