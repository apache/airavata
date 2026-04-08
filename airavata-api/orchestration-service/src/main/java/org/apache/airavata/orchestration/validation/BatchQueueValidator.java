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
package org.apache.airavata.orchestration.validation;

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.interfaces.RegistryHandler;
import org.apache.airavata.model.appcatalog.computeresource.proto.BatchQueue;
import org.apache.airavata.model.appcatalog.computeresource.proto.ComputeResourceDescription;
import org.apache.airavata.model.error.proto.ValidationResults;
import org.apache.airavata.model.error.proto.ValidatorResult;
import org.apache.airavata.model.experiment.proto.*;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.model.scheduling.proto.ComputationalResourceSchedulingModel;
import org.apache.airavata.task.SchedulerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchQueueValidator implements JobMetadataValidator {
    private static final Logger logger = LoggerFactory.getLogger(BatchQueueValidator.class);

    private final RegistryHandler registryHandler;

    public BatchQueueValidator() {
        this.registryHandler = SchedulerUtils.getRegistryHandler();
    }

    public ValidationResults validate(ExperimentModel experiment, ProcessModel processModel) {
        boolean valid = true;
        List<ValidatorResult> validatorResultList;
        try {
            validatorResultList = validateUserConfiguration(experiment, processModel);
            for (ValidatorResult result : validatorResultList) {
                if (!result.getResult()) {
                    valid = false;
                    break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while validating", e);
        }
        return ValidationResults.newBuilder()
                .setValidationState(valid)
                .addAllValidationResultList(validatorResultList)
                .build();
    }

    private List<ValidatorResult> validateUserConfiguration(ExperimentModel experiment, ProcessModel processModel)
            throws Exception {
        List<ValidatorResult> validatorResultList = new ArrayList<ValidatorResult>();
        UserConfigurationDataModel userConfigurationData = experiment.getUserConfigurationData();
        ComputationalResourceSchedulingModel computationalResourceScheduling =
                userConfigurationData.getComputationalResourceScheduling();
        if (userConfigurationData.getAiravataAutoSchedule()) {
            logger.info("User enabled Auto-Schedule. Hence we don't do validation..");
            validatorResultList.add(ValidatorResult.newBuilder().setResult(true).build());
        } else {
            ComputeResourceDescription computeResource;
            if (processModel == null) {
                computeResource = registryHandler.getComputeResource(experiment
                        .getUserConfigurationData()
                        .getComputationalResourceScheduling()
                        .getResourceHostId());
            } else {
                computeResource = registryHandler.getComputeResource(
                        processModel.getProcessResourceSchedule().getResourceHostId());
            }

            List<BatchQueue> batchQueues = computeResource.getBatchQueuesList();

            if (!experiment.hasUserConfigurationData() || !userConfigurationData.hasComputationalResourceScheduling()) {
                validatorResultList.add(ValidatorResult.newBuilder()
                        .setResult(false)
                        .setErrorDetails(
                                "No compute resource scheduling for experiment " + experiment.getExperimentId())
                        .build());
                return validatorResultList;
            }

            if (computationalResourceScheduling.getQueueName().isEmpty()) {
                validatorResultList.add(ValidatorResult.newBuilder()
                        .setResult(false)
                        .setErrorDetails("No queue name for experiment " + experiment.getExperimentId())
                        .build());
                return validatorResultList;
            }

            if (!batchQueues.isEmpty()) {
                String experimentQueueName =
                        computationalResourceScheduling.getQueueName().trim();
                int experimentWallTimeLimit = computationalResourceScheduling.getWallTimeLimit();
                int experimentNodeCount = computationalResourceScheduling.getNodeCount();
                int experimentCPUCount = computationalResourceScheduling.getTotalCpuCount();

                // Set the validation to false. Once all the queue's are looped, if nothing matches, then this gets
                // passed.
                ValidatorResult.Builder queueNameResultBuilder = ValidatorResult.newBuilder()
                        .setResult(false)
                        .setErrorDetails(
                                "The specified queue " + experimentQueueName
                                        + " does not exist. If you believe this is an error, contact the administrator to verify App-Catalog Configurations");
                for (BatchQueue queue : batchQueues) {
                    String resourceQueueName = queue.getQueueName();
                    int maxQueueRunTime = queue.getMaxRunTime();
                    int maxNodeCount = queue.getMaxNodes();
                    int maxcpuCount = queue.getMaxProcessors();
                    if (!resourceQueueName.isEmpty() && resourceQueueName.equals(experimentQueueName)) {
                        queueNameResultBuilder.setResult(true).setErrorDetails("");

                        // Validate if the specified wall time is within allowable limit
                        ValidatorResult.Builder wallTimeResultBuilder = ValidatorResult.newBuilder();
                        if (experimentWallTimeLimit == 0) {
                            wallTimeResultBuilder
                                    .setResult(false)
                                    .setErrorDetails("Walltime cannot be zero for queue " + resourceQueueName);
                        } else {
                            if (maxQueueRunTime == 0) {
                                wallTimeResultBuilder
                                        .setResult(true)
                                        .setErrorDetails("Maximum wall time is not configured for the queue,"
                                                + "Validation is being skipped");
                                logger.info("Maximum wall time is not configured for the queue"
                                        + "Validation is being skipped");
                            } else {
                                if (maxQueueRunTime < experimentWallTimeLimit) {
                                    wallTimeResultBuilder
                                            .setResult(false)
                                            .setErrorDetails("Job Execution walltime " + experimentWallTimeLimit
                                                    + "exceeds the allowable walltime"
                                                    + maxQueueRunTime + "for queue "
                                                    + resourceQueueName);
                                } else {
                                    wallTimeResultBuilder.setResult(true).setErrorDetails("");
                                }
                            }
                        }
                        // validate max node count
                        ValidatorResult.Builder nodeCountResultBuilder = ValidatorResult.newBuilder();
                        if (maxNodeCount == 0) {
                            nodeCountResultBuilder
                                    .setResult(true)
                                    .setErrorDetails("Max node count is not configured for the queue,"
                                            + "Validation is being skipped");
                            logger.info(
                                    "Max node count is not configured for the queue" + "Validation is being skipped");
                        } else {
                            if (experimentNodeCount == 0) {
                                nodeCountResultBuilder
                                        .setResult(false)
                                        .setErrorDetails("Job Execution node count cannot be zero for queue "
                                                + resourceQueueName);
                            } else {
                                if (maxNodeCount < experimentNodeCount) {
                                    nodeCountResultBuilder
                                            .setResult(false)
                                            .setErrorDetails("Job Execution node count " + experimentNodeCount
                                                    + "exceeds the allowable node count"
                                                    + maxNodeCount + "for queue "
                                                    + resourceQueueName);
                                } else {
                                    nodeCountResultBuilder.setResult(true).setErrorDetails("");
                                }
                            }
                        }
                        // validate cpu count
                        ValidatorResult.Builder cpuCountResultBuilder = ValidatorResult.newBuilder();
                        if (maxcpuCount == 0) {
                            cpuCountResultBuilder
                                    .setResult(true)
                                    .setErrorDetails("Max cpu count is not configured for the queue,"
                                            + "Validation is being skipped");
                            logger.info(
                                    "Max cpu count is not configured for the queue" + "Validation is being skipped");
                        } else {
                            if (experimentCPUCount == 0) {
                                cpuCountResultBuilder
                                        .setResult(false)
                                        .setErrorDetails("Job Execution cpu count cannot be zero for queue "
                                                + resourceQueueName);
                            } else {
                                if (maxcpuCount < experimentCPUCount) {
                                    cpuCountResultBuilder
                                            .setResult(false)
                                            .setErrorDetails("Job Execution cpu count " + experimentCPUCount
                                                    + "exceeds the allowable cpu count"
                                                    + maxcpuCount + "for queue "
                                                    + resourceQueueName);
                                } else {
                                    cpuCountResultBuilder.setResult(true).setErrorDetails("");
                                }
                            }
                        }
                        validatorResultList.add(wallTimeResultBuilder.build());
                        validatorResultList.add(nodeCountResultBuilder.build());
                        validatorResultList.add(cpuCountResultBuilder.build());
                    }
                }
                validatorResultList.add(queueNameResultBuilder.build());

            } else {
                // for some compute resources, you dnt need to specify queue names
                logger.info("There are not queues defined under the compute resource. Airavata assumes this experiment "
                        + "does not need a queue name...");
                validatorResultList.add(
                        ValidatorResult.newBuilder().setResult(true).build());
            }
        }
        return validatorResultList;
    }
}
