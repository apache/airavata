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

package org.apache.airavata.gfac.impl;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.gfac.core.GFacEngine;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.GFacUtils;
import org.apache.airavata.gfac.core.context.ProcessContext;
import org.apache.airavata.gfac.core.context.TaskContext;
import org.apache.airavata.gfac.core.task.JobSubmissionTask;
import org.apache.airavata.gfac.core.task.Task;
import org.apache.airavata.gfac.core.task.TaskException;
import org.apache.airavata.gfac.impl.task.SSHEnvironmentSetupTask;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.computeresource.LOCALSubmission;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManager;
import org.apache.airavata.model.appcatalog.computeresource.SSHJobSubmission;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.model.status.TaskStatus;
import org.apache.airavata.model.task.DataStagingTaskModel;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.airavata.registry.cpi.AppCatalog;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.ExpCatChildDataType;
import org.apache.airavata.registry.cpi.ExperimentCatalog;
import org.apache.airavata.registry.cpi.ExperimentCatalogModelType;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class GFacEngineImpl implements GFacEngine {

	private static final Logger log = LoggerFactory.getLogger(GFacEngineImpl.class);

	public GFacEngineImpl() throws GFacException {

	}

	@Override
	public ProcessContext populateProcessContext(String processId, String gatewayId, String
			tokenId) throws GFacException {
		try {
			ProcessContext processContext = new ProcessContext(processId, gatewayId, tokenId);
			AppCatalog appCatalog = Factory.getDefaultAppCatalog();
			processContext.setAppCatalog(appCatalog);
			ExperimentCatalog expCatalog = Factory.getDefaultExpCatalog();
			processContext.setExperimentCatalog(expCatalog);
			processContext.setCuratorClient(Factory.getCuratorClient());
			processContext.setStatusPublisher(Factory.getStatusPublisher());

			ProcessModel processModel = (ProcessModel) expCatalog.get(ExperimentCatalogModelType.PROCESS, processId);
			processContext.setProcessModel(processModel);
			GatewayResourceProfile gatewayProfile = appCatalog.getGatewayProfile().getGatewayProfile(gatewayId);
			processContext.setGatewayResourceProfile(gatewayProfile);
			processContext.setComputeResourcePreference(appCatalog.getGatewayProfile().getComputeResourcePreference
					(gatewayId, processModel.getComputeResourceId()));
			processContext.setComputeResourceDescription(appCatalog.getComputeResource().getComputeResource
					(processContext.getComputeResourcePreference().getComputeResourceId()));
			processContext.setApplicationDeploymentDescription(appCatalog.getApplicationDeployment()
					.getApplicationDeployement(processModel.getApplicationDeploymentId()));
			processContext.setApplicationInterfaceDescription(appCatalog.getApplicationInterface()
					.getApplicationInterface(processModel.getApplicationInterfaceId()));
			processContext.setResourceJobManager(getResourceJobManager(processContext));
			processContext.setRemoteCluster(Factory.getRemoteCluster(processContext.getJobSubmissionProtocol(),
					processContext.getComputeResourceId(), processContext.getResourceJobManager()));

			String inputPath = ServerSettings.getOutputLocation();
			if (inputPath != null) {
				processContext.setLocalWorkingDir((inputPath.endsWith("/") ? inputPath : inputPath + "/") +
						processContext.getProcessId());
			}
			return processContext;
		} catch (AppCatalogException e) {
			throw new GFacException("App catalog access exception ", e);
		} catch (RegistryException e) {
			throw new GFacException("Registry access exception", e);
		} catch (AiravataException e) {
			throw new GFacException("Remote cluster initialization error", e);
		}
	}

	@Override
	public void executeProcess(ProcessContext processContext) throws GFacException {
		if (processContext.isHandOver()) {
			return;
		}
		TaskContext taskCtx = null;
		List<TaskContext> taskChain = new ArrayList<>();
		processContext.setProcessStatus(new ProcessStatus(ProcessState.CONFIGURING_WORKSPACE));
		GFacUtils.saveAndPublishProcessStatus(processContext);
		// Run all environment setup tasks
		taskCtx = getEnvSetupTaskContext(processContext);
		saveTaskModel(taskCtx);
		GFacUtils.saveAndPublishTaskStatus(taskCtx);
		SSHEnvironmentSetupTask envSetupTask = new SSHEnvironmentSetupTask();
		TaskStatus taskStatus = executeTask(taskCtx, envSetupTask);
		if (taskStatus.getState() == TaskState.FAILED) {
			log.error("expId: {}, processId: {}, taskId: {} type: {},:- Input statging failed, " +
					"reason:" + " {}", taskCtx.getParentProcessContext().getExperimentId(), taskCtx
					.getParentProcessContext().getProcessId(), taskCtx.getTaskId(), envSetupTask.getType
					().name(), taskStatus.getReason());
			throw new GFacException("Error while environment setup");
		}
		if (processContext.isHandOver()) {
			return;
		}
		// execute process inputs
		processContext.setProcessStatus(new ProcessStatus(ProcessState.INPUT_DATA_STAGING));
		GFacUtils.saveAndPublishProcessStatus(processContext);
		List<InputDataObjectType> processInputs = processContext.getProcessModel().getProcessInputs();
		sortByInputOrder(processInputs);
		if (processInputs != null) {
			for (InputDataObjectType processInput : processInputs) {
				if (processContext.isHandOver()) {
					return;
				}
				DataType type = processInput.getType();
				switch (type) {
					case STDERR:
						break;
					case STDOUT:
						break;
					case URI:
						try {
							taskCtx = getDataStagingTaskContext(processContext, processInput);
						} catch (TException e) {
							throw new GFacException("Error while serializing data staging sub task model");
						}
						saveTaskModel(taskCtx);
						GFacUtils.saveAndPublishTaskStatus(taskCtx);
						Task dMoveTask = Factory.getDataMovementTask(processContext.getDataMovementProtocol());
						taskStatus = executeTask(taskCtx, dMoveTask);
						if (taskStatus.getState() == TaskState.FAILED) {
							log.error("expId: {}, processId: {}, taskId: {} type: {},:- Input statging failed, " +
									"reason:" + " {}", taskCtx.getParentProcessContext().getExperimentId(), taskCtx
									.getParentProcessContext().getProcessId(), taskCtx.getTaskId(), dMoveTask.getType
									().name(), taskStatus.getReason());
							throw new GFacException("Error while staging input data");
						}
						break;
					default:
						// nothing to do
						break;
				}
			}
		}
		if (processContext.isHandOver()) {
			return;
		}
		processContext.setProcessStatus(new ProcessStatus(ProcessState.EXECUTING));
		GFacUtils.saveAndPublishProcessStatus(processContext);
		taskCtx = getJobSubmissionTaskContext(processContext);
		saveTaskModel(taskCtx);
		GFacUtils.saveAndPublishTaskStatus(taskCtx);
		JobSubmissionTask jobSubmissionTask = Factory.getJobSubmissionTask(processContext.getJobSubmissionProtocol());
		if (processContext.isHandOver()) {
			return;
		}
		taskStatus = executeTask(taskCtx, jobSubmissionTask);
		if (taskStatus.getState() == TaskState.FAILED) {
			throw new GFacException("Job submission task failed");
		}
		processContext.setTaskChain(taskChain);
		if (processContext.isHandOver()) {
			return;
		}
	}


	@Override
	public void recoverProcess(ProcessContext processContext) throws GFacException {

	}

	@Override
	public void runProcessOutflow(ProcessContext processContext) throws GFacException {
		if (processContext.isHandOver()) {
			return;
		}
		TaskContext taskCtx = null;
		processContext.setProcessStatus(new ProcessStatus(ProcessState.OUTPUT_DATA_STAGING));
		GFacUtils.saveAndPublishProcessStatus(processContext);
		List<OutputDataObjectType> processOutputs = processContext.getProcessModel().getProcessOutputs();
		for (OutputDataObjectType processOutput : processOutputs) {
			if (processContext.isHandOver()) {
				return;
			}
			DataType type = processOutput.getType();
			switch (type) {
				case STDERR:
					break;
				case STDOUT:
					break;
				case URI:
					try {
						taskCtx = getDataStagingTaskContext(processContext, processOutput);
					} catch (TException e) {
						throw new GFacException("Thrift model to byte[] convertion issue", e);
					}
					File localWorkingdir = new File(taskCtx.getLocalWorkingDir());
					localWorkingdir.mkdirs(); // make local dir if not exist
					saveTaskModel(taskCtx);
					GFacUtils.saveAndPublishTaskStatus(taskCtx);
					Task dMoveTask = Factory.getDataMovementTask(processContext.getDataMovementProtocol());
					TaskStatus taskStatus = executeTask(taskCtx, dMoveTask);
					if (taskStatus.getState() == TaskState.FAILED) {
						log.error("expId: {}, processId: {}, taskId: {} type: {},:- Input statging failed, " +
								"reason:" + " {}", taskCtx.getParentProcessContext().getExperimentId(), taskCtx
								.getParentProcessContext().getProcessId(), taskCtx.getTaskId(), dMoveTask.getType
								().name(), taskStatus.getReason());
						throw new GFacException("Error while staging input data");
					}
					break;
				default:
					// nothing to do
					break;
			}
		}
		if (processContext.isHandOver()) {
			return;
		}
		processContext.setProcessStatus(new ProcessStatus(ProcessState.POST_PROCESSING));
		GFacUtils.saveAndPublishProcessStatus(processContext);
//		taskCtx = getEnvCleanupTaskContext(processContext);

	}

	@Override
	public void recoverProcessOutflow(ProcessContext processContext) throws GFacException {

	}

	@Override
	public void cancelProcess() throws GFacException {

	}

	private TaskStatus executeTask(TaskContext taskCtx, Task task) throws GFacException {
		taskCtx.setTaskStatus(new TaskStatus(TaskState.EXECUTING));
		GFacUtils.saveAndPublishTaskStatus(taskCtx);
		TaskStatus taskStatus = task.execute(taskCtx);
		taskCtx.setTaskStatus(taskStatus);
		GFacUtils.saveAndPublishTaskStatus(taskCtx);
		return taskCtx.getTaskStatus();
	}

	private TaskContext getJobSubmissionTaskContext(ProcessContext processContext) throws GFacException {
		TaskContext taskCtx = new TaskContext();
		taskCtx.setParentProcessContext(processContext);

		TaskModel taskModel = new TaskModel();
		taskModel.setParentProcessId(processContext.getProcessId());
		taskModel.setCreationTime(new Date().getTime());
		taskModel.setLastUpdateTime(taskModel.getCreationTime());
		taskModel.setTaskStatus(new TaskStatus(TaskState.CREATED));
		taskModel.setTaskType(TaskTypes.JOB_SUBMISSION);
		taskCtx.setTaskModel(taskModel);
		return taskCtx;
	}

	private TaskContext getDataStagingTaskContext(ProcessContext processContext, InputDataObjectType processInput)
			throws TException {
		TaskContext taskCtx = new TaskContext();
		taskCtx.setParentProcessContext(processContext);
		// create new task model for this task
		TaskModel taskModel = new TaskModel();
		taskModel.setParentProcessId(processContext.getProcessId());
		taskModel.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());
		taskModel.setLastUpdateTime(taskModel.getCreationTime());
		taskModel.setTaskStatus(new TaskStatus(TaskState.CREATED));
		taskModel.setTaskType(TaskTypes.DATA_STAGING);
		// create data staging sub task model
		DataStagingTaskModel submodel = new DataStagingTaskModel();
		submodel.setSource(processInput.getValue());
		submodel.setDestination(processContext.getDataMovementProtocol().name() + ":" + processContext.getWorkingDir
				());
		taskModel.setSubTaskModel(ThriftUtils.serializeThriftObject(submodel));
		taskCtx.setTaskModel(taskModel);
		return taskCtx;
	}

	private TaskContext getDataStagingTaskContext(ProcessContext processContext, OutputDataObjectType processOutput)
			throws TException {
		TaskContext taskCtx = new TaskContext();
		taskCtx.setParentProcessContext(processContext);
		// create new task model for this task
		TaskModel taskModel = new TaskModel();
		taskModel.setParentProcessId(processContext.getProcessId());
		taskModel.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());
		taskModel.setLastUpdateTime(taskModel.getCreationTime());
		taskModel.setTaskStatus(new TaskStatus(TaskState.CREATED));
		taskModel.setTaskType(TaskTypes.DATA_STAGING);
		// create data staging sub task model
		String remoteOutputDir = processContext.getOutputDir();
		remoteOutputDir = remoteOutputDir.endsWith("/") ? remoteOutputDir : remoteOutputDir + "/";
		DataStagingTaskModel submodel = new DataStagingTaskModel();
		submodel.setSource(processContext.getDataMovementProtocol().name() + ":" + remoteOutputDir + processOutput
				.getValue());
		String localWorkingDir = processContext.getLocalWorkingDir();
		submodel.setDestination("file://" + localWorkingDir);
		taskModel.setSubTaskModel(ThriftUtils.serializeThriftObject(submodel));
		taskCtx.setTaskModel(taskModel);
		return taskCtx;
	}

	/**
	 * Persist task model
	 */
	private void saveTaskModel(TaskContext taskContext) throws GFacException {
		try {
			TaskModel taskModel = taskContext.getTaskModel();
			taskContext.getParentProcessContext().getExperimentCatalog().add(ExpCatChildDataType.TASK, taskModel,
					taskModel.getParentProcessId());
		} catch (RegistryException e) {
			throw new GFacException("Error while saving task model", e);
		}
	}

	private TaskContext getEnvSetupTaskContext(ProcessContext processContext) {
		TaskContext taskCtx = new TaskContext();
		taskCtx.setParentProcessContext(processContext);
		TaskModel taskModel = new TaskModel();
		taskModel.setParentProcessId(processContext.getProcessId());
		taskModel.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());
		taskModel.setLastUpdateTime(taskModel.getCreationTime());
		taskModel.setTaskStatus(new TaskStatus(TaskState.CREATED));
		taskModel.setTaskType(TaskTypes.ENV_SETUP);
		taskCtx.setTaskModel(taskModel);
		return taskCtx;
	}


	/**
	 * Sort input data type by input order.
	 */
	private void sortByInputOrder(List<InputDataObjectType> processInputs) {
		Collections.sort(processInputs, new Comparator<InputDataObjectType>() {
			@Override
			public int compare(InputDataObjectType inputDT_1, InputDataObjectType inputDT_2) {
				return inputDT_1.getInputOrder() - inputDT_2.getInputOrder();
			}
		});
	}

	public static ResourceJobManager getResourceJobManager(ProcessContext processCtx) throws AppCatalogException {
		List<JobSubmissionInterface> jobSubmissionInterfaces = Factory.getDefaultAppCatalog().getComputeResource()
				.getComputeResource(processCtx.getComputeResourceId()).getJobSubmissionInterfaces();

		ResourceJobManager resourceJobManager = null;
		JobSubmissionInterface jsInterface = null;
		for (JobSubmissionInterface jobSubmissionInterface : jobSubmissionInterfaces) {
			if (jobSubmissionInterface.getJobSubmissionProtocol() == processCtx.getJobSubmissionProtocol()) {
				jsInterface = jobSubmissionInterface;
				break;
			}
		}
		if (jsInterface == null) {
			// TODO: throw an exception.
		} else if (jsInterface.getJobSubmissionProtocol() == JobSubmissionProtocol.SSH) {
			SSHJobSubmission sshJobSubmission = Factory.getDefaultAppCatalog().getComputeResource().getSSHJobSubmission
					(jsInterface.getJobSubmissionInterfaceId());
			processCtx.setMonitorMode(sshJobSubmission.getMonitorMode()); // fixme - Move this to populate process
			// context method.
			resourceJobManager = sshJobSubmission.getResourceJobManager();
		} else if (jsInterface.getJobSubmissionProtocol() == JobSubmissionProtocol.LOCAL) {
			LOCALSubmission localSubmission = Factory.getDefaultAppCatalog().getComputeResource().getLocalJobSubmission
					(jsInterface.getJobSubmissionInterfaceId());
			resourceJobManager = localSubmission.getResourceJobManager();
		} else {
			// TODO : throw an not supported jobsubmission protocol exception. we only support SSH and LOCAL
		}

		if (resourceJobManager == null) {
			// TODO throw an exception
		}
		return resourceJobManager;
	}
}
