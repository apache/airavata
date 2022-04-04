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
import org.apache.airavata.messaging.core.*;
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
import org.apache.airavata.model.messaging.event.*;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.status.ExperimentStatus;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.airavata.model.util.ExperimentModelUtil;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.core.schedule.HostScheduler;
import org.apache.airavata.orchestrator.core.utils.OrchestratorConstants;
import org.apache.airavata.orchestrator.cpi.OrchestratorService;
import org.apache.airavata.orchestrator.cpi.impl.SimpleOrchestratorImpl;
import org.apache.airavata.orchestrator.util.OrchestratorServerThreadPoolExecutor;
import org.apache.airavata.orchestrator.util.OrchestratorUtils;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.RegistryService.Client;
import org.apache.airavata.registry.api.client.RegistryServiceClientFactory;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
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

import java.text.MessageFormat;
import java.util.*;

public class OrchestratorServerHandler implements OrchestratorService.Iface {
	private static Logger log = LoggerFactory.getLogger(OrchestratorServerHandler.class);
	private SimpleOrchestratorImpl orchestrator = null;
	private String airavataUserName;
	private String gatewayName;
	private Publisher publisher;
	private final Subscriber statusSubscribe;
	private final Subscriber experimentSubscriber;

	private CuratorFramework curatorClient;

	/**
	 * Query orchestrator server to fetch the CPI version
	 */
	@Override
	public String getAPIVersion() throws TException {
		return null;
	}

