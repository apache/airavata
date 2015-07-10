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
			processContext.setRemoteCluster(Factory.getRemoteCluster(processContext));

			//
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
		TaskContext taskCtx = null;
		List<TaskContext> taskChain = new ArrayList<>();
		processContext.setProcessStatus(new ProcessStatus(ProcessState.CONFIGURING_WORKSPACE));
		// Run all environment setup tasks
		taskCtx = getEnvSetupTaskContext(processContext);
		saveTaskModel(taskCtx);
		GFacUtils.saveAndPublishTaskStatus(taskCtx);
		SSHEnvironmentSetupTask envSetupTask = new SSHEnvironmentSetupTask();
		executeTask(taskCtx, envSetupTask);
		// execute process inputs
		processContext.setProcessStatus(new ProcessStatus(ProcessState.INPUT_DATA_STAGING));
		List<InputDataObjectType> processInputs = processContext.getProcessModel().getProcessInputs();
		sortByInputOrder(processInputs);
		if (processInputs != null) {
			for (InputDataObjectType processInput : processInputs) {
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
						GFacUtils.saveAndPublishTaskStatus(taskCtx);
						Task dMoveTask = Factory.getDataMovementTask(processContext.getDataMovementProtocol());
						executeTask(taskCtx, dMoveTask);
						break;
					default:
						// nothing to do
						break;
				}
			}
		}
		processContext.setProcessStatus(new ProcessStatus(ProcessState.EXECUTING));
		taskCtx = getJobSubmissionTaskContext(processContext);
		GFacUtils.saveAndPublishTaskStatus(taskCtx);
		JobSubmissionTask jobSubmissionTask = Factory.getJobSubmissionTask(processContext.getJobSubmissionProtocol());
		executeTask(taskCtx, jobSubmissionTask);
		processContext.setTaskChain(taskChain);
	}

	private void executeTask(TaskContext taskCtx, Task task) throws GFacException {
		try {
			taskCtx.setTaskStatus(new TaskStatus(TaskState.EXECUTING));
			GFacUtils.saveAndPublishTaskStatus(taskCtx);
			task.execute(taskCtx);
			taskCtx.setTaskStatus(new TaskStatus(TaskState.COMPLETED));
			GFacUtils.saveAndPublishTaskStatus(taskCtx);
		} catch (TaskException e) {
			TaskStatus status = new TaskStatus(TaskState.FAILED);
			status.setReason(taskCtx.getTaskType().toString() + " Task Failed to execute");
			taskCtx.setTaskStatus(status);
			GFacUtils.saveAndPublishTaskStatus(taskCtx);
		}

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

	private TaskContext getDataStagingTaskContext(ProcessContext processContext, InputDataObjectType processInput) throws TException {
		TaskContext taskCtx = new TaskContext();
		taskCtx.setParentProcessContext(processContext);
		// create new task model for this task
		TaskModel taskModel = new TaskModel();
		taskModel.setParentProcessId(processContext.getProcessId());
		taskModel.setCreationTime(new Date().getTime());
		taskModel.setLastUpdateTime(taskModel.getCreationTime());
		taskModel.setTaskStatus(new TaskStatus(TaskState.CREATED));
		taskModel.setTaskType(TaskTypes.DATA_STAGING);
		// create data staging sub task model
		DataStagingTaskModel submodel = new DataStagingTaskModel();
		submodel.setSource(processInput.getValue());
		submodel.setDestination(processContext.getWorkingDir());
		taskModel.setSubTaskModel(ThriftUtils.serializeThriftObject(submodel));
		taskCtx.setTaskModel(taskModel);
		return taskCtx;
	}

	/**
	 * Persist task model
	 * @param taskContext
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

	@Override
	public void recoverProcess(ProcessContext processContext) throws GFacException {

	}

	@Override
	public void runProcessOutflow(ProcessContext processContext) throws GFacException {
		TaskContext taskCtx = null;
		TaskModel taskModel = null;
		List<TaskContext> taskChain = new ArrayList<>();
		List<OutputDataObjectType> processOutputs = processContext.getProcessModel().getProcessOutputs();
		for (OutputDataObjectType processOutput : processOutputs) {
			DataType type = processOutput.getType();
			switch (type) {
				case STDERR:
					break;
				case STDOUT:
					break;
				case URI:
					// TODO : Provide data staging data model
					try {
						taskCtx = new TaskContext();
						taskCtx.setParentProcessContext(processContext);

						// create new task model for this task
						taskModel = new TaskModel();
						taskModel.setParentProcessId(processContext.getProcessId());
						taskModel.setTaskStatus(new TaskStatus(TaskState.CREATED));
						taskModel.setTaskType(TaskTypes.DATA_STAGING);
						// create data staging sub task model
						DataStagingTaskModel submodel = new DataStagingTaskModel();
						submodel.setSource(processContext.getWorkingDir() + "/" + processOutput.getValue());
						submodel.setDestination(processOutput.getValue());
						taskModel.setSubTaskModel(ThriftUtils.serializeThriftObject(submodel));
						taskChain.add(taskCtx);
					} catch (TException e) {
						throw new GFacException("Thift model to byte[] convertion issue", e);
					}
					break;
			}
		}

	}

	@Override
	public void recoverProcessOutflow(ProcessContext processContext) throws GFacException {

	}

	@Override
	public void cancelProcess() throws GFacException {

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


}
