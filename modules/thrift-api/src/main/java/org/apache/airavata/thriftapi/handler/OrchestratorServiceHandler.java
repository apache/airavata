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
package org.apache.airavata.thriftapi.handler;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.orchestrator.utils.OrchestratorServerThreadPoolExecutor;
import org.apache.airavata.service.orchestrator.OrchestratorService;
import org.apache.airavata.thriftapi.mapper.ProcessModelMapper;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OrchestratorServiceHandler implements org.apache.airavata.thriftapi.orchestrator.model.OrchestratorService.Iface {
    private static Logger log = LoggerFactory.getLogger(OrchestratorServiceHandler.class);
    private final OrchestratorService orchestratorService;
    private final ProcessModelMapper processModelMapper = ProcessModelMapper.INSTANCE;

    /**
     * Query orchestrator server to fetch the CPI version
     */
    @Override
    public String getAPIVersion() throws org.apache.airavata.thriftapi.exception.AiravataSystemException, TException {
        return org.apache.airavata.thriftapi.orchestrator.model.orchestrator_cpiConstants.ORCHESTRATOR_CPI_VERSION;
    }

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
    public boolean launchExperiment(String experimentId, String gatewayId)
            throws org.apache.airavata.thriftapi.exception.AiravataSystemException, TException {
        var pool = OrchestratorServerThreadPoolExecutor.getCachedThreadPool();
        try {
            return orchestratorService.launchExperiment(experimentId, gatewayId, pool);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftAiravataSystemException(e);
        } catch (Throwable e) {
            log.error("Error launching experiment: " + experimentId, e);
            org.apache.airavata.thriftapi.exception.AiravataSystemException exception =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            exception.setAiravataErrorType(
                    org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
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
    public boolean validateExperiment(String experimentId)
            throws org.apache.airavata.thriftapi.exception.LaunchValidationException, TException {
        try {
            return orchestratorService.validateExperiment(experimentId);
        } catch (org.apache.airavata.common.exception.LaunchValidationException e) {
            throw convertToThriftLaunchValidationException(e);
        } catch (Throwable e) {
            log.error("Error validating experiment: " + experimentId, e);
            org.apache.airavata.thriftapi.exception.LaunchValidationException exception =
                    new org.apache.airavata.thriftapi.exception.LaunchValidationException();
            exception.setErrorMessage(
                    "Error validating experiment: " + experimentId + ". More info: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    public boolean validateProcess(
            String experimentId, List<org.apache.airavata.thriftapi.model.ProcessModel> processes)
            throws org.apache.airavata.thriftapi.exception.LaunchValidationException, TException {
        try {
            List<ProcessModel> domainProcesses =
                    processes.stream().map(processModelMapper::toDomain).collect(Collectors.toList());
            return orchestratorService.validateProcess(experimentId, domainProcesses);
        } catch (org.apache.airavata.common.exception.LaunchValidationException e) {
            throw convertToThriftLaunchValidationException(e);
        } catch (Throwable e) {
            log.error("Error validating process: " + experimentId, e);
            org.apache.airavata.thriftapi.exception.LaunchValidationException exception =
                    new org.apache.airavata.thriftapi.exception.LaunchValidationException();
            exception.setErrorMessage("Error validating process: " + experimentId + ". More info: " + e.getMessage());
            exception.initCause(e);
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
    public boolean terminateExperiment(String experimentId, String gatewayId)
            throws org.apache.airavata.thriftapi.exception.AiravataSystemException, TException {
        log.info("Experiment: {} is cancelling  !!!!!", experimentId);
        try {
            return orchestratorService.terminateExperiment(experimentId, gatewayId);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftAiravataSystemException(e);
        } catch (Throwable e) {
            log.error("Error terminating experiment: " + experimentId, e);
            org.apache.airavata.thriftapi.exception.AiravataSystemException exception =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            exception.setAiravataErrorType(
                    org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error terminating experiment: " + experimentId + ". More info: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    public void fetchIntermediateOutputs(String experimentId, String gatewayId, List<String> outputNames)
            throws org.apache.airavata.thriftapi.exception.AiravataSystemException, TException {
        try {
            orchestratorService.fetchIntermediateOutputs(experimentId, gatewayId, outputNames);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftAiravataSystemException(e);
        } catch (Throwable e) {
            log.error("Error fetching intermediate outputs for experiment: " + experimentId, e);
            org.apache.airavata.thriftapi.exception.AiravataSystemException exception =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            exception.setAiravataErrorType(
                    org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error fetching intermediate outputs for experiment: " + experimentId + ". More info: "
                    + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    public boolean launchProcess(String processId, String airavataCredStoreToken, String gatewayId)
            throws org.apache.airavata.thriftapi.exception.AiravataSystemException, TException {
        try {
            return orchestratorService.launchProcess(processId, airavataCredStoreToken, gatewayId);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftAiravataSystemException(e);
        } catch (Throwable e) {
            log.error("Error launching process: " + processId, e);
            org.apache.airavata.thriftapi.exception.AiravataSystemException exception =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            exception.setAiravataErrorType(
                    org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error launching process: " + processId + ". More info: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    // Helper methods for exception conversion
    private org.apache.airavata.thriftapi.exception.AiravataSystemException convertToThriftAiravataSystemException(
            org.apache.airavata.common.exception.AiravataSystemException e) {
        org.apache.airavata.thriftapi.exception.AiravataSystemException thriftException =
                new org.apache.airavata.thriftapi.exception.AiravataSystemException();
        thriftException.setMessage(e.getMessage());
        if (e.getAiravataErrorType() != null) {
            thriftException.setAiravataErrorType(
                    org.apache.airavata.thriftapi.exception.AiravataErrorType.valueOf(e.getAiravataErrorType().name()));
        }
        thriftException.initCause(e);
        return thriftException;
    }

    private org.apache.airavata.thriftapi.exception.LaunchValidationException convertToThriftLaunchValidationException(
            org.apache.airavata.common.exception.LaunchValidationException e) {
        org.apache.airavata.thriftapi.exception.LaunchValidationException thriftException =
                new org.apache.airavata.thriftapi.exception.LaunchValidationException();
        thriftException.setErrorMessage(e.getErrorMessage());
        thriftException.initCause(e);
        return thriftException;
    }
}
