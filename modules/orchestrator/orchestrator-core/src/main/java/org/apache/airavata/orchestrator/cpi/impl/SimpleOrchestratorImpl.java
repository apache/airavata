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
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.core.job.JobSubmitter;
import org.apache.airavata.orchestrator.core.utils.OrchestratorUtils;
import org.apache.airavata.orchestrator.core.validator.JobMetadataValidator;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleOrchestratorImpl extends AbstractOrchestrator {
    private final static Logger logger = LoggerFactory.getLogger(SimpleOrchestratorImpl.class);
    private ExecutorService executor;


    // this is going to be null unless the thread count is 0
    private JobSubmitter jobSubmitter = null;

    private JobMetadataValidator jobMetadataValidator = null;


    public SimpleOrchestratorImpl() throws OrchestratorException {
        try {
            try {
                String submitterClass = this.orchestratorContext.getOrchestratorConfiguration().getNewJobSubmitterClass();
                Class<? extends JobSubmitter> aClass = Class.forName(submitterClass.trim()).asSubclass(JobSubmitter.class);
                jobSubmitter = aClass.newInstance();
                jobSubmitter.initialize(this.orchestratorContext);

                String validatorClzz = this.orchestratorContext.getOrchestratorConfiguration().getValidatorClass();
                if (this.orchestratorConfiguration.isEnableValidation()) {
                    if (validatorClzz == null) {
                        logger.error("Job validation class is not properly set, so Validation will be turned off !");
                    }
                    Class<? extends JobMetadataValidator> vClass = Class.forName(validatorClzz.trim()).asSubclass(JobMetadataValidator.class);
                    jobMetadataValidator = vClass.newInstance();
                }
            } catch (Exception e) {
                String error = "Error creating JobSubmitter in non threaded mode ";
                logger.error(error);
                throw new OrchestratorException(error, e);
            }
        } catch (OrchestratorException e) {
            logger.error("Error Constructing the Orchestrator");
            throw e;
        }
    }

    public boolean launchExperiment(String experimentID) throws OrchestratorException {
        // we give higher priority to userExperimentID
        //todo support multiple validators
        if (this.orchestratorConfiguration.isEnableValidation()) {
            if(jobMetadataValidator.validate(experimentID)){
                logger.info("validation Successful for the experiment: " +  experimentID);
            }else {
                throw new OrchestratorException("Validation Failed, so Job will not be submitted to GFAC");
            }
        }
        try {
            airavataRegistry.changeStatus(experimentID, AiravataJobState.State.ACCEPTED);
            if (orchestratorContext.getOrchestratorConfiguration().getThreadPoolSize() == 0) {
                jobSubmitter.directJobSubmit(experimentID);
            }
        } catch (RegistryException e) {
            //todo put more meaningful error message
            logger.error("Failed to create experiment for the request from " + experimentID);
            return false;
        }
        return true;
    }
}
