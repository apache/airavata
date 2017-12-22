/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.orchestrator.server;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.logging.MDCConstants;
import org.apache.airavata.common.logging.MDCUtil;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.common.utils.ZkConstants;
import org.apache.airavata.gfac.core.GFacUtils;
import org.apache.airavata.gfac.core.scheduler.HostScheduler;
import org.apache.airavata.messaging.core.*;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.data.replica.DataProductModel;
import org.apache.airavata.model.data.replica.DataReplicaLocationModel;
import org.apache.airavata.model.data.replica.ReplicaLocationCategory;
import org.apache.airavata.model.error.LaunchValidationException;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.messaging.event.*;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.process.ProcessType;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.status.ExperimentStatus;
import org.apache.airavata.model.util.ExperimentModelUtil;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.cpi.OrchestratorService;
import org.apache.airavata.orchestrator.cpi.impl.SimpleOrchestratorImpl;
import org.apache.airavata.orchestrator.cpi.orchestrator_cpiConstants;
import org.apache.airavata.orchestrator.util.OrchestratorServerThreadPoolExecutor;
import org.apache.airavata.orchestrator.util.OrchestratorUtils;
import org.apache.airavata.registry.core.app.catalog.resources.AppCatAbstractResource;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.core.experiment.catalog.resources.AbstractExpCatResource;
import org.apache.airavata.registry.cpi.*;
import org.apache.airavata.registry.cpi.utils.Constants;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ZKPaths;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.*;

public class OrchestratorServerHandler implements OrchestratorService.Iface {
	private static Logger log = LoggerFactory.getLogger(OrchestratorServerHandler.class);
	private SimpleOrchestratorImpl orchestrator = null;
	private ExperimentCatalog experimentCatalog;
    private AppCatalog appCatalog;
	private static Integer mutex = new Integer(-1);
	private String airavataUserName;
	private String gatewayName;
	private Publisher publisher;
	private final Subscriber statusSubscribe;
	private final Subscriber experimentSubscriber;
	private CuratorFramework curatorClient;

    /**
	 * Query orchestrator server to fetch the CPI version
	 */
	public String getOrchestratorCPIVersion() throws TException {
		return orchestrator_cpiConstants.ORCHESTRATOR_CPI_VERSION;
	}

	public OrchestratorServerHandler() throws OrchestratorException{
		// orchestrator init
		try {
			// first constructing the monitorManager and orchestrator, then fill
			// the required properties
			setAiravataUserName(ServerSettings.getDefaultUser());
			orchestrator = new SimpleOrchestratorImpl();
			experimentCatalog = RegistryFactory.getDefaultExpCatalog();
			appCatalog = RegistryFactory.getAppCatalog();

			publisher = MessagingFactory.getPublisher(Type.STATUS);
			orchestrator.initialize();
			orchestrator.getOrchestratorContext().setPublisher(this.publisher);
			statusSubscribe = getStatusSubscriber();
			experimentSubscriber  = getExperimentSubscriber();
			startCurator();
		} catch (OrchestratorException | RegistryException | AppCatalogException | AiravataException e) {
			log.error(e.getMessage(), e);
			throw new OrchestratorException("Error while initializing orchestrator service", e);
		}
	}

	private Subscriber getStatusSubscriber() throws AiravataException {
		List<String> routingKeys = new ArrayList<>();
//			routingKeys.add("*"); // listen for gateway level messages
//			routingKeys.add("*.*"); // listen for gateway/experiment level messages
		routingKeys.add("*.*.*"); // listen for gateway/experiment/process level messages
		return MessagingFactory.getSubscriber(new ProcessStatusHandler(),routingKeys, Type.STATUS);
	}

	private Subscriber getExperimentSubscriber() throws AiravataException {
		List<String> routingKeys = new ArrayList<>();
		routingKeys.add(ServerSettings.getRabbitmqExperimentLaunchQueueName());
		return MessagingFactory.getSubscriber(new ExperimentHandler(), routingKeys, Type.EXPERIMENT_LAUNCH);
	}

