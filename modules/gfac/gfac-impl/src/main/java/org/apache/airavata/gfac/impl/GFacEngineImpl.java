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
import org.apache.airavata.gfac.core.GFac;
import org.apache.airavata.gfac.core.GFacEngine;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.GFacUtils;
import org.apache.airavata.gfac.core.context.ProcessContext;
import org.apache.airavata.gfac.core.context.TaskContext;
import org.apache.airavata.gfac.core.monitor.JobMonitor;
import org.apache.airavata.gfac.core.task.JobSubmissionTask;
import org.apache.airavata.gfac.core.task.Task;
import org.apache.airavata.gfac.core.task.TaskException;
import org.apache.airavata.gfac.impl.task.SSHEnvironmentSetupTask;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.computeresource.LOCALSubmission;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManager;
import org.apache.airavata.model.appcatalog.computeresource.SSHJobSubmission;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.model.status.JobStatus;
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
            ApplicationInterfaceDescription applicationInterface = appCatalog.getApplicationInterface()
                    .getApplicationInterface(processModel.getApplicationInterfaceId());
            processContext.setApplicationInterfaceDescription(applicationInterface);
            List<OutputDataObjectType> applicationOutputs = applicationInterface.getApplicationOutputs();
            if (applicationOutputs != null && !applicationOutputs.isEmpty()){
                for (OutputDataObjectType outputDataObjectType : applicationOutputs){
                    if (outputDataObjectType.getType().equals(DataType.STDOUT)){
                        if (outputDataObjectType.getValue() == null || outputDataObjectType.getValue().equals("")){
                            outputDataObjectType.setValue(applicationInterface.getApplicationName()+ ".stdout");
                            processContext.setStdoutLocation(applicationInterface.getApplicationName()+ ".stdout");
                        }else {
                            processContext.setStdoutLocation(outputDataObjectType.getValue());
                        }
                    }
                    if (outputDataObjectType.getType().equals(DataType.STDERR)){
                        if (outputDataObjectType.getValue() == null || outputDataObjectType.getValue().equals("")){
                            String stderrLocation = applicationInterface.getApplicationName() + ".stderr";
                            outputDataObjectType.setValue(stderrLocation);
                            processContext.setStderrLocation(stderrLocation);
                        }else {
                            processContext.setStderrLocation(outputDataObjectType.getValue());
                        }
                    }
                }
            }
            expCatalog.update(ExperimentCatalogModelType.PROCESS, processModel, processId);
            processModel.setProcessOutputs(applicationOutputs);
            processContext.setResourceJobManager(getResourceJobManager(processContext));
			processContext.setRemoteCluster(Factory.getRemoteCluster(processContext));

			String inputPath = ServerSettings.getLocalDataLocation();
			if (inputPath != null) {
				processContext.setLocalWorkingDir((inputPath.endsWith("/") ? inputPath : inputPath + "/") +
						processContext.getProcessId());
			}

			List<Object> jobModels = expCatalog.get(ExperimentCatalogModelType.JOB, "processId", processId);
			if (jobModels != null && !jobModels.isEmpty()) {
				if (jobModels.size() > 1) {
					log.warn("Process has more than one job model, take first one");
				}
				processContext.setJobModel(((JobModel) jobModels.get(0)));
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
		if (processContext.isInterrupted()) {
			GFacUtils.handleProcessInterrupt(processContext);
			return;
		}
//		List<TaskContext> taskChain = new ArrayList<>();
		if (configureWorkspace(processContext, false)) return;

		// exit if process is handed over to another instance while input staging
		if (inputDataStaging(processContext, false)) return;

		// exit if process is handed orver to another instance while job submission.
		if (executeJobSubmission(processContext)) return;
//		processContext.setTaskChain(taskChain);
		if (processContext.isInterrupted()) {
			GFacUtils.handleProcessInterrupt(processContext);
			return;
		}
	}

	private boolean executeJobSubmission(ProcessContext processContext) throws GFacException {
		if (processContext.isInterrupted()) {
			GFacUtils.handleProcessInterrupt(processContext);
			return true;
		}
		TaskContext taskCtx;
		TaskStatus taskStatus;
		processContext.setProcessStatus(new ProcessStatus(ProcessState.EXECUTING));
		JobSubmissionTask jobSubmissionTask = Factory.getJobSubmissionTask(processContext.getJobSubmissionProtocol());
		if (processContext.isInterrupted()) {
			GFacUtils.handleProcessInterrupt(processContext);
			return true;
		}
		GFacUtils.saveAndPublishProcessStatus(processContext);
		taskCtx = getJobSubmissionTaskContext(processContext);
		saveTaskModel(taskCtx);
		GFacUtils.saveAndPublishTaskStatus(taskCtx);
		taskStatus = executeTask(taskCtx, jobSubmissionTask, false);
		if (taskStatus.getState() == TaskState.FAILED) {
            log.error("expId: {}, processId: {}, taskId: {} type: {},:- Job submission task failed, " +
                    "reason:" + " {}", taskCtx.getParentProcessContext().getExperimentId(), taskCtx
                    .getParentProcessContext().getProcessId(), taskCtx.getTaskId(), jobSubmissionTask.getType
                    ().name(), taskStatus.getReason());
            String errorMsg = "expId: {}, processId: {}, taskId: {} type: {},:- Job submission task failed, " +
                    "reason:" + " {}" + processContext.getExperimentId() + processContext.getProcessId() + taskCtx.getTaskId() + jobSubmissionTask.getType().name() + taskStatus.getReason();
            ErrorModel errorModel = new ErrorModel();
            errorModel.setUserFriendlyMessage("Job submission task failed");
            errorModel.setActualErrorMessage(errorMsg);
            GFacUtils.saveTaskError(taskCtx, errorModel);
            ProcessStatus processStatus = processContext.getProcessStatus();
            processStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            processStatus.setReason(errorMsg);
            processStatus.setState(ProcessState.FAILED);
            processContext.setProcessStatus(processStatus);
            GFacUtils.saveAndPublishProcessStatus(processContext);
			throw new GFacException("Job submission task failed");
		}
		if (processContext.isInterrupted()) {
			GFacUtils.handleProcessInterrupt(processContext);
			return true;
		}
		return false;
	}

	private boolean inputDataStaging(ProcessContext processContext, boolean recover) throws GFacException {
		if (processContext.isInterrupted()) {
			GFacUtils.handleProcessInterrupt(processContext);
			return true;
		}
		TaskContext taskCtx;
		TaskStatus taskStatus;// execute process inputs
		processContext.setProcessStatus(new ProcessStatus(ProcessState.INPUT_DATA_STAGING));
		GFacUtils.saveAndPublishProcessStatus(processContext);
		List<InputDataObjectType> processInputs = processContext.getProcessModel().getProcessInputs();
		sortByInputOrder(processInputs);
		if (processInputs != null) {
			for (InputDataObjectType processInput : processInputs) {
				if (processContext.isInterrupted()) {
					GFacUtils.handleProcessInterrupt(processContext);
					return true;
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
						taskStatus = executeTask(taskCtx, dMoveTask, false);
						if (taskStatus.getState() == TaskState.FAILED) {
							log.error("expId: {}, processId: {}, taskId: {} type: {},:- Input statging failed, " +
                                    "reason:" + " {}", taskCtx.getParentProcessContext().getExperimentId(), taskCtx
                                    .getParentProcessContext().getProcessId(), taskCtx.getTaskId(), dMoveTask.getType
                                    ().name(), taskStatus.getReason());
                            String errorMsg = "expId: {}, processId: {}, taskId: {} type: {},:- Input staging failed, " +
                                    "reason:" + " {}" + processContext.getExperimentId() + processContext.getProcessId() + taskCtx.getTaskId() + dMoveTask.getType().name() + taskStatus.getReason();
                            ErrorModel errorModel = new ErrorModel();
                            errorModel.setUserFriendlyMessage("Error while staging input data");
                            errorModel.setActualErrorMessage(errorMsg);
                            GFacUtils.saveTaskError(taskCtx, errorModel);
                            ProcessStatus processStatus = processContext.getProcessStatus();
                            processStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                            processStatus.setReason(errorMsg);
                            processStatus.setState(ProcessState.FAILED);
                            processContext.setProcessStatus(processStatus);
                            GFacUtils.saveAndPublishProcessStatus(processContext);
							throw new GFacException("Error while staging input data");
						}
						break;
					default:
						// nothing to do
						break;
				}
			}
		}
		if (processContext.isInterrupted()) {
			GFacUtils.handleProcessInterrupt(processContext);
			return true;
		}
		return false;
	}

	private boolean configureWorkspace(ProcessContext processContext, boolean recover) throws GFacException {
		if (processContext.isInterrupted()) {
			GFacUtils.handleProcessInterrupt(processContext);
			return true;
		}
		TaskContext taskCtx;
		processContext.setProcessStatus(new ProcessStatus(ProcessState.CONFIGURING_WORKSPACE));
		GFacUtils.saveAndPublishProcessStatus(processContext);
		// Run all environment setup tasks
		taskCtx = getEnvSetupTaskContext(processContext);
		saveTaskModel(taskCtx);
		GFacUtils.saveAndPublishTaskStatus(taskCtx);
		SSHEnvironmentSetupTask envSetupTask = new SSHEnvironmentSetupTask();
		TaskStatus taskStatus = executeTask(taskCtx, envSetupTask, recover);
		if (taskStatus.getState() == TaskState.FAILED) {
			log.error("expId: {}, processId: {}, taskId: {} type: {},:- Input staging failed, " +
					"reason:" + " {}", taskCtx.getParentProcessContext().getExperimentId(), taskCtx
					.getParentProcessContext().getProcessId(), taskCtx.getTaskId(), envSetupTask.getType
					().name(), taskStatus.getReason());
            String errorMsg = "expId: {}, processId: {}, taskId: {} type: {},:- Input staging failed, " +
                    "reason:" + " {}" + processContext.getExperimentId() + processContext.getProcessId() + taskCtx.getTaskId() + envSetupTask.getType().name() + taskStatus.getReason();
            ErrorModel errorModel = new ErrorModel();
            errorModel.setUserFriendlyMessage("Error while environment setup");
            errorModel.setActualErrorMessage(errorMsg);
            GFacUtils.saveTaskError(taskCtx, errorModel);
            ProcessStatus processStatus = processContext.getProcessStatus();
            processStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            processStatus.setReason(errorMsg);
            processContext.setProcessStatus(processStatus);
            GFacUtils.saveAndPublishProcessStatus(processContext);
			throw new GFacException("Error while environment setup");
		}
		if (processContext.isInterrupted()) {
			GFacUtils.handleProcessInterrupt(processContext);
			return true;
		}
		return false;
	}


	@Override
	public void recoverProcess(ProcessContext processContext) throws GFacException {
		ProcessState state = processContext.getProcessStatus().getState();
		switch (state) {
			case CREATED:
			case VALIDATED:
			case STARTED:
				executeProcess(processContext);
				break;
			case PRE_PROCESSING:
			case CONFIGURING_WORKSPACE:
				if (configureWorkspace(processContext, true)) return;
				if (inputDataStaging(processContext, false))  return;
				if (executeJobSubmission(processContext))  return;
				break;
			case INPUT_DATA_STAGING:
				if (inputDataStaging(processContext, true))  return;
				if (executeJobSubmission(processContext))  return;
				break;
			case EXECUTING:
				if (executeJobSubmission(processContext))  return;
				break;
			default:
				throw new GFacException("Invalid process recovery invocation");
		}
	}

	@Override
	public void runProcessOutflow(ProcessContext processContext) throws GFacException {
		if (processContext.isInterrupted()) {
			GFacUtils.handleProcessInterrupt(processContext);
			return;
		}
		// exit if process is handed over to another instance while output staging.
		if (outputDataStaging(processContext, false)) return;

		if (processContext.isInterrupted()) {
			GFacUtils.handleProcessInterrupt(processContext);
			return;
		}

		postProcessing(processContext,false);

		if (processContext.isInterrupted()) {
			GFacUtils.handleProcessInterrupt(processContext);
		}
	}

	/**
	 *
	 * @param processContext
	 * @param recovery
	 * @return <code>true</code> if you need to interrupt processing <code>false</code> otherwise.
	 * @throws GFacException
	 */
	private boolean postProcessing(ProcessContext processContext, boolean recovery) throws GFacException {
		processContext.setProcessStatus(new ProcessStatus(ProcessState.POST_PROCESSING));
		GFacUtils.saveAndPublishProcessStatus(processContext);
//		taskCtx = getEnvCleanupTaskContext(processContext);
		if (processContext.isInterrupted()) {
			GFacUtils.handleProcessInterrupt(processContext);
			return true;
		}
		return false;
	}

	/**
	 *
	 * @param processContext
	 * @param recovery
	 * @return <code>true</code> if process execution interrupted , <code>false</code> otherwise.
	 * @throws GFacException
	 */
	private boolean outputDataStaging(ProcessContext processContext, boolean recovery) throws GFacException {
		TaskContext taskCtx;
		processContext.setProcessStatus(new ProcessStatus(ProcessState.OUTPUT_DATA_STAGING));
		GFacUtils.saveAndPublishProcessStatus(processContext);
        File localWorkingdir = new File(processContext.getLocalWorkingDir());
        localWorkingdir.mkdirs(); // make local dir if not exist
        List<OutputDataObjectType> processOutputs = processContext.getProcessModel().getProcessOutputs();
		for (OutputDataObjectType processOutput : processOutputs) {
			if (processContext.isInterrupted()) {
				GFacUtils.handleProcessInterrupt(processContext);
				return true;
			}
			DataType type = processOutput.getType();
			switch (type) {
				case URI: case STDERR: case STDOUT:
					try {
						taskCtx = getDataStagingTaskContext(processContext, processOutput);
					} catch (TException e) {
						throw new GFacException("Thrift model to byte[] conversion issue", e);
					}
					saveTaskModel(taskCtx);
					GFacUtils.saveAndPublishTaskStatus(taskCtx);
					Task dMoveTask = Factory.getDataMovementTask(processContext.getDataMovementProtocol());
					TaskStatus taskStatus = executeTask(taskCtx, dMoveTask, recovery);
					if (taskStatus.getState() == TaskState.FAILED) {
						log.error("expId: {}, processId: {}, taskId: {} type: {},:- output staging failed, " +
								"reason:" + " {}", taskCtx.getParentProcessContext().getExperimentId(), taskCtx
								.getParentProcessContext().getProcessId(), taskCtx.getTaskId(), dMoveTask.getType
								().name(), taskStatus.getReason());

                        String errorMsg = "expId: {}, processId: {}, taskId: {} type: {},:- output staging failed, " +
                                "reason:" + " {}" + processContext.getExperimentId() + processContext.getProcessId() + taskCtx.getTaskId() + dMoveTask.getType().name() + taskStatus.getReason();
                        ErrorModel errorModel = new ErrorModel();
                        errorModel.setUserFriendlyMessage("Error while staging output data");
                        errorModel.setActualErrorMessage(errorMsg);
                        GFacUtils.saveTaskError(taskCtx, errorModel);
                        ProcessStatus processStatus = processContext.getProcessStatus();
                        processStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                        processStatus.setReason(errorMsg);
                        processStatus.setState(ProcessState.FAILED);
                        processContext.setProcessStatus(processStatus);
                        GFacUtils.saveAndPublishProcessStatus(processContext);
						throw new GFacException("Error while staging output data");
					}
					break;
				default:
					// nothing to do
					break;
			}
		}
		return false;
	}

	@Override
	public void recoverProcessOutflow(ProcessContext processContext) throws GFacException {
		ProcessState processState = processContext.getProcessStatus().getState();
		switch (processState) {
			case OUTPUT_DATA_STAGING:
				if (outputDataStaging(processContext, true)) return;
				if (postProcessing(processContext, false)) return;
			case POST_PROCESSING:
				postProcessing(processContext, true);
				break;
		}
		runProcessOutflow(processContext); // TODO implement recover steps
	}

	@Override
	public void cancelProcess(ProcessContext processContext) throws GFacException {
		if (processContext.getProcessState() == ProcessState.MONITORING) {
			// get job submission task and invoke cancel
			JobSubmissionTask jobSubmissionTask = Factory.getJobSubmissionTask(processContext.getJobSubmissionProtocol());
			TaskContext taskCtx = getJobSubmissionTaskContext(processContext);
			executeCancel(taskCtx, jobSubmissionTask);
		}
	}

	private TaskStatus executeTask(TaskContext taskCtx, Task task, boolean recover) throws GFacException {
		taskCtx.setTaskStatus(new TaskStatus(TaskState.EXECUTING));
		GFacUtils.saveAndPublishTaskStatus(taskCtx);
		TaskStatus taskStatus = null;
		if (recover) {
			taskStatus = task.recover(taskCtx);
		} else {
			taskStatus = task.execute(taskCtx);
		}
		taskCtx.setTaskStatus(taskStatus);
		GFacUtils.saveAndPublishTaskStatus(taskCtx);
		return taskCtx.getTaskStatus();
	}

	private void executeCancel(TaskContext taskContext, JobSubmissionTask jSTask) throws GFacException {
		try {
			JobStatus oldJobStatus = jSTask.cancel(taskContext);

			if (oldJobStatus != null && oldJobStatus.getJobState() == JobState.QUEUED) {
				JobMonitor monitorService = Factory.getMonitorService(taskContext.getParentProcessContext().getMonitorMode());
				monitorService.stopMonitor(taskContext.getParentProcessContext().getJobModel().getJobId(), true);
				JobStatus newJobStatus = new JobStatus(JobState.CANCELED);
				newJobStatus.setReason("Job cancelled");
				taskContext.getParentProcessContext().getJobModel().setJobStatus(newJobStatus);
				GFacUtils.saveJobStatus(taskContext.getParentProcessContext(), taskContext.getParentProcessContext()
						.getJobModel());
			}
		} catch (TaskException e) {
			throw new GFacException("Error while cancelling job");
		} catch (AiravataException e) {
			throw new GFacException("Error wile getting monitoring service");
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
		submodel.setDestination(processContext.getDataMovementProtocol().name() + ":" + processContext.getWorkingDir());
		taskModel.setSubTaskModel(ThriftUtils.serializeThriftObject(submodel));
		taskCtx.setTaskModel(taskModel);
        taskCtx.setProcessInput(processInput);
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
        taskCtx.setProcessOutput(processOutput);
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

	public static ResourceJobManager getResourceJobManager(ProcessContext processCtx) throws AppCatalogException, GFacException {
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
	        throw new GFacException("Job Submission interface cannot be empty at this point");
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
		} else if (jsInterface.getJobSubmissionProtocol() == JobSubmissionProtocol.SSH_FORK) {
			SSHJobSubmission sshJobSubmission = Factory.getDefaultAppCatalog().getComputeResource().getSSHJobSubmission
					(jsInterface.getJobSubmissionInterfaceId());
			processCtx.setMonitorMode(sshJobSubmission.getMonitorMode()); // fixme - Move this to populate process
			resourceJobManager = sshJobSubmission.getResourceJobManager();
		} else {
			throw new GFacException("Unsupported JobSubmissionProtocol - " + jsInterface.getJobSubmissionProtocol()
					.name());
		}

		if (resourceJobManager == null) {
			throw new GFacException("Resource Job Manager is empty.");
		}
		return resourceJobManager;
	}
}
