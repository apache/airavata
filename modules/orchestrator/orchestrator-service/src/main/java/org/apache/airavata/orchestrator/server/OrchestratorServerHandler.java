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
import org.apache.airavata.credential.store.store.CredentialReader;
import org.apache.airavata.gfac.core.GFacUtils;
import org.apache.airavata.gfac.core.scheduler.HostScheduler;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.messaging.core.PublisherFactory;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.error.LaunchValidationException;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.messaging.event.ExperimentStatusChangeEvent;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.status.ExperimentStatus;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.cpi.OrchestratorService;
import org.apache.airavata.orchestrator.cpi.impl.SimpleOrchestratorImpl;
import org.apache.airavata.orchestrator.cpi.orchestrator_cpi_serviceConstants;
import org.apache.airavata.orchestrator.util.OrchestratorServerThreadPoolExecutor;
import org.apache.airavata.registry.core.app.catalog.resources.AppCatAbstractResource;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.core.experiment.catalog.resources.AbstractExpCatResource;
import org.apache.airavata.registry.cpi.*;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrchestratorServerHandler implements OrchestratorService.Iface {
	private static Logger log = LoggerFactory.getLogger(OrchestratorServerHandler.class);
	private SimpleOrchestratorImpl orchestrator = null;
	private ExperimentCatalog experimentCatalog;
    private AppCatalog appCatalog;
	private static Integer mutex = new Integer(-1);
	private String airavataUserName;
	private String gatewayName;
	private Publisher publisher;

    /**
	 * Query orchestrator server to fetch the CPI version
	 */
	public String getOrchestratorCPIVersion() throws TException {
		return orchestrator_cpi_serviceConstants.ORCHESTRATOR_CPI_VERSION;
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
        } catch (OrchestratorException e) {
            log.error(e.getMessage(), e);
            throw new OrchestratorException("Error while initializing orchestrator service", e);
		} catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new OrchestratorException("Error while initializing orchestrator service", e);
		} catch (AppCatalogException e) {
            log.error(e.getMessage(), e);
            throw new OrchestratorException("Error while initializing orchestrator service", e);
        }
    }

    /**
	 * * After creating the experiment Data user have the * experimentID as the
	 * handler to the experiment, during the launchExperiment * We just have to
	 * give the experimentID * * @param experimentID * @return sucess/failure *
	 * *
	 * 
	 * @param experimentId
	 */
	public boolean launchExperiment(String experimentId, String token) throws TException {
        ExperimentModel experiment = null;
        try {
            experiment = (ExperimentModel) experimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT, experimentId);
            if (experiment == null) {
                log.error(experimentId, "Error retrieving the Experiment by the given experimentID: {} ", experimentId);
                return false;
            }
            CredentialReader credentialReader = GFacUtils.getCredentialReader();
            String gatewayId = null;
            if (credentialReader != null) {
                try {
                    gatewayId = credentialReader.getGatewayID(token);
                } catch (Exception e) {
                    log.error(e.getLocalizedMessage());
                }
            }
            if (gatewayId == null) {
                gatewayId = ServerSettings.getDefaultUserGateway();
                log.info("Couldn't identify the gateway Id using the credential token, Use default gateway Id");
//                throw new AiravataException("Couldn't identify the gateway Id using the credential token");
            }
            ExperimentType executionType = experiment.getExperimentType();
            if (executionType == ExperimentType.SINGLE_APPLICATION) {
                //its an single application execution experiment
                log.debug(experimentId, "Launching single application experiment {}.", experimentId);
                OrchestratorServerThreadPoolExecutor.getCachedThreadPool().execute(new SingleAppExperimentRunner(experimentId, token));
            } else if (executionType == ExperimentType.WORKFLOW) {
                //its a workflow execution experiment
                log.debug(experimentId, "Launching workflow experiment {}.", experimentId);
                launchWorkflowExperiment(experimentId, token);
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
			List<String> ids = experimentCatalog.getIds(ExperimentCatalogModelType.PROCESS,AbstractExpCatResource.ProcessConstants.EXPERIMENT_ID, experimentId);
			if (ids != null && !ids.isEmpty()){
                for (String processId : ids) {
                    ProcessModel processModel = (ProcessModel) experimentCatalog.get(ExperimentCatalogModelType.PROCESS,processId);
                    return orchestrator.validateExperiment(experimentModel,processModel).isSetValidationState();
                }
            }
		} catch (OrchestratorException e) {
            log.error(experimentId, "Error while validating experiment", e);
			throw new TException(e);
		} catch (RegistryException e) {
            log.error(experimentId, "Error while validating experiment", e);
			throw new TException(e);
		}
		return false;
	}

	/**
	 * This can be used to cancel a running experiment and store the status to
	 * terminated in registry
	 * 
	 * @param experimentId
	 * @return
	 * @throws TException
	 */
	public boolean terminateExperiment(String experimentId, String tokenId) throws TException {
        log.info(experimentId, "Experiment: {} is cancelling  !!!!!", experimentId);
        return validateStatesAndCancel(experimentId, tokenId);
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
	public boolean launchProcess(String processId, String airavataCredStoreToken) throws TException {
		try {
			ProcessModel processModel = (ProcessModel) experimentCatalog.get(
					ExperimentCatalogModelType.PROCESS, processId);
            String experimentId = processModel.getExperimentId();
            ExperimentModel experimentModel = (ExperimentModel)experimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT, experimentId);
            String applicationId = processModel.getApplicationInterfaceId();
			if (applicationId == null) {
                log.error(processId, "Application interface id shouldn't be null.");
				throw new OrchestratorException("Error executing the job, application interface id shouldn't be null.");
			}
            ApplicationDeploymentDescription applicationDeploymentDescription = getAppDeployment(processModel, applicationId);
            processModel.setApplicationDeploymentId(applicationDeploymentDescription.getAppDeploymentId());
			experimentCatalog.update(ExperimentCatalogModelType.PROCESS, processModel,processModel.getProcessId());
		    return orchestrator.launchExperiment(experimentModel, processModel, airavataCredStoreToken);
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

    private boolean validateStatesAndCancel(String experimentId, String tokenId)throws TException{
        // FIXME
//        try {
//            Experiment experiment = (Experiment) experimentCatalog.get(
//                    ExperimentCatalogModelType.EXPERIMENT, experimentId);
//			log.info("Waiting for zookeeper to connect to the server");
//			synchronized (mutex){
//				mutex.wait(5000);
//			}
//            if (experiment == null) {
//                log.errorId(experimentId, "Error retrieving the Experiment by the given experimentID: {}.", experimentId);
//                throw new OrchestratorException("Error retrieving the Experiment by the given experimentID: " + experimentId);
//            }
//            ExperimentState experimentState = experiment.getExperimentStatus().getExperimentState();
//            if (isCancelValid(experimentState)){
//                ExperimentStatus status = new ExperimentStatus();
//                status.setExperimentState(ExperimentState.CANCELING);
//                status.setTimeOfStateChange(Calendar.getInstance()
//                        .getTimeInMillis());
//                experiment.setExperimentStatus(status);
//                experimentCatalog.update(ExperimentCatalogModelType.EXPERIMENT, experiment,
//                        experimentId);
//
//                List<String> ids = experimentCatalog.getIds(
//                        ExperimentCatalogModelType.WORKFLOW_NODE_DETAIL,
//                        WorkflowNodeConstants.EXPERIMENT_ID, experimentId);
//                for (String workflowNodeId : ids) {
//                    WorkflowNodeDetails workflowNodeDetail = (WorkflowNodeDetails) experimentCatalog
//                            .get(ExperimentCatalogModelType.WORKFLOW_NODE_DETAIL,
//                                    workflowNodeId);
//                    int value = workflowNodeDetail.getWorkflowNodeStatus().getWorkflowNodeState().getValue();
//                    if ( value> 1 && value < 7) { // we skip the unknown state
//                        log.error(workflowNodeDetail.getNodeName() + " Workflow Node status cannot mark as cancelled, because " +
//                                "current status is " + workflowNodeDetail.getWorkflowNodeStatus().getWorkflowNodeState().toString());
//                        continue; // this continue is very useful not to process deeper loops if the upper layers have non-cancel states
//                    } else {
//                        WorkflowNodeStatus workflowNodeStatus = new WorkflowNodeStatus();
//                        workflowNodeStatus.setWorkflowNodeState(WorkflowNodeState.CANCELING);
//                        workflowNodeStatus.setTimeOfStateChange(Calendar.getInstance()
//                                .getTimeInMillis());
//                        workflowNodeDetail.setWorkflowNodeStatus(workflowNodeStatus);
//                        experimentCatalog.update(ExperimentCatalogModelType.WORKFLOW_NODE_DETAIL, workflowNodeDetail,
//                                workflowNodeId);
//                    }
//                    List<Object> taskDetailList = experimentCatalog.get(
//                            ExperimentCatalogModelType.TASK_DETAIL,
//                            TaskDetailConstants.NODE_ID, workflowNodeId);
//                    for (Object o : taskDetailList) {
//                        TaskDetails taskDetails = (TaskDetails) o;
//                        TaskStatus taskStatus = ((TaskDetails) o).getTaskStatus();
//                        if (taskStatus.getExecutionState().getValue() > 7 && taskStatus.getExecutionState().getValue()<12) {
//                            log.error(((TaskDetails) o).getTaskID() + " Task status cannot mark as cancelled, because " +
//                                    "current task state is " + ((TaskDetails) o).getTaskStatus().getExecutionState().toString());
//                            continue;// this continue is very useful not to process deeper loops if the upper layers have non-cancel states
//                        } else {
//                            taskStatus.setExecutionState(TaskState.CANCELING);
//                            taskStatus.setTimeOfStateChange(Calendar.getInstance()
//                                    .getTimeInMillis());
//                            taskDetails.setTaskStatus(taskStatus);
//                            experimentCatalog.update(ExperimentCatalogModelType.TASK_DETAIL, o,
//                                    taskDetails.getTaskID());
//                        }
//                        orchestrator.cancelExperiment(experiment,
//                                workflowNodeDetail, taskDetails, tokenId);
//                        // Status update should be done at the monitor
//                    }
//                }
//            }else {
//                if (isCancelAllowed(experimentState)){
//                    // when experiment status is < 3 no jobDetails object is created,
//                    // so we don't have to worry, we simply have to change the status and stop the execution
//                    ExperimentStatus status = new ExperimentStatus();
//                    status.setExperimentState(ExperimentState.CANCELED);
//                    status.setTimeOfStateChange(Calendar.getInstance()
//                            .getTimeInMillis());
//                    experiment.setExperimentStatus(status);
//                    experimentCatalog.update(ExperimentCatalogModelType.EXPERIMENT, experiment,
//                            experimentId);
//                    List<String> ids = experimentCatalog.getIds(
//                            ExperimentCatalogModelType.WORKFLOW_NODE_DETAIL,
//                            WorkflowNodeConstants.EXPERIMENT_ID, experimentId);
//                    for (String workflowNodeId : ids) {
//                        WorkflowNodeDetails workflowNodeDetail = (WorkflowNodeDetails) experimentCatalog
//                                .get(ExperimentCatalogModelType.WORKFLOW_NODE_DETAIL,
//                                        workflowNodeId);
//                        WorkflowNodeStatus workflowNodeStatus = new WorkflowNodeStatus();
//                        workflowNodeStatus.setWorkflowNodeState(WorkflowNodeState.CANCELED);
//                        workflowNodeStatus.setTimeOfStateChange(Calendar.getInstance()
//                                .getTimeInMillis());
//                        workflowNodeDetail.setWorkflowNodeStatus(workflowNodeStatus);
//                        experimentCatalog.update(ExperimentCatalogModelType.WORKFLOW_NODE_DETAIL, workflowNodeDetail,
//                                workflowNodeId);
//                        List<Object> taskDetailList = experimentCatalog.get(
//                                ExperimentCatalogModelType.TASK_DETAIL,
//                                TaskDetailConstants.NODE_ID, workflowNodeId);
//                        for (Object o : taskDetailList) {
//                            TaskDetails taskDetails = (TaskDetails) o;
//                            TaskStatus taskStatus = ((TaskDetails) o).getTaskStatus();
//                            taskStatus.setExecutionState(TaskState.CANCELED);
//                            taskStatus.setTimeOfStateChange(Calendar.getInstance()
//                                    .getTimeInMillis());
//                            taskDetails.setTaskStatus(taskStatus);
//                            experimentCatalog.update(ExperimentCatalogModelType.TASK_DETAIL, o,
//                                    taskDetails);
//                        }
//                    }
//                }else {
//                    log.errorId(experimentId, "Unable to mark experiment as Cancelled, current state {} doesn't allow to cancel the experiment {}.",
//                            experiment.getExperimentStatus().getExperimentState().toString(), experimentId);
//                    throw new OrchestratorException("Unable to mark experiment as Cancelled, because current state is: "
//                            + experiment.getExperimentStatus().getExperimentState().toString());
//                }
//            }
//            log.info("Experiment: " + experimentId + " is cancelled !!!!!");
//        } catch (Exception e) {
//            throw new TException(e);
//        }
        return true;
    }

    private void launchWorkflowExperiment(String experimentId, String airavataCredStoreToken) throws TException {
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
        public SingleAppExperimentRunner(String experimentId,String airavataCredStoreToken){
            this.experimentId = experimentId;
            this.airavataCredStoreToken = airavataCredStoreToken;
        }
        @Override
        public void run() {
            try {
                launchSingleAppExperiment();
            } catch (TException e) {
                e.printStackTrace();
            }
        }

        private boolean launchSingleAppExperiment() throws TException {
            try {
                List<String> processIds = experimentCatalog.getIds(ExperimentCatalogModelType.PROCESS, AbstractExpCatResource.ProcessConstants.EXPERIMENT_ID, experimentId);
                for (String processId : processIds) {
                    String gatewayId = null;
                    CredentialReader credentialReader = GFacUtils.getCredentialReader();
                    if (credentialReader != null) {
                        try {
                            gatewayId = credentialReader.getGatewayID(airavataCredStoreToken);
                        } catch (Exception e) {
                            log.error(e.getLocalizedMessage());
                        }
                    }
                    if (gatewayId == null || gatewayId.isEmpty()) {
                        gatewayId = ServerSettings.getDefaultUserGateway();
                    }
                    ExperimentStatusChangeEvent event = new ExperimentStatusChangeEvent(ExperimentState.LAUNCHED,
                            experimentId,
                            gatewayId);
                    String messageId = AiravataUtils.getId("EXPERIMENT");
                    MessageContext messageContext = new MessageContext(event, MessageType.EXPERIMENT, messageId, gatewayId);
                    messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
                    publisher.publish(messageContext);
                    launchProcess(processId, airavataCredStoreToken);
                }

            } catch (Exception e) {
                ExperimentStatus status = new ExperimentStatus();
                status.setState(ExperimentState.FAILED);
                status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                try {
                    experimentCatalog.update(ExperimentCatalogModelType.EXPERIMENT_STATUS, status, experimentId);
                } catch (RegistryException e1) {
                    log.error(experimentId, "Error while updating experiment status to " + status.toString(), e);
                    throw new TException(e);
                }
                log.error(experimentId, "Error while updating task status, hence updated experiment status to " + status.toString(), e);
                throw new TException(e);
            }
            return true;
        }
    }
}
