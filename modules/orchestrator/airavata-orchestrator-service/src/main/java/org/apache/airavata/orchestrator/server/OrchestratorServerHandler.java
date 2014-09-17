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

import org.airavata.appcatalog.cpi.AppCatalog;
import org.airavata.appcatalog.cpi.AppCatalogException;
import org.airavata.appcatalog.cpi.ComputeResource;
import org.apache.aiaravata.application.catalog.data.impl.AppCatalogFactory;
import org.apache.aiaravata.application.catalog.data.resources.AbstractResource;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataZKUtils;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.gfac.core.scheduler.HostScheduler;
import org.apache.airavata.gfac.core.utils.GFacUtils;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.error.LaunchValidationException;
import org.apache.airavata.model.workspace.experiment.*;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.cpi.OrchestratorService;
import org.apache.airavata.orchestrator.cpi.impl.SimpleOrchestratorImpl;
import org.apache.airavata.orchestrator.cpi.orchestrator_cpi_serviceConstants;
import org.apache.airavata.orchestrator.util.OrchestratorRecoveryHandler;
import org.apache.airavata.persistance.registry.jpa.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.RegistryModelType;
import org.apache.airavata.registry.cpi.utils.Constants.FieldConstants.TaskDetailConstants;
import org.apache.airavata.registry.cpi.utils.Constants.FieldConstants.WorkflowNodeConstants;
import org.apache.thrift.TException;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class OrchestratorServerHandler implements OrchestratorService.Iface,
		Watcher {
	private static Logger log = LoggerFactory
			.getLogger(OrchestratorServerHandler.class);

	private SimpleOrchestratorImpl orchestrator = null;

	private Registry registry;

	private ZooKeeper zk;

	private static Integer mutex = new Integer(-1);

	private String airavataUserName;
	private String gatewayName;

	/**
	 * Query orchestrator server to fetch the CPI version
	 */
	public String getOrchestratorCPIVersion() throws TException {

		return orchestrator_cpi_serviceConstants.ORCHESTRATOR_CPI_VERSION;
	}

	public OrchestratorServerHandler() {
		// registering with zk
		try {
			String zkhostPort = AiravataZKUtils.getZKhostPort();
			String airavataServerHostPort = ServerSettings
					.getSetting(Constants.ORCHESTRATOR_SERVER_HOST)
					+ ":"
					+ ServerSettings
							.getSetting(Constants.ORCHESTRATOR_SERVER_PORT);
            setGatewayName(ServerSettings.getSystemUserGateway());
            setAiravataUserName(ServerSettings.getSystemUser());
			try {
				zk = new ZooKeeper(zkhostPort, 6000, this); // no watcher is
															// required, this
															// will only use to
															// store some data
				String OrchServer = ServerSettings
						.getSetting(org.apache.airavata.common.utils.Constants.ZOOKEEPER_ORCHESTRATOR_SERVER_NODE);
				synchronized (mutex) {
					mutex.wait(); // waiting for the syncConnected event
				}
                registerOrchestratorService(airavataServerHostPort, OrchServer);
				// creating a watch in orchestrator to monitor the gfac
				// instances
				zk.getChildren(ServerSettings.getSetting(
						Constants.ZOOKEEPER_GFAC_SERVER_NODE, "/gfac-server"),
						this);
				log.info("Finished starting ZK: " + zk);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (KeeperException e) {
				e.printStackTrace();
			}
		} catch (ApplicationSettingsException e) {
			e.printStackTrace();
		}
		// orchestrator init
		try {
			// first constructing the monitorManager and orchestrator, then fill
			// the required properties
			orchestrator = new SimpleOrchestratorImpl();
			registry = RegistryFactory.getDefaultRegistry();
			orchestrator.initialize();
			orchestrator.getOrchestratorContext().setZk(this.zk);
		} catch (OrchestratorException e) {
			e.printStackTrace();
		} catch (RegistryException e) {
			e.printStackTrace();
		}
	}

    private void registerOrchestratorService(String airavataServerHostPort, String orchServer) throws KeeperException, InterruptedException {
        Stat zkStat = zk.exists(orchServer, false);
        if (zkStat == null) {
            zk.create(orchServer, new byte[0],
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
        String instantNode = orchServer
                + File.separator
                + String.valueOf(new Random()
                        .nextInt(Integer.MAX_VALUE));
        zkStat = zk.exists(instantNode, false);
        if (zkStat == null) {
            zk.create(instantNode, airavataServerHostPort.getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
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
	public boolean launchExperiment(String experimentId) throws TException {
        Experiment experiment = null; // this will inside the bottom catch statement
        try {
            experiment = (Experiment) registry.get(
                    RegistryModelType.EXPERIMENT, experimentId);
            if (experiment == null) {
                log.error("Error retrieving the Experiment by the given experimentID: "
                        + experimentId);
                return false;
            }
            List<String> ids = registry.getIds(
					RegistryModelType.WORKFLOW_NODE_DETAIL,
					WorkflowNodeConstants.EXPERIMENT_ID, experimentId);
            for (String workflowNodeId : ids) {
                WorkflowNodeDetails workflowNodeDetail = (WorkflowNodeDetails) registry
						.get(RegistryModelType.WORKFLOW_NODE_DETAIL,
								workflowNodeId);
                List<Object> taskDetailList = registry.get(
						RegistryModelType.TASK_DETAIL,
						TaskDetailConstants.NODE_ID, workflowNodeId);
                for (Object o : taskDetailList) {
                    TaskDetails taskID = (TaskDetails) o;
                    // iterate through all the generated tasks and performs the
                    // job submisssion+monitoring
					ExperimentStatus status = new ExperimentStatus();
					status.setExperimentState(ExperimentState.LAUNCHED);
					status.setTimeOfStateChange(Calendar.getInstance()
							.getTimeInMillis());
					experiment.setExperimentStatus(status);
					registry.update(RegistryModelType.EXPERIMENT, experiment,
							experimentId);
					// launching the experiment
					orchestrator.launchExperiment(experiment,
							workflowNodeDetail, taskID,null);
				}
			}

		} catch (Exception e) {
			// Here we really do not have to do much because only potential
			// failure can happen
			// is in gfac, if there are errors in gfac, it will handle the
			// experiment/task/job statuses
			// We might get failures in registry access before submitting the
			// jobs to gfac, in that case we
			// leave the status of these as created.
			ExperimentStatus status = new ExperimentStatus();
			status.setExperimentState(ExperimentState.FAILED);
			status.setTimeOfStateChange(Calendar.getInstance()
					.getTimeInMillis());
			experiment.setExperimentStatus(status);
			try {
				registry.update(RegistryModelType.EXPERIMENT, experiment,
						experimentId);
			} catch (RegistryException e1) {
				throw new TException(e);
			}

			throw new TException(e);
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
	public boolean validateExperiment(String experimentId) throws TException,
			LaunchValidationException {
		// TODO: Write the Orchestrator implementaion
		try {
			List<TaskDetails> tasks = orchestrator.createTasks(experimentId);
			if (tasks.size() > 1) {
				log.info("There are multiple tasks for this experiment, So Orchestrator will launch multiple Jobs");
			}
			List<String> ids = registry.getIds(
					RegistryModelType.WORKFLOW_NODE_DETAIL,
					WorkflowNodeConstants.EXPERIMENT_ID, experimentId);
			for (String workflowNodeId : ids) {
				WorkflowNodeDetails workflowNodeDetail = (WorkflowNodeDetails) registry
						.get(RegistryModelType.WORKFLOW_NODE_DETAIL,
								workflowNodeId);
				List<Object> taskDetailList = registry.get(
						RegistryModelType.TASK_DETAIL,
						TaskDetailConstants.NODE_ID, workflowNodeId);
				for (Object o : taskDetailList) {
					TaskDetails taskID = (TaskDetails) o;
					// iterate through all the generated tasks and performs the
					// job submisssion+monitoring
					Experiment experiment = (Experiment) registry.get(
							RegistryModelType.EXPERIMENT, experimentId);
					if (experiment == null) {
						log.error("Error retrieving the Experiment by the given experimentID: "
								+ experimentId);
						return false;
					}
					return orchestrator.validateExperiment(experiment,
							workflowNodeDetail, taskID).isSetValidationState();
				}
			}

		} catch (OrchestratorException e) {
			throw new TException(e);
		} catch (RegistryException e) {
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
	public boolean terminateExperiment(String experimentId) throws TException {
        log.info("Experiment: " + experimentId + " is cancelling  !!!!!");
        return validateStatesAndCancel(experimentId);
	}

	/**
	 * This method gracefully handler gfac node failures
	 */
	synchronized public void process(WatchedEvent watchedEvent) {
		synchronized (mutex) {
			try {
				Event.KeeperState state = watchedEvent.getState();
				switch (state) {
				case SyncConnected:
					mutex.notify();
					break;
                case Expired:case Disconnected:
                        try {
                            zk = new ZooKeeper(AiravataZKUtils.getZKhostPort(), 6000, this);
                            String airavataServerHostPort = ServerSettings
                                    .getSetting(Constants.ORCHESTRATOR_SERVER_HOST)
                                    + ":"
                                    + ServerSettings
                                    .getSetting(Constants.ORCHESTRATOR_SERVER_PORT);
                            String OrchServer = ServerSettings
                                    .getSetting(org.apache.airavata.common.utils.Constants.ZOOKEEPER_ORCHESTRATOR_SERVER_NODE);
                            registerOrchestratorService(airavataServerHostPort, OrchServer);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ApplicationSettingsException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (KeeperException e) {
                            e.printStackTrace();
                        }
                }
				if (watchedEvent.getPath() != null
						&& watchedEvent.getPath().startsWith(
								ServerSettings.getSetting(
										Constants.ZOOKEEPER_GFAC_SERVER_NODE,
										"/gfac-server"))) {
					List<String> children = zk.getChildren(ServerSettings
							.getSetting(Constants.ZOOKEEPER_GFAC_SERVER_NODE,
									"/gfac-server"), true);
					for (String gfacNodes : children) {
						zk.exists(
								ServerSettings.getSetting(
										Constants.ZOOKEEPER_GFAC_SERVER_NODE,
										"/gfac-server")
										+ File.separator + gfacNodes, this);
					}
					switch (watchedEvent.getType()) {
					case NodeCreated:
						mutex.notify();
						break;
					case NodeDeleted:
						// here we have to handle gfac node shutdown case
						if (children.size() == 0) {
							log.error("There are not gfac instances to route failed jobs");
							return;
						}
						// we recover one gfac node at a time
						final WatchedEvent event = watchedEvent;
						final OrchestratorServerHandler handler = this;
						(new Thread() {
							public void run() {
								int retry = 0;
								while (retry < 3) {
									try {
										(new OrchestratorRecoveryHandler(
												handler, event.getPath()))
												.recover();
										break;
									} catch (Exception e) {
										e.printStackTrace();
										log.error("error recovering the jobs for gfac-node: "
												+ event.getPath());
										log.error("Retrying again to recover jobs and retry attempt: "
												+ ++retry);
									}
								}

							}
						}).start();
						break;
					}


				}
			} catch (KeeperException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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
	public boolean launchTask(String taskId, String airavataCredStoreToken) throws TException {
		try {
			TaskDetails taskData = (TaskDetails) registry.get(
					RegistryModelType.TASK_DETAIL, taskId);
			String applicationId = taskData.getApplicationId();
			if (applicationId == null) {
				throw new OrchestratorException(
						"Error executing the job because there is no Application Name in this Experiment:  "
								+ applicationId);
			}
			ApplicationDeploymentDescription applicationDeploymentDescription = getAppDeployment(taskData, applicationId);
            taskData.setApplicationDeploymentId(applicationDeploymentDescription.getAppDeploymentId());
			registry.update(RegistryModelType.TASK_DETAIL, taskData,taskData.getTaskID());
			List<Object> workflowNodeDetailList = registry.get(RegistryModelType.WORKFLOW_NODE_DETAIL,
							org.apache.airavata.registry.cpi.utils.Constants.FieldConstants.WorkflowNodeConstants.TASK_LIST, taskData);
			if (workflowNodeDetailList != null
					&& workflowNodeDetailList.size() > 0) {
				List<Object> experimentList = registry.get(RegistryModelType.EXPERIMENT,
								org.apache.airavata.registry.cpi.utils.Constants.FieldConstants.ExperimentConstants.WORKFLOW_NODE_LIST,
								(WorkflowNodeDetails) workflowNodeDetailList.get(0));
				if (experimentList != null && experimentList.size() > 0) {
					return orchestrator
							.launchExperiment(
									(Experiment) experimentList.get(0),
									(WorkflowNodeDetails) workflowNodeDetailList
											.get(0), taskData,airavataCredStoreToken);
				}
			}
		} catch (Exception e) {
            throw new TException(e);
        }
        return false;
	}

	private ApplicationDeploymentDescription getAppDeployment(
			TaskDetails taskData, String applicationId)
			throws AppCatalogException, OrchestratorException,
			ClassNotFoundException, ApplicationSettingsException,
			InstantiationException, IllegalAccessException {
		AppCatalog appCatalog = AppCatalogFactory.getAppCatalog();
		String selectedModuleId = getModuleId(appCatalog, applicationId);
		ApplicationDeploymentDescription applicationDeploymentDescription = getAppDeployment(
				appCatalog, taskData, selectedModuleId);
		return applicationDeploymentDescription;
	}

	private ApplicationDeploymentDescription getAppDeployment(
			AppCatalog appCatalog, TaskDetails taskData, String selectedModuleId)
			throws AppCatalogException, ClassNotFoundException,
			ApplicationSettingsException, InstantiationException,
			IllegalAccessException {
		Map<String, String> moduleIdFilter = new HashMap<String, String>();
		moduleIdFilter.put(AbstractResource.ApplicationDeploymentConstants.APP_MODULE_ID, selectedModuleId);
		if (taskData.getTaskScheduling()!=null && taskData.getTaskScheduling().getResourceHostId() != null) {
		    moduleIdFilter.put(AbstractResource.ApplicationDeploymentConstants.COMPUTE_HOST_ID, taskData.getTaskScheduling().getResourceHostId());
		}
		List<ApplicationDeploymentDescription> applicationDeployements = appCatalog.getApplicationDeployment().getApplicationDeployements(moduleIdFilter);
		Map<ComputeResourceDescription, ApplicationDeploymentDescription> deploymentMap = new HashMap<ComputeResourceDescription, ApplicationDeploymentDescription>();
		ComputeResource computeResource = appCatalog.getComputeResource();
		for (ApplicationDeploymentDescription deploymentDescription : applicationDeployements) {
			deploymentMap.put(computeResource.getComputeResource(deploymentDescription.getComputeHostId()),deploymentDescription);
		}
		List<ComputeResourceDescription> computeHostList = Arrays.asList(deploymentMap.keySet().toArray(new ComputeResourceDescription[]{}));	
		Class<? extends HostScheduler> aClass = Class.forName(
				ServerSettings.getHostScheduler()).asSubclass(
				HostScheduler.class);
		HostScheduler hostScheduler = aClass.newInstance();
		ComputeResourceDescription ComputeResourceDescription = hostScheduler.schedule(computeHostList);
		ApplicationDeploymentDescription applicationDeploymentDescription = deploymentMap.get(ComputeResourceDescription);
		return applicationDeploymentDescription;
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

    private boolean validateStatesAndCancel(String experimentId)throws TException{
        try {
            Experiment experiment = (Experiment) registry.get(
                    RegistryModelType.EXPERIMENT, experimentId);
            if (experiment == null) {
                log.error("Error retrieving the Experiment by the given experimentID: "
                        + experimentId);
                throw new OrchestratorException("Error retrieving the Experiment by the given experimentID:\n" +
                        experimentId);
            }
            ExperimentState experimentState = experiment.getExperimentStatus().getExperimentState();
            if (experimentState.getValue()> 5 && experimentState.getValue()<10){
                    throw new OrchestratorException("Unable to mark experiment as Cancelled, because current state is: "
                    + experiment.getExperimentStatus().getExperimentState().toString());
            }else if(experimentState.getValue()<3){
                // when experiment status is < 3 no jobDetails object is created,
                // so we don't have to worry, we simply have to change the status and stop the execution
                ExperimentStatus status = new ExperimentStatus();
                status.setExperimentState(ExperimentState.CANCELED);
                status.setTimeOfStateChange(Calendar.getInstance()
                        .getTimeInMillis());
                experiment.setExperimentStatus(status);
                registry.update(RegistryModelType.EXPERIMENT, experiment,
                        experimentId);
                List<String> ids = registry.getIds(
                        RegistryModelType.WORKFLOW_NODE_DETAIL,
                        WorkflowNodeConstants.EXPERIMENT_ID, experimentId);
                for (String workflowNodeId : ids) {
                    WorkflowNodeDetails workflowNodeDetail = (WorkflowNodeDetails) registry
                            .get(RegistryModelType.WORKFLOW_NODE_DETAIL,
                                    workflowNodeId);
                    WorkflowNodeStatus workflowNodeStatus = new WorkflowNodeStatus();
                    workflowNodeStatus.setWorkflowNodeState(WorkflowNodeState.CANCELED);
                    workflowNodeStatus.setTimeOfStateChange(Calendar.getInstance()
                            .getTimeInMillis());
                    workflowNodeDetail.setWorkflowNodeStatus(workflowNodeStatus);
                    registry.update(RegistryModelType.WORKFLOW_NODE_DETAIL, workflowNodeDetail,
                            workflowNodeId);
                    List<Object> taskDetailList = registry.get(
                            RegistryModelType.TASK_DETAIL,
                            TaskDetailConstants.NODE_ID, workflowNodeId);
                    for (Object o : taskDetailList) {
                        TaskDetails taskDetails = (TaskDetails) o;
                        TaskStatus taskStatus = ((TaskDetails) o).getTaskStatus();
                        taskStatus.setExecutionState(TaskState.CANCELED);
                        taskStatus.setTimeOfStateChange(Calendar.getInstance()
                                .getTimeInMillis());
                        taskDetails.setTaskStatus(taskStatus);
                        registry.update(RegistryModelType.TASK_DETAIL, o,
                                taskDetails);
                        GFacUtils.setExperimentCancel(experimentId, taskDetails.getTaskID(), zk);
                    }
                }
            }else {

                ExperimentStatus status = new ExperimentStatus();
                status.setExperimentState(ExperimentState.CANCELING);
                status.setTimeOfStateChange(Calendar.getInstance()
                        .getTimeInMillis());
                experiment.setExperimentStatus(status);
                registry.update(RegistryModelType.EXPERIMENT, experiment,
                        experimentId);

                List<String> ids = registry.getIds(
                        RegistryModelType.WORKFLOW_NODE_DETAIL,
                        WorkflowNodeConstants.EXPERIMENT_ID, experimentId);
                for (String workflowNodeId : ids) {
                    WorkflowNodeDetails workflowNodeDetail = (WorkflowNodeDetails) registry
                            .get(RegistryModelType.WORKFLOW_NODE_DETAIL,
                                    workflowNodeId);
                    int value = workflowNodeDetail.getWorkflowNodeStatus().getWorkflowNodeState().getValue();
                    if ( value> 1 && value < 7) { // we skip the unknown state
                        log.error(workflowNodeDetail.getNodeName() + " Workflow Node status cannot mark as cancelled, because " +
                                "current status is " + workflowNodeDetail.getWorkflowNodeStatus().getWorkflowNodeState().toString());
                        continue; // this continue is very useful not to process deeper loops if the upper layers have non-cancel states
                    } else {
                        WorkflowNodeStatus workflowNodeStatus = new WorkflowNodeStatus();
                        workflowNodeStatus.setWorkflowNodeState(WorkflowNodeState.CANCELING);
                        workflowNodeStatus.setTimeOfStateChange(Calendar.getInstance()
                                .getTimeInMillis());
                        workflowNodeDetail.setWorkflowNodeStatus(workflowNodeStatus);
                        registry.update(RegistryModelType.WORKFLOW_NODE_DETAIL, workflowNodeDetail,
                                workflowNodeId);
                    }
                    List<Object> taskDetailList = registry.get(
                            RegistryModelType.TASK_DETAIL,
                            TaskDetailConstants.NODE_ID, workflowNodeId);
                    for (Object o : taskDetailList) {
                        TaskDetails taskDetails = (TaskDetails) o;
                        TaskStatus taskStatus = ((TaskDetails) o).getTaskStatus();
                        if (taskStatus.getExecutionState().getValue() > 7 && taskStatus.getExecutionState().getValue()<12) {
                            log.error(((TaskDetails) o).getTaskID() + " Task status cannot mark as cancelled, because " +
                                    "current task state is " + ((TaskDetails) o).getTaskStatus().getExecutionState().toString());
                            continue;// this continue is very useful not to process deeper loops if the upper layers have non-cancel states
                        } else {
                            taskStatus.setExecutionState(TaskState.CANCELING);
                            taskStatus.setTimeOfStateChange(Calendar.getInstance()
                                    .getTimeInMillis());
                            taskDetails.setTaskStatus(taskStatus);
                            registry.update(RegistryModelType.TASK_DETAIL, o,
                                    taskDetails.getTaskID());
                            GFacUtils.setExperimentCancel(experimentId, taskDetails.getTaskID(), zk);
                        }
                        // iterate through all the generated tasks and performs the
                        // job submisssion+monitoring
                        // launching the experiment
                        orchestrator.cancelExperiment(experiment,
                                workflowNodeDetail, taskDetails, null);

                        // after performing gfac level cancel operation
                        // mark task cancelled
                        taskStatus.setExecutionState(TaskState.CANCELED);
                        taskStatus.setTimeOfStateChange(Calendar.getInstance()
                                .getTimeInMillis());
                        taskDetails.setTaskStatus(taskStatus);
                        registry.update(RegistryModelType.TASK_DETAIL, o,
                                taskDetails.getTaskID());
                    }
                    // mark workflownode cancelled
                    WorkflowNodeStatus workflowNodeStatus = new WorkflowNodeStatus();
                    workflowNodeStatus.setWorkflowNodeState(WorkflowNodeState.CANCELED);
                    workflowNodeStatus.setTimeOfStateChange(Calendar.getInstance()
                            .getTimeInMillis());
                    workflowNodeDetail.setWorkflowNodeStatus(workflowNodeStatus);
                    registry.update(RegistryModelType.WORKFLOW_NODE_DETAIL, workflowNodeDetail,
                            workflowNodeId);
                }
                // mark experiment cancelled
                status = new ExperimentStatus();
                status.setExperimentState(ExperimentState.CANCELED);
                status.setTimeOfStateChange(Calendar.getInstance()
                        .getTimeInMillis());
                experiment.setExperimentStatus(status);
                registry.update(RegistryModelType.EXPERIMENT, experiment,
                        experimentId);
            }
            log.info("Experiment: " + experimentId + " is cancelled !!!!!");

        } catch (Exception e) {
            throw new TException(e);
        }
        return true;
    }
}
