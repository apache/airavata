/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.orchestrator.impl;

import java.io.IOException;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.orchestrator.Orchestrator;
import org.apache.airavata.orchestrator.OrchestratorConfiguration;
import org.apache.airavata.orchestrator.context.OrchestratorContext;
import org.apache.airavata.orchestrator.exception.OrchestratorException;
import org.apache.airavata.orchestrator.utils.OrchestratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractOrchestrator implements Orchestrator {
    private static final Logger logger = LoggerFactory.getLogger(AbstractOrchestrator.class);
    protected OrchestratorContext orchestratorContext;
    protected OrchestratorConfiguration orchestratorConfiguration;
    protected AiravataServerProperties properties;

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

    public AbstractOrchestrator() throws OrchestratorException {
        // Properties will be injected by Spring for subclasses that are components
    }

    public void initialize(AiravataServerProperties props) throws OrchestratorException {
        this.properties = props;
        try {
            /* Initializing the OrchestratorConfiguration object */
            if (properties != null) {
                orchestratorConfiguration = OrchestratorUtils.loadOrchestratorConfiguration(properties);
                setGatewayProperties();
            } else {
                // Fallback for non-Spring usage
                orchestratorConfiguration = new OrchestratorConfiguration();
                orchestratorConfiguration.setEnableValidation(true);
            }
            orchestratorContext = new OrchestratorContext();
            orchestratorContext.setOrchestratorConfiguration(orchestratorConfiguration);
        } catch (IOException e) {
            logger.error("Failed to initializing Orchestrator - Error parsing configuration files");
            OrchestratorException orchestratorException = new OrchestratorException(e);
            throw orchestratorException;
        }
    }

    protected void setGatewayProperties() {
        if (properties != null) {
            var defaultRegistry = properties.services.default_;
            setAiravataUserName(defaultRegistry.user);
            setGatewayName(defaultRegistry.gateway);
        }
    }

    public OrchestratorContext getOrchestratorContext() {
        return orchestratorContext;
    }

    public OrchestratorConfiguration getOrchestratorConfiguration() {
        return orchestratorConfiguration;
    }
}
