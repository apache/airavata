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
package org.apache.airavata.gfac.impl.task;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.gfac.core.SSHApiException;
import org.apache.airavata.gfac.core.context.TaskContext;
import org.apache.airavata.gfac.core.task.TaskException;
import org.apache.airavata.gfac.impl.SSHUtils;
import org.apache.airavata.model.status.TaskStatus;
import org.apache.airavata.model.task.DataStagingTaskModel;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.thrift.TException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class SCPInputDataStageTask extends AbstractSCPTask {

	public SCPInputDataStageTask() {
	}

	@Override
	public TaskStatus execute(TaskContext taskContext) {

/*		if (taskContext.getTaskModel().getTaskType() != TaskTypes.DATA_STAGING) {
			throw new TaskException("Invalid task call, expected " + TaskTypes.DATA_STAGING.toString() + " but found "
					+ taskContext.getTaskModel().getTaskType().toString());
		}
		try {
			DataStagingTaskModel subTaskModel = (DataStagingTaskModel) ThriftUtils.getSubTaskModel(taskContext
					.getTaskModel());
			URI sourceURI = new URI(subTaskModel.getSource());
			URI destinationURI = new URI(subTaskModel.getDestination());

			if (sourceURI.getScheme().equalsIgnoreCase("file")) {  //  local --> Airavata --> RemoteCluster
				taskContext.getParentProcessContext().getRemoteCluster().scpTo(sourceURI.getPath(),
						subTaskModel.getDestination());
			} else { // PGA(client) --> Airavata --> RemoteCluster
				// PGA(client) --> Airavata
				JSch jsch = new JSch();
				jsch.addIdentity(privateKeyPath, passPhrase);
				Session session = jsch.getSession(userName, hostName, DEFAULT_SSH_PORT);
				SSHUtils.scpFrom(sourceURI.getPath(), taskContext.getLocalWorkingDir() , session);

				// Airavata --> RemoteCluster
				taskContext.getParentProcessContext().getRemoteCluster().scpTo(destinationURI.getPath(),
						taskContext.getLocalWorkingDir());
			}
		} catch (SSHApiException e) {
			throw new TaskException("Scp attempt failed", e);
		} catch (JSchException | IOException e) {
			throw new TaskException("Scp failed", e);
		} catch (TException e) {
			throw new TaskException("Invalid task invocation");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}*/
		return null;
	}

	@Override
	public TaskStatus recover(TaskContext taskContext) {
		return null;
	}

	@Override
	public TaskTypes getType() {
		return TaskTypes.DATA_STAGING;
	}


}
