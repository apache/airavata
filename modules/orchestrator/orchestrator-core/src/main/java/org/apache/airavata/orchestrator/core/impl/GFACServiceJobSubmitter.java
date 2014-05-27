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

import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.gfac.cpi.GfacService;
import org.apache.airavata.gfac.util.Constants;
import org.apache.airavata.orchestrator.core.context.OrchestratorContext;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.core.gfac.GFACInstance;
import org.apache.airavata.orchestrator.core.gfac.GFacClientFactory;
import org.apache.airavata.orchestrator.core.job.JobSubmitter;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * this class is responsible for submitting a job to gfac in service mode,
 * it will select a gfac instance based on the incoming request and submit to that
 * gfac instance.
 */
public class GFACServiceJobSubmitter implements JobSubmitter {
    private final static Logger logger = LoggerFactory.getLogger(GFACServiceJobSubmitter.class);

    private OrchestratorContext orchestratorContext;

    public void initialize(OrchestratorContext orchestratorContext) throws OrchestratorException {
        this.orchestratorContext = orchestratorContext;
    }

    public GFACInstance selectGFACInstance() throws OrchestratorException {
        // currently we only support one instance but future we have to pick an instance
        return null;
    }

    public boolean submit(String experimentID, String taskID) throws OrchestratorException {
        GfacService.Client localhost = GFacClientFactory.createOrchestratorClient("localhost",
                Integer.parseInt(ServerSettings.getSetting(Constants.GFAC_SERVER_PORT, "8950")));
        try {
            return localhost.submitJob(experimentID, taskID);
        } catch (TException e) {
            throw new OrchestratorException(e);
        }
    }
}
