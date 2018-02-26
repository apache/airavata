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
import org.apache.airavata.gfac.core.context.TaskContext;
import org.apache.airavata.gfac.core.task.Task;
import org.apache.airavata.gfac.core.task.TaskException;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.model.status.TaskStatus;
import org.apache.airavata.model.task.DataStagingTaskModel;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;

public class DataStageTask implements Task {
	private static final Logger log = LoggerFactory.getLogger(DataStageTask.class);

	@Override
	public void init(Map<String, String> propertyMap) throws TaskException {

	}

	@Override
	public TaskStatus execute(TaskContext taskContext) {
		TaskStatus status = new TaskStatus(TaskState.COMPLETED);
		if (taskContext.getTaskModel().getTaskType() != TaskTypes.DATA_STAGING) {
			status.setState(TaskState.FAILED);
			status.setReason("Invalid task call, expected " + TaskTypes.DATA_STAGING.toString() + " but found "
					+ taskContext.getTaskModel().getTaskType().toString());
		} else {
			try {
				DataStagingTaskModel subTaskModel = ((DataStagingTaskModel) taskContext.getSubTaskModel());
				URI sourceURI = new URI(subTaskModel.getSource());
				URI destinationURI = new URI(subTaskModel.getDestination());

				ProcessState processState = taskContext.getParentProcessContext().getProcessState();
				if (processState == ProcessState.INPUT_DATA_STAGING) {
					/**
					 * copy local file to compute resource.
					 */
					taskContext.getParentProcessContext().getDataMovementRemoteCluster().copyTo(sourceURI.getPath(), destinationURI
							.getPath());
				} else if (processState == ProcessState.OUTPUT_DATA_STAGING) {
					/**
					 * copy remote file from compute resource.
					 */
					taskContext.getParentProcessContext().getDataMovementRemoteCluster().copyFrom(sourceURI.getPath(), destinationURI
							.getPath());
				}
				status.setReason("Successfully staged data");
			} catch (GFacException e) {
				String msg = "Scp attempt failed";
				log.error(msg, e);
				status.setState(TaskState.FAILED);
				status.setReason(msg);
				ErrorModel errorModel = new ErrorModel();
				errorModel.setActualErrorMessage(e.getMessage());
				errorModel.setUserFriendlyMessage(msg);
				taskContext.getTaskModel().setTaskErrors(Arrays.asList(errorModel));
			} catch (TException e) {
				String msg = "Invalid task invocation";
				log.error(msg, e);
				status.setState(TaskState.FAILED);
				status.setReason(msg);
				ErrorModel errorModel = new ErrorModel();
				errorModel.setActualErrorMessage(e.getMessage());
				errorModel.setUserFriendlyMessage(msg);
				taskContext.getTaskModel().setTaskErrors(Arrays.asList(errorModel));
			} catch (URISyntaxException e) {
				String msg = "source or destination is not a valid URI";
				log.error(msg, e);
				status.setState(TaskState.FAILED);
				status.setReason(msg);
				ErrorModel errorModel = new ErrorModel();
				errorModel.setActualErrorMessage(e.getMessage());
				errorModel.setUserFriendlyMessage(msg);
				taskContext.getTaskModel().setTaskErrors(Arrays.asList(errorModel));
			}
		}
		return status;
	}

	@Override
	public TaskStatus recover(TaskContext taskContext) {
        TaskState state = taskContext.getTaskStatus().getState();
        if (state == TaskState.EXECUTING || state == TaskState.CREATED) {
            return execute(taskContext);
        } else {
            // files already transferred or failed
            return taskContext.getTaskStatus();
        }
	}

	@Override
	public TaskTypes getType() {
		return TaskTypes.DATA_STAGING;
	}
}
