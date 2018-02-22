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
package org.apache.airavata.orchestrator.cpi.impl;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.orchestrator.core.OrchestratorConfiguration;
import org.apache.airavata.orchestrator.core.context.OrchestratorContext;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.core.utils.OrchestratorUtils;
import org.apache.airavata.orchestrator.cpi.Orchestrator;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class AbstractOrchestrator implements Orchestrator {
	private final static Logger logger = LoggerFactory.getLogger(AbstractOrchestrator.class);
	protected OrchestratorContext orchestratorContext;
	protected OrchestratorConfiguration orchestratorConfiguration;

    private String registryURL;

    private String gatewayName;

    private String airavataUserName;
   
    public String getRegistryURL() {
		return registryURL;
	}

	public void setRegistryURL(String registryURL) {
		this.registryURL = registryURL;
	}

	public String getGatewayName() {
		return gatewayName;
	}

	public void setGatewayName(String gatewayName) {
		this.gatewayName = gatewayName;
	}
	public String getAiravataUserName() {
		return airavataUserName;
	}

	public void setAiravataUserName(String airavataUserName) {
		this.airavataUserName = airavataUserName;
	}
	public AbstractOrchestrator() throws OrchestratorException, TException {
        try {
            /* Initializing the OrchestratorConfiguration object */
        	orchestratorConfiguration = OrchestratorUtils.loadOrchestratorConfiguration();
            setGatewayProperties();
            orchestratorContext = new OrchestratorContext();
            orchestratorContext.setOrchestratorConfiguration(orchestratorConfiguration);
        }  catch (IOException e) {
            logger.error("Failed to initializing Orchestrator - Error parsing configuration files");
            OrchestratorException orchestratorException = new OrchestratorException(e);
            throw orchestratorException;
		} catch (ApplicationSettingsException e) {
			throw new OrchestratorException(e);
		}
    }
	
    protected void setGatewayProperties() {
        try {
            setAiravataUserName(ServerSettings.getDefaultUser());
            setGatewayName(ServerSettings.getDefaultUserGateway());
        }  catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("Error while setting gateway properties.", e);
        }
    }

    public OrchestratorContext getOrchestratorContext() {
        return orchestratorContext;
    }

    public OrchestratorConfiguration getOrchestratorConfiguration() {
        return orchestratorConfiguration;
    }
}
