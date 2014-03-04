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
package org.apache.airavata.orchestrator.cpi.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.apache.airavata.client.AiravataAPIFactory;
import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.common.exception.AiravataConfigurationException;
import org.apache.airavata.orchestrator.core.OrchestratorConfiguration;
import org.apache.airavata.orchestrator.core.context.OrchestratorContext;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.core.gfac.GFACInstance;
import org.apache.airavata.orchestrator.core.model.ExperimentRequest;
import org.apache.airavata.orchestrator.core.utils.OrchestratorConstants;
import org.apache.airavata.orchestrator.core.utils.OrchestratorUtils;
import org.apache.airavata.orchestrator.cpi.Orchestrator;
import org.apache.airavata.persistance.registry.jpa.impl.RegistryImpl;
import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.api.AiravataRegistryFactory;
import org.apache.airavata.registry.api.AiravataUser;
import org.apache.airavata.registry.api.Gateway;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractOrchestrator implements Orchestrator {
	private final static Logger logger = LoggerFactory.getLogger(AbstractOrchestrator.class);
	protected AiravataRegistry2 airavataRegistry;
	protected AiravataAPI airavataAPI;
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
	public AbstractOrchestrator() throws OrchestratorException {
        try {
            /* Initializing the OrchestratorConfiguration object */
        	orchestratorConfiguration = OrchestratorUtils.loadOrchestratorConfiguration();
            setGatewayProperties();
            /* initializing the Orchestratorcontext object */
            airavataRegistry = AiravataRegistryFactory.getRegistry(new Gateway(getGatewayName()), new AiravataUser(getAiravataUserName()));
            // todo move this code to gfac service mode Jobsubmitter,
            // todo this is ugly, SHOULD fix these isEmbedded mode code from Orchestrator
            if (!orchestratorConfiguration.isEmbeddedMode()) {
                Map<String, Integer> gfacNodeList = airavataRegistry.getGFACNodeList();
                if (gfacNodeList.size() == 0) {
                    String error = "No GFAC instances available in the system, Can't initialize Orchestrator";
                    logger.error(error);
                    throw new OrchestratorException(error);
                }
                Set<String> uriList = gfacNodeList.keySet();
                Iterator<String> iterator = uriList.iterator();
                // todo consume these data to
                List<GFACInstance> gfacInstanceList = new ArrayList<GFACInstance>();
                while (iterator.hasNext()) {
                    String uri = iterator.next();
                    Integer integer = gfacNodeList.get(uri);
                    gfacInstanceList.add(new GFACInstance(uri, integer));
                }
            }
            orchestratorContext = new OrchestratorContext();
            orchestratorContext.setOrchestratorConfiguration(orchestratorConfiguration);
            orchestratorConfiguration.setAiravataAPI(getAiravataAPI());
            orchestratorContext.setRegistry(airavataRegistry);

            /* initializing registry cpi */
            orchestratorContext.setNewRegistry(new RegistryImpl());
        } catch (RegistryException e) {
            logger.error("Failed to initializing Orchestrator");
            OrchestratorException orchestratorException = new OrchestratorException(e);
            throw orchestratorException;
        } catch (AiravataConfigurationException e) {
            logger.error("Failed to initializing Orchestrator");
            OrchestratorException orchestratorException = new OrchestratorException(e);
            throw orchestratorException;
        } catch (IOException e) {
            logger.error("Failed to initializing Orchestrator - Error parsing configuration files");
            OrchestratorException orchestratorException = new OrchestratorException(e);
            throw orchestratorException;
        }
    }
	
	//get the registry URL and the credentials from the property file
    protected void setGatewayProperties() {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(OrchestratorConstants.AIRAVATA_PROPERTIES);
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setAiravataUserName(properties.getProperty("system.user"));
        setGatewayName(properties.getProperty("system.gateway"));
        setRegistryURL(properties.getProperty("airavata.server.url"));
    }

   private AiravataAPI getAiravataAPI() {
       if (airavataAPI == null) {
           try {
               airavataAPI = AiravataAPIFactory.getAPI(getGatewayName(), getAiravataUserName());
           }  catch (AiravataAPIInvocationException e) {
               logger.error("Unable to create Airavata API", e);
           }
       }
       return airavataAPI;
   }
}
