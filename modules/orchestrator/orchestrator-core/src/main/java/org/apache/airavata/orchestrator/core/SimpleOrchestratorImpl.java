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
package org.apache.airavata.orchestrator.core;

import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.registry.api.JobRequest;

public class SimpleOrchestratorImpl extends AbstractOrchestrator{

	@Override
	public boolean initialize() throws OrchestratorException {
		super.initialize();
		return false;
	}

	@Override
	public boolean launchExperiment(JobRequest request) throws OrchestratorException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean cancelExperiment(String experimentID) throws OrchestratorException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void startJobSubmitter() throws OrchestratorException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutdown() throws OrchestratorException {
		// TODO Auto-generated method stub
		
	}

}
