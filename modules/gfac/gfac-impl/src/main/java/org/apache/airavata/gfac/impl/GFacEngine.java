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

import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.config.DataTransferTaskConfig;
import org.apache.airavata.gfac.core.config.GFacYamlConfigruation;
import org.apache.airavata.gfac.core.config.JobSubmitterTaskConfig;
import org.apache.airavata.gfac.core.config.ResourceConfig;
import org.apache.airavata.gfac.core.context.ProcessContext;
import org.apache.airavata.gfac.core.task.Task;
import org.apache.airavata.model.appcatalog.computeresource.DataMovementProtocol;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManagerType;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.task.TaskModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GFacEngine {
	private static GFacEngine engine;
	Map<JobSubmissionProtocol, Task> jobSubmissionTask;
	Map<DataMovementProtocol, Task> dataMovementTask;
	Map<ResourceJobManagerType, ResourceConfig> resources;


	private GFacEngine() throws GFacException {
		GFacYamlConfigruation config = new GFacYamlConfigruation();
		for (JobSubmitterTaskConfig jobSubmitterTaskConfig : config.getJobSbumitters()) {
			jobSubmissionTask.put(jobSubmitterTaskConfig.getSubmissionProtocol(), null);
		}

		for (DataTransferTaskConfig dataTransferTaskConfig : config.getFileTransferTasks()) {
			dataMovementTask.put(dataTransferTaskConfig.getTransferProtocol(), null);
		}

		for (ResourceConfig resourceConfig : config.getResourceConfiguration()) {
			resources.put(resourceConfig.getJobManagerType(), resourceConfig);
		}
	}

	public static GFacEngine getInstance() throws GFacException {
		if (engine == null) {
			synchronized (GFacEngine.class) {
				if (engine == null) {
					engine = new GFacEngine();
				}
			}
		}
		return engine;
	}

	public ProcessContext populateProcessContext(ProcessContext processContext) {
		processContext.setProcessModel(new ProcessModel()); // TODO: get rocess model from app catalog
		// TODO: set datamovement protocol and jobsubmission protocol
		//TODO: set up gatewayResourceProfile.
		return processContext;
	}

	public void createTaskChain(ProcessContext processContext) throws GFacException {
		List<InputDataObjectType> processInputs = processContext.getProcessModel().getProcessInputs();
		List<TaskModel> taskChain = new ArrayList<>();
		if (processInputs != null) {
			for (InputDataObjectType processInput : processInputs) {
				DataType type = processInput.getType();
				switch (type) {
					case STDERR:
						//
						break;
					case STDOUT:
						//
						break;
					case URI:
						// add URI type Task
						break;
					default:
						// nothing to do
						break;
				}
			}
		}
	}

	public void executeProcess(ProcessContext processContext) throws GFacException {


	}

	public void recoverProcess(ProcessContext processContext) throws GFacException {

	}

	public void runProcessOutflow(ProcessContext processContext) throws GFacException {

	}

	public void recoverProcessOutflow(ProcessContext processContext) throws GFacException {

	}

	public void cancelProcess() throws GFacException {

	}
}
