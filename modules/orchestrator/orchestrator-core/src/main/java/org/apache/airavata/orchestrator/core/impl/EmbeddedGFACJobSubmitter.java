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
package org.apache.airavata.orchestrator.core.impl;


import java.util.*;

import org.apache.airavata.common.utils.AiravataJobState;
import org.apache.airavata.gfac.cpi.GFac;
import org.apache.airavata.orchestrator.core.context.OrchestratorContext;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.core.gfac.GFACInstance;
import org.apache.airavata.orchestrator.core.job.JobSubmitter;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.gfac.cpi.GFacImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the simplest implementation for JobSubmitter,
 * This is calling gfac invocation methods to invoke the gfac embedded mode,so this does not really implement
 * the selectGFACInstance method
 */
public class EmbeddedGFACJobSubmitter implements JobSubmitter {
    private final static Logger logger = LoggerFactory.getLogger(EmbeddedGFACJobSubmitter.class);

    private OrchestratorContext orchestratorContext;

    public void initialize(OrchestratorContext orchestratorContext) throws OrchestratorException {
        this.orchestratorContext = orchestratorContext;
    }

    public GFACInstance selectGFACInstance() throws OrchestratorException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    public boolean submitJob(GFACInstance gfac, List<String> experimentIDList) throws OrchestratorException {

        for (int i = 0; i < experimentIDList.size(); i++) {
            try {
                // once its fetched it's status will changed to fetched state
                launchGfacWithJobRequest(experimentIDList.get(i));
            } catch (Exception e) {
                logger.error("Error getting job related information");
                throw new OrchestratorException(e);
            }
        }
        return true;
    }

    //FIXME: (MEP) This method is pretty gruesome.  If we really expect multiple implementations of the JobSubmitter
    // interface and at least some of them will need to do the stuff in this method, then we need a parent class GenericJobSubmitterImpl.java (maybe abstract) that includes launchGfacWithJobRequest() so that subclasses can inherit it.
    private void launchGfacWithJobRequest(String experimentID) throws OrchestratorException {
        Registry newRegistry = orchestratorContext.getNewRegistry();
        try {
            //todo init this during submitter init
            GFac gFac = new GFacImpl(newRegistry, orchestratorContext.getOrchestratorConfiguration().getAiravataAPI(), orchestratorContext.getRegistry());
            gFac.submitJob(experimentID);
            orchestratorContext.getRegistry().changeStatus(experimentID, AiravataJobState.State.SUBMITTED);
        } catch (Exception e)
        {
            throw new OrchestratorException("Error launching the Job", e);
        }

    }

    public boolean directJobSubmit(String experimentID) throws OrchestratorException {
        try {
            launchGfacWithJobRequest(experimentID);
        } catch (Exception e) {
            String error = "Error launching the job : " + experimentID;
            logger.error(error);
            throw new OrchestratorException(error);
        }
        return true;
    }
}
