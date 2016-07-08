/*
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
 *
 */

package org.apache.airavata.orchestrator.server;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.common.utils.ZkConstants;
import org.apache.airavata.gfac.core.GFacUtils;
import org.apache.airavata.gfac.core.scheduler.HostScheduler;
import org.apache.airavata.messaging.core.*;
import org.apache.airavata.messaging.core.impl.RabbitMQStatusConsumer;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.data.replica.DataProductModel;
import org.apache.airavata.model.data.replica.DataReplicaLocationModel;
import org.apache.airavata.model.data.replica.ReplicaLocationCategory;
import org.apache.airavata.model.error.LaunchValidationException;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.messaging.event.ExperimentStatusChangeEvent;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.messaging.event.ProcessIdentifier;
import org.apache.airavata.model.messaging.event.ProcessStatusChangeEvent;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.status.ExperimentStatus;
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
	private RabbitMQStatusConsumer statusConsumer;
	private CuratorFramework curatorClient;

    /**
	 * Query orchestrator server to fetch the CPI version
	 */
	public String getOrchestratorCPIVersion() throws TException {
		return orchestrator_cpiConstants.ORCHESTRATOR_CPI_VERSION;
	}

	public OrchestratorServerHandler() throws OrchestratorException{
		try {
	        publisher = PublisherFactory.createActivityPublisher();
            setAiravataUserName(ServerSettings.getDefaultUser());
		} catch (AiravataException e) {
            log.error(e.getMessage(), e);
            throw new OrchestratorException("Error while initializing orchestrator service", e);
		}
		// orchestrator init
		try {
			// first constructing the monitorManager and orchestrator, then fill
			// the required properties
			orchestrator = new SimpleOrchestratorImpl();
			experimentCatalog = RegistryFactory.getDefaultExpCatalog();
			appCatalog = RegistryFactory.getAppCatalog();
			orchestrator.initialize();
			orchestrator.getOrchestratorContext().setPublisher(this.publisher);
			String brokerUrl = ServerSettings.getSetting(MessagingConstants.RABBITMQ_BROKER_URL);
			String exchangeName = ServerSettings.getSetting(MessagingConstants.RABBITMQ_STATUS_EXCHANGE_NAME);
			statusConsumer = new RabbitMQStatusConsumer(brokerUrl, exchangeName);
			statusConsumer.listen(new ProcessStatusHandler());
			startCurator();
		} catch (OrchestratorException | RegistryException | AppCatalogException | AiravataException e) {
			log.error(e.getMessage(), e);
			throw new OrchestratorException("Error while initializing orchestrator service", e);
		}
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
                log.error(experimentId, "Error retrieving the Experiment by the given experimentID: {} ", experimentId);
                return false;
            }

            ComputeResourcePreference computeResourcePreference = appCatalog.getGatewayProfile().
					getComputeResourcePreference(gatewayId,
							experiment.getUserConfigurationData().getComputationalResourceScheduling().getResourceHostId());
            String token = computeResourcePreference.getResourceSpecificCredentialStoreToken();
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
					//FIXME Resolving replica if available. This is a very crude way of resolving input replicas. A full featured
					//FIXME replica resolving logic should come here
					ReplicaCatalog replicaCatalog = RegistryFactory.getReplicaCatalog();
					processModel.getProcessInputs().stream().forEach(pi -> {
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
                OrchestratorServerThreadPoolExecutor.getCachedThreadPool().execute(new SingleAppExperimentRunner(experimentId, token, gatewayId));
            } else if (executionType == ExperimentType.WORKFLOW) {
                //its a workflow execution experiment
                log.debug(experimentId, "Launching workflow experiment {}.", experimentId);
                launchWorkflowExperiment(experimentId, token, gatewayId);
            } else {
                log.error(experimentId, "Couldn't identify experiment type, experiment {} is neither single application nor workflow.", experimentId);
                throw new TException("Experiment '" + experimentId + "' launch failed. Unable to figureout execution type for application " + experiment.getExecutionId());
            }
        } catch (Exception e) {
            throw new TException("Experiment '" + experimentId + "' launch failed. Unable to figureout execution type for application " + experiment.getExecutionId(), e);
        }
        return true;
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
            ExperimentModel experimentModel = (ExperimentModel)experimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT, experimentId);
            for (ProcessModel processModel : processes){
                boolean state = orchestrator.validateProcess(experimentModel, processModel).isSetValidationState();
                if (!state){
                    return false;
                }
            }
            return true;
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
			processModel.setComputeResourceId(processModel.getResourceSchedule().getResourceHostId());
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
        if (processModel.getResourceSchedule() != null && processModel.getResourceSchedule().getResourceHostId() != null) {
            moduleIdFilter.put(AppCatAbstractResource.ApplicationDeploymentConstants.COMPUTE_HOST_ID, processModel.getResourceSchedule().getResourceHostId());
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

		@Override
		public Map<String, Object> getProperties() {
			Map<String, Object> props = new HashMap<>();
			List<String> routingKeys = new ArrayList<>();
//			routingKeys.add("*"); // listen for gateway level messages
//			routingKeys.add("*.*"); // listen for gateway/experiment level messages
			routingKeys.add("*.*.*"); // listern for gateway/experiment/process level messages
			props.put(MessagingConstants.RABBIT_ROUTING_KEY, routingKeys);
			return props;
		}

		/**
		 * This method only handle MessageType.PROCESS type messages.
		 * @param message
		 */
		@Override
		public void onMessage(MessageContext message) {
			if (message.getType().equals(MessageType.PROCESS)) {
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
									status.setState(ExperimentState.EXECUTING);
									status.setReason("process  started");
								}
							} catch (RegistryException e) {
								status.setState(ExperimentState.EXECUTING);
								status.setReason("process  started");
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
									status.setState(ExperimentState.COMPLETED);
									status.setReason("process  completed");
								}
							} catch (RegistryException e) {
								status.setState(ExperimentState.COMPLETED);
								status.setReason("process  completed");
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
									status.setState(ExperimentState.FAILED);
									status.setReason("process  failed");
								}
							} catch (RegistryException e) {
								status.setState(ExperimentState.FAILED);
								status.setReason("process  failed");
							}
							break;
						case CANCELED:
							// TODO if experiment have more than one process associated with it, then this should be changed.
							status.setState(ExperimentState.CANCELED);
							status.setReason("process  cancelled");
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
}
