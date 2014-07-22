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

package org.apache.airavata.registry.api.orchestrator.impl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.airavata.registry.api.orchestrator.OrchestratorData;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class OrchestratorDataImpl implements OrchestratorData{

	private int orchestratorId;
	private String experimentId;
	private String user;
	private String status;
	private String state;
	private String gfacEPR;
	private String applicationName;
	private boolean lazyLoaded=false;

    public OrchestratorDataImpl() {
        this(false);
    }

    public OrchestratorDataImpl(boolean lazyLoaded) {
        this.lazyLoaded = lazyLoaded;
    }
	@Override
	public int getOrchestratorId() {
		return orchestratorId;
	}

	@Override
	public String getExperimentId() {
		return experimentId;
	}

	@Override
	public String getUser() {
		return user;
	}

	@Override
	public String getGFACServiceEPR() {
		return gfacEPR;
	}

	@Override
	public String getState() {
		return state;
	}

	@Override
	public String getStatus() {
		return status.toString();
	}

	@Override
	public void setExperimentId(String experimentId) {
	this.experimentId =  experimentId;	
	}

	@Override
	public void setUser(String user) {
		this.user = user;
		
	}

	@Override
	public void setGFACServiceEPR(String gfacEPR) {
		this.gfacEPR = gfacEPR;
	}

	@Override
	public void setState(String state) {
		this.state = state;
	}

	@Override
	public void setStatus(String status) {
		this.status = status;
	}
	@Override
	public String getApplicationName() {
		return applicationName;
	}
	@Override
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

}
