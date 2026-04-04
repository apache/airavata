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
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.BatchQueueResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.ComputeResourcePolicy;
import org.apache.airavata.model.error.proto.ValidationResults;
import org.apache.airavata.model.error.proto.ValidatorResult;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.model.experiment.proto.UserConfigurationDataModel;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.model.scheduling.proto.ComputationalResourceSchedulingModel;
import org.apache.airavata.task.SchedulerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupResourceProfileValidator implements JobMetadataValidator {

    private static final Logger logger = LoggerFactory.getLogger(GroupResourceProfileValidator.class);

    private final RegistryHandler registryHandler;

    public GroupResourceProfileValidator() {
        this.registryHandler = SchedulerUtils.getRegistryHandler();
    }

    @Override
    public ValidationResults validate(ExperimentModel experiment, ProcessModel processModel) {
        boolean valid = true;
        List<ValidatorResult> validatorResultList;
        try {
            validatorResultList = validateGroupResourceProfile(experiment, processModel);
            for (ValidatorResult result : validatorResultList) {
                if (!result.getResult()) {
                    valid = false;
                    break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while validating Group Resource Profile", e);
        }
        return ValidationResults.newBuilder()
                .setValidationState(valid)
                .addAllValidationResultList(validatorResultList)
                .build();
    }

    private List<ValidatorResult> validateGroupResourceProfile(ExperimentModel experiment, ProcessModel processModel)
            throws Exception {
        List<ValidatorResult> validatorResultList = new ArrayList<ValidatorResult>();
        UserConfigurationDataModel userConfigurationData = experiment.getUserConfigurationData();
        ComputationalResourceSchedulingModel computationalResourceScheduling =
                userConfigurationData.getComputationalResourceScheduling();

        String groupResourceProfileId = userConfigurationData.getGroupResourceProfileId();
        String computeResourceId;
        if (processModel == null) {
            computeResourceId = computationalResourceScheduling.getResourceHostId();
        } else {
            computeResourceId = processModel.getProcessResourceSchedule().getResourceHostId();
        }

        List<BatchQueueResourcePolicy> batchQueueResourcePolicies =
                registryHandler.getGroupBatchQueueResourcePolicyList(groupResourceProfileId);
        List<ComputeResourcePolicy> computeResourcePolicies =
                registryHandler.getGroupComputeResourcePolicyList(groupResourceProfileId);
        ComputeResourcePolicy groupComputeResourcePolicy = computeResourcePolicies.stream()
                .filter(computeResourcePolicy -> computeResourceId.equals(computeResourcePolicy.getComputeResourceId()))
                .findFirst()
                .get();

        if (groupComputeResourcePolicy != null) {
            List<String> ComputeResourcePolicyBatchQueues = groupComputeResourcePolicy.getAllowedBatchQueuesList();
            String queueName = computationalResourceScheduling.getQueueName().trim();
            if (ComputeResourcePolicyBatchQueues.contains(queueName)) {
                BatchQueueResourcePolicy batchQueueResourcePolicy = batchQueueResourcePolicies.stream()
                        .filter(bqResourcePolicy -> computeResourceId.equals(bqResourcePolicy.getComputeResourceId())
                                && queueName.equals(bqResourcePolicy.getQueuename()))
                        .findFirst()
                        .get();

                if (batchQueueResourcePolicy != null) {
                    validatorResultList.addAll(
                            batchQueuePolicyValidate(computationalResourceScheduling, batchQueueResourcePolicy));
                } else {
                    logger.info(
                            "There is no batch queue resource policy specified for the group resource profile and queue name");
                    validatorResultList.add(
                            ValidatorResult.newBuilder().setResult(true).build());
                }
            } else {
                validatorResultList.add(ValidatorResult.newBuilder()
                        .setResult(false)
                        .setErrorDetails("The specified queue " + queueName
                                + " does not exist in the list of allowed queues for the group resource profile.")
                        .build());
            }
        } else {
            logger.info("There is no compute resource policy specified for the group resource profile");
            validatorResultList.add(ValidatorResult.newBuilder().setResult(true).build());

            // verify if batchQueueResourcePolicy exists without computeResourcePolicy
            if (batchQueueResourcePolicies != null && !batchQueueResourcePolicies.isEmpty()) {
                String queueName =
                        computationalResourceScheduling.getQueueName().trim();
                BatchQueueResourcePolicy batchQueueResourcePolicy = batchQueueResourcePolicies.stream()
                        .filter(bqResourcePolicy -> computeResourceId.equals(bqResourcePolicy.getComputeResourceId())
                                && queueName.equals(bqResourcePolicy.getQueuename()))
                        .findFirst()
                        .get();

                if (batchQueueResourcePolicy != null) {
                    validatorResultList.addAll(
                            batchQueuePolicyValidate(computationalResourceScheduling, batchQueueResourcePolicy));
                } else {
                    logger.info(
                            "There is no batch queue resource policy specified for the group resource profile and queue name");
                    validatorResultList.add(
                            ValidatorResult.newBuilder().setResult(true).build());
                }
            } else {
                logger.info("There is no batch resource policy specified for the group resource profile");
            }
        }
        return validatorResultList;
    }

    private List<ValidatorResult> batchQueuePolicyValidate(
            ComputationalResourceSchedulingModel computationalResourceScheduling,
            BatchQueueResourcePolicy batchQueueResourcePolicy) {
        List<ValidatorResult> batchQueuevalidatorResultList = new ArrayList<ValidatorResult>();
        int experimentWallTimeLimit = computationalResourceScheduling.getWallTimeLimit();
        int experimentNodeCount = computationalResourceScheduling.getNodeCount();
        int experimentCPUCount = computationalResourceScheduling.getTotalCpuCount();

        ValidatorResult wallTimeResult;
        if (experimentWallTimeLimit > batchQueueResourcePolicy.getMaxAllowedWalltime()) {
            wallTimeResult = ValidatorResult.newBuilder()
                    .setResult(false)
                    .setErrorDetails("Job Execution walltime " + experimentWallTimeLimit
                            + " exceeds the allowable walltime for the group resource profile "
                            + batchQueueResourcePolicy.getMaxAllowedWalltime() + " for queue "
                            + batchQueueResourcePolicy.getQueuename())
                    .build();
        } else {
            wallTimeResult = ValidatorResult.newBuilder().setResult(true).build();
        }

        ValidatorResult nodeCountResult;
        if (experimentNodeCount > batchQueueResourcePolicy.getMaxAllowedNodes()) {
            nodeCountResult = ValidatorResult.newBuilder()
                    .setResult(false)
                    .setErrorDetails("Job Execution node count " + experimentNodeCount
                            + " exceeds the allowable node count for the group resource profile "
                            + batchQueueResourcePolicy.getMaxAllowedNodes() + " for queue "
                            + batchQueueResourcePolicy.getQueuename())
                    .build();
        } else {
            nodeCountResult = ValidatorResult.newBuilder().setResult(true).build();
        }

        ValidatorResult cpuCountResult;
        if (experimentCPUCount > batchQueueResourcePolicy.getMaxAllowedCores()) {
            cpuCountResult = ValidatorResult.newBuilder()
                    .setResult(false)
                    .setErrorDetails("Job Execution cpu count " + experimentCPUCount
                            + " exceeds the allowable cpu count for the group resource profile "
                            + batchQueueResourcePolicy.getMaxAllowedCores() + " for queue "
                            + batchQueueResourcePolicy.getQueuename())
                    .build();
        } else {
            cpuCountResult = ValidatorResult.newBuilder().setResult(true).build();
        }

        batchQueuevalidatorResultList.add(wallTimeResult);
        batchQueuevalidatorResultList.add(nodeCountResult);
        batchQueuevalidatorResultList.add(cpuCountResult);
        return batchQueuevalidatorResultList;
    }
}
