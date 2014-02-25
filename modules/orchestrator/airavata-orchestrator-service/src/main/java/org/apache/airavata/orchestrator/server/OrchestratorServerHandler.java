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

package org.apache.airavata.orchestrator.server;

import org.apache.airavata.orchestrator.cpi.OrchestratorService;
import org.apache.airavata.orchestrator.cpi.orchestrator_cpi_serviceConstants;
import org.apache.thrift.TException;

public class OrchestratorServerHandler implements OrchestratorService.Iface {

    /**
     * Query orchestrator server to fetch the CPI version
     */
    @Override
    public String getOrchestratorCPIVersion() throws TException {
        return orchestrator_cpi_serviceConstants.ORCHESTRATOR_CPI_VERSION;
    }

    /**
     * * After creating the experiment Data user have the
     * * experimentID as the handler to the experiment, during the launchExperiment
     * * We just have to give the experimentID
     * *
     * * @param experimentID
     * * @return sucess/failure
     * *
     * *
     *
     * @param experimentId
     */
    @Override
    public boolean launchExperiment(String experimentId) throws TException {
        return false;
    }

    @Override
    public boolean terminateExperiment(String experimentId) throws TException {
        return false;
    }
}
