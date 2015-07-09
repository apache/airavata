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

import org.apache.airavata.gfac.core.task.TaskException;
import org.apache.airavata.gfac.core.SSHApiException;
import org.apache.airavata.gfac.core.cluster.RemoteCluster;
import org.apache.airavata.gfac.core.context.TaskContext;
import org.apache.airavata.gfac.core.task.Task;
import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.model.task.TaskTypes;

import java.util.Map;

public class SSHEnvironmentSetupTask implements Task {

	@Override
	public void init(Map<String, String> propertyMap) throws TaskException {

	}

	@Override
	public TaskState execute(TaskContext taskContext) throws TaskException {

		try {
			RemoteCluster remoteCluster = taskContext.getParentProcessContext().getRemoteCluster();
			remoteCluster.makeDirectory(taskContext.getParentProcessContext().getWorkingDir());
		} catch (SSHApiException e) {
			throw new TaskException("Error while environment setup", e);
		}
		return null;
	}

	@Override
	public TaskState recover(TaskContext taskContext) throws TaskException {
		return execute(taskContext);
	}

	@Override
	public TaskTypes getType() {
		return TaskTypes.ENV_SETUP;
	}
}
