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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.airavata.common.utils.AiravataJobState;
import org.apache.airavata.orchestrator.core.HangedJobWorker;
import org.apache.airavata.orchestrator.core.NewJobWorker;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.core.job.JobSubmitter;
import org.apache.airavata.orchestrator.core.utils.OrchestratorUtils;
import org.apache.airavata.registry.api.JobRequest;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleOrchestratorImpl extends AbstractOrchestrator {
    private final static Logger logger = LoggerFactory.getLogger(SimpleOrchestratorImpl.class);
    private ExecutorService executor;


    // this is going to be null unless the thread count is 0
    private JobSubmitter jobSubmitter = null;

    public boolean initialize() throws OrchestratorException {
        super.initialize();
        // we have a thread to run normal new jobs except to monitor hanged jobs
        if (orchestratorConfiguration.getThreadPoolSize() != 0) {
            executor = Executors.newFixedThreadPool(orchestratorConfiguration.getThreadPoolSize() + 1);
            this.startJobSubmitter();
        } else {

            try {
                String submitterClass = this.orchestratorContext.getOrchestratorConfiguration().getNewJobSubmitterClass();
                Class<? extends JobSubmitter> aClass = Class.forName(submitterClass.trim()).asSubclass(JobSubmitter.class);
                jobSubmitter = aClass.newInstance();
                jobSubmitter.initialize(this.orchestratorContext);
            } catch (Exception e) {
                String error = "Error creating JobSubmitter in non threaded mode ";
                logger.error(error);
                throw new OrchestratorException(error, e);
            }
        }
        return true;
    }


    public void shutdown() throws OrchestratorException {
        executor.shutdown();

    }

    public boolean launchExperiment(JobRequest request) throws OrchestratorException {
        // validate the jobRequest first
        if (!OrchestratorUtils.validateJobRequest(request)) {
            logger.error("Invalid Job request sent, Experiment creation failed");
            return false;
        }
        String experimentID = OrchestratorUtils.getUniqueID(request);
        // we give higher priority to userExperimentID
        if (experimentID == null) {
            logger.error("Invalid Experiment ID given: " + request.getUserName());
            return false;
        }
        //todo use a more concrete user type in to this
        //FIXME: (MEP) Why don't we pass the JobRequest to the registry and let it do all of this?  Or just store the JobRequest as an object directly in the registry?
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
                    if (!airavataRegistry.isApplicationDescriptorExists(request.getServiceDescription().getType().getName(),
                            request.getHostDescription().getType().getHostName(), request.getApplicationDescription().getType().getApplicationName().getStringValue())) {
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
        //FIXME: (MEP) Why create a new thread for jobSubmittedWorker but use the pool for HangedJobWorker?
        //FIXME: (MEP) As discussed on the dev list, we need to improve this
        NewJobWorker jobSubmitterWorker = new NewJobWorker(orchestratorContext);
        executor.execute(jobSubmitterWorker);

        for (int i = 0; i < orchestratorContext.getOrchestratorConfiguration().getThreadPoolSize() - 1; i++) {
            HangedJobWorker hangedJobWorker = new HangedJobWorker(orchestratorContext);
            executor.execute(hangedJobWorker);
        }
    }

    public boolean terminateExperiment(String experimentID) throws OrchestratorException {
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

    public boolean validateExperiment(String experimentID) {
        return false;
    }
}
