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
import org.apache.airavata.api.server.handler.ThriftExceptionHandler;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.error.LaunchValidationException;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.orchestrator.cpi.OrchestratorService;
import org.apache.airavata.orchestrator.cpi.orchestrator_cpiConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrchestratorServerHandler implements OrchestratorService.Iface {
    private static Logger log = LoggerFactory.getLogger(OrchestratorServerHandler.class);
    private org.apache.airavata.service.OrchestratorService orchestratorService;

    /**
     * Query orchestrator server to fetch the CPI version
     */
    @Override
    public String getAPIVersion() throws AiravataSystemException {
        return orchestrator_cpiConstants.ORCHESTRATOR_CPI_VERSION;
    }

    public OrchestratorServerHandler() {
        try {
            orchestratorService = new org.apache.airavata.service.OrchestratorService();
        } catch (Exception e) {
            log.error("Error while initializing orchestrator service", e);
            throw new RuntimeException("Error while initializing orchestrator service", e);
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
    @Override
    public boolean launchExperiment(String experimentId, String gatewayId) throws AiravataSystemException {
        try {
            return orchestratorService.launchExperimentWithErrorHandling(
                    experimentId, gatewayId, org.apache.airavata.orchestrator.util.OrchestratorServerThreadPoolExecutor.getCachedThreadPool());
        } catch (Throwable e) {
            ThriftExceptionHandler.convertException(e, "Error launching experiment: " + experimentId);
            return false; // unreachable
        }
    }

    /**
     * This method will validate the experiment before launching, if is failed
     * we do not run the launch in airavata thrift service (only if validation
     * is enabled
     *
     * @param experimentId
     * @return
     */
    @Override
    public boolean validateExperiment(String experimentId) throws LaunchValidationException {
        try {
            return orchestratorService.validateExperiment(experimentId);
        } catch (LaunchValidationException e) {
            throw e;
        } catch (Throwable e) {
            ThriftExceptionHandler.convertException(e, "Error validating experiment: " + experimentId);
            return false; // unreachable
        }
    }

    @Override
    public boolean validateProcess(String experimentId, List<ProcessModel> processes) throws LaunchValidationException {
        try {
            return orchestratorService.validateProcess(experimentId, processes);
        } catch (LaunchValidationException e) {
            throw e;
        } catch (Throwable e) {
            ThriftExceptionHandler.convertException(e, "Error validating process: " + experimentId);
            return false; // unreachable
        }
    }

    /**
     * This can be used to cancel a running experiment and store the status to
     * terminated in registry
     *
     * @param experimentId
     * @return
     */
    @Override
    public boolean terminateExperiment(String experimentId, String gatewayId) throws AiravataSystemException {
        log.info("Experiment: {} is cancelling  !!!!!", experimentId);
        try {
            return orchestratorService.terminateExperiment(experimentId, gatewayId);
        } catch (Throwable e) {
            ThriftExceptionHandler.convertException(e, "Error terminating experiment: " + experimentId);
            return false; // unreachable
        }
    }

    public void fetchIntermediateOutputs(String experimentId, String gatewayId, List<String> outputNames) {
        try {
            orchestratorService.fetchIntermediateOutputs(experimentId, gatewayId, outputNames);
        } catch (Throwable e) {
            log.error("Error fetching intermediate outputs for experiment: " + experimentId, e);
            ThriftExceptionHandler.convertException(e, "Error fetching intermediate outputs: " + experimentId);
        }
    }

    @Override
    public boolean launchProcess(String processId, String airavataCredStoreToken, String gatewayId) throws AiravataSystemException {
        try {
            return orchestratorService.launchProcess(processId, airavataCredStoreToken, gatewayId);
        } catch (Throwable e) {
            ThriftExceptionHandler.convertException(e, "Error launching process: " + processId);
            return false; // unreachable
        }
    }
}
