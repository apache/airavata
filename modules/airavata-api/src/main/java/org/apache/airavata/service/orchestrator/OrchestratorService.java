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
import org.apache.airavata.common.exception.AiravataException;
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
import org.apache.airavata.common.model.ExperimentStatusChangeEvent;
import org.apache.airavata.common.model.ExperimentSubmitEvent;
import org.apache.airavata.common.model.ExperimentType;
import org.apache.airavata.common.model.GroupComputeResourcePreference;
import org.apache.airavata.common.model.GroupResourceProfile;
import org.apache.airavata.common.model.MessageType;
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
import org.apache.airavata.dapr.messaging.DaprMessagingFactory;
import org.apache.airavata.dapr.messaging.MessageContext;
import org.apache.airavata.dapr.messaging.Publisher;
import org.apache.airavata.dapr.messaging.Subscriber;
import org.apache.airavata.dapr.messaging.Type;
import org.apache.airavata.dapr.state.DaprStateKeys;
import org.apache.airavata.dapr.state.DaprStateManager;
import org.apache.airavata.metascheduler.core.api.ProcessScheduler;
import org.apache.airavata.orchestrator.exception.OrchestratorException;
import org.apache.airavata.orchestrator.impl.SimpleOrchestratorImpl;
import org.apache.airavata.orchestrator.schedule.HostScheduler;
import org.apache.airavata.orchestrator.utils.OrchestratorConstants;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.util.ExperimentModelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnApiService
@ConditionalOnBean({
    org.apache.airavata.service.orchestrator.OrchestratorRegistryService.class,
    org.apache.airavata.orchestrator.impl.SimpleOrchestratorImpl.class
})
public class OrchestratorService {
    private static final Logger logger = LoggerFactory.getLogger(OrchestratorService.class);

    private final OrchestratorRegistryService orchestratorRegistryService;
    private final RegistryService registryService;
    private final AiravataServerProperties properties;
    private final ProcessScheduler processScheduler;
    private final SimpleOrchestratorImpl orchestrator;
    private final DaprMessagingFactory messagingFactory;
    private final HostScheduler hostScheduler;
    private final DaprStateManager daprStateManager;

    private Publisher publisher;
    private Subscriber experimentSubscriber;