	public OrchestratorServerHandler() throws OrchestratorException, TException {
		// orchestrator init
		try {
			// first constructing the monitorManager and orchestrator, then fill
			// the required properties
			setAiravataUserName(ServerSettings.getDefaultUser());
			orchestrator = new SimpleOrchestratorImpl();

			publisher = MessagingFactory.getPublisher(Type.STATUS);
			orchestrator.initialize();
			orchestrator.getOrchestratorContext().setPublisher(this.publisher);
			statusSubscribe = getStatusSubscriber();
			experimentSubscriber  = getExperimentSubscriber();
			startCurator();
		} catch (OrchestratorException | AiravataException e) {
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
		final RegistryService.Client registryClient = getRegistryServiceClient();
        try {
        	// TODO deprecate this approach as we are replacing gfac
			String experimentNodePath = getExperimentNodePath (experimentId);
			ZKPaths.mkdirs(curatorClient.getZookeeperClient().getZooKeeper(), experimentNodePath);
			String experimentCancelNode = ZKPaths.makePath(experimentNodePath, ZkConstants.ZOOKEEPER_CANCEL_LISTENER_NODE);
			ZKPaths.mkdirs(curatorClient.getZookeeperClient().getZooKeeper(), experimentCancelNode);
            experiment = registryClient.getExperiment(experimentId);
            if (experiment == null) {
				throw new Exception("Error retrieving the Experiment by the given experimentID: " + experimentId);
            }

			UserConfigurationDataModel userConfigurationData = experiment.getUserConfigurationData();
			String token = null;
			final String groupResourceProfileId = userConfigurationData.getGroupResourceProfileId();
			if (groupResourceProfileId == null) {
				throw new Exception("Experiment not configured with a Group Resource Profile: " + experimentId);
			}
			GroupComputeResourcePreference groupComputeResourcePreference = registryClient.getGroupComputeResourcePreference(
					userConfigurationData.getComputationalResourceScheduling().getResourceHostId(),
					groupResourceProfileId);
			if (groupComputeResourcePreference.getResourceSpecificCredentialStoreToken() != null) {
				token = groupComputeResourcePreference.getResourceSpecificCredentialStoreToken();
			}
            if (token == null || token.isEmpty()){
                // try with group resource profile level token
				GroupResourceProfile groupResourceProfile = registryClient.getGroupResourceProfile(groupResourceProfileId);
				token = groupResourceProfile.getDefaultCredentialStoreToken();
            }
            // still the token is empty, then we fail the experiment
            if (token == null || token.isEmpty()){
				throw new Exception("You have not configured credential store token at group resource profile or compute resource preference." +
						" Please provide the correct token at group resource profile or compute resource preference.");
            }
            ExperimentType executionType = experiment.getExperimentType();
            if (executionType == ExperimentType.SINGLE_APPLICATION) {
                //its an single application execution experiment
                List<ProcessModel> processes = orchestrator.createProcesses(experimentId, gatewayId);

				for (ProcessModel processModel : processes){
					//FIXME Resolving replica if available. This is a very crude way of resolving input replicas. A full featured
					//FIXME replica resolving logic should come here
					processModel.getProcessInputs().stream().forEach(pi -> {
						if (pi.getType().equals(DataType.URI) && pi.getValue() != null && pi.getValue().startsWith("airavata-dp://")) {
							try {
								DataProductModel dataProductModel = registryClient.getDataProduct(pi.getValue());
								Optional<DataReplicaLocationModel> rpLocation = dataProductModel.getReplicaLocations()
										.stream().filter(rpModel -> rpModel.getReplicaLocationCategory().
												equals(ReplicaLocationCategory.GATEWAY_DATA_STORE)).findFirst();
								if (rpLocation.isPresent()) {
									pi.setValue(rpLocation.get().getFilePath());
									pi.setStorageResourceId(rpLocation.get().getStorageResourceId());
								} else {
									log.error("Could not find a replica for the URI " + pi.getValue());
								}
							} catch (RegistryServiceException e) {
								throw new RuntimeException("Error while launching experiment", e);
                            } catch (TException e) {
								throw new RuntimeException("Error while launching experiment", e);
                            }
                        } else if (pi.getType().equals(DataType.URI_COLLECTION) && pi.getValue() != null && pi.getValue().contains("airavata-dp://")) {
							try {
								String[] uriList = pi.getValue().split(",");
								final ArrayList<String> filePathList = new ArrayList<>();
								for (String uri : uriList) {
									if (uri.startsWith("airavata-dp://")) {
										DataProductModel dataProductModel = registryClient.getDataProduct(uri);
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
							} catch (RegistryServiceException e) {
								throw new RuntimeException("Error while launching experiment", e);
                            } catch (TException e) {
								throw new RuntimeException("Error while launching experiment", e);
                            }
                        }
					});
					String taskDag = orchestrator.createAndSaveTasks(gatewayId, processModel, experiment.getUserConfigurationData().isAiravataAutoSchedule());
					processModel.setTaskDag(taskDag);
					registryClient.updateProcess(processModel, processModel.getProcessId());
				}

				if (!validateProcess(experimentId, processes)) {
					throw new Exception("Validating process fails for given experiment Id : " + experimentId);
				}

				log.debug(experimentId, "Launching single application experiment {}.", experimentId);
                ExperimentStatus status = new ExperimentStatus(ExperimentState.LAUNCHED);
                status.setReason("submitted all processes");
                status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                OrchestratorUtils.updateAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
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
			OrchestratorUtils.updateAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
			throw new TException("Experiment '" + experimentId + "' launch failed. Experiment failed to validate: " + launchValidationException.getErrorMessage(), launchValidationException);
        } catch (Exception e) {
			ExperimentStatus status = new ExperimentStatus(ExperimentState.FAILED);
			status.setReason("Unexpected error occurred: " + e.getMessage());
			status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
			OrchestratorUtils.updateAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
            throw new TException("Experiment '" + experimentId + "' launch failed.", e);
		} finally {
			if (registryClient != null) {
				ThriftUtils.close(registryClient);
			}
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
		final RegistryService.Client registryClient = getRegistryServiceClient();
		try {
            ExperimentModel experimentModel = registryClient.getExperiment(experimentId);
            return orchestrator.validateExperiment(experimentModel).isValidationState();
		} catch (OrchestratorException e) {
            log.error(experimentId, "Error while validating experiment", e);
			throw new TException(e);
		} finally {
			if (registryClient != null) {
				ThriftUtils.close(registryClient);
			}
		}
	}

    @Override
    public boolean validateProcess(String experimentId, List<ProcessModel> processes) throws LaunchValidationException, TException {
		final RegistryService.Client registryClient = getRegistryServiceClient();
        try {
			ExperimentModel experimentModel = registryClient.getExperiment(experimentId);
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
			registryClient.addErrors(OrchestratorConstants.EXPERIMENT_ERROR, details, experimentId);
			throw lve;
        } catch (OrchestratorException e) {
            log.error(experimentId, "Error while validating process", e);
            throw new TException(e);
		} finally {
			if (registryClient != null) {
				ThriftUtils.close(registryClient);
			}
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
		final RegistryService.Client registryClient = getRegistryServiceClient();
        log.info(experimentId, "Experiment: {} is cancelling  !!!!!", experimentId);
		try {
			return validateStatesAndCancel(registryClient, experimentId, gatewayId);
		} catch (Exception e) {
			log.error("expId : " + experimentId + " :- Error while cancelling experiment", e);
			return false;
		} finally {
			if (registryClient != null) {
				ThriftUtils.close(registryClient);
			}
		}
	}

	public void fetchIntermediateOutputs(String experimentId, String gatewayId, List<String> outputNames) throws TException {
		final RegistryService.Client registryClient = getRegistryServiceClient();
		try {
			submitIntermediateOutputsProcess(registryClient, experimentId, gatewayId, outputNames);
		} catch (Exception e) {
			log.error("expId : " + experimentId + " :- Error while fetching intermediate", e);
		} finally {
			if (registryClient != null) {
				ThriftUtils.close(registryClient);
			}
		}
	}

	private void submitIntermediateOutputsProcess(Client registryClient, String experimentId,
												  String gatewayId, List<String> outputNames) throws Exception {

		ExperimentModel experimentModel = registryClient.getExperiment(experimentId);
		ProcessModel processModel = ExperimentModelUtil.cloneProcessFromExperiment(experimentModel);
		processModel.setExperimentDataDir(processModel.getExperimentDataDir() + "/intermediates");

		List<OutputDataObjectType> applicationOutputs = registryClient.getApplicationOutputs(
				experimentModel.getExecutionId()); // This is to get a clean output object set
		List<OutputDataObjectType> requestedOutputs = new ArrayList<>();

		for (OutputDataObjectType output : applicationOutputs) {
			if (outputNames.contains(output.getName())) {
				requestedOutputs.add(output);
			}
		}
		processModel.setProcessOutputs(requestedOutputs);
		String processId = registryClient.addProcess(processModel, experimentId);
		processModel.setProcessId(processId);

		try {
			// Find the process that is responsible for main experiment workflow by
			// looking for the process that has the JOB_SUBMISSION task
			Optional<ProcessModel> jobSubmissionProcess = experimentModel.getProcesses().stream()
					.filter(p -> p.getTasks().stream().anyMatch(t -> t.getTaskType() == TaskTypes.JOB_SUBMISSION))
					.findFirst();
			if (!jobSubmissionProcess.isPresent()) {
				throw new Exception(MessageFormat.format(
						"Could not find job submission process for experiment {0}, unable to fetch intermediate outputs {1}",
						experimentId, outputNames));
			}
			String taskDag = orchestrator.createAndSaveIntermediateOutputFetchingTasks(gatewayId, processModel,
					jobSubmissionProcess.get());
			processModel.setTaskDag(taskDag);

			registryClient.updateProcess(processModel, processModel.getProcessId());

			// Figure out the credential token
			UserConfigurationDataModel userConfigurationData = experimentModel.getUserConfigurationData();
			String token = null;
			final String groupResourceProfileId = userConfigurationData.getGroupResourceProfileId();
			if (groupResourceProfileId == null) {
				throw new Exception("Experiment not configured with a Group Resource Profile: " + experimentId);
			}
			GroupComputeResourcePreference groupComputeResourcePreference = registryClient.getGroupComputeResourcePreference(
					userConfigurationData.getComputationalResourceScheduling().getResourceHostId(),
					groupResourceProfileId);
			if (groupComputeResourcePreference.getResourceSpecificCredentialStoreToken() != null) {
				token = groupComputeResourcePreference.getResourceSpecificCredentialStoreToken();
			}
			if (token == null || token.isEmpty()){
				// try with group resource profile level token
				GroupResourceProfile groupResourceProfile = registryClient.getGroupResourceProfile(groupResourceProfileId);
				token = groupResourceProfile.getDefaultCredentialStoreToken();
			}
			// still the token is empty, then we fail the experiment
			if (token == null || token.isEmpty()){
				throw new Exception("You have not configured credential store token at group resource profile or compute resource preference." +
						" Please provide the correct token at group resource profile or compute resource preference.");
			}
			orchestrator.launchProcess(processModel, token);
		} catch (Exception e) {
			log.error("Failed to launch process for intermediate output fetching", e);

			// Update Process status to FAILED
			ProcessStatus status = new ProcessStatus(ProcessState.FAILED);
			status.setReason("Intermediate output fetching process failed to launch: " + e.getMessage());
			status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
			registryClient.addProcessStatus(status, processId);

			throw e;
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
		final RegistryService.Client registryClient = getRegistryServiceClient();
		try {
            ProcessStatus processStatus = registryClient.getProcessStatus(processId);

            switch (processStatus.getState()) {
                case CREATED: case VALIDATED:
                    ProcessModel processModel = registryClient.getProcess(processId);
                    String applicationId = processModel.getApplicationInterfaceId();
                    if (applicationId == null) {
                        log.error(processId, "Application interface id shouldn't be null.");
                        throw new OrchestratorException("Error executing the job, application interface id shouldn't be null.");
                    }
                    // set application deployment id to process model
                    ApplicationDeploymentDescription applicationDeploymentDescription = getAppDeployment(registryClient, processModel, applicationId);
                    if (applicationDeploymentDescription == null) {
                    	log.error("Could not find an application deployment for " + processModel.getComputeResourceId() + " and application " + applicationId);
						throw new OrchestratorException("Could not find an application deployment for " + processModel.getComputeResourceId() + " and application " + applicationId);
                    }
                    processModel.setApplicationDeploymentId(applicationDeploymentDescription.getAppDeploymentId());
                    // set compute resource id to process model, default we set the same in the user preferred compute host id
                    processModel.setComputeResourceId(processModel.getProcessResourceSchedule().getResourceHostId());
                    registryClient.updateProcess(processModel, processModel.getProcessId());
                    return orchestrator.launchProcess(processModel, airavataCredStoreToken);

                default:
                    log.warn("Process " + processId + " is already launched. So it can not be relaunched");
                    return false;
            }

		} catch (Exception e) {
            log.error(processId, "Error while launching process ", e);
            throw new TException(e);
		} finally {
			if (registryClient != null) {
				ThriftUtils.close(registryClient);
			}
        }
	}

    private ApplicationDeploymentDescription getAppDeployment(RegistryService.Client registryClient, ProcessModel processModel, String applicationId)
            throws OrchestratorException,
            ClassNotFoundException, ApplicationSettingsException,
            InstantiationException, IllegalAccessException, TException {
        String selectedModuleId = getModuleId(registryClient, applicationId);
        return getAppDeploymentForModule(registryClient, processModel, selectedModuleId);
    }

    private ApplicationDeploymentDescription getAppDeploymentForModule(RegistryService.Client registryClient, ProcessModel processModel, String selectedModuleId)
            throws ClassNotFoundException,
            ApplicationSettingsException, InstantiationException,
            IllegalAccessException, TException {

        List<ApplicationDeploymentDescription> applicationDeployements = registryClient.getApplicationDeployments(selectedModuleId);
        Map<ComputeResourceDescription, ApplicationDeploymentDescription> deploymentMap = new HashMap<ComputeResourceDescription, ApplicationDeploymentDescription>();

		for (ApplicationDeploymentDescription deploymentDescription : applicationDeployements) {
			if (processModel.getComputeResourceId().equals(deploymentDescription.getComputeHostId())) {
				deploymentMap.put(registryClient.getComputeResource(deploymentDescription.getComputeHostId()), deploymentDescription);
			}
		}
        List<ComputeResourceDescription> computeHostList = Arrays.asList(deploymentMap.keySet().toArray(new ComputeResourceDescription[]{}));
        Class<? extends HostScheduler> aClass = Class.forName(
                ServerSettings.getHostScheduler()).asSubclass(
		        HostScheduler.class);
        HostScheduler hostScheduler = aClass.newInstance();
        ComputeResourceDescription ComputeResourceDescription = hostScheduler.schedule(computeHostList);
        return deploymentMap.get(ComputeResourceDescription);
    }

	private String getModuleId(RegistryService.Client registryClient, String applicationId)
            throws OrchestratorException, TException {
		ApplicationInterfaceDescription applicationInterface = registryClient.getApplicationInterface(applicationId);
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

    private boolean validateStatesAndCancel(RegistryService.Client registryClient, String experimentId, String gatewayId) throws Exception {
		ExperimentStatus experimentStatus = registryClient.getExperimentStatus(experimentId);
		switch (experimentStatus.getState()) {
			case COMPLETED: case CANCELED: case FAILED: case CANCELING:
				log.warn("Can't terminate already {} experiment", experimentStatus.getState().name());
				return false;
			case CREATED:
				log.warn("Experiment termination is only allowed for launched experiments.");
				return false;
			default:
				ExperimentModel experimentModel = registryClient.getExperiment(experimentId);
				final UserConfigurationDataModel userConfigurationData = experimentModel.getUserConfigurationData();
				final String groupResourceProfileId = userConfigurationData.getGroupResourceProfileId();

				GroupComputeResourcePreference groupComputeResourcePreference = registryClient.getGroupComputeResourcePreference(
						userConfigurationData.getComputationalResourceScheduling().getResourceHostId(),
						groupResourceProfileId);
                String token = groupComputeResourcePreference.getResourceSpecificCredentialStoreToken();
                if (token == null || token.isEmpty()){
                    // try with group resource profile level token
					GroupResourceProfile groupResourceProfile = registryClient.getGroupResourceProfile(groupResourceProfileId);
                    token = groupResourceProfile.getDefaultCredentialStoreToken();
                }
                // still the token is empty, then we fail the experiment
                if (token == null || token.isEmpty()){
                    log.error("You have not configured credential store token at group resource profile or compute resource preference." +
                            " Please provide the correct token at group resource profile or compute resource preference.");
                    return false;
                }

				orchestrator.cancelExperiment(experimentModel, token);
				// TODO deprecate this approach as we are replacing gfac
				String expCancelNodePath = ZKPaths.makePath(ZKPaths.makePath(ZkConstants.ZOOKEEPER_EXPERIMENT_NODE,
						experimentId), ZkConstants.ZOOKEEPER_CANCEL_LISTENER_NODE);
				Stat stat = curatorClient.checkExists().forPath(expCancelNodePath);
				if (stat != null) {
					curatorClient.setData().withVersion(-1).forPath(expCancelNodePath, ZkConstants.ZOOKEEPER_CANCEL_REQEUST
							.getBytes());
					ExperimentStatus status = new ExperimentStatus(ExperimentState.CANCELING);
					status.setReason("Experiment cancel request processed");
					status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
					OrchestratorUtils.updateAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
					log.info("expId : " + experimentId + " :- Experiment status updated to " + status.getState());
				}
				return true;
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
				throw new RuntimeException("Error while launching experiment", e);
            } catch (AiravataException e) {
                log.error("Unable to publish experiment status..", e);
            }
        }

        private boolean launchSingleAppExperiment() throws TException, AiravataException {
			final RegistryService.Client registryClient = getRegistryServiceClient();
            try {
                List<String> processIds = registryClient.getProcessIds(experimentId);
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
	            OrchestratorUtils.updateAndPublishExperimentStatus(experimentId, status, publisher, gatewayId);
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
			} finally {
				if (registryClient != null) {
					ThriftUtils.close(registryClient);
				}
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
					try {
						ProcessModel process = OrchestratorUtils.getProcess(processIdentity.getProcessId());
						boolean isIntermediateOutputFetchingProcess = process.getTasks().stream().anyMatch(t -> t.getTaskType() == TaskTypes.OUTPUT_FETCHING);
						if (isIntermediateOutputFetchingProcess) {
							log.info("Not updating experiment status because process is an intermediate output fetching one");
							return;
						}
					} catch (ApplicationSettingsException e) {
						throw new RuntimeException("Error getting process " + processIdentity.getProcessId(), e);
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
									status.setState(ExperimentState.EXECUTING);
									status.setReason("process  started");
								}
							} catch (ApplicationSettingsException e) {
								throw new RuntimeException("Error ", e);
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
							}  catch (ApplicationSettingsException e) {
								throw new RuntimeException("Error ", e);
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
							} catch (ApplicationSettingsException e) {
								throw new RuntimeException("Unable to create registry client...", e);
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
						OrchestratorUtils.updateAndPublishExperimentStatus(processIdentity.getExperimentId(), status, publisher,  processIdentity.getGatewayId());
						log.info("expId : " + processIdentity.getExperimentId() + " :- Experiment status updated to " +
								status.getState());
					}
				} catch (TException e) {
					log.error("Message Id : " + message.getMessageId() + ", Message type : " + message.getType() +
							"Error" + " while prcessing process status change event");
					throw new RuntimeException("Error while updating experiment status", e);
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
				case INTERMEDIATE_OUTPUTS:
					handleIntermediateOutputsEvent(messageContext);
					break;
				default:
					experimentSubscriber.sendAck(messageContext.getDeliveryTag());
					log.error("Orchestrator got un-support message type : " + messageContext.getType());
					break;
			}
			MDC.clear();
		}

		private void cancelExperiment(MessageContext messageContext) {
			try {
				byte[] bytes = ThriftUtils.serializeThriftObject(messageContext.getEvent());
				ExperimentSubmitEvent expEvent = new ExperimentSubmitEvent();
				ThriftUtils.createThriftFromBytes(bytes, expEvent);
				log.info("Cancelling experiment with experimentId: {} gateway Id: {}", expEvent.getExperimentId(), expEvent.getGatewayId());
				terminateExperiment(expEvent.getExperimentId(), expEvent.getGatewayId());
			} catch (TException e) {
				log.error("Error while cancelling experiment", e);
				throw new RuntimeException("Error while cancelling experiment", e);
			}finally {
				experimentSubscriber.sendAck(messageContext.getDeliveryTag());
			}

		}

		private void handleIntermediateOutputsEvent(MessageContext messageContext) {
			try {
				byte[] bytes = ThriftUtils.serializeThriftObject(messageContext.getEvent());
				ExperimentIntermediateOutputsEvent event =  new ExperimentIntermediateOutputsEvent();
				ThriftUtils.createThriftFromBytes(bytes, event);
				log.info("INTERMEDIATE_OUTPUTS event for experimentId: {} gateway Id: {} outputs: {}", event.getExperimentId(), event.getGatewayId(), event.getOutputNames());
				fetchIntermediateOutputs(event.getExperimentId(), event.getGatewayId(), event.getOutputNames());
			} catch (TException e) {
				log.error("Error while fetching intermediate outputs", e);
				throw new RuntimeException("Error while fetching intermediate outputs", e);
			} finally {
				experimentSubscriber.sendAck(messageContext.getDeliveryTag());
			}

		}
	}

	private void launchExperiment(MessageContext messageContext) {
		ExperimentSubmitEvent expEvent = new ExperimentSubmitEvent();
		final RegistryService.Client registryClient = getRegistryServiceClient();
		try {
			byte[] bytes = ThriftUtils.serializeThriftObject(messageContext.getEvent());
			ThriftUtils.createThriftFromBytes(bytes, expEvent);
			MDC.put(MDCConstants.EXPERIMENT_ID, expEvent.getExperimentId());
			log.info("Launching experiment with experimentId: {} gateway Id: {}", expEvent.getExperimentId(), expEvent.getGatewayId());
			if (messageContext.isRedeliver()) {
				ExperimentModel experimentModel = registryClient.
						getExperiment(expEvent.getExperimentId());
				MDC.put(MDCConstants.EXPERIMENT_NAME, experimentModel.getExperimentName());
				if (experimentModel.getExperimentStatus().get(0).getState() == ExperimentState.CREATED) {
					launchExperiment(expEvent.getExperimentId(), expEvent.getGatewayId());
				}
            } else {
                launchExperiment(expEvent.getExperimentId(), expEvent.getGatewayId());
            }
		} catch (TException e) {
			String logMessage = expEvent.getExperimentId() != null && expEvent.getGatewayId() != null ?
					String.format("Experiment launch failed due to Thrift conversion error, experimentId: %s, gatewayId: %s",
							expEvent.getExperimentId(), expEvent.getGatewayId()) : "Experiment launch failed due to Thrift conversion error";
			log.error(logMessage, e);
		} catch (Exception e) {
			log.error("An unknown issue while launching experiment " + Optional.ofNullable(expEvent.getExperimentId()).orElse("missing experiment") +
					" on gateway " + Optional.ofNullable(expEvent.getGatewayId()).orElse("missing gateway"), e);
		} finally {
			experimentSubscriber.sendAck(messageContext.getDeliveryTag());
			MDC.clear();
			if (registryClient != null) {
				ThriftUtils.close(registryClient);
			}
		}
	}

	private RegistryService.Client getRegistryServiceClient() {
		try {
			final int serverPort = Integer.parseInt(ServerSettings.getRegistryServerPort());
			final String serverHost = ServerSettings.getRegistryServerHost();
			return RegistryServiceClientFactory.createRegistryClient(serverHost, serverPort);
		} catch (RegistryServiceException|ApplicationSettingsException e) {
			throw new RuntimeException("Unable to create registry client...", e);
		}
	}

	private void startCurator() throws ApplicationSettingsException {
		String connectionSting = ServerSettings.getZookeeperConnection();
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5);
		curatorClient = CuratorFrameworkFactory.newClient(connectionSting, retryPolicy);
		curatorClient.start();
	}

	public String getExperimentNodePath(String experimentId) {
		return ZKPaths.makePath(ZkConstants.ZOOKEEPER_EXPERIMENT_NODE, experimentId);
	}
}
