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


import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.gfac.core.cpi.GFac;
import org.apache.airavata.gfac.core.cpi.GFacImpl;
import org.apache.airavata.orchestrator.core.context.OrchestratorContext;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.core.gfac.GFACInstance;
import org.apache.airavata.orchestrator.core.job.JobSubmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the simplest implementation for JobSubmitter,
 * This is calling gfac invocation methods to invoke the gfac embedded mode,so this does not really implement
 * the selectGFACInstance method
 */
public class GFACEmbeddedJobSubmitter implements JobSubmitter {
    private final static Logger logger = LoggerFactory.getLogger(GFACEmbeddedJobSubmitter.class);

    private OrchestratorContext orchestratorContext;

    private GFac gfac;


    public void initialize(OrchestratorContext orchestratorContext) throws OrchestratorException {
        this.orchestratorContext = orchestratorContext;
        gfac = new GFacImpl(orchestratorContext.getNewRegistry(), null, orchestratorContext.getRegistry());
    }

    public GFACInstance selectGFACInstance() throws OrchestratorException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    public boolean submit(String experimentID, String taskID) throws OrchestratorException {
        try {
            return gfac.submitJob(experimentID, taskID, ServerSettings.getSetting(Constants.GATEWAY_NAME));
        } catch (Exception e) {
            String error = "Error launching the job : " + experimentID;
            logger.error(error);
            throw new OrchestratorException(error);
        }
    }


    public boolean submit(String experimentID, String taskID,String tokenId) throws OrchestratorException {
        try {
            return submit(experimentID,taskID);
        } catch (Exception e) {
            String error = "Error launching the job : " + experimentID;
            logger.error(error);
            throw new OrchestratorException(error);
        }
    }

    public boolean terminate(String experimentID, String taskID) throws OrchestratorException {
        return false;
    }

    public GFac getGfac() {
        return gfac;
    }

    public void setGfac(GFac gfac) {
        this.gfac = gfac;
    }

    public OrchestratorContext getOrchestratorContext() {
        return orchestratorContext;
    }

    public void setOrchestratorContext(OrchestratorContext orchestratorContext) {
        this.orchestratorContext = orchestratorContext;
    }
}
