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

import java.text.MessageFormat;
import java.util.*;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.logging.MDCUtil;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.common.utils.ZkConstants;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.Publisher;
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
import org.apache.airavata.model.error.LaunchValidationException;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.status.ExperimentStatus;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.model.status.QueueStatusModel;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.airavata.model.messaging.event.ExperimentIntermediateOutputsEvent;
import org.apache.airavata.model.messaging.event.ExperimentSubmitEvent;
import org.apache.airavata.model.messaging.event.ProcessIdentifier;
import org.apache.airavata.model.messaging.event.ProcessStatusChangeEvent;
import org.apache.airavata.model.util.ExperimentModelUtil;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.core.schedule.HostScheduler;
import org.apache.airavata.orchestrator.core.utils.OrchestratorConstants;
import org.apache.airavata.orchestrator.cpi.impl.SimpleOrchestratorImpl;
import org.apache.airavata.orchestrator.util.OrchestratorUtils;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.apache.thrift.TException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrchestratorService {
    private static final Logger logger = LoggerFactory.getLogger(OrchestratorService.class);
    
    private OrchestratorRegistryService orchestratorRegistryService;
    private SimpleOrchestratorImpl orchestrator;
    private CuratorFramework curatorClient;
    private Publisher publisher;

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

    public boolean launchExperiment(String experimentId, String gatewayId) throws Exception {
        String experimentNodePath = getExperimentNodePath(experimentId);
        ZKPaths.mkdirs(curatorClient.getZookeeperClient().getZooKeeper(), experimentNodePath);
        String experimentCancelNode =
                ZKPaths.makePath(experimentNodePath, ZkConstants.ZOOKEEPER_CANCEL_LISTENER_NODE);
        ZKPaths.mkdirs(curatorClient.getZookeeperClient().getZooKeeper(), experimentCancelNode);
        ExperimentModel experiment = orchestratorRegistryService.getExperiment(experimentId);
        if (experiment == null) {
            throw new Exception("Error retrieving the Experiment by the given experimentID: " + experimentId);
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
            throw new TException("Experiment '" + experimentId
                    + "' launch failed. Unable to figureout execution type for application "
                    + experiment.getExecutionId());
        }
    }

    private boolean launchSingleAppExperiment(
            ExperimentModel experiment, String experimentId, String gatewayId, String token) throws Exception {
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
            throw new Exception("Validating process fails for given experiment Id : " + experimentId);
        }

        ProcessScheduler scheduler = new ProcessSchedulerImpl();
        if (!experiment.getUserConfigurationData().isAiravataAutoSchedule()
                || scheduler.canLaunch(experimentId)) {
            createAndValidateTasks(experiment, false);
            return true; // runExperimentLauncher will be called separately
        } else {
            logger.debug(experimentId, "Queuing single application experiment {}.", experimentId);
            ExperimentStatus status = new ExperimentStatus(ExperimentState.SCHEDULED);
            status.setReason("Compute resources are not ready");
            status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            OrchestratorUtils.updateAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
            logger.info("expId: {}, Scheduled experiment ", experimentId);
            return false;
        }
    }

    private void resolveInputReplicas(ProcessModel processModel) throws Exception {
        for (var pi : processModel.getProcessInputs()) {
            if (pi.getType().equals(DataType.URI)
                    && pi.getValue() != null
                    && pi.getValue().startsWith("airavata-dp://")) {
                try {
                    DataProductModel dataProductModel = orchestratorRegistryService.getDataProduct(pi.getValue());
                    Optional<DataReplicaLocationModel> rpLocation =
                            dataProductModel.getReplicaLocations().stream()
                                    .filter(rpModel -> rpModel.getReplicaLocationCategory()
                                            .equals(ReplicaLocationCategory.GATEWAY_DATA_STORE))
                                    .findFirst();
                    if (rpLocation.isPresent()) {
                        pi.setValue(rpLocation.get().getFilePath());
                        pi.setStorageResourceId(rpLocation.get().getStorageResourceId());
                    } else {
                        logger.error("Could not find a replica for the URI " + pi.getValue());
                    }
                } catch (RegistryException e) {
                    throw new Exception("Error while launching experiment", e);
                }
            } else if (pi.getType().equals(DataType.URI_COLLECTION)
                    && pi.getValue() != null
                    && pi.getValue().contains("airavata-dp://")) {
                try {
                    String[] uriList = pi.getValue().split(",");
                    final ArrayList<String> filePathList = new ArrayList<>();
                    for (String uri : uriList) {
                        if (uri.startsWith("airavata-dp://")) {
                            DataProductModel dataProductModel = orchestratorRegistryService.getDataProduct(uri);
                            Optional<DataReplicaLocationModel> rpLocation =
                                    dataProductModel.getReplicaLocations().stream()
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
                } catch (RegistryException e) {
                    throw new Exception("Error while launching experiment", e);
                }
            }
        }
    }

    public String getCredentialToken(ExperimentModel experiment, UserConfigurationDataModel userConfigurationData) throws Exception {
        String token = null;
        final String groupResourceProfileId = userConfigurationData.getGroupResourceProfileId();
        if (groupResourceProfileId == null) {
            throw new Exception("Experiment not configured with a Group Resource Profile: " + experiment.getExperimentId());
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
            throw new Exception(
                    "You have not configured credential store token at group resource profile or compute resource preference."
                            + " Please provide the correct token at group resource profile or compute resource preference.");
        }
        return token;
    }

    public boolean validateExperiment(String experimentId) throws TException, LaunchValidationException, RegistryException, OrchestratorException {
        ExperimentModel experimentModel = orchestratorRegistryService.getExperiment(experimentId);
        return orchestrator.validateExperiment(experimentModel).isValidationState();
    }

    public boolean validateProcess(String experimentId, List<ProcessModel> processes)
            throws LaunchValidationException, TException, RegistryException, OrchestratorException {
        ExperimentModel experimentModel = orchestratorRegistryService.getExperiment(experimentId);
        for (ProcessModel processModel : processes) {
            boolean state = orchestrator
                    .validateProcess(experimentModel, processModel)
                    .isSetValidationState();
            if (!state) {
                return false;
            }
        }
        return true;
    }

    public boolean terminateExperiment(String experimentId, String gatewayId) throws Exception {
        logger.info(experimentId, "Experiment: {} is cancelling  !!!!!", experimentId);
        return validateStatesAndCancel(experimentId, gatewayId);
    }

    private boolean validateStatesAndCancel(String experimentId, String gatewayId) throws Exception {
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
                Stat stat = curatorClient.checkExists().forPath(expCancelNodePath);
                if (stat != null) {
                    curatorClient
                            .setData()
                            .withVersion(-1)
                            .forPath(expCancelNodePath, ZkConstants.ZOOKEEPER_CANCEL_REQEUST.getBytes());
                    ExperimentStatus status = new ExperimentStatus(ExperimentState.CANCELING);
                    status.setReason("Experiment cancel request processed");
                    status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                    OrchestratorUtils.updateAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
                    logger.info("expId : " + experimentId + " :- Experiment status updated to " + status.getState());
                }
                return true;
        }
    }

    public void fetchIntermediateOutputs(String experimentId, String gatewayId, List<String> outputNames)
            throws Exception {
        submitIntermediateOutputsProcess(experimentId, gatewayId, outputNames);
    }

    private void submitIntermediateOutputsProcess(
            String experimentId, String gatewayId, List<String> outputNames) throws Exception {

        ExperimentModel experimentModel = orchestratorRegistryService.getExperiment(experimentId);
        ProcessModel processModel = ExperimentModelUtil.cloneProcessFromExperiment(experimentModel);
        processModel.setExperimentDataDir(processModel.getExperimentDataDir() + "/intermediates");

        List<OutputDataObjectType> applicationOutputs = orchestratorRegistryService.getApplicationOutputs(
                experimentModel.getExecutionId());
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
                throw new Exception(MessageFormat.format(
                        "Could not find job submission process for experiment {0}, unable to fetch intermediate outputs {1}",
                        experimentId, outputNames));
            }
            String taskDag = orchestrator.createAndSaveIntermediateOutputFetchingTasks(
                    gatewayId, processModel, jobSubmissionProcess.get());
            processModel.setTaskDag(taskDag);

            orchestratorRegistryService.updateProcess(processModel, processModel.getProcessId());

            String token = getCredentialToken(experimentModel, experimentModel.getUserConfigurationData());
            orchestrator.launchProcess(processModel, token);
        } catch (Exception e) {
            logger.error("Failed to launch process for intermediate output fetching", e);

            ProcessStatus status = new ProcessStatus(ProcessState.FAILED);
            status.setReason("Intermediate output fetching process failed to launch: " + e.getMessage());
            status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            orchestratorRegistryService.addProcessStatus(status, processId);

            throw e;
        }
    }

    public boolean launchProcess(String processId, String airavataCredStoreToken, String gatewayId) throws Exception {
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

    private ApplicationDeploymentDescription getAppDeployment(
            ProcessModel processModel, String applicationId)
            throws Exception {
        String selectedModuleId = getModuleId(applicationId);
        return getAppDeploymentForModule(processModel, selectedModuleId);
    }

    private ApplicationDeploymentDescription getAppDeploymentForModule(
            ProcessModel processModel, String selectedModuleId)
            throws Exception {

        List<ApplicationDeploymentDescription> applicationDeployements =
                orchestratorRegistryService.getApplicationDeployments(selectedModuleId);
        Map<ComputeResourceDescription, ApplicationDeploymentDescription> deploymentMap =
                new HashMap<>();

        for (ApplicationDeploymentDescription deploymentDescription : applicationDeployements) {
            if (processModel.getComputeResourceId().equals(deploymentDescription.getComputeHostId())) {
                deploymentMap.put(
                        orchestratorRegistryService.getComputeResource(deploymentDescription.getComputeHostId()),
                        deploymentDescription);
            }
        }
        List<ComputeResourceDescription> computeHostList =
                Arrays.asList(deploymentMap.keySet().toArray(new ComputeResourceDescription[] {}));
        Class<? extends HostScheduler> aClass =
                Class.forName(ServerSettings.getHostScheduler()).asSubclass(HostScheduler.class);
        HostScheduler hostScheduler = aClass.newInstance();
        ComputeResourceDescription ComputeResourceDescription = hostScheduler.schedule(computeHostList);
        return deploymentMap.get(ComputeResourceDescription);
    }

    private String getModuleId(String applicationId)
            throws Exception {
        ApplicationInterfaceDescription applicationInterface = orchestratorRegistryService.getApplicationInterface(applicationId);
        List<String> applicationModules = applicationInterface.getApplicationModules();
        if (applicationModules.size() == 0) {
            throw new OrchestratorException("No modules defined for application " + applicationId);
        }
        String selectedModuleId = applicationModules.get(0);
        return selectedModuleId;
    }

    private void launchWorkflowExperiment(String experimentId, String airavataCredStoreToken, String gatewayId)
            throws TException {
        // FIXME - Workflow support not implemented
    }

    public void createAndValidateTasks(ExperimentModel experiment, boolean recreateTaskDag) throws Exception {
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
                throw new Exception(
                        "Validating process fails for given experiment Id : " + experiment.getExperimentId());
            }
        }
    }

    public void addProcessValidationErrors(String experimentId, ErrorModel details) throws RegistryException {
        orchestratorRegistryService.addErrors(OrchestratorConstants.EXPERIMENT_ERROR, details, experimentId);
    }

    public String getExperimentNodePath(String experimentId) {
        return ZKPaths.makePath(ZkConstants.ZOOKEEPER_EXPERIMENT_NODE, experimentId);
    }

    public boolean launchSingleAppExperimentInternal(String experimentId, String airavataCredStoreToken, String gatewayId) throws Exception {
        try {
            List<String> processIds = orchestratorRegistryService.getProcessIds(experimentId);
            for (String processId : processIds) {
                launchProcess(processId, airavataCredStoreToken, gatewayId);
            }
            return true;
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            ExperimentStatus status = new ExperimentStatus(ExperimentState.FAILED);
            status.setReason("Error while retrieving process IDs");
            OrchestratorUtils.updateAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
            logger.error("expId: " + experimentId + ", Error while retrieving process IDs", e);
            throw new Exception("Error while retrieving process IDs", e);
        } catch (Exception e) {
            ExperimentStatus status = new ExperimentStatus(ExperimentState.FAILED);
            status.setReason("Error while launching processes");
            OrchestratorUtils.updateAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
            logger.error("expId: " + experimentId + ", Error while launching processes", e);
            throw e;
        }
    }

    public void launchQueuedExperiment(String experimentId) throws Exception {
        ExperimentModel experiment = orchestratorRegistryService.getExperiment(experimentId);
        if (experiment == null) {
            throw new Exception("Error retrieving the Experiment by the given experimentID: " + experimentId);
        }

        UserConfigurationDataModel userConfigurationData = experiment.getUserConfigurationData();
        String token = getCredentialToken(experiment, userConfigurationData);
        createAndValidateTasks(experiment, true);
        
        // Publish experiment launched status and run launcher
        ExperimentStatus status = new ExperimentStatus(ExperimentState.LAUNCHED);
        status.setReason("submitted all processes");
        status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        OrchestratorUtils.updateAndPublishExperimentStatus(experimentId, status, publisher, experiment.getGatewayId());
        logger.info("expId: {}, Launched experiment ", experimentId);
        
        // Launch processes
        launchSingleAppExperimentInternal(experimentId, token, experiment.getGatewayId());
    }

    public void handleProcessStatusChange(
            ProcessStatusChangeEvent processStatusChangeEvent,
            ProcessIdentifier processIdentity) throws Exception {
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
                ExperimentStatus stat = orchestratorRegistryService.getExperimentStatus(processIdentity.getExperimentId());
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
            OrchestratorUtils.updateAndPublishExperimentStatus(
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
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            logger.error("Error while registering queue statuses", e);
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            logger.error("Error while getting compute resource for queue status", e);
        }
    }

    public void handleLaunchExperiment(ExperimentSubmitEvent expEvent) throws Exception {
        ExperimentModel experimentModel = orchestratorRegistryService.getExperiment(expEvent.getExperimentId());
        if (experimentModel.getExperimentStatus().get(0).getState() == ExperimentState.CREATED) {
            launchExperiment(expEvent.getExperimentId(), expEvent.getGatewayId());
        }
    }

    /**
     * Handle launch experiment from message context with deserialization and redelivery checks
     */
    public void handleLaunchExperimentFromMessage(MessageContext messageContext) throws Exception {
        ExperimentSubmitEvent expEvent = new ExperimentSubmitEvent();
        byte[] bytes = ThriftUtils.serializeThriftObject(messageContext.getEvent());
        ThriftUtils.createThriftFromBytes(bytes, expEvent);
        
        if (messageContext.isRedeliver()) {
            ExperimentModel experimentModel = orchestratorRegistryService.getExperiment(expEvent.getExperimentId());
            if (experimentModel != null && experimentModel.getExperimentStatus().get(0).getState() == ExperimentState.CREATED) {
                handleLaunchExperiment(expEvent);
            }
        } else {
            handleLaunchExperiment(expEvent);
        }
    }

    public void handleCancelExperiment(ExperimentSubmitEvent expEvent) throws Exception {
        terminateExperiment(expEvent.getExperimentId(), expEvent.getGatewayId());
    }

    public void handleIntermediateOutputsEvent(ExperimentIntermediateOutputsEvent event) throws Exception {
        fetchIntermediateOutputs(event.getExperimentId(), event.getGatewayId(), event.getOutputNames());
    }

    public boolean launchExperimentWithErrorHandling(String experimentId, String gatewayId, java.util.concurrent.ExecutorService executorService) throws TException {
        try {
            boolean result = launchExperiment(experimentId, gatewayId);
            if (result) {
                ExperimentModel experiment = orchestratorRegistryService.getExperiment(experimentId);
                String token = getCredentialToken(experiment, experiment.getUserConfigurationData());
                ExperimentStatus status = new ExperimentStatus(ExperimentState.LAUNCHED);
                status.setReason("submitted all processes");
                status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                OrchestratorUtils.updateAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
                logger.info("expId: {}, Launched experiment ", experimentId);
                
                // Execute the single app experiment runner in the provided thread pool
                if (executorService != null) {
                    Runnable runner = () -> {
                        try {
                            launchSingleAppExperimentInternal(experimentId, token, gatewayId);
                        } catch (Exception e) {
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
            OrchestratorUtils.updateAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
            throw new TException(
                    "Experiment '" + experimentId + "' launch failed. Experiment failed to validate: "
                            + launchValidationException.getErrorMessage(),
                    launchValidationException);
        } catch (org.apache.airavata.registry.cpi.RegistryException e) {
            ExperimentStatus status = new ExperimentStatus(ExperimentState.FAILED);
            status.setReason("Registry error: " + e.getMessage());
            status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            OrchestratorUtils.updateAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
            throw new TException("Experiment '" + experimentId + "' launch failed.", e);
        } catch (org.apache.airavata.registry.cpi.AppCatalogException e) {
            ExperimentStatus status = new ExperimentStatus(ExperimentState.FAILED);
            status.setReason("App catalog error: " + e.getMessage());
            status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            OrchestratorUtils.updateAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
            throw new TException("Experiment '" + experimentId + "' launch failed.", e);
        } catch (Exception e) {
            ExperimentStatus status = new ExperimentStatus(ExperimentState.FAILED);
            status.setReason("Unexpected error occurred: " + e.getMessage());
            status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            OrchestratorUtils.updateAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
            throw new TException("Experiment '" + experimentId + "' launch failed.", e);
        }
    }
}

