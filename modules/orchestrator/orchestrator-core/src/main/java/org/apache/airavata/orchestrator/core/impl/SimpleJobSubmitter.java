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

import org.apache.airavata.common.utils.AiravataJobState;
import org.apache.airavata.orchestrator.core.context.OrchestratorContext;
import org.apache.airavata.orchestrator.core.gfac.GFACInstance;
import org.apache.airavata.orchestrator.core.job.JobSubmitter;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.junit.After;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SimpleJobSubmitter implements JobSubmitter{
    private final static Logger logger = LoggerFactory.getLogger(SimpleJobSubmitter.class);

    private OrchestratorContext orchestratorContext;

    public SimpleJobSubmitter(OrchestratorContext orchestratorContext) {
        this.orchestratorContext = orchestratorContext;
    }

    public GFACInstance selectGFACInstance(OrchestratorContext context) {
        return null;
    }


    public boolean submitJob(GFACInstance gfac, List<String> experimentIDList) {

        for(int i=0;i<experimentIDList.size();i++){
            try {
                // once its fetched it's status will changed to fetched state
                String s = orchestratorContext.getRegistry().fetchAcceptedJob(experimentIDList.get(i));
                //todo submit the jobs

                //after successfully submitting the jobs set the status of the job to submitted

                orchestratorContext.getRegistry().changeStatus(experimentIDList.get(i), AiravataJobState.State.SUBMITTED);
            } catch (RegistryException e) {
                logger.error("Error getting job related information");
            }
        }
    }
}
