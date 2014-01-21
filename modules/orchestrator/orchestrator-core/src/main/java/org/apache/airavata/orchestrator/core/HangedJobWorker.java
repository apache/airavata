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

import org.apache.airavata.orchestrator.core.context.OrchestratorContext;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.core.gfac.GFACInstance;
import org.apache.airavata.orchestrator.core.job.JobSubmitter;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * this worker is handling hanged jobs and invoke the submitter
 * after finding hanged jobs
 */
public class HangedJobWorker implements Runnable{
     private final static Logger logger = LoggerFactory.getLogger(HangedJobWorker.class);

    private OrchestratorContext orchestratorContext;

    private JobSubmitter jobSubmitter;

    // Set the default submit interval value
    private int submitInterval = 1000;


    public HangedJobWorker(OrchestratorContext orchestratorContext) throws OrchestratorException {
        this.orchestratorContext = orchestratorContext;
        try {
            String submitterClass = this.orchestratorContext.getOrchestratorConfiguration().getSubmitterClass();
				//FIXME: (MEP) Do you want to use the same submit interval for hung jobs as newly submitted jobs?  Suggest separate parameters.
            submitInterval = this.orchestratorContext.getOrchestratorConfiguration().getSubmitterInterval();
				//FIXME: (MEP) It is possible that you want to have a different JobSubmitter for hung jobs and for new jobs, so the property file needs to have separate name/value pairs for these.
            Class<? extends JobSubmitter> aClass = Class.forName(submitterClass.trim()).asSubclass(JobSubmitter.class);
            jobSubmitter = aClass.newInstance();
            jobSubmitter.initialize(this.orchestratorContext);
        } catch (ClassNotFoundException e) {
            logger.error("Error while loading Job Submitter");
        } catch (InstantiationException e) {
            logger.error("Error while loading Job Submitter");
            throw new OrchestratorException(e);
        } catch (IllegalAccessException e) {
            logger.error("Error while loading Job Submitter");
            throw new OrchestratorException(e);
        }

    }

    public void run() {
        /* implement logic to submit job batches time to time */
        int idleCount = 0;
        while (true) {
            try {
                Thread.sleep(submitInterval);
            } catch (InterruptedException e) {
                logger.error("Error in JobSubmitter during sleeping process before submit jobs");
                e.printStackTrace();
            }
            /* Here the worker pick bunch of jobs available to submit and submit that to a single
              GFAC instance, we do not handle job by job submission to each gfac instance
            */
            try {

            GFACInstance gfacInstance = jobSubmitter.selectGFACInstance();

            // Now we have picked a gfac instance to submit set of jobs at this time, now its time to
            // select what are the jobs available to submit

                List<String> allHangedJobs = orchestratorContext.getRegistry().getAllHangedJobs();
					 //FIXME: (MEP) Suggest putting this in a separate method, and you'll need a method to also decrease the submitInterval if you are busy. This submitInterval adjustment seems to be too fined grained of a detail to worry about now.
                if (allHangedJobs.size() == 0) {
                    idleCount++;

                    if (idleCount == 10) {
                        try {
                            Thread.sleep(submitInterval*2);
                        } catch (InterruptedException e) {
                            logger.error("Error in JobSubmitter during sleeping process before submit jobs");
                            e.printStackTrace();
                        }
                        idleCount=0;
                    }
                    continue;
                }

                jobSubmitter.submitJob(gfacInstance,allHangedJobs);

                /* After submitting available jobs try to schedule again and then submit*/
                jobSubmitter.submitJob(jobSubmitter.selectGFACInstance(),allHangedJobs);
            } catch (Exception e) {
                logger.error("Error while trying to retrieve available ");
            }
        }
    }

    public OrchestratorContext getOrchestratorContext() {
        return orchestratorContext;
    }

    public void setOrchestratorContext(OrchestratorContext orchestratorContext) {
        this.orchestratorContext = orchestratorContext;
    }
}
