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
package org.apache.airavata.gfac.impl.task;

import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.SSHApiException;
import org.apache.airavata.gfac.core.cluster.RemoteCluster;
import org.apache.airavata.gfac.core.context.TaskContext;
import org.apache.airavata.gfac.core.task.Task;
import org.apache.airavata.gfac.core.task.TaskException;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.model.status.TaskStatus;
import org.apache.airavata.model.task.TaskTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;

public class EnvironmentSetupTask implements Task {

	private static final Logger log = LoggerFactory.getLogger(EnvironmentSetupTask.class);
	@Override
	public void init(Map<String, String> propertyMap) throws TaskException {

	}

	@Override
	public TaskStatus execute(TaskContext taskContext) {
		TaskStatus status = new TaskStatus(TaskState.COMPLETED);
		try {
			RemoteCluster remoteCluster = taskContext.getParentProcessContext().getJobSubmissionRemoteCluster();
			remoteCluster.makeDirectory(taskContext.getParentProcessContext().getWorkingDir());
			status.setReason("Successfully created environment");
		} catch (GFacException e) {
			String msg = "Error while environment setup";
			log.error(msg, e);
			status.setState(TaskState.FAILED);
			status.setReason(msg);
			ErrorModel errorModel = new ErrorModel();
			errorModel.setActualErrorMessage(e.getMessage());
			errorModel.setUserFriendlyMessage(msg);
			taskContext.getTaskModel().setTaskErrors(Arrays.asList(errorModel));
		}
		return status;
	}

	@Override
	public TaskStatus recover(TaskContext taskContext) {
		return execute(taskContext);
	}

	@Override
	public TaskTypes getType() {
		return TaskTypes.ENV_SETUP;
	}
}
