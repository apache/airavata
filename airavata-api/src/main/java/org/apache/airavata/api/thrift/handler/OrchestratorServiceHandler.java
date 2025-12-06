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
package org.apache.airavata.api.thrift.handler;

import java.util.List;
import org.apache.airavata.model.error.AiravataErrorType;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.error.LaunchValidationException;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.orchestrator.util.OrchestratorServerThreadPoolExecutor;
import org.apache.airavata.service.OrchestratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrchestratorServiceHandler implements org.apache.airavata.orchestrator.cpi.OrchestratorService.Iface {
    private static Logger log = LoggerFactory.getLogger(OrchestratorServiceHandler.class);
    private final OrchestratorService orchestratorService;

    /**
     * Query orchestrator server to fetch the CPI version
     */
    @Override
    public String getAPIVersion() throws AiravataSystemException {
        return org.apache.airavata.orchestrator.cpi.orchestrator_cpiConstants.ORCHESTRATOR_CPI_VERSION;
    }

    @Autowired
    public OrchestratorServiceHandler(OrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
        log.info("OrchestratorServiceHandler initialized with Spring-injected OrchestratorService");
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
        var pool = OrchestratorServerThreadPoolExecutor.getCachedThreadPool();
        try {
            return orchestratorService.launchExperiment(experimentId, gatewayId, pool);
        } catch (Throwable e) {
            log.error("Error launching experiment: " + experimentId, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error launching experiment: " + experimentId + ". More info: " + e.getMessage());
            exception.initCause(e);
            throw exception;
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
            log.error("Error validating experiment: " + experimentId, e);
            LaunchValidationException exception = new LaunchValidationException();
            exception.setErrorMessage(
                    "Error validating experiment: " + experimentId + ". More info: " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public boolean validateProcess(String experimentId, List<ProcessModel> processes) throws LaunchValidationException {
        try {
            return orchestratorService.validateProcess(experimentId, processes);
        } catch (LaunchValidationException e) {
            throw e;
        } catch (Throwable e) {
            log.error("Error validating process: " + experimentId, e);
            LaunchValidationException exception = new LaunchValidationException();
            exception.setErrorMessage("Error validating process: " + experimentId + ". More info: " + e.getMessage());
            throw exception;
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
            log.error("Error terminating experiment: " + experimentId, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error terminating experiment: " + experimentId + ". More info: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    public void fetchIntermediateOutputs(String experimentId, String gatewayId, List<String> outputNames)
            throws AiravataSystemException {
        try {
            orchestratorService.fetchIntermediateOutputs(experimentId, gatewayId, outputNames);
        } catch (Throwable e) {
            log.error("Error fetching intermediate outputs for experiment: " + experimentId, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error fetching intermediate outputs for experiment: " + experimentId + ". More info: "
                    + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    public boolean launchProcess(String processId, String airavataCredStoreToken, String gatewayId)
            throws AiravataSystemException {
        try {
            return orchestratorService.launchProcess(processId, airavataCredStoreToken, gatewayId);
        } catch (Throwable e) {
            log.error("Error launching process: " + processId, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error launching process: " + processId + ". More info: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }
}
