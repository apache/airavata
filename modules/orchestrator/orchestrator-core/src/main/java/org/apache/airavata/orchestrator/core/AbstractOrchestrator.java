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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;

import org.apache.airavata.client.AiravataAPIFactory;
import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.common.exception.AiravataConfigurationException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.orchestrator.core.context.OrchestratorContext;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.core.gfac.GFACInstance;
import org.apache.airavata.orchestrator.core.job.JobSubmitter;
import org.apache.airavata.orchestrator.core.utils.OrchestratorUtils;
import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.api.AiravataRegistryFactory;
import org.apache.airavata.registry.api.AiravataUser;
import org.apache.airavata.registry.api.Gateway;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractOrchestrator implements Orchestrator{
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
	public boolean initialize() throws OrchestratorException {
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
        } catch (RegistryException e) {
            logger.error("Failed to initializing Orchestrator");
            OrchestratorException orchestratorException = new OrchestratorException(e);
            throw orchestratorException;
        } catch (AiravataConfigurationException e) {
            logger.error("Failed to initializing Orchestrator");
            OrchestratorException orchestratorException = new OrchestratorException(e);
            throw orchestratorException;
        } catch (IOException e) {
            logger.error("Failed to initializing Orchestrator - Error parsing orchestrator.properties");
            OrchestratorException orchestratorException = new OrchestratorException(e);
            throw orchestratorException;
        }
        return true;
    }
	
	//get the registry URL and the credentials from the property file
    protected void setGatewayProperties() {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("gateway.properties");
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        setAiravataUserName(properties.getProperty("airavata.server.user"));
        setGatewayName(properties.getProperty("gateway.name"));
        setRegistryURL(properties.getProperty("airavata.server.url"));
    }
    //todo decide whether to return an error or do what
	 //FIXME: (MEP) as posted on dev list, I think this should return a JobRequest with the experimentID set. This would simplify some of the validation in EmbeddedGFACJobSubmitter's launcGfacWithJobRequest--just throw the job away if the JobRequest is incomplete or malformed.
   public String createExperiment(ExperimentRequest request) throws OrchestratorException {
       //todo use a consistent method to create the experiment ID
		  //FIXME: (MEP) Should you trust the user to do this?  What if the same experimentID is sent twice by the same gateway?
       String experimentID = request.getUserExperimentID();
       if(experimentID == null){
       	experimentID = UUID.randomUUID().toString(); 
       }
       try {
           airavataRegistry.storeExperiment(request.getSubmitterUserName(), experimentID, request.getApplicationName(), request.getJobRequest());
       } catch (RegistryException e) {
           //todo put more meaningful error  message
           logger.error("Failed to create experiment for the request from " + request.getSubmitterUserName());
           throw new OrchestratorException(e);
       }
       return experimentID;
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
