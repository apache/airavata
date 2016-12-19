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
package org.apache.airavata.gfac.core.cluster;

import org.apache.airavata.gfac.core.JobManagerConfiguration;
import org.apache.airavata.gfac.core.authentication.AuthenticationInfo;

import java.util.Map;

public abstract class AbstractRemoteCluster implements RemoteCluster {

	protected final OutputParser outputParser;
	protected final AuthenticationInfo authenticationInfo;
	protected final ServerInfo serverInfo;
	protected final JobManagerConfiguration jobManagerConfiguration;

	public AbstractRemoteCluster(ServerInfo serverInfo,
								 JobManagerConfiguration jobManagerConfiguration,
								 AuthenticationInfo authenticationInfo) {

		this.serverInfo = serverInfo;
		this.jobManagerConfiguration = jobManagerConfiguration;
		this.authenticationInfo = authenticationInfo;
		if (jobManagerConfiguration != null) {
			this.outputParser = jobManagerConfiguration.getParser();
		}else {
			this.outputParser = null;
		}
	}
}