    public OrchestratorService(
            OrchestratorRegistryService orchestratorRegistryService,
            RegistryService registryService,
            AiravataServerProperties properties,
            SimpleOrchestratorImpl orchestrator,
            ProcessScheduler processScheduler,
            DaprMessagingFactory messagingFactory,
            HostScheduler hostScheduler,
            @Autowired(required = false) DaprStateManager daprStateManager) {
        this.orchestratorRegistryService = orchestratorRegistryService;
        this.registryService = registryService;
        this.properties = properties;
        this.orchestrator = orchestrator;
        this.processScheduler = processScheduler;
        this.messagingFactory = messagingFactory;
        this.hostScheduler = hostScheduler;
        this.daprStateManager = daprStateManager;
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
            this.publisher = messagingFactory.getPublisher(Type.STATUS);
        } catch (AiravataException e) {
            logger.warn(
                    "Failed to initialize StatusPublisher for OrchestratorService: {}. Publisher will be unavailable.",
                    e.getMessage());
            // Continue without publisher - Dapr may not be available
        }
        try {
            this.orchestrator.initialize();
            if (this.publisher != null) {
                this.orchestrator.getOrchestratorContext().setPublisher(this.publisher);
            }
            // Dapr State for cancel is used when daprStateManager is available; no init needed
        } catch (Exception e) {
            logger.warn("Failed to initialize orchestrator: {}. Some features may be unavailable.", e.getMessage());
        }
        try {
            getExperimentSubscriber();
        } catch (AiravataException e) {
            logger.warn(
                    "Failed to initialize ExperimentSubscriber for OrchestratorService: {}. Subscriber will be unavailable.",
                    e.getMessage());
            // Continue without subscriber - Dapr may not be available
        }
    }

    private boolean launchExperimentInternal(String experimentId, String gatewayId)
            throws ExperimentNotFoundException, OrchestratorException, RegistryException, LaunchValidationException {
        if (daprStateManager != null && daprStateManager.isAvailable()) {
            try {
                daprStateManager.saveState(DaprStateKeys.cancelExperiment(experimentId), "");
            } catch (Exception e) {
                logger.warn("Could not set Dapr cancel state for experiment {} at launch", experimentId, e);
            }
        }
        ExperimentModel experiment = orchestratorRegistryService.getExperiment(experimentId);
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
            orchestratorRegistryService.updateProcess(processModel, processModel.getProcessId());
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
            updateAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
            logger.info("expId: {}, Scheduled experiment ", experimentId);
            return false;
        }
    }

    private void resolveInputReplicas(ProcessModel processModel) throws RegistryException {
        for (var pi : processModel.getProcessInputs()) {
            if (pi.getType().equals(DataType.URI)
                    && pi.getValue() != null
                    && pi.getValue().startsWith("airavata-dp://")) {
                DataProductModel dataProductModel = orchestratorRegistryService.getDataProduct(pi.getValue());
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
                        DataProductModel dataProductModel = orchestratorRegistryService.getDataProduct(uri);
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
                    orchestratorRegistryService.getGroupComputeResourcePreference(
                            userConfigurationData
                                    .getComputationalResourceScheduling()
                                    .getResourceHostId(),
                            groupResourceProfileId);

            if (groupComputeResourcePreference.getResourceSpecificCredentialStoreToken() != null) {
                token = groupComputeResourcePreference.getResourceSpecificCredentialStoreToken();
            }
        }
        if (token == null || token.isEmpty()) {
            GroupResourceProfile groupResourceProfile =
                    orchestratorRegistryService.getGroupResourceProfile(groupResourceProfileId);
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
        ExperimentModel experimentModel = orchestratorRegistryService.getExperiment(experimentId);
        return orchestrator.validateExperiment(experimentModel).getValidationState();
    }

    public boolean validateProcess(String experimentId, List<ProcessModel> processes)
            throws LaunchValidationException, RegistryException, OrchestratorException {
        ExperimentModel experimentModel = orchestratorRegistryService.getExperiment(experimentId);
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
        var experimentStatus = orchestratorRegistryService.getExperimentStatus(experimentId);
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
                var experimentModel = orchestratorRegistryService.getExperiment(experimentId);
                final var userConfigurationData = experimentModel.getUserConfigurationData();
                final var groupResourceProfileId = userConfigurationData.getGroupResourceProfileId();

                var groupComputeResourcePreference = orchestratorRegistryService.getGroupComputeResourcePreference(
                        userConfigurationData
                                .getComputationalResourceScheduling()
                                .getResourceHostId(),
                        groupResourceProfileId);
                var token = groupComputeResourcePreference.getResourceSpecificCredentialStoreToken();
                if (token == null || token.isEmpty()) {
                    var groupResourceProfile =
                            orchestratorRegistryService.getGroupResourceProfile(groupResourceProfileId);
                    token = groupResourceProfile.getDefaultCredentialStoreToken();
                }
                if (token == null || token.isEmpty()) {
                    logger.error(
                            "You have not configured credential store token at group resource profile or compute resource preference."
                                    + " Please provide the correct token at group resource profile or compute resource preference.");
                    yield false;
                }

                orchestrator.cancelExperiment(experimentModel, token);
                if (daprStateManager != null && daprStateManager.isAvailable()) {
                    try {
                        daprStateManager.saveState(DaprStateKeys.cancelExperiment(experimentId), "CANCEL_REQUEST");
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
                updateAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
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

        ExperimentModel experimentModel = orchestratorRegistryService.getExperiment(experimentId);
        ProcessModel processModel = ExperimentModelUtil.cloneProcessFromExperiment(experimentModel);
        processModel.setExperimentDataDir(processModel.getExperimentDataDir() + "/intermediates");

        List<OutputDataObjectType> applicationOutputs =
                orchestratorRegistryService.getApplicationOutputs(experimentModel.getExecutionId());
        List<OutputDataObjectType> requestedOutputs = new ArrayList<>();

        for (OutputDataObjectType output : applicationOutputs) {
            if (outputNames.contains(output.getName())) {
                requestedOutputs.add(output);
            }
        }
        processModel.setProcessOutputs(requestedOutputs);
        String processId = orchestratorRegistryService.addProcess(processModel, experimentId);
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

            orchestratorRegistryService.updateProcess(processModel, processModel.getProcessId());

            String token = getCredentialToken(experimentModel, experimentModel.getUserConfigurationData());
            orchestrator.launchProcess(processModel, token);
        } catch (RegistryException | OrchestratorException e) {
            logger.error("Failed to launch process for intermediate output fetching", e);

            ProcessStatus status = new ProcessStatus(ProcessState.FAILED);
            status.setReason("Intermediate output fetching process failed to launch: " + e.getMessage());
            status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            orchestratorRegistryService.addProcessStatus(status, processId);

            throw e;
        }
    }

    public boolean launchProcess(String processId, String airavataCredStoreToken, String gatewayId)
            throws RegistryException, OrchestratorException {
        var processStatus = orchestratorRegistryService.getProcessStatus(processId);

        return switch (processStatus.getState()) {
            case CREATED, VALIDATED, DEQUEUING -> {
                var processModel = orchestratorRegistryService.getProcess(processId);
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
                orchestratorRegistryService.updateProcess(processModel, processModel.getProcessId());
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
                orchestratorRegistryService.getApplicationDeployments(selectedModuleId);
        Map<ComputeResourceDescription, ApplicationDeploymentDescription> deploymentMap = new HashMap<>();

        for (ApplicationDeploymentDescription deploymentDescription : applicationDeployements) {
            if (processModel.getComputeResourceId().equals(deploymentDescription.getComputeHostId())) {
                deploymentMap.put(
                        orchestratorRegistryService.getComputeResource(deploymentDescription.getComputeHostId()),
                        deploymentDescription);
            }
        }
        List<ComputeResourceDescription> computeHostList =
                Arrays.asList(deploymentMap.keySet().toArray(new ComputeResourceDescription[] {}));
        ComputeResourceDescription ComputeResourceDescription = hostScheduler.schedule(computeHostList);
        return deploymentMap.get(ComputeResourceDescription);
    }

    private String getModuleId(String applicationId) throws OrchestratorException, RegistryException {
        ApplicationInterfaceDescription applicationInterface =
                orchestratorRegistryService.getApplicationInterface(applicationId);
        List<String> applicationModules = applicationInterface.getApplicationModules();
        if (applicationModules.size() == 0) {
            throw new OrchestratorException("No modules defined for application " + applicationId);
        }
        String selectedModuleId = applicationModules.get(0);
        return selectedModuleId;
    }

    private void launchWorkflowExperiment(String experimentId, String airavataCredStoreToken, String gatewayId)
            throws OrchestratorException {
        throw new UnsupportedOperationException("Workflow support not implemented");
    }

    public void createAndValidateTasks(ExperimentModel experiment, boolean recreateTaskDag)
            throws OrchestratorException, RegistryException, LaunchValidationException {
        if (experiment.getUserConfigurationData().getAiravataAutoSchedule()) {
            List<ProcessModel> processModels = orchestratorRegistryService.getProcessList(experiment.getExperimentId());
            for (ProcessModel processModel : processModels) {
                if (processModel.getTaskDag() == null || recreateTaskDag) {
                    orchestratorRegistryService.deleteTasks(processModel.getProcessId());
                    String taskDag = orchestrator.createAndSaveTasks(experiment.getGatewayId(), processModel);
                    processModel.setTaskDag(taskDag);
                    orchestratorRegistryService.updateProcess(processModel, processModel.getProcessId());
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
        orchestratorRegistryService.addErrors(OrchestratorConstants.EXPERIMENT_ERROR, details, experimentId);
    }

    public boolean launchSingleAppExperimentInternal(
            String experimentId, String airavataCredStoreToken, String gatewayId)
            throws RegistryException, OrchestratorException {
        try {
            List<String> processIds = orchestratorRegistryService.getProcessIds(experimentId);
            for (String processId : processIds) {
                launchProcess(processId, airavataCredStoreToken, gatewayId);
            }
            return true;
        } catch (RegistryException e) {
            ExperimentStatus status = new ExperimentStatus();
            status.setState(ExperimentState.FAILED);
            status.setReason("Error while retrieving process IDs");
            updateAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
            logger.error("expId: " + experimentId + ", Error while retrieving process IDs", e);
            throw e;
        } catch (OrchestratorException e) {
            ExperimentStatus status = new ExperimentStatus();
            status.setState(ExperimentState.FAILED);
            status.setReason("Error while launching processes");
            updateAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
            logger.error("expId: " + experimentId + ", Error while launching processes", e);
            throw e;
        }
    }

    public void launchQueuedExperiment(String experimentId)
            throws ExperimentNotFoundException, OrchestratorException, RegistryException, LaunchValidationException {
        ExperimentModel experiment = orchestratorRegistryService.getExperiment(experimentId);
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
        updateAndPublishExperimentStatus(experimentId, status, publisher, experiment.getGatewayId());
        logger.info("expId: {}, Launched experiment ", experimentId);

        // Launch processes
        launchSingleAppExperimentInternal(experimentId, token, experiment.getGatewayId());
    }

    public void handleProcessStatusChange(
            ProcessStatusChangeEvent processStatusChangeEvent, ProcessIdentifier processIdentity)
            throws ExperimentNotFoundException, OrchestratorException, RegistryException, LaunchValidationException {
        ExperimentStatus status = new ExperimentStatus();

        // Check if this is an intermediate output fetching process
        ProcessModel process = orchestratorRegistryService.getProcess(processIdentity.getProcessId());
        boolean isIntermediateOutputFetchingProcess =
                process.getTasks().stream().anyMatch(t -> t.getTaskType() == TaskTypes.OUTPUT_FETCHING);
        if (isIntermediateOutputFetchingProcess) {
            logger.info("Not updating experiment status because process is an intermediate output fetching one");
            return;
        }

        switch (processStatusChangeEvent.getState()) {
            case STARTED -> {
                var stat = orchestratorRegistryService.getExperimentStatus(processIdentity.getExperimentId());
                if (stat.getState() == ExperimentState.CANCELING) {
                    status.setState(ExperimentState.CANCELING);
                    status.setReason("Process started but experiment cancelling is triggered");
                } else {
                    status.setState(ExperimentState.EXECUTING);
                    status.setReason("process  started");
                }
            }
            case COMPLETED -> {
                var stat = orchestratorRegistryService.getExperimentStatus(processIdentity.getExperimentId());
                if (stat.getState() == ExperimentState.CANCELING) {
                    status.setState(ExperimentState.CANCELED);
                    status.setReason("Process competed but experiment cancelling is triggered");
                } else {
                    status.setState(ExperimentState.COMPLETED);
                    status.setReason("process  completed");
                }
            }
            case FAILED -> {
                var stat = orchestratorRegistryService.getExperimentStatus(processIdentity.getExperimentId());
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
                var stat = orchestratorRegistryService.getExperimentStatus(processIdentity.getExperimentId());
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
            updateAndPublishExperimentStatus(
                    processIdentity.getExperimentId(), status, publisher, processIdentity.getGatewayId());
            logger.info("expId : " + processIdentity.getExperimentId() + " :- Experiment status updated to "
                    + status.getState());
        }
    }

    private void registerQueueStatusForRequeue(String experimentId) {
        try {
            List<QueueStatusModel> queueStatusModels = new ArrayList<>();
            ExperimentModel experimentModel = orchestratorRegistryService.getExperiment(experimentId);
            UserConfigurationDataModel userConfigurationDataModel = experimentModel.getUserConfigurationData();
            if (userConfigurationDataModel != null) {
                ComputationalResourceSchedulingModel computationalResourceSchedulingModel =
                        userConfigurationDataModel.getComputationalResourceScheduling();
                if (computationalResourceSchedulingModel != null) {
                    String queueName = computationalResourceSchedulingModel.getQueueName();
                    String resourceId = computationalResourceSchedulingModel.getResourceHostId();
                    ComputeResourceDescription comResourceDes =
                            orchestratorRegistryService.getComputeResource(resourceId);
                    QueueStatusModel queueStatusModel = new QueueStatusModel();
                    queueStatusModel.setHostName(comResourceDes.getHostName());
                    queueStatusModel.setQueueName(queueName);
                    queueStatusModel.setQueueUp(false);
                    queueStatusModel.setRunningJobs(0);
                    queueStatusModel.setQueuedJobs(0);
                    queueStatusModel.setTime(AiravataUtils.getUniqueTimestamp().getTime());
                    queueStatusModels.add(queueStatusModel);
                    orchestratorRegistryService.registerQueueStatuses(queueStatusModels);
                }
            }
        } catch (RegistryException e) {
            logger.error("Error while registering queue statuses", e);
        }
    }

    public void handleLaunchExperiment(ExperimentSubmitEvent expEvent)
            throws ExperimentNotFoundException, OrchestratorException, RegistryException, LaunchValidationException {
        ExperimentModel experimentModel = orchestratorRegistryService.getExperiment(expEvent.getExperimentId());
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
                ExperimentModel experimentModel = orchestratorRegistryService.getExperiment(expEvent.getExperimentId());
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

    private void updateAndPublishExperimentStatus(
            String experimentId, ExperimentStatus status, Publisher publisher, String gatewayId) {
        try {
            ExperimentStatus currentStatus = orchestratorRegistryService.getExperimentStatus(experimentId);
            org.apache.airavata.common.model.ExperimentState currentState =
                    currentStatus != null ? currentStatus.getState() : null;

            if (!org.apache.airavata.statemachine.StateTransitionService.validateAndLog(
                    org.apache.airavata.statemachine.ExperimentStateValidator.INSTANCE,
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

            orchestratorRegistryService.updateExperimentStatus(status, experimentId);
            ExperimentStatusChangeEvent event = new ExperimentStatusChangeEvent();
            event.setState(status.getState());
            event.setExperimentId(experimentId);
            event.setGatewayId(gatewayId);
            String messageId = AiravataUtils.getId("EXPERIMENT");
            MessageContext messageContext = new MessageContext(event, MessageType.EXPERIMENT, messageId, gatewayId);
            messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            publisher.publish(messageContext);
        } catch (AiravataException e) {
            logger.error(
                    "expId : " + experimentId + " Error while publishing experiment status to " + status.toString(), e);
        } catch (RegistryException e) {
            logger.error(
                    "expId : " + experimentId + " Error while updating experiment status to " + status.toString(), e);
        }
    }

    public ExperimentStatus getExperimentStatus(String experimentId) throws RegistryException {
        return orchestratorRegistryService.getExperimentStatus(experimentId);
    }

    public ProcessModel getProcess(String processId) throws RegistryException {
        return orchestratorRegistryService.getProcess(processId);
    }

    public boolean launchExperiment(String experimentId, String gatewayId, ExecutorService executorService)
            throws OrchestratorException {
        try {
            boolean result = launchExperimentInternal(experimentId, gatewayId);
            if (result) {
                ExperimentModel experiment = orchestratorRegistryService.getExperiment(experimentId);
                String token = getCredentialToken(experiment, experiment.getUserConfigurationData());
                ExperimentStatus status = new ExperimentStatus();
                status.setState(ExperimentState.LAUNCHED);
                status.setReason("submitted all processes");
                status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                updateAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
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
            updateAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
            throw new OrchestratorException(
                    "Experiment '" + experimentId + "' launch failed. Experiment failed to validate: "
                            + launchValidationException.getErrorMessage(),
                    launchValidationException);
        } catch (RegistryException e) {
            ExperimentStatus status = new ExperimentStatus();
            status.setState(ExperimentState.FAILED);
            status.setReason("Registry error: " + e.getMessage());
            status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            updateAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
            throw new OrchestratorException("Experiment '" + experimentId + "' launch failed.", e);
        } catch (ExperimentNotFoundException e) {
            ExperimentStatus status = new ExperimentStatus();
            status.setState(ExperimentState.FAILED);
            status.setReason("Unexpected error occurred: " + e.getMessage());
            status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            updateAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
            throw new OrchestratorException("Experiment '" + experimentId + "' launch failed.", e);
        } catch (RuntimeException e) {
            ExperimentStatus status = new ExperimentStatus();
            status.setState(ExperimentState.FAILED);
            status.setReason("Unexpected runtime error occurred: " + e.getMessage());
            status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            updateAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
            throw new OrchestratorException("Experiment '" + experimentId + "' launch failed.", e);
        }
    }

    private Subscriber getExperimentSubscriber() throws AiravataException {
        // Create handler first (subscriber will be set after creation)
        ExperimentMessageHandler handler = new ExperimentMessageHandler(this, null);
        // Create subscriber with handler
        this.experimentSubscriber = messagingFactory.getSubscriber(handler, List.of(), Type.EXPERIMENT_LAUNCH);
        // Set subscriber reference in handler for acks
        handler.setSubscriber(this.experimentSubscriber);
        return this.experimentSubscriber;
    }
}