	/**
	 * * After creating the experiment Data user have the * experimentID as the
	 * handler to the experiment, during the launchProcess * We just have to
	 * give the experimentID * * @param experimentID * @return sucess/failure *
	 * *
	 * 
	 * @param experimentId
	 */
	public boolean launchExperiment(String experimentId, String gatewayId) throws TException {
        ExperimentModel experiment = null;
        try {
            String experimentNodePath = GFacUtils.getExperimentNodePath (experimentId);
			ZKPaths.mkdirs(curatorClient.getZookeeperClient().getZooKeeper(), experimentNodePath);
			String experimentCancelNode = ZKPaths.makePath(experimentNodePath, ZkConstants.ZOOKEEPER_CANCEL_LISTENER_NODE);
			ZKPaths.mkdirs(curatorClient.getZookeeperClient().getZooKeeper(), experimentCancelNode);
            experiment = (ExperimentModel) experimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT, experimentId);
            if (experiment == null) {
                log.error("Error retrieving the Experiment by the given experimentID: {} ", experimentId);
                return false;
            }

            String token = getCredentialsStoreToken(experiment, gatewayId);

            if (token == null || token.isEmpty()){
                // try with gateway profile level token
                GatewayResourceProfile gatewayProfile = appCatalog.getGatewayProfile().getGatewayProfile(gatewayId);
                token = gatewayProfile.getCredentialStoreToken();
            }
            // still the token is empty, then we fail the experiment
            if (token == null || token.isEmpty()){
                log.error("You have not configured credential store token at gateway profile or compute resource preference." +
						" Please provide the correct token at gateway profile or compute resource preference.");
                return false;
            }
            ExperimentType executionType = experiment.getExperimentType();
            if (executionType == ExperimentType.SINGLE_APPLICATION) {
                //its an single application execution experiment
                List<ProcessModel> processes = orchestrator.createProcesses(experimentId, gatewayId);

				for (ProcessModel processModel : processes){
					parseInputOutput(processModel);
					String taskDag = orchestrator.createAndSaveTasks(gatewayId, processModel, experiment.getUserConfigurationData().isAiravataAutoSchedule());
					processModel.setTaskDag(taskDag);
					experimentCatalog.update(ExperimentCatalogModelType.PROCESS, processModel, processModel.getProcessId());
				}

				if (!validateProcess(experimentId, processes)) {
					log.error("Validating process fails for given experiment Id : {}", experimentId);
					return false;
				}

				log.debug(experimentId, "Launching single application experiment {}.", experimentId);
                ExperimentStatus status = new ExperimentStatus(ExperimentState.LAUNCHED);
                status.setReason("submitted all processes");
                status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                OrchestratorUtils.updageAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
                log.info("expId: {}, Launched experiment ", experimentId);
                OrchestratorServerThreadPoolExecutor.getCachedThreadPool().execute(MDCUtil.wrapWithMDC(new SingleAppExperimentRunner(experimentId, token, gatewayId)));
            } else if (executionType == ExperimentType.WORKFLOW) {
                //its a workflow execution experiment
                log.debug(experimentId, "Launching workflow experiment {}.", experimentId);
                launchWorkflowExperiment(experimentId, token, gatewayId);
            } else {
                log.error(experimentId, "Couldn't identify experiment type, experiment {} is neither single application nor workflow.", experimentId);
                throw new TException("Experiment '" + experimentId + "' launch failed. Unable to figureout execution type for application " + experiment.getExecutionId());
            }
		} catch (LaunchValidationException launchValidationException) {
			ExperimentStatus status = new ExperimentStatus(ExperimentState.FAILED);
			status.setReason("Validation failed: " + launchValidationException.getErrorMessage());
			status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
			OrchestratorUtils.updageAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
			throw new TException("Experiment '" + experimentId + "' launch failed. Experiment failed to validate: " + launchValidationException.getErrorMessage(), launchValidationException);
        } catch (Exception e) {
            throw new TException("Experiment '" + experimentId + "' launch failed. Unable to figureout execution type for application " + experiment.getExecutionId(), e);
        }
        return true;
	}

	private String getCredentialsStoreToken(ExperimentModel experiment, String gatewayId) throws AppCatalogException {
        ComputeResourcePreference computeResourcePreference = appCatalog.getGatewayProfile().
                getComputeResourcePreference(gatewayId,
                        experiment.getUserConfigurationData().getComputationalResourceScheduling().getResourceHostId());
        return computeResourcePreference.getResourceSpecificCredentialStoreToken();
    }

	private void parseInputOutput(ProcessModel processModel) throws ReplicaCatalogException {
        //FIXME Resolving replica if available. This is a very crude way of resolving input replicas. A full featured
        //FIXME replica resolving logic should come here

        ReplicaCatalog replicaCatalog = RegistryFactory.getReplicaCatalog();
        processModel.getProcessInputs().forEach(pi -> {
            if (pi.getType().equals(DataType.URI) && pi.getValue().startsWith("airavata-dp://")) {
                try {
                    DataProductModel dataProductModel = replicaCatalog.getDataProduct(pi.getValue());
                    Optional<DataReplicaLocationModel> rpLocation = dataProductModel.getReplicaLocations()
                            .stream().filter(rpModel -> rpModel.getReplicaLocationCategory().
                                    equals(ReplicaLocationCategory.GATEWAY_DATA_STORE)).findFirst();
                    if (rpLocation.isPresent()) {
                        pi.setValue(rpLocation.get().getFilePath());
                        pi.setStorageResourceId(rpLocation.get().getStorageResourceId());
                    } else {
                        log.error("Could not find a replica for the URI " + pi.getValue());
                    }
                } catch (ReplicaCatalogException e) {
                    log.error(e.getMessage(), e);
                }
            } else if (pi.getType().equals(DataType.URI_COLLECTION) && pi.getValue().contains("airavata-dp://")) {
                try {
                    String[] uriList = pi.getValue().split(",");
                    final ArrayList<String> filePathList = new ArrayList<>();
                    for (String uri : uriList) {
                        if (uri.startsWith("airavata-dp://")) {
                            DataProductModel dataProductModel = replicaCatalog.getDataProduct(uri);
                            Optional<DataReplicaLocationModel> rpLocation = dataProductModel.getReplicaLocations()
                                    .stream().filter(rpModel -> rpModel.getReplicaLocationCategory().
                                            equals(ReplicaLocationCategory.GATEWAY_DATA_STORE)).findFirst();
                            if (rpLocation.isPresent()) {
                                filePathList.add(rpLocation.get().getFilePath());
                            } else {
                                log.error("Could not find a replica for the URI " + pi.getValue());
                            }
                        } else {
                            // uri is in file path format
                            filePathList.add(uri);
                        }
                    }
                    pi.setValue(StringUtils.join(filePathList, ','));
                } catch (ReplicaCatalogException e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
    }

	private ProcessModel createAndSavePostProcessingProcess(ExperimentModel experiment, String gatewayId) throws
            RegistryException, AiravataException, AppCatalogException {


        List<Object> processes = experimentCatalog.get(ExperimentCatalogModelType.PROCESS,
                Constants.FieldConstants.ProcessConstants.EXPERIMENT_ID, experiment.getExperimentId());

        ProcessModel primaryProcess = null;
        if (processes.size() > 0) {
            Optional<ProcessModel> primaryProcessOp = processes.stream().map(ProcessModel.class::cast)
                    .filter(processModel -> ProcessType.PRIMARY == processModel.getProcessType()).findFirst();
            if (primaryProcessOp.isPresent()) {
                primaryProcess = primaryProcessOp.get();
            }
        }

        if (primaryProcess == null) {
            log.error("Can not create post processing process without the primary process for experiment with id {}",
                    experiment.getExperimentId());
            throw new AiravataException("Can not create post processing process without the primary process for " +
                    "experiment with id " + experiment.getExperimentId());
        }

        ProcessModel processModel = ExperimentModelUtil.cloneProcessFromExperiment(experiment);
        processModel.setProcessType(ProcessType.FORCE_POST_PROCESING);

        String processId = (String)experimentCatalog.add(ExpCatChildDataType.PROCESS, processModel, experiment.getExperimentId());
        processModel.setProcessId(processId);

        parseInputOutput(processModel);

        List<String> tasks = orchestrator.createAndSaveOutputDataStagingTasks(processModel, gatewayId,
                primaryProcess.getProcessId()); /* Make the working dir of this process as the working dir of
         primary process as this is dealing with the content created by primary process */

        String taskDag = orchestrator.getTaskDag(tasks);
        processModel.setTaskDag(taskDag);

        experimentCatalog.update(ExperimentCatalogModelType.PROCESS, processModel, processModel.getProcessId());
        return processModel;
    }
	/**
	 * This method will validate the experiment before launching, if is failed
	 * we do not run the launch in airavata thrift service (only if validation
	 * is enabled
	 * 
	 * @param experimentId
	 * @return
	 * @throws TException
	 */
	public boolean validateExperiment(String experimentId) throws TException, LaunchValidationException {
		try {
            ExperimentModel experimentModel = (ExperimentModel)experimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT, experimentId);
            return orchestrator.validateExperiment(experimentModel).isValidationState();
		} catch (OrchestratorException e) {
            log.error(experimentId, "Error while validating experiment", e);
			throw new TException(e);
		} catch (RegistryException e) {
            log.error(experimentId, "Error while validating experiment", e);
			throw new TException(e);
		}
	}

    @Override
    public boolean validateProcess(String experimentId, List<ProcessModel> processes) throws LaunchValidationException, TException {
        try {
			ExperimentModel experimentModel = (ExperimentModel) experimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT, experimentId);
			for (ProcessModel processModel : processes) {
				boolean state = orchestrator.validateProcess(experimentModel, processModel).isSetValidationState();
				if (!state) {
					return false;
				}
			}
			return true;
		} catch (LaunchValidationException lve) {

			// If a process failed to validate, also add an error message at the experiment level
			ErrorModel details = new ErrorModel();
			details.setActualErrorMessage(lve.getErrorMessage());
			details.setCreationTime(Calendar.getInstance().getTimeInMillis());
			try {
				experimentCatalog.add(ExpCatChildDataType.EXPERIMENT_ERROR, details, experimentId);
			} catch (RegistryException e) {
			    log.error("Failed to add EXPERIMENT_ERROR regarding LaunchValidationException to experiment " + experimentId, e);
			}
			throw lve;
        } catch (OrchestratorException e) {
            log.error(experimentId, "Error while validating process", e);
            throw new TException(e);
        } catch (RegistryException e) {
            log.error(experimentId, "Error while validating process", e);
            throw new TException(e);
        }
    }

    /**
	 * This can be used to cancel a running experiment and store the status to
	 * terminated in registry
	 * 
	 * @param experimentId
	 * @return
	 * @throws TException
	 */
	public boolean terminateExperiment(String experimentId, String gatewayId) throws TException {
        log.info(experimentId, "Experiment: {} is cancelling  !!!!!", experimentId);
		try {
			return validateStatesAndCancel(experimentId, gatewayId);
		} catch (Exception e) {
			log.error("expId : " + experimentId + " :- Error while cancelling experiment", e);
			return false;
		}
	}

    /**
     * This method will create and launch a force post processing process of an experiment. This newly created
     * process includes only the output data staging tasks of the experiment.
     *
     * @param experimentId target experiment id
     * @param gatewayId gateway id
     * @return true if the post processing process launched successfully, false otherwise
     * @throws TException if the post processing of the experiment failed
     */
    @Override
    public boolean launchPostProcessingOfExperiment(String experimentId, String gatewayId) throws TException {
        log.info(experimentId, "Launching the post processing of Experiment: {}", experimentId);
        try {
            return validateStatesAndStartPostProcessing(experimentId, gatewayId);
        } catch (Exception e) {
            log.error("expId : " + experimentId + " :- Error while launching post processing", e);
            return false;
        }
    }

    private String getAiravataUserName() {
		return airavataUserName;
	}

	private String getGatewayName() {
		return gatewayName;
	}

	public void setAiravataUserName(String airavataUserName) {
		this.airavataUserName = airavataUserName;
	}

	public void setGatewayName(String gatewayName) {
		this.gatewayName = gatewayName;
	}

	@Override
	public boolean launchProcess(String processId, String airavataCredStoreToken, String gatewayId) throws TException {
		try {
			ProcessModel processModel = (ProcessModel) experimentCatalog.get(
					ExperimentCatalogModelType.PROCESS, processId);
            String applicationId = processModel.getApplicationInterfaceId();
			if (applicationId == null) {
                log.error(processId, "Application interface id shouldn't be null.");
				throw new OrchestratorException("Error executing the job, application interface id shouldn't be null.");
			}
			// set application deployment id to process model
            ApplicationDeploymentDescription applicationDeploymentDescription = getAppDeployment(processModel, applicationId);
            processModel.setApplicationDeploymentId(applicationDeploymentDescription.getAppDeploymentId());
			// set compute resource id to process model, default we set the same in the user preferred compute host id
			processModel.setComputeResourceId(processModel.getProcessResourceSchedule().getResourceHostId());
			experimentCatalog.update(ExperimentCatalogModelType.PROCESS, processModel,processModel.getProcessId());
		    return orchestrator.launchProcess(processModel, airavataCredStoreToken);
		} catch (Exception e) {
            log.error(processId, "Error while launching process ", e);
            throw new TException(e);
        }
	}

    private ApplicationDeploymentDescription getAppDeployment(ProcessModel processModel, String applicationId)
            throws AppCatalogException, OrchestratorException,
            ClassNotFoundException, ApplicationSettingsException,
            InstantiationException, IllegalAccessException {
        String selectedModuleId = getModuleId(appCatalog, applicationId);
        return getAppDeploymentForModule(processModel, selectedModuleId);
    }

    private ApplicationDeploymentDescription getAppDeploymentForModule(ProcessModel processModel, String selectedModuleId)
            throws AppCatalogException, ClassNotFoundException,
            ApplicationSettingsException, InstantiationException,
            IllegalAccessException {
        Map<String, String> moduleIdFilter = new HashMap<String, String>();
        moduleIdFilter.put(AppCatAbstractResource.ApplicationDeploymentConstants.APP_MODULE_ID, selectedModuleId);
        if (processModel.getProcessResourceSchedule() != null && processModel.getProcessResourceSchedule().getResourceHostId() != null) {
            moduleIdFilter.put(AppCatAbstractResource.ApplicationDeploymentConstants.COMPUTE_HOST_ID, processModel.getProcessResourceSchedule().getResourceHostId());
        }
        List<ApplicationDeploymentDescription> applicationDeployements = appCatalog.getApplicationDeployment().getApplicationDeployements(moduleIdFilter);
        Map<ComputeResourceDescription, ApplicationDeploymentDescription> deploymentMap = new HashMap<ComputeResourceDescription, ApplicationDeploymentDescription>();
        ComputeResource computeResource = appCatalog.getComputeResource();
        for (ApplicationDeploymentDescription deploymentDescription : applicationDeployements) {
            deploymentMap.put(computeResource.getComputeResource(deploymentDescription.getComputeHostId()), deploymentDescription);
        }
        List<ComputeResourceDescription> computeHostList = Arrays.asList(deploymentMap.keySet().toArray(new ComputeResourceDescription[]{}));
        Class<? extends HostScheduler> aClass = Class.forName(
                ServerSettings.getHostScheduler()).asSubclass(
		        HostScheduler.class);
        HostScheduler hostScheduler = aClass.newInstance();
        ComputeResourceDescription ComputeResourceDescription = hostScheduler.schedule(computeHostList);
        return deploymentMap.get(ComputeResourceDescription);
    }

	private String getModuleId(AppCatalog appCatalog, String applicationId)
			throws AppCatalogException, OrchestratorException {
		ApplicationInterfaceDescription applicationInterface = appCatalog.getApplicationInterface().getApplicationInterface(applicationId);
		List<String> applicationModules = applicationInterface.getApplicationModules();
		if (applicationModules.size()==0){
			throw new OrchestratorException(
					"No modules defined for application "
							+ applicationId);
		}
//			AiravataAPI airavataAPI = getAiravataAPI();
		String selectedModuleId=applicationModules.get(0);
		return selectedModuleId;
	}

	private boolean validateStatesAndStartPostProcessing(String experimentId, String gatewayId) throws Exception {
        ExperimentStatus experimentStatus = OrchestratorUtils.getExperimentStatus(experimentId);
        switch (experimentStatus.getState()) {
            case CREATED: case COMPLETED: case CANCELED: case CANCELING: case VALIDATED:
                log.warn("Can't run post processing of the {} experiment with id {}", experimentStatus.getState().name(), experimentId);
                return false;
            case EXECUTING:
            case LAUNCHED:
            case SCHEDULED:
            case FAILED:
            case FORCE_POST_PROCESSING_COMPLETED:
            case FORCE_POST_PROCESSING_CANCELED:
            case FORCE_POST_PROCESSING_EXECUTING:
            case FORCE_POST_PROCESSING_FAILED:

                log.debug("State validation succeeded for the post processing of eperiment with id {}", experimentId);

                ExperimentModel experiment = (ExperimentModel) experimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT, experimentId);

                if (experiment == null) {
                    log.error("Error retrieving the Experiment by the given experiment id: {} ", experimentId);
                    return false;
                }

                ProcessModel processModel = createAndSavePostProcessingProcess(experiment, gatewayId);
                String token = getCredentialsStoreToken(experiment, gatewayId);
                OrchestratorServerThreadPoolExecutor.getCachedThreadPool()
                        .execute(MDCUtil.wrapWithMDC(new PostProcessingProcessRunner(processModel.getProcessId(), token, gatewayId)));
                return true;
            default:
                log.warn("Invalid state for experiment with id {}", experimentId);
                return false;
        }
    }

    private boolean validateStatesAndCancel(String experimentId, String gatewayId) throws Exception {
		ExperimentStatus experimentStatus = OrchestratorUtils.getExperimentStatus(experimentId);
		switch (experimentStatus.getState()) {
			case COMPLETED: case CANCELED: case FAILED: case CANCELING:
				log.warn("Can't terminate already {} experiment", experimentStatus.getState().name());
				return false;
			case CREATED:
				log.warn("Experiment termination is only allowed for launched experiments.");
				return false;
			default:
				String expCancelNodePath = ZKPaths.makePath(ZKPaths.makePath(ZkConstants.ZOOKEEPER_EXPERIMENT_NODE,
						experimentId), ZkConstants.ZOOKEEPER_CANCEL_LISTENER_NODE);
				Stat stat = curatorClient.checkExists().forPath(expCancelNodePath);
				if (stat != null) {
					curatorClient.setData().withVersion(-1).forPath(expCancelNodePath, ZkConstants.ZOOKEEPER_CANCEL_REQEUST
							.getBytes());
					ExperimentStatus status = new ExperimentStatus(ExperimentState.CANCELING);
					status.setReason("Experiment cancel request processed");
					status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
					OrchestratorUtils.updageAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
					log.info("expId : " + experimentId + " :- Experiment status updated to " + status.getState());
					return true;
				}
				return false;
		}
    }

    private void launchWorkflowExperiment(String experimentId, String airavataCredStoreToken, String gatewayId) throws TException {
        // FIXME
//        try {
//            WorkflowEnactmentService.getInstance().
//                    submitWorkflow(experimentId, airavataCredStoreToken, getGatewayName(), getRabbitMQProcessPublisher());
//        } catch (Exception e) {
//            log.error("Error while launching workflow", e);
//        }
    }

	private void startCurator() throws ApplicationSettingsException {
		String connectionSting = ServerSettings.getZookeeperConnection();
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5);
		curatorClient = CuratorFrameworkFactory.newClient(connectionSting, retryPolicy);
		curatorClient.start();
	}

	private class PostProcessingProcessRunner implements Runnable {
	    String processId;
	    String gatewayId;
        String airavataCredStoreToken;

        public PostProcessingProcessRunner(String processId, String gatewayId, String airavataCredStoreToken) {
            this.processId = processId;
            this.gatewayId = gatewayId;
            this.airavataCredStoreToken = airavataCredStoreToken;
        }

        @Override
        public void run() {
            try {
                launchProcess(processId, airavataCredStoreToken, gatewayId);
            } catch (TException e) {
                log.error("Error while launching post processing process {}", processId, e);
                // TODO handle states
            }
        }
    }
    private class SingleAppExperimentRunner implements Runnable {

        String experimentId;
        String airavataCredStoreToken;
        String gatewayId;
        public SingleAppExperimentRunner(String experimentId,String airavataCredStoreToken, String gatewayId){
            this.experimentId = experimentId;
            this.airavataCredStoreToken = airavataCredStoreToken;
            this.gatewayId = gatewayId;
        }
        @Override
        public void run() {
            try {
                launchSingleAppExperiment();
            } catch (TException e) {
                log.error("Unable to launch experiment..", e);
            } catch (AiravataException e) {
                log.error("Unable to publish experiment status..", e);
            }
        }

        private boolean launchSingleAppExperiment() throws TException, AiravataException {
            try {
                List<String> processIds = experimentCatalog.getIds(ExperimentCatalogModelType.PROCESS,
						AbstractExpCatResource.ProcessConstants.EXPERIMENT_ID, experimentId);
                for (String processId : processIds) {
                    launchProcess(processId, airavataCredStoreToken, gatewayId);
                }
//				ExperimentStatus status = new ExperimentStatus(ExperimentState.LAUNCHED);
//				status.setReason("submitted all processes");
//				status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
//				OrchestratorUtils.updageAndPublishExperimentStatus(experimentId, status);
//				log.info("expId: {}, Launched experiment ", experimentId);
			} catch (Exception e) {
	            ExperimentStatus status = new ExperimentStatus(ExperimentState.FAILED);
	            status.setReason("Error while updating task status");
	            OrchestratorUtils.updageAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
	            log.error("expId: " + experimentId + ", Error while updating task status, hence updated experiment status to " +
			            ExperimentState.FAILED, e);
                ExperimentStatusChangeEvent event = new ExperimentStatusChangeEvent(ExperimentState.FAILED,
                        experimentId,
                        gatewayId);
                String messageId = AiravataUtils.getId("EXPERIMENT");
                MessageContext messageContext = new MessageContext(event, MessageType.EXPERIMENT, messageId, gatewayId);
                messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
                publisher.publish(messageContext);
	            throw new TException(e);
            }
            return true;
        }

    }

	private class ProcessStatusHandler implements MessageHandler {
		/**
		 * This method only handle MessageType.PROCESS type messages.
		 * @param message
		 */
		@Override
		public void onMessage(MessageContext message) {
			if (message.getType().equals(MessageType.PROCESS)) {
			    // TODO: Get a lock for the experiment id as there couldbe multiple processes
                // including post processing processes updating the status
				try {
					ProcessStatusChangeEvent processStatusChangeEvent = new ProcessStatusChangeEvent();
					TBase event = message.getEvent();
					byte[] bytes = ThriftUtils.serializeThriftObject(event);
					ThriftUtils.createThriftFromBytes(bytes, processStatusChangeEvent);
					ExperimentStatus status = new ExperimentStatus();
					ProcessIdentifier processIdentity = processStatusChangeEvent.getProcessIdentity();
					log.info("expId: {}, processId: {} :- Process status changed event received for status {}",
							processIdentity.getExperimentId(), processIdentity.getProcessId(),
							processStatusChangeEvent.getState().name());

					ProcessModel processModel = null;
					ExperimentModel experimentModel = null;

					try {
                        processModel = (ProcessModel) experimentCatalog.get(ExperimentCatalogModelType.PROCESS, processIdentity.getProcessId());
                    } catch (RegistryException e) {
                        log.error("Failed to find the process with id {}", processIdentity.getProcessId(), e);
                        return;
                    }

                    try {
                        experimentModel = (ExperimentModel) experimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT, processIdentity.getExperimentId());
                    } catch (RegistryException e) {
                        log.error("Failed to find the experiment with id {}", processIdentity.getProcessId(), e);
                        return;
                    }

                    switch (processStatusChangeEvent.getState()) {
//						case CREATED:
//						case VALIDATED:
						case STARTED:
							try {
								ExperimentStatus stat = OrchestratorUtils.getExperimentStatus(processIdentity
										.getExperimentId());
								if (stat.getState() == ExperimentState.CANCELING) {
									status.setState(ExperimentState.CANCELING);
									status.setReason("Process started but experiment cancelling is triggered");
								} else {
								    if (processModel.getProcessType() == ProcessType.PRIMARY) {
                                        status.setState(ExperimentState.EXECUTING);
                                        status.setReason("process started");

                                    } else if (processModel.getProcessType() == ProcessType.FORCE_POST_PROCESING) {
                                        status.setState(ExperimentState.FORCE_POST_PROCESSING_EXECUTING);
                                        status.setReason("force post processing process started");
                                    }
								}
							} catch (RegistryException e) {
								status.setState(ExperimentState.EXECUTING);
								status.setReason("process started");
							}
							break;
//						case PRE_PROCESSING:
//							break;
//						case CONFIGURING_WORKSPACE:
//						case INPUT_DATA_STAGING:
//						case EXECUTING:
//						case MONITORING:
//						case OUTPUT_DATA_STAGING:
//						case POST_PROCESSING:
//						case CANCELLING:
//							break;
						case COMPLETED:
							try {
								ExperimentStatus stat = OrchestratorUtils.getExperimentStatus(processIdentity
										.getExperimentId());
								if (stat.getState() == ExperimentState.CANCELING) {
									status.setState(ExperimentState.CANCELED);
									status.setReason("Process competed but experiment cancelling is triggered");
								} else {

								    if (processModel.getProcessType() == ProcessType.PRIMARY) {
                                        status.setState(ExperimentState.COMPLETED);
                                        status.setReason("process completed");
                                    } else if (processModel.getProcessType() == ProcessType.FORCE_POST_PROCESING) {

                                        if (stat.getState() == ExperimentState.COMPLETED) {
                                            // Ignore silently and mark as completed
                                            status.setState(ExperimentState.COMPLETED);
                                            status.setReason("process completed");
                                        } else {
                                            // otherwise mark post processing is completed
                                            status.setState(ExperimentState.FORCE_POST_PROCESSING_COMPLETED);
                                            status.setReason("force post processing process completed");
                                        }

                                    }
								}
							} catch (RegistryException e) {
							    // TODO dimuthu: is this logic correct?
								status.setState(ExperimentState.COMPLETED);
								status.setReason("process completed");
							}
							break;
						case FAILED:
							try {
								ExperimentStatus stat = OrchestratorUtils.getExperimentStatus(processIdentity
										.getExperimentId());
								if (stat.getState() == ExperimentState.CANCELING) {
									status.setState(ExperimentState.CANCELED);
									status.setReason("Process failed but experiment cancelling is triggered");
								} else {
                                    if (processModel.getProcessType() == ProcessType.PRIMARY) {
                                        status.setState(ExperimentState.FAILED);
                                        status.setReason("process failed");

                                    } else if (processModel.getProcessType() == ProcessType.FORCE_POST_PROCESING) {

                                        if (stat.getState() != ExperimentState.COMPLETED) {
                                            // if experiment completed, silently ignore this
                                            status.setState(ExperimentState.FORCE_POST_PROCESSING_FAILED);
                                            status.setReason("force post processing process failed");
                                        }
                                    }
								}
							} catch (RegistryException e) {
								status.setState(ExperimentState.FAILED);
								status.setReason("process failed");
							}
							break;
						case CANCELED:
							// TODO if experiment have more than one process associated with it, then this should be changed.
                            if (processModel.getProcessType() == ProcessType.PRIMARY) {
                                status.setState(ExperimentState.CANCELED);
                                status.setReason("process cancelled");
                            } else if (processModel.getProcessType() == ProcessType.FORCE_POST_PROCESING) {
                                status.setState(ExperimentState.FORCE_POST_PROCESSING_CANCELED);
                                status.setReason("process cancelled");
                            }
							break;
						default:
							// ignore other status changes, thoes will not affect for experiment status changes
							return;
					}
					if (status.getState() != null) {
						status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
						OrchestratorUtils.updageAndPublishExperimentStatus(processIdentity.getExperimentId(), status, publisher,  processIdentity.getGatewayId());
						log.info("expId : " + processIdentity.getExperimentId() + " :- Experiment status updated to " +
								status.getState());
					}
				} catch (TException e) {
					log.error("Message Id : " + message.getMessageId() + ", Message type : " + message.getType() +
							"Error" + " while prcessing process status change event");
				}
			} else {
				System.out.println("Message Recieved with message id " + message.getMessageId() + " and with message " +
						"type " + message.getType().name());
			}
		}
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
                case POSTPROCESSING_START:
                    startPostProcessingOfExperiment(messageContext);
                    break;
				default:
					experimentSubscriber.sendAck(messageContext.getDeliveryTag());
					log.error("Orchestrator got un-support message type : " + messageContext.getType());
					break;
			}
			MDC.clear();
		}

        private void startPostProcessingOfExperiment(MessageContext messageContext) {
            try {
                byte[] bytes = ThriftUtils.serializeThriftObject(messageContext.getEvent());
                ExperimentSubmitEvent expEvent = new ExperimentSubmitEvent();
                ThriftUtils.createThriftFromBytes(bytes, expEvent);
                log.info("Starting the post processing of the experiment with experimentId: {} gateway Id: {}", expEvent.getExperimentId(), expEvent.getGatewayId());
                launchPostProcessingOfExperiment(expEvent.getExperimentId(), expEvent.getGatewayId());
            } catch (TException e) {
                log.error("Experiment cancellation failed due to Thrift conversion error", e);
            }finally {
                experimentSubscriber.sendAck(messageContext.getDeliveryTag());
            }
        }
		private void cancelExperiment(MessageContext messageContext) {
			try {
				byte[] bytes = ThriftUtils.serializeThriftObject(messageContext.getEvent());
				ExperimentSubmitEvent expEvent = new ExperimentSubmitEvent();
				ThriftUtils.createThriftFromBytes(bytes, expEvent);
				log.info("Cancelling experiment with experimentId: {} gateway Id: {}", expEvent.getExperimentId(), expEvent.getGatewayId());
				terminateExperiment(expEvent.getExperimentId(), expEvent.getGatewayId());
			} catch (TException e) {
				log.error("Experiment cancellation failed due to Thrift conversion error", e);
			}finally {
				experimentSubscriber.sendAck(messageContext.getDeliveryTag());
			}

		}
	}

	private void launchExperiment(MessageContext messageContext) {
		ExperimentSubmitEvent expEvent = new ExperimentSubmitEvent();
		try {
			byte[] bytes = ThriftUtils.serializeThriftObject(messageContext.getEvent());
			ThriftUtils.createThriftFromBytes(bytes, expEvent);
			MDC.put(MDCConstants.EXPERIMENT_ID, expEvent.getExperimentId());
			log.info("Launching experiment with experimentId: {} gateway Id: {}", expEvent.getExperimentId(), expEvent.getGatewayId());
			if (messageContext.isRedeliver()) {
				ExperimentModel experimentModel = (ExperimentModel) experimentCatalog.
						get(ExperimentCatalogModelType.EXPERIMENT, expEvent.getExperimentId());
				MDC.put(MDCConstants.EXPERIMENT_NAME, experimentModel.getExperimentName());
				if (experimentModel.getExperimentStatus().get(0).getState() == ExperimentState.CREATED) {
					launchExperiment(expEvent.getExperimentId(), expEvent.getGatewayId());
				}
            } else {
                launchExperiment(expEvent.getExperimentId(), expEvent.getGatewayId());
            }
		} catch (TException e) {
			String logMessage =  expEvent.getExperimentId() != null && expEvent.getGatewayId() != null ?
					String.format("Experiment launch failed due to Thrift conversion error, experimentId: %s, gatewayId: %s",
					expEvent.getExperimentId(), expEvent.getGatewayId()): "Experiment launch failed due to Thrift conversion error";
            log.error(logMessage,  e);
		} catch (RegistryException e) {
			String logMessage =  expEvent.getExperimentId() != null && expEvent.getGatewayId() != null ?
					String.format("Experiment launch failed due to registry access issue, experimentId: %s, gatewayId: %s",
					expEvent.getExperimentId(), expEvent.getGatewayId()): "Experiment launch failed due to registry access issue";
			log.error(logMessage, e);
		}finally {
			experimentSubscriber.sendAck(messageContext.getDeliveryTag());
			MDC.clear();
		}
	}

}
