/**
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
 */
package org.apache.airavata.orchestrator.core.validator.impl;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.appcatalog.computeresource.BatchQueue;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.error.ValidationResults;
import org.apache.airavata.model.error.ValidatorResult;
import org.apache.airavata.model.experiment.*;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.orchestrator.core.validator.JobMetadataValidator;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.client.RegistryServiceClientFactory;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class BatchQueueValidator implements JobMetadataValidator {
    private final static Logger logger = LoggerFactory.getLogger(BatchQueueValidator.class);

    private RegistryService.Client registryClient;

    public BatchQueueValidator() throws TException, ApplicationSettingsException {
        this.registryClient = getRegistryServiceClient();
    }

    public ValidationResults validate(ExperimentModel experiment, ProcessModel processModel) {
        ValidationResults validationResults = new ValidationResults();
        validationResults.setValidationState(true);
        try {
            List<ValidatorResult> validatorResultList = validateUserConfiguration(experiment, processModel);
            for (ValidatorResult result : validatorResultList){
                if (!result.isResult()){
                    validationResults.setValidationState(false);
                    break;
                }
            }
            validationResults.setValidationResultList(validatorResultList);
        } catch (TException e) {
            throw new RuntimeException("Error while validating", e);
        }
        return validationResults;
    }

    private List<ValidatorResult> validateUserConfiguration (ExperimentModel experiment, ProcessModel processModel) throws TException {
        List<ValidatorResult> validatorResultList = new ArrayList<ValidatorResult>();
        UserConfigurationDataModel userConfigurationData = experiment.getUserConfigurationData();
        ComputationalResourceSchedulingModel computationalResourceScheduling = userConfigurationData.getComputationalResourceScheduling();
        if (userConfigurationData.isAiravataAutoSchedule()) {
            logger.info("User enabled Auto-Schedule. Hence we don't do validation..");
            ValidatorResult validatorResult = new ValidatorResult();
            validatorResult.setResult(true);
            validatorResultList.add(validatorResult);
        } else {
            ComputeResourceDescription computeResource;
            if (processModel == null) {
                computeResource = registryClient.getComputeResource(experiment.getUserConfigurationData().getComputationalResourceScheduling().getResourceHostId());
            } else {
                computeResource = registryClient.getComputeResource(processModel.getProcessResourceSchedule().getResourceHostId());
            }

            List<BatchQueue> batchQueues = computeResource.getBatchQueues();

            if (computationalResourceScheduling == null) {
                ValidatorResult queueNameResult = new ValidatorResult();
                queueNameResult.setResult(false);
                queueNameResult.setErrorDetails("No compute resource scheduling for experiment " + experiment.getExperimentId());
                validatorResultList.add(queueNameResult);
                return validatorResultList;
            }

            if (computationalResourceScheduling.getQueueName() == null) {
                ValidatorResult queueNameResult = new ValidatorResult();
                queueNameResult.setResult(false);
                queueNameResult.setErrorDetails("No queue name for experiment " + experiment.getExperimentId());
                validatorResultList.add(queueNameResult);
                return validatorResultList;
            }

            if (batchQueues != null && !batchQueues.isEmpty()) {
                String experimentQueueName = computationalResourceScheduling.getQueueName().trim();
                int experimentWallTimeLimit = computationalResourceScheduling.getWallTimeLimit();
                int experimentNodeCount = computationalResourceScheduling.getNodeCount();
                int experimentCPUCount = computationalResourceScheduling.getTotalCPUCount();
                ValidatorResult queueNameResult = new ValidatorResult();

                //Set the validation to false. Once all the queue's are looped, if nothing matches, then this gets passed.
                queueNameResult.setResult(false);
                queueNameResult.setErrorDetails("The specified queue " + experimentQueueName +
                        " does not exist. If you believe this is an error, contact the administrator to verify App-Catalog Configurations");
                for (BatchQueue queue : batchQueues) {
                    String resourceQueueName = queue.getQueueName();
                    int maxQueueRunTime = queue.getMaxRunTime();
                    int maxNodeCount = queue.getMaxNodes();
                    int maxcpuCount = queue.getMaxProcessors();
                    if (resourceQueueName != null && resourceQueueName.equals(experimentQueueName)) {
                        queueNameResult.setResult(true);
                        queueNameResult.setErrorDetails("");

                        //Validate if the specified wall time is within allowable limit
                        ValidatorResult wallTimeResult = new ValidatorResult();
                        if (experimentWallTimeLimit == 0) {
                            wallTimeResult.setResult(false);
                            wallTimeResult.setErrorDetails("Walltime cannot be zero for queue " + resourceQueueName);
                        } else {
                            if (maxQueueRunTime == 0) {
                                wallTimeResult.setResult(true);
                                wallTimeResult.setErrorDetails("Maximum wall time is not configured for the queue," +
                                        "Validation is being skipped");
                                logger.info("Maximum wall time is not configured for the queue" +
                                        "Validation is being skipped");
                            } else {
                                if (maxQueueRunTime < experimentWallTimeLimit) {
                                    wallTimeResult.setResult(false);
                                    wallTimeResult.setErrorDetails("Job Execution walltime " + experimentWallTimeLimit +
                                            "exceeds the allowable walltime" + maxQueueRunTime +
                                            "for queue " + resourceQueueName);
                                } else {
                                    wallTimeResult.setResult(true);
                                    wallTimeResult.setErrorDetails("");
                                }
                            }
                        }
                        //validate max node count
                        ValidatorResult nodeCountResult = new ValidatorResult();
                        if (maxNodeCount == 0) {
                            nodeCountResult.setResult(true);
                            nodeCountResult.setErrorDetails("Max node count is not configured for the queue," +
                                    "Validation is being skipped");
                            logger.info("Max node count is not configured for the queue" +
                                    "Validation is being skipped");
                        } else {
                            if (experimentNodeCount == 0) {
                                nodeCountResult.setResult(false);
                                nodeCountResult.setErrorDetails("Job Execution node count cannot be zero for queue " + resourceQueueName);
                            } else {
                                if (maxNodeCount < experimentNodeCount) {
                                    nodeCountResult.setResult(false);
                                    nodeCountResult.setErrorDetails("Job Execution node count " + experimentNodeCount +
                                            "exceeds the allowable node count" + maxNodeCount +
                                            "for queue " + resourceQueueName);
                                } else {
                                    nodeCountResult.setResult(true);
                                    nodeCountResult.setErrorDetails("");
                                }
                            }
                        }
                        // validate cpu count
                        ValidatorResult cpuCountResult = new ValidatorResult();
                        if (maxcpuCount == 0) {
                            cpuCountResult.setResult(true);
                            cpuCountResult.setErrorDetails("Max cpu count is not configured for the queue," +
                                    "Validation is being skipped");
                            logger.info("Max cpu count is not configured for the queue" +
                                    "Validation is being skipped");
                        } else {
                            if (experimentCPUCount == 0) {
                                cpuCountResult.setResult(false);
                                cpuCountResult.setErrorDetails("Job Execution cpu count cannot be zero for queue " + resourceQueueName);
                            } else {
                                if (maxcpuCount < experimentCPUCount) {
                                    cpuCountResult.setResult(false);
                                    cpuCountResult.setErrorDetails("Job Execution cpu count " + experimentCPUCount +
                                            "exceeds the allowable cpu count" + maxcpuCount +
                                            "for queue " + resourceQueueName);
                                } else {
                                    cpuCountResult.setResult(true);
                                    cpuCountResult.setErrorDetails("");
                                }
                            }
                        }
                        validatorResultList.add(wallTimeResult);
                        validatorResultList.add(nodeCountResult);
                        validatorResultList.add(cpuCountResult);
                    }
                }
                validatorResultList.add(queueNameResult);

            } else {
                // for some compute resources, you dnt need to specify queue names
                ValidatorResult result = new ValidatorResult();
                logger.info("There are not queues defined under the compute resource. Airavata assumes this experiment " +
                        "does not need a queue name...");
                result.setResult(true);
                validatorResultList.add(result);
            }
        }
        return validatorResultList;
    }

    private RegistryService.Client getRegistryServiceClient() throws ApplicationSettingsException {
        final int serverPort = Integer.parseInt(ServerSettings.getRegistryServerPort());
        final String serverHost = ServerSettings.getRegistryServerHost();
        try {
            return RegistryServiceClientFactory.createRegistryClient(serverHost, serverPort);
        } catch (RegistryServiceException e) {
            throw new RuntimeException("Unable to create registry client...", e);
        }
    }
}
