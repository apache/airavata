/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.orchestrator.server;

import java.util.*;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.model.error.LaunchValidationException;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.cpi.OrchestratorService;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrchestratorServerHandler implements OrchestratorService.Iface {
    private static Logger log = LoggerFactory.getLogger(OrchestratorServerHandler.class);
    private org.apache.airavata.service.OrchestratorService orchestratorService;

    /**
     * Query orchestrator server to fetch the CPI version
     */
    @Override
    public String getAPIVersion() throws TException {
        return null;
    }

    public OrchestratorServerHandler() throws OrchestratorException, TException {
        try {
            orchestratorService = new org.apache.airavata.service.OrchestratorService();
        } catch (OrchestratorException e) {
            log.error(e.getMessage(), e);
            throw new OrchestratorException("Error while initializing orchestrator service", e);
        }
    }

    /**
     * * After creating the experiment Data user have the * experimentID as the
     * handler to the experiment, during the launchProcess * We just have to
     * give the experimentID * * @param experimentID * @return sucess/failure *
     * *
     *
     * @param experimentId
     */
    public boolean launchExperiment(String experimentId, String gatewayId) throws TException {
        return orchestratorService.launchExperimentWithErrorHandling(
                experimentId, gatewayId, org.apache.airavata.orchestrator.util.OrchestratorServerThreadPoolExecutor.getCachedThreadPool());
    }

    /**
     * This method will validate the experiment before launching, if is failed
     * we do not run the launch in airavata thrift service (only if validation
     * is enabled
     *
     * @param experimentId
     * @return
     * @throws TException
     */
    public boolean validateExperiment(String experimentId) throws TException, LaunchValidationException {
        return orchestratorService.validateExperiment(experimentId);
    }

    @Override
    public boolean validateProcess(String experimentId, List<ProcessModel> processes)
            throws LaunchValidationException, TException {
        return orchestratorService.validateProcess(experimentId, processes);
    }

    /**
     * This can be used to cancel a running experiment and store the status to
     * terminated in registry
     *
     * @param experimentId
     * @return
     * @throws TException
     */
    public boolean terminateExperiment(String experimentId, String gatewayId) throws TException {
        log.info(experimentId, "Experiment: {} is cancelling  !!!!!", experimentId);
        return orchestratorService.terminateExperiment(experimentId, gatewayId);
    }

    public void fetchIntermediateOutputs(String experimentId, String gatewayId, List<String> outputNames)
            throws TException {
        orchestratorService.fetchIntermediateOutputs(experimentId, gatewayId, outputNames);
    }

    @Override
    public boolean launchProcess(String processId, String airavataCredStoreToken, String gatewayId) throws TException {
        return orchestratorService.launchProcess(processId, airavataCredStoreToken, gatewayId);
    }
}
