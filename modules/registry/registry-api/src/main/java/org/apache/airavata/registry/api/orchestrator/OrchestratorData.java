/**
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

package org.apache.airavata.registry.api.orchestrator;

import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.apache.airavata.registry.api.orchestrator.impl.OrchestratorDataImpl;

@WebService
@XmlSeeAlso(OrchestratorDataImpl.class)
public interface OrchestratorData {

	/**
	 * Returns the orchestrator run id
	 * 
	 * @return
	 */
	public int getOrchestratorId();

	/**
	 * 
	 * @return the unique experiment id
	 */
	public String getExperimentId();

	/**
	 * Returns the user of the run
	 * 
	 * @return
	 */
	public String getUser();

	/**
	 * Returns application name to execute
	 */
	public String getApplicationName();
	/**
	 * Returns GFAC service URL
	 * 
	 * @return
	 */
	public String getGFACServiceEPR();

	/**
	 * Returns state of processing
	 * 
	 * @return
	 */
	public String getState();

	/**
	 * Returns run status
	 * 
	 * @return
	 */
	public String getStatus();

	/**
	 * 
	 * @param experimentId
	 */
	public void setExperimentId(String experimentId);

	/**
	 * 
	 * @param user
	 */
	public void setUser(String user);

	/**
	 * 
	 * @param gfacEPR
	 */
	public void setGFACServiceEPR(String gfacEPR);
	
	/**
	 * 
	 * @param state
	 */
	public void setState(String state);
	
	/**
	 * 
	 * @param status
	 */
	public void setStatus(String status);

	/**
	 * 
	 * @param applicationName
	 */
	public void setApplicationName(String applicationName);

	
}
