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

import org.apache.airavata.gfac.core.task.Task;
import org.apache.airavata.gfac.core.task.TaskException;

import java.util.Map;

public abstract class AbstractSCPTask implements Task {
	protected static final int DEFAULT_SSH_PORT = 22;
	protected String password;
	protected String publicKeyPath;
	protected String passPhrase;
	protected String privateKeyPath;
	protected String userName;
	protected String hostName;
	protected String inputPath;

	@Override
	public void init(Map<String, String> propertyMap) throws TaskException {
		password = propertyMap.get("password");
		passPhrase = propertyMap.get("passPhrase");
		privateKeyPath = propertyMap.get("privateKeyPath");
		publicKeyPath = propertyMap.get("publicKeyPath");
		userName = propertyMap.get("userName");
		hostName = propertyMap.get("hostName");
		inputPath = propertyMap.get("inputPath");
	}

}
