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
package org.apache.airavata.service;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import org.apache.airavata.api.thrift.util.ThriftUtils;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.logging.MDCConstants;
import org.apache.airavata.common.logging.MDCUtil;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ZkConstants;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessageHandler;
import org.apache.airavata.messaging.core.MessagingFactory;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.messaging.core.Subscriber;
import org.apache.airavata.messaging.core.Type;
import org.apache.airavata.metascheduler.core.api.ProcessScheduler;
import org.apache.airavata.metascheduler.process.scheduling.api.ProcessSchedulerImpl;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupResourceProfile;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.data.replica.DataProductModel;
import org.apache.airavata.model.data.replica.DataReplicaLocationModel;
import org.apache.airavata.model.data.replica.ReplicaLocationCategory;
import org.apache.airavata.model.error.ExperimentNotFoundException;
import org.apache.airavata.model.error.LaunchValidationException;
import org.apache.airavata.model.error.ValidationResults;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.messaging.event.*;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.status.ExperimentStatus;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.model.status.QueueStatusModel;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.airavata.model.util.ExperimentModelUtil;
import org.apache.airavata.orchestrator.exception.OrchestratorException;
import org.apache.airavata.orchestrator.impl.SimpleOrchestratorImpl;
import org.apache.airavata.orchestrator.schedule.HostScheduler;
import org.apache.airavata.orchestrator.utils.OrchestratorConstants;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ZKPaths;
import org.apache.thrift.TException;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrchestratorService {
    private static final Logger logger = LoggerFactory.getLogger(OrchestratorService.class);

    @Autowired
    private OrchestratorRegistryService orchestratorRegistryService;

    @Autowired
    private AiravataServerProperties properties;
    
    @Autowired
    private SimpleOrchestratorImpl orchestrator;
    
    private CuratorFramework curatorClient;
    private Publisher publisher;
    private Subscriber experimentSubscriber;

    public OrchestratorService() throws OrchestratorException {
        // Default constructor for Spring - initialization happens after injection
        // Note: This constructor is called by Spring, but initialization is deferred
        // to allow Spring to inject dependencies first
    }
    
    @PostConstruct
    public void postConstruct() {
        try {
            initializeInternal();
        } catch (OrchestratorException e) {
            logger.error("Failed to initialize OrchestratorService", e);
            throw new RuntimeException("Failed to initialize OrchestratorService", e);
        }
    }
    
    private void initializeInternal() throws OrchestratorException {
        try {
            this.publisher = MessagingFactory.getPublisher(Type.STATUS);
            this.orchestrator.initialize();
            this.orchestrator.getOrchestratorContext().setPublisher(this.publisher);
            startCurator();
            this.experimentSubscriber = getExperimentSubscriber();
        } catch (AiravataException e) {
            throw new OrchestratorException("Error initializing OrchestratorService", e);
        }
    }

    public OrchestratorService(
            OrchestratorRegistryService orchestratorRegistryService,
            SimpleOrchestratorImpl orchestrator,
            CuratorFramework curatorClient,
            Publisher publisher) {
        this.orchestratorRegistryService = orchestratorRegistryService;
        this.orchestrator = orchestrator;
        this.curatorClient = curatorClient;
        this.publisher = publisher;
    }

    private boolean launchExperimentInternal(String experimentId, String gatewayId)
            throws ExperimentNotFoundException, OrchestratorException, RegistryServiceException,
                    LaunchValidationException {
        String experimentNodePath = getExperimentNodePath(experimentId);
        try {
            ZKPaths.mkdirs(curatorClient.getZookeeperClient().getZooKeeper(), experimentNodePath);
            String experimentCancelNode =
                    ZKPaths.makePath(experimentNodePath, ZkConstants.ZOOKEEPER_CANCEL_LISTENER_NODE);
            ZKPaths.mkdirs(curatorClient.getZookeeperClient().getZooKeeper(), experimentCancelNode);
        } catch (KeeperException | InterruptedException e) {
            throw new OrchestratorException("Error creating ZooKeeper nodes for experiment: " + experimentId, e);
        } catch (Exception e) {
            throw new OrchestratorException("Error creating ZooKeeper nodes for experiment: " + experimentId, e);
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
            throws OrchestratorException, RegistryServiceException, LaunchValidationException {
        List<ProcessModel> processes = orchestrator.createProcesses(experimentId, gatewayId);

        for (ProcessModel processModel : processes) {
            resolveInputReplicas(processModel);

            if (!experiment.getUserConfigurationData().isAiravataAutoSchedule()) {
                String taskDag = orchestrator.createAndSaveTasks(gatewayId, processModel);
                processModel.setTaskDag(taskDag);
            }
            orchestratorRegistryService.updateProcess(processModel, processModel.getProcessId());
        }

        if (!experiment.getUserConfigurationData().isAiravataAutoSchedule()
                && !validateProcess(experimentId, processes)) {
            LaunchValidationException exception = new LaunchValidationException();
            ValidationResults validationResults = new ValidationResults();
            validationResults.setValidationState(false);
            validationResults.setValidationResultList(new ArrayList<>());
            exception.setValidationResult(validationResults);
            exception.setErrorMessage("Validating process fails for given experiment Id : " + experimentId);
            throw exception;
        }

        ProcessScheduler scheduler = new ProcessSchedulerImpl();
        if (!experiment.getUserConfigurationData().isAiravataAutoSchedule() || scheduler.canLaunch(experimentId)) {
            createAndValidateTasks(experiment, false);
            return true; // runExperimentLauncher will be called separately
        } else {
            logger.debug(experimentId, "Queuing single application experiment {}.", experimentId);
            ExperimentStatus status = new ExperimentStatus(ExperimentState.SCHEDULED);
            status.setReason("Compute resources are not ready");
            status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            updateAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
            logger.info("expId: {}, Scheduled experiment ", experimentId);
            return false;
        }
    }

    private void resolveInputReplicas(ProcessModel processModel) throws RegistryServiceException {
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
                pi.setValue(StringUtils.join(filePathList, ','));
            }
        }
    }

    public String getCredentialToken(ExperimentModel experiment, UserConfigurationDataModel userConfigurationData)
            throws OrchestratorException, RegistryServiceException {
        String token = null;
        final String groupResourceProfileId = userConfigurationData.getGroupResourceProfileId();
        if (groupResourceProfileId == null) {
            throw new OrchestratorException(
                    "Experiment not configured with a Group Resource Profile: " + experiment.getExperimentId());
        }

        if (userConfigurationData.getComputationalResourceScheduling() != null
                && userConfigurationData
                        .getComputationalResourceScheduling()
                        .isSet(ComputationalResourceSchedulingModel._Fields.RESOURCE_HOST_ID)) {
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
            throws LaunchValidationException, RegistryServiceException, OrchestratorException {
        ExperimentModel experimentModel = orchestratorRegistryService.getExperiment(experimentId);
        return orchestrator.validateExperiment(experimentModel).isValidationState();
    }

    public boolean validateProcess(String experimentId, List<ProcessModel> processes)
            throws LaunchValidationException, RegistryServiceException, OrchestratorException {
        ExperimentModel experimentModel = orchestratorRegistryService.getExperiment(experimentId);
        for (ProcessModel processModel : processes) {
            boolean state =
                    orchestrator.validateProcess(experimentModel, processModel).isSetValidationState();
            if (!state) {
                return false;
            }
        }
        return true;
    }

    public boolean terminateExperiment(String experimentId, String gatewayId)
            throws RegistryServiceException, OrchestratorException {
        logger.info(experimentId, "Experiment: {} is cancelling  !!!!!", experimentId);
        return validateStatesAndCancel(experimentId, gatewayId);
    }

    private boolean validateStatesAndCancel(String experimentId, String gatewayId)
            throws RegistryServiceException, OrchestratorException {
        ExperimentStatus experimentStatus = orchestratorRegistryService.getExperimentStatus(experimentId);
        switch (experimentStatus.getState()) {
            case COMPLETED:
            case CANCELED:
            case FAILED:
            case CANCELING:
                logger.warn(
                        "Can't terminate already {} experiment",
                        experimentStatus.getState().name());
                return false;
            case CREATED:
                logger.warn("Experiment termination is only allowed for launched experiments.");
                return false;
            default:
                ExperimentModel experimentModel = orchestratorRegistryService.getExperiment(experimentId);
                final UserConfigurationDataModel userConfigurationData = experimentModel.getUserConfigurationData();
                final String groupResourceProfileId = userConfigurationData.getGroupResourceProfileId();

                GroupComputeResourcePreference groupComputeResourcePreference =
                        orchestratorRegistryService.getGroupComputeResourcePreference(
                                userConfigurationData
                                        .getComputationalResourceScheduling()
                                        .getResourceHostId(),
                                groupResourceProfileId);
                String token = groupComputeResourcePreference.getResourceSpecificCredentialStoreToken();
                if (token == null || token.isEmpty()) {
                    GroupResourceProfile groupResourceProfile =
                            orchestratorRegistryService.getGroupResourceProfile(groupResourceProfileId);
                    token = groupResourceProfile.getDefaultCredentialStoreToken();
                }
                if (token == null || token.isEmpty()) {
                    logger.error(
                            "You have not configured credential store token at group resource profile or compute resource preference."
                                    + " Please provide the correct token at group resource profile or compute resource preference.");
                    return false;
                }

                orchestrator.cancelExperiment(experimentModel, token);
                String expCancelNodePath = ZKPaths.makePath(
                        ZKPaths.makePath(ZkConstants.ZOOKEEPER_EXPERIMENT_NODE, experimentId),
                        ZkConstants.ZOOKEEPER_CANCEL_LISTENER_NODE);
                Stat stat;
                try {
                    stat = curatorClient.checkExists().forPath(expCancelNodePath);
                } catch (KeeperException | InterruptedException e) {
                    logger.error("Error checking existence of Zookeeper node: " + expCancelNodePath, e);
                    throw new OrchestratorException(
                            "Error checking existence of Zookeeper node: " + expCancelNodePath, e);
                } catch (Exception e) {
                    logger.error("Error checking existence of Zookeeper node: " + expCancelNodePath, e);
                    throw new OrchestratorException(
                            "Error checking existence of Zookeeper node: " + expCancelNodePath, e);
                }
                if (stat != null) {
                    try {
                        curatorClient
                                .setData()
                                .withVersion(-1)
                                .forPath(expCancelNodePath, ZkConstants.ZOOKEEPER_CANCEL_REQEUST.getBytes());
                    } catch (KeeperException | InterruptedException e) {
                        logger.error("Error setting data for Zookeeper node: " + expCancelNodePath, e);
                        throw new OrchestratorException(
                                "Error setting data for Zookeeper node: " + expCancelNodePath, e);
                    } catch (Exception e) {
                        logger.error("Error setting data for Zookeeper node: " + expCancelNodePath, e);
                        throw new OrchestratorException(
                                "Error setting data for Zookeeper node: " + expCancelNodePath, e);
                    }
                    ExperimentStatus status = new ExperimentStatus(ExperimentState.CANCELING);
                    status.setReason("Experiment cancel request processed");
                    status.setTimeOfStateChange(
                            AiravataUtils.getCurrentTimestamp().getTime());
                    updateAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
                    logger.info("expId : " + experimentId + " :- Experiment status updated to " + status.getState());
                }
                return true;
        }
    }

    public void fetchIntermediateOutputs(String experimentId, String gatewayId, List<String> outputNames)
            throws RegistryServiceException, OrchestratorException {
        submitIntermediateOutputsProcess(experimentId, gatewayId, outputNames);
    }

    private void submitIntermediateOutputsProcess(String experimentId, String gatewayId, List<String> outputNames)
            throws RegistryServiceException, OrchestratorException {

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
        } catch (RegistryServiceException | OrchestratorException e) {
            logger.error("Failed to launch process for intermediate output fetching", e);

            ProcessStatus status = new ProcessStatus(ProcessState.FAILED);
            status.setReason("Intermediate output fetching process failed to launch: " + e.getMessage());
            status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            orchestratorRegistryService.addProcessStatus(status, processId);

            throw e;
        }
    }

    public boolean launchProcess(String processId, String airavataCredStoreToken, String gatewayId)
            throws RegistryServiceException, OrchestratorException {
        ProcessStatus processStatus = orchestratorRegistryService.getProcessStatus(processId);

        switch (processStatus.getState()) {
            case CREATED:
            case VALIDATED:
            case DEQUEUING:
                ProcessModel processModel = orchestratorRegistryService.getProcess(processId);
                String applicationId = processModel.getApplicationInterfaceId();
                if (applicationId == null) {
                    logger.error(processId, "Application interface id shouldn't be null.");
                    throw new OrchestratorException(
                            "Error executing the job, application interface id shouldn't be null.");
                }
                ApplicationDeploymentDescription applicationDeploymentDescription =
                        getAppDeployment(processModel, applicationId);
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
                return orchestrator.launchProcess(processModel, airavataCredStoreToken);

            default:
                logger.warn("Process " + processId + " is already launched. So it can not be relaunched");
                return false;
        }
    }

    private ApplicationDeploymentDescription getAppDeployment(ProcessModel processModel, String applicationId)
            throws OrchestratorException, RegistryServiceException {
        String selectedModuleId = getModuleId(applicationId);
        return getAppDeploymentForModule(processModel, selectedModuleId);
    }

    private ApplicationDeploymentDescription getAppDeploymentForModule(
            ProcessModel processModel, String selectedModuleId) throws OrchestratorException, RegistryServiceException {

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
        HostScheduler hostScheduler;
        try {
            var schedulerClass =
                    Class.forName(properties.getOther().getHostScheduler()).asSubclass(HostScheduler.class);
            hostScheduler = schedulerClass.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException
                | NoSuchMethodException
                | InstantiationException
                | IllegalAccessException
                | InvocationTargetException e) {
            throw new OrchestratorException("Failed to instantiate HostScheduler", e);
        }
        ComputeResourceDescription ComputeResourceDescription = hostScheduler.schedule(computeHostList);
        return deploymentMap.get(ComputeResourceDescription);
    }

    private String getModuleId(String applicationId) throws OrchestratorException, RegistryServiceException {
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
            throws OrchestratorException, RegistryServiceException, LaunchValidationException {
        if (experiment.getUserConfigurationData().isAiravataAutoSchedule()) {
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

    public void addProcessValidationErrors(String experimentId, ErrorModel details) throws RegistryServiceException {
        orchestratorRegistryService.addErrors(OrchestratorConstants.EXPERIMENT_ERROR, details, experimentId);
    }

    public String getExperimentNodePath(String experimentId) {
        return ZKPaths.makePath(ZkConstants.ZOOKEEPER_EXPERIMENT_NODE, experimentId);
    }

    public boolean launchSingleAppExperimentInternal(
            String experimentId, String airavataCredStoreToken, String gatewayId)
            throws RegistryServiceException, OrchestratorException {
        try {
            List<String> processIds = orchestratorRegistryService.getProcessIds(experimentId);
            for (String processId : processIds) {
                launchProcess(processId, airavataCredStoreToken, gatewayId);
            }
            return true;
        } catch (RegistryServiceException e) {
            ExperimentStatus status = new ExperimentStatus(ExperimentState.FAILED);
            status.setReason("Error while retrieving process IDs");
            updateAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
            logger.error("expId: " + experimentId + ", Error while retrieving process IDs", e);
            throw e;
        } catch (OrchestratorException e) {
            ExperimentStatus status = new ExperimentStatus(ExperimentState.FAILED);
            status.setReason("Error while launching processes");
            updateAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
            logger.error("expId: " + experimentId + ", Error while launching processes", e);
            throw e;
        }
    }

    public void launchQueuedExperiment(String experimentId)
            throws ExperimentNotFoundException, OrchestratorException, RegistryServiceException,
                    LaunchValidationException {
        ExperimentModel experiment = orchestratorRegistryService.getExperiment(experimentId);
        if (experiment == null) {
            throw new ExperimentNotFoundException(
                    "Error retrieving the Experiment by the given experimentID: " + experimentId);
        }

        UserConfigurationDataModel userConfigurationData = experiment.getUserConfigurationData();
        String token = getCredentialToken(experiment, userConfigurationData);
        createAndValidateTasks(experiment, true);

        // Publish experiment launched status and run launcher
        ExperimentStatus status = new ExperimentStatus(ExperimentState.LAUNCHED);
        status.setReason("submitted all processes");
        status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        updateAndPublishExperimentStatus(experimentId, status, publisher, experiment.getGatewayId());
        logger.info("expId: {}, Launched experiment ", experimentId);

        // Launch processes
        launchSingleAppExperimentInternal(experimentId, token, experiment.getGatewayId());
    }

    public void handleProcessStatusChange(
            ProcessStatusChangeEvent processStatusChangeEvent, ProcessIdentifier processIdentity)
            throws ExperimentNotFoundException, OrchestratorException, RegistryServiceException,
                    LaunchValidationException {
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
            case STARTED:
                ExperimentStatus stat =
                        orchestratorRegistryService.getExperimentStatus(processIdentity.getExperimentId());
                if (stat.getState() == ExperimentState.CANCELING) {
                    status.setState(ExperimentState.CANCELING);
                    status.setReason("Process started but experiment cancelling is triggered");
                } else {
                    status.setState(ExperimentState.EXECUTING);
                    status.setReason("process  started");
                }
                break;
            case COMPLETED:
                stat = orchestratorRegistryService.getExperimentStatus(processIdentity.getExperimentId());
                if (stat.getState() == ExperimentState.CANCELING) {
                    status.setState(ExperimentState.CANCELED);
                    status.setReason("Process competed but experiment cancelling is triggered");
                } else {
                    status.setState(ExperimentState.COMPLETED);
                    status.setReason("process  completed");
                }
                break;
            case FAILED:
                stat = orchestratorRegistryService.getExperimentStatus(processIdentity.getExperimentId());
                if (stat.getState() == ExperimentState.CANCELING) {
                    status.setState(ExperimentState.CANCELED);
                    status.setReason("Process failed but experiment cancelling is triggered");
                } else {
                    status.setState(ExperimentState.FAILED);
                    status.setReason("process  failed");
                }
                break;
            case CANCELED:
                status.setState(ExperimentState.CANCELED);
                status.setReason("process  cancelled");
                break;
            case QUEUED:
                status.setState(ExperimentState.SCHEDULED);
                status.setReason("Process started but compute resource not avaialable");
                break;
            case REQUEUED:
                status.setState(ExperimentState.SCHEDULED);
                status.setReason("Job submission failed,  requeued to resubmit");
                registerQueueStatusForRequeue(processIdentity.getExperimentId());
                break;
            case DEQUEUING:
                stat = orchestratorRegistryService.getExperimentStatus(processIdentity.getExperimentId());
                if (stat.getState() == ExperimentState.CANCELING) {
                    status.setState(ExperimentState.CANCELING);
                    status.setReason("Process started but experiment cancelling is triggered");
                } else {
                    launchQueuedExperiment(processIdentity.getExperimentId());
                }
                break;
            default:
                // ignore other status changes
                return;
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
                    queueStatusModel.setTime(System.currentTimeMillis());
                    queueStatusModels.add(queueStatusModel);
                    orchestratorRegistryService.registerQueueStatuses(queueStatusModels);
                }
            }
        } catch (RegistryServiceException e) {
            logger.error("Error while registering queue statuses", e);
        }
    }

    public void handleLaunchExperiment(ExperimentSubmitEvent expEvent)
            throws ExperimentNotFoundException, OrchestratorException, RegistryServiceException,
                    LaunchValidationException {
        ExperimentModel experimentModel = orchestratorRegistryService.getExperiment(expEvent.getExperimentId());
        if (experimentModel.getExperimentStatus().get(0).getState() == ExperimentState.CREATED) {
            launchExperimentInternal(expEvent.getExperimentId(), expEvent.getGatewayId());
        }
    }

    /**
     * Handle launch experiment from message context with deserialization and redelivery checks
     */
    public void handleLaunchExperimentFromMessage(MessageContext messageContext)
            throws ExperimentNotFoundException, OrchestratorException, RegistryServiceException,
                    LaunchValidationException {
        ExperimentSubmitEvent expEvent = new ExperimentSubmitEvent();
        try {
            byte[] bytes = ThriftUtils.serializeThriftObject(messageContext.getEvent());
            ThriftUtils.createThriftFromBytes(bytes, expEvent);
        } catch (TException e) {
            String msg = String.format(
                    "Error while handling launch experiment from message: messageContext=%s, isRedeliver=%s. Reason: %s",
                    messageContext != null ? messageContext.getGatewayId() : "null",
                    messageContext != null ? messageContext.isRedeliver() : "null",
                    e.getMessage());
            logger.error(msg, e);
            throw new OrchestratorException(msg, e);
        }

        if (messageContext.isRedeliver()) {
            try {
                ExperimentModel experimentModel = orchestratorRegistryService.getExperiment(expEvent.getExperimentId());
                if (experimentModel != null
                        && experimentModel.getExperimentStatus().get(0).getState() == ExperimentState.CREATED) {
                    handleLaunchExperiment(expEvent);
                }
            } catch (RegistryServiceException e) {
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

    public void handleCancelExperiment(ExperimentSubmitEvent expEvent)
            throws RegistryServiceException, OrchestratorException {
        terminateExperiment(expEvent.getExperimentId(), expEvent.getGatewayId());
    }

    public void handleIntermediateOutputsEvent(ExperimentIntermediateOutputsEvent event) throws OrchestratorException {
        try {
            fetchIntermediateOutputs(event.getExperimentId(), event.getGatewayId(), event.getOutputNames());
        } catch (RegistryServiceException e) {
            String msg = String.format(
                    "Error handling intermediate outputs event: experimentId=%s, gatewayId=%s, outputNames=%s. Reason: %s",
                    event.getExperimentId(), event.getGatewayId(), event.getOutputNames(), e.getMessage());
            logger.error(msg, e);
            OrchestratorException exception = new OrchestratorException(msg);
            exception.initCause(e);
            throw exception;
        } catch (OrchestratorException e) {
            String msg = String.format(
                    "Error handling intermediate outputs event: experimentId=%s, gatewayId=%s, outputNames=%s. Reason: %s",
                    event.getExperimentId(), event.getGatewayId(), event.getOutputNames(), e.getMessage());
            logger.error(msg, e);
            throw e;
        }
    }

    private void updateAndPublishExperimentStatus(
            String experimentId, ExperimentStatus status, Publisher publisher, String gatewayId) {
        try {
            orchestratorRegistryService.updateExperimentStatus(status, experimentId);
            ExperimentStatusChangeEvent event =
                    new ExperimentStatusChangeEvent(status.getState(), experimentId, gatewayId);
            String messageId = AiravataUtils.getId("EXPERIMENT");
            MessageContext messageContext = new MessageContext(event, MessageType.EXPERIMENT, messageId, gatewayId);
            messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            publisher.publish(messageContext);
        } catch (AiravataException e) {
            logger.error(
                    "expId : " + experimentId + " Error while publishing experiment status to " + status.toString(), e);
        } catch (RegistryServiceException e) {
            logger.error(
                    "expId : " + experimentId + " Error while updating experiment status to " + status.toString(), e);
        }
    }

    public ExperimentStatus getExperimentStatus(String experimentId) throws RegistryServiceException {
        return orchestratorRegistryService.getExperimentStatus(experimentId);
    }

    public ProcessModel getProcess(String processId) throws RegistryServiceException {
        return orchestratorRegistryService.getProcess(processId);
    }

    public boolean launchExperiment(String experimentId, String gatewayId, ExecutorService executorService)
            throws OrchestratorException {
        try {
            boolean result = launchExperimentInternal(experimentId, gatewayId);
            if (result) {
                ExperimentModel experiment = orchestratorRegistryService.getExperiment(experimentId);
                String token = getCredentialToken(experiment, experiment.getUserConfigurationData());
                ExperimentStatus status = new ExperimentStatus(ExperimentState.LAUNCHED);
                status.setReason("submitted all processes");
                status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                updateAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
                logger.info("expId: {}, Launched experiment ", experimentId);

                // Execute the single app experiment runner in the provided thread pool
                if (executorService != null) {
                    Runnable runner = () -> {
                        try {
                            launchSingleAppExperimentInternal(experimentId, token, gatewayId);
                        } catch (RegistryServiceException | OrchestratorException e) {
                            logger.error("expId: " + experimentId + ", Error while launching single app experiment", e);
                        }
                    };
                    executorService.execute(MDCUtil.wrapWithMDC(runner));
                }
            }
            return result;
        } catch (LaunchValidationException launchValidationException) {
            ExperimentStatus status = new ExperimentStatus(ExperimentState.FAILED);
            status.setReason("Validation failed: " + launchValidationException.getErrorMessage());
            status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            updateAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
            throw new OrchestratorException(
                    "Experiment '" + experimentId + "' launch failed. Experiment failed to validate: "
                            + launchValidationException.getErrorMessage(),
                    launchValidationException);
        } catch (RegistryServiceException e) {
            ExperimentStatus status = new ExperimentStatus(ExperimentState.FAILED);
            status.setReason("Registry error: " + e.getMessage());
            status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            updateAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
            throw new OrchestratorException("Experiment '" + experimentId + "' launch failed.", e);
        } catch (ExperimentNotFoundException e) {
            ExperimentStatus status = new ExperimentStatus(ExperimentState.FAILED);
            status.setReason("Unexpected error occurred: " + e.getMessage());
            status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            updateAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
            throw new OrchestratorException("Experiment '" + experimentId + "' launch failed.", e);
        } catch (RuntimeException e) {
            ExperimentStatus status = new ExperimentStatus(ExperimentState.FAILED);
            status.setReason("Unexpected runtime error occurred: " + e.getMessage());
            status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            updateAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
            throw new OrchestratorException("Experiment '" + experimentId + "' launch failed.", e);
        }
    }

    private void startCurator() {
        String connectionSting = properties.getZookeeper().getServerConnection();
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5);
        curatorClient = CuratorFrameworkFactory.newClient(connectionSting, retryPolicy);
        curatorClient.start();
    }

    private Subscriber getExperimentSubscriber() throws AiravataException {
        List<String> routingKeys = new ArrayList<>();
        routingKeys.add(properties.getRabbitMQ().getExperimentLaunchQueueName());
        return MessagingFactory.getSubscriber(new ExperimentHandler(), routingKeys, Type.EXPERIMENT_LAUNCH);
    }

    private class ExperimentHandler implements MessageHandler {

        @Override
        public void onMessage(MessageContext messageContext) {
            MDC.put(MDCConstants.GATEWAY_ID, messageContext.getGatewayId());
            switch (messageContext.getType()) {
                case EXPERIMENT:
                    launchExperiment(messageContext);
                    break;
                case EXPERIMENT_CANCEL:
                    cancelExperiment(messageContext);
                    break;
                case INTERMEDIATE_OUTPUTS:
                    handleIntermediateOutputsEvent(messageContext);
                    break;
                default:
                    experimentSubscriber.sendAck(messageContext.getDeliveryTag());
                    logger.error("Orchestrator got un-support message type : " + messageContext.getType());
                    break;
            }
            MDC.clear();
        }

        private void cancelExperiment(MessageContext messageContext) {
            try {
                byte[] bytes = ThriftUtils.serializeThriftObject(messageContext.getEvent());
                ExperimentSubmitEvent expEvent = new ExperimentSubmitEvent();
                ThriftUtils.createThriftFromBytes(bytes, expEvent);
                logger.info(
                        "Cancelling experiment with experimentId: {} gateway Id: {}",
                        expEvent.getExperimentId(),
                        expEvent.getGatewayId());
                handleCancelExperiment(expEvent);
            } catch (RegistryServiceException | OrchestratorException e) {
                String msg = String.format(
                        "Error while cancelling experiment: messageContext gatewayId=%s, deliveryTag=%s, isRedeliver=%s. Reason: %s",
                        messageContext.getGatewayId(),
                        messageContext.getDeliveryTag(),
                        messageContext.isRedeliver(),
                        e.getMessage());
                logger.error(msg, e);
                // Log but don't throw - we need to ack the message
            } catch (TException e) {
                String msg = String.format(
                        "Error while cancelling experiment: messageContext gatewayId=%s, deliveryTag=%s, isRedeliver=%s. Reason: %s",
                        messageContext.getGatewayId(),
                        messageContext.getDeliveryTag(),
                        messageContext.isRedeliver(),
                        e.getMessage());
                logger.error(msg, e);
                // Log but don't throw - we need to ack the message
            } catch (RuntimeException e) {
                String msg = String.format(
                        "Error while cancelling experiment: messageContext gatewayId=%s, deliveryTag=%s, isRedeliver=%s. Reason: %s",
                        messageContext.getGatewayId(),
                        messageContext.getDeliveryTag(),
                        messageContext.isRedeliver(),
                        e.getMessage());
                logger.error(msg, e);
                // Log but don't throw - we need to ack the message
            } finally {
                experimentSubscriber.sendAck(messageContext.getDeliveryTag());
            }
        }

        private void handleIntermediateOutputsEvent(MessageContext messageContext) {
            try {
                byte[] bytes = ThriftUtils.serializeThriftObject(messageContext.getEvent());
                ExperimentIntermediateOutputsEvent event = new ExperimentIntermediateOutputsEvent();
                ThriftUtils.createThriftFromBytes(bytes, event);
                logger.info(
                        "INTERMEDIATE_OUTPUTS event for experimentId: {} gateway Id: {} outputs: {}",
                        event.getExperimentId(),
                        event.getGatewayId(),
                        event.getOutputNames());
                OrchestratorService.this.handleIntermediateOutputsEvent(event);
            } catch (OrchestratorException e) {
                String msg = String.format(
                        "Error while fetching intermediate outputs: messageContext gatewayId=%s, deliveryTag=%s, isRedeliver=%s. Reason: %s",
                        messageContext.getGatewayId(),
                        messageContext.getDeliveryTag(),
                        messageContext.isRedeliver(),
                        e.getMessage());
                logger.error(msg, e);
                // Log but don't throw - we need to ack the message
            } catch (TException e) {
                String msg = String.format(
                        "Error while fetching intermediate outputs: messageContext gatewayId=%s, deliveryTag=%s, isRedeliver=%s. Reason: %s",
                        messageContext.getGatewayId(),
                        messageContext.getDeliveryTag(),
                        messageContext.isRedeliver(),
                        e.getMessage());
                logger.error(msg, e);
                // Log but don't throw - we need to ack the message
            } catch (RuntimeException e) {
                String msg = String.format(
                        "Error while fetching intermediate outputs: messageContext gatewayId=%s, deliveryTag=%s, isRedeliver=%s. Reason: %s",
                        messageContext.getGatewayId(),
                        messageContext.getDeliveryTag(),
                        messageContext.isRedeliver(),
                        e.getMessage());
                logger.error(msg, e);
                // Log but don't throw - we need to ack the message
            } finally {
                experimentSubscriber.sendAck(messageContext.getDeliveryTag());
            }
        }

        private void launchExperiment(MessageContext messageContext) {
            try {
                handleLaunchExperimentFromMessage(messageContext);
            } catch (RegistryServiceException | OrchestratorException e) {
                logger.error("Experiment launch failed due to registry or orchestrator error", e);
            } catch (TException e) {
                logger.error("An unknown issue while launching experiment", e);
            } catch (RuntimeException e) {
                logger.error("An unknown runtime issue while launching experiment", e);
            } finally {
                experimentSubscriber.sendAck(messageContext.getDeliveryTag());
                MDC.clear();
            }
        }
    }
}
