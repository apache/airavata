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

import org.apache.airavata.gfac.provider.GFacProvider;
import org.apache.airavata.orchestrator.core.context.OrchestratorContext;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.core.gfac.GFACInstance;
import org.apache.airavata.orchestrator.core.job.JobSubmitter;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/*
This is the worker class to handle the jobs stored in to registry as
fresh and this will pick those jobs and invoke the defined submitter
 */
public class NewJobWorker implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(NewJobWorker.class);

    private OrchestratorContext orchestratorContext;

    private JobSubmitter jobSubmitter;

    // Set the default submit interval value
    private int submitInterval = 1000;


    public NewJobWorker(OrchestratorContext orchestratorContext) throws OrchestratorException {
        this.orchestratorContext = orchestratorContext;
        try {
            String submitterClass = this.orchestratorContext.getOrchestratorConfiguration().getSubmitterClass();
            submitInterval = this.orchestratorContext.getOrchestratorConfiguration().getSubmitterInterval();
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

                List<String> allAcceptedJobs = orchestratorContext.getRegistry().getAllAcceptedJobs();
                if (allAcceptedJobs.size() == 0) {
						  //FIXME: (MEP) this stuff should be in a separate method, and I'm not sure it is necessary.  You have no way to decrease the submit interval if busy. 
                    idleCount++;

                    if (idleCount == 10) {
                        try {
                            Thread.sleep(submitInterval * 2);
                        } catch (InterruptedException e) {
                            logger.error("Error in JobSubmitter during sleeping process before submit jobs");
                            e.printStackTrace();
                        }
                        idleCount = 0;
                    }
                    continue;
                }
                jobSubmitter.submitJob(gfacInstance, allAcceptedJobs);
            } catch (Exception e) {
                logger.error("Error while trying to retrieve available ");
            }
            // Now we have picked a gfac instance to submit set of jobs at this time, now its time to
            // select what are the jobs available to submit

        }
    }

    public OrchestratorContext getOrchestratorContext() {
        return orchestratorContext;
    }

    public void setOrchestratorContext(OrchestratorContext orchestratorContext) {
        this.orchestratorContext = orchestratorContext;
    }
}
