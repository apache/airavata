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
import org.apache.airavata.orchestrator.internal.util.OrchestratorServerThreadPoolExecutor;
import org.apache.airavata.service.orchestrator.OrchestratorService;
import org.apache.airavata.thriftapi.mapper.ProcessModelMapper;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OrchestratorServiceHandler
        implements org.apache.airavata.thriftapi.orchestrator.model.OrchestratorService.Iface {
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

    private TException wrapException(Throwable e) {
        if (e instanceof TException te) return te;
        TException thriftException = null;

        if (e instanceof org.apache.airavata.common.exception.LaunchValidationException) {
            var ex = new org.apache.airavata.thriftapi.exception.LaunchValidationException();
            ex.setErrorMessage(((org.apache.airavata.common.exception.LaunchValidationException) e).getErrorMessage());
            ex.initCause(e);
            thriftException = ex;
        } else if (e instanceof org.apache.airavata.orchestrator.exception.OrchestratorException) {
            var ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setMessage(e.getMessage());
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.initCause(e);
            thriftException = ex;
        }

        if (thriftException == null) {
            var ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setMessage("Internal Error: " + e.getMessage());
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.initCause(e);
            thriftException = ex;
        }
        return thriftException;
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
        } catch (Throwable e) {
            log.error("Error launching experiment: " + experimentId, e);
            throw wrapException(e);
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
        } catch (Throwable e) {
            log.error("Error validating experiment: " + experimentId, e);
            throw wrapException(e);
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
        } catch (Throwable e) {
            log.error("Error validating process: " + experimentId, e);
            throw wrapException(e);
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
        } catch (Throwable e) {
            log.error("Error terminating experiment: " + experimentId, e);
            throw wrapException(e);
        }
    }

    public void fetchIntermediateOutputs(String experimentId, String gatewayId, List<String> outputNames)
            throws org.apache.airavata.thriftapi.exception.AiravataSystemException, TException {
        try {
            orchestratorService.fetchIntermediateOutputs(experimentId, gatewayId, outputNames);
        } catch (Throwable e) {
            log.error("Error fetching intermediate outputs for experiment: " + experimentId, e);
            throw wrapException(e);
        }
    }

    @Override
    public boolean launchProcess(String processId, String airavataCredStoreToken, String gatewayId)
            throws org.apache.airavata.thriftapi.exception.AiravataSystemException, TException {
        try {
            return orchestratorService.launchProcess(processId, airavataCredStoreToken, gatewayId);
        } catch (Throwable e) {
            log.error("Error launching process: " + processId, e);
            throw wrapException(e);
        }
    }
}
