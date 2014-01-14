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

import org.apache.airavata.client.AiravataAPIFactory;
import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.common.exception.AiravataConfigurationException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataJobState;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.orchestrator.core.context.OrchestratorContext;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.core.gfac.GFACInstance;
import org.apache.airavata.orchestrator.core.job.JobSubmitter;
import org.apache.airavata.orchestrator.core.utils.OrchestratorConstants;
import org.apache.airavata.orchestrator.core.utils.OrchestratorUtils;
import org.apache.airavata.registry.api.*;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PullBasedOrchestrator implements Orchestrator {
    private final static Logger logger = LoggerFactory.getLogger(PullBasedOrchestrator.class);

    private OrchestratorContext orchestratorContext;

    private AiravataRegistry2 airavataRegistry;

    private ExecutorService executor;

    private AiravataAPI airavataAPI;

    // this is going to be null unless the thread count is 0
    private JobSubmitter jobSubmitter = null;

    public boolean initialize() throws OrchestratorException {
        try {
            /* Initializing the OrchestratorConfiguration object */
            OrchestratorConfiguration orchestratorConfiguration = OrchestratorUtils.loadOrchestratorConfiguration();

            /* initializing the Orchestratorcontext object */
            airavataRegistry = AiravataRegistryFactory.getRegistry(new Gateway("default"), new AiravataUser("admin"));
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
            /* Starting submitter thread pool */

            // we have a thread to run normal new jobs except to monitor hanged jobs
            if (orchestratorConfiguration.getThreadPoolSize() != 0) {
                executor = Executors.newFixedThreadPool(orchestratorConfiguration.getThreadPoolSize() + 1);
                this.startJobSubmitter();
            } else {

                try {
                    String submitterClass = this.orchestratorContext.getOrchestratorConfiguration().getSubmitterClass();
                    Class<? extends JobSubmitter> aClass = Class.forName(submitterClass.trim()).asSubclass(JobSubmitter.class);
                    jobSubmitter = aClass.newInstance();
                    jobSubmitter.initialize(this.orchestratorContext);
                } catch (Exception e) {
                    String error = "Error creating JobSubmitter in non threaded mode ";
                    logger.error(error);
                    throw new OrchestratorException(error, e);
                }
            }
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


    public void shutdown() throws OrchestratorException {
        executor.shutdown();

    }

    //todo decide whether to return an error or do what

    public String createExperiment(ExperimentRequest request) throws OrchestratorException {
        //todo use a consistent method to create the experiment ID
        String experimentID = request.getUserExperimentID();
        String orchestratorID = UUID.randomUUID().toString();
        String username = request.getUserName();
        try {
            airavataRegistry.storeExperiment(username, experimentID, orchestratorID);
        } catch (RegistryException e) {
            //todo put more meaningful error  message
            logger.error("Failed to create experiment for the request from " + request.getUserName());
            throw new OrchestratorException(e);
        }
        return experimentID;
    }

    public boolean launchExperiment(JobRequest request) throws OrchestratorException {
        // validate the jobRequest first
        if (!OrchestratorUtils.validateJobRequest(request)) {
            logger.error("Invalid Job request sent, Experiment creation failed");
            return false;
        }
        String experimentID = null;
        // we give higher priority to userExperimentID
        if (request.getUserExperimentID() != null) {
            experimentID = request.getUserExperimentID();
        } else if (request.getSystemExperimentID() != null) {
            experimentID = request.getSystemExperimentID();
        } else {
            logger.error("Invalid Experiment ID given: " + request.getUserName());
            return false;
        }
        //todo use a more concrete user type in to this
        try {
            if (request.getHostDescription() != null) {
                if (!airavataRegistry.isHostDescriptorExists(request.getHostDescription().getType().getHostName())) {
                    airavataRegistry.addHostDescriptor(request.getHostDescription());
                }
            }
            if (request.getServiceDescription() != null) {
                if (!airavataRegistry.isServiceDescriptorExists(request.getServiceDescription().getType().getName())) {
                    airavataRegistry.addServiceDescriptor(request.getServiceDescription());
                }
            }
            if (request.getApplicationDescription() != null) {
                if (request.getServiceDescription() != null && request.getHostDescription() != null) {
                    if(!airavataRegistry.isApplicationDescriptorExists(request.getServiceDescription().getType().getName(),
                            request.getHostDescription().getType().getHostName(),request.getApplicationDescription().getType().getApplicationName().getStringValue())){
                    airavataRegistry.addApplicationDescriptor(request.getServiceDescription(),
                            request.getHostDescription(), request.getApplicationDescription());
                    }
                } else {
                    String error = "Providing just Application Descriptor is not sufficient to save to Registry";
                    logger.error(error);
                    throw new OrchestratorException(error);
                }
            }
            airavataRegistry.changeStatus(experimentID, AiravataJobState.State.ACCEPTED);
            if (orchestratorContext.getOrchestratorConfiguration().getThreadPoolSize() == 0) {
                jobSubmitter.directJobSubmit(request);
            }

            //todo save jobRequest data in to the database
        } catch (RegistryException e) {
            //todo put more meaningful error message
            logger.error("Failed to create experiment for the request from " + request.getUserName());
            return false;
        }
        return true;
    }

    public void startJobSubmitter() throws OrchestratorException {
        NewJobWorker jobSubmitterWorker = new NewJobWorker(orchestratorContext);
        executor.execute(jobSubmitterWorker);

        for (int i = 0; i < orchestratorContext.getOrchestratorConfiguration().getThreadPoolSize() - 1; i++) {
            HangedJobWorker hangedJobWorker = new HangedJobWorker(orchestratorContext);
            executor.execute(hangedJobWorker);
        }
    }

    public boolean cancelExperiment(String experimentID) throws OrchestratorException {
        try {
            AiravataJobState state = orchestratorContext.getRegistry().getState(experimentID);
            if (state.getJobState().equals(AiravataJobState.State.RUNNING) || state.getJobState().equals(AiravataJobState.State.PENDING) ||
                    state.getJobState().equals(AiravataJobState.State.ACTIVE) || state.getJobState().equals(AiravataJobState.State.SUBMITTED)) {

                //todo perform cancelling and last peform the database update

                orchestratorContext.getRegistry().changeStatus(experimentID, AiravataJobState.State.CANCELLED);
            } else if (state.getJobState().equals(AiravataJobState.State.DONE)) {
                String error = "Job is already Finished so cannot cancel the job " + experimentID;
                logger.error(error);
                new OrchestratorException(error);
            } else {
                // do nothing but simply change the job state to cancelled because job is not yet submitted to the resource
                orchestratorContext.getRegistry().changeStatus(experimentID, AiravataJobState.State.CANCELLED);
            }

        } catch (RegistryException e) {
            String error = "Error reading the job state for the given Experiment ID: " + experimentID;
            logger.error(error);
            throw new OrchestratorException(error, e);
        }
        return true;
    }

    private AiravataAPI getAiravataAPI() {
        if (airavataAPI == null) {
            try {
                String systemUserName = ServerSettings.getSystemUser();
                String gateway = ServerSettings.getSystemUserGateway();
                airavataAPI = AiravataAPIFactory.getAPI(gateway, systemUserName);
            } catch (ApplicationSettingsException e) {
                logger.error("Unable to read the properties file", e);
            } catch (AiravataAPIInvocationException e) {
                logger.error("Unable to create Airavata API", e);
            }
        }
        return airavataAPI;
    }
}
