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
import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.model.task.DataStagingTaskModel;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.thrift.TException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class SCPInputDataStageTask extends AbstractSCPTask {

	public SCPInputDataStageTask() {
	}

	@Override
	public TaskState execute(TaskContext taskContext) throws TaskException {

		if (taskContext.getTaskModel().getTaskType() != TaskTypes.DATA_STAGING) {
			throw new TaskException("Invalid task call, expected " + TaskTypes.DATA_STAGING.toString() + " but found "
					+ taskContext.getTaskModel().getTaskType().toString());
		}
		try {
			DataStagingTaskModel subTaskModel = (DataStagingTaskModel) ThriftUtils.getSubTaskModel(taskContext.getTaskModel());
			URL sourceURL = new URL(subTaskModel.getSource());
			URL destinationURL = new URL(subTaskModel.getDestination());

			if (sourceURL.getProtocol().equalsIgnoreCase("file")) {  //  local --> Airavata --> RemoteCluster
				taskContext.getParentProcessContext().getRemoteCluster().scpTo(sourceURL.getPath(),
						subTaskModel.getDestination());
			} else { // PGA(client) --> Airavata --> RemoteCluster
				// PGA(client) --> Airavata
				JSch jsch = new JSch();
				jsch.addIdentity(privateKeyPath, passPhrase);
				Session session = jsch.getSession(userName, hostName, DEFAULT_SSH_PORT);
				SSHUtils.scpFrom(sourceURL.getPath(), inputPath, session);

				// Airavata --> RemoteCluster
				taskContext.getParentProcessContext().getRemoteCluster().scpTo(destinationURL.getPath(), inputPath);
			}
		} catch (MalformedURLException e) {
			throw new TaskException("Wrong source or destination file path.", e);
		} catch (SSHApiException e) {
			throw new TaskException("Scp attempt failed", e);
		} catch (JSchException | IOException e) {
			throw new TaskException("Scp failed", e);
		} catch (TException e) {
			throw new TaskException("Invalid task invocation");
		}
		return null;
	}

	@Override
	public TaskState recover(TaskContext taskContext) throws TaskException {
		return null;
	}


}
