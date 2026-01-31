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
package org.apache.airavata.orchestrator.validator;

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.common.exception.ValidationExceptions.ValidationResults;
import org.apache.airavata.common.exception.ValidationExceptions.ValidatorResult;
import org.apache.airavata.common.model.BatchQueueResourcePolicy;
import org.apache.airavata.common.model.ComputationalResourceSchedulingModel;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.config.conditional.ServiceConditionals.ConditionalOnApiService;
import org.apache.airavata.registry.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.service.registry.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@ConditionalOnApiService
@Conditional(ComputeValidatorEnabledCondition.class)
public class GroupResourceProfileValidator implements JobMetadataValidator {

    private static final Logger logger = LoggerFactory.getLogger(GroupResourceProfileValidator.class);

    private final RegistryService registryService;

    public GroupResourceProfileValidator(RegistryService registryService) {
        this.registryService = registryService;
    }

    @Override
    public ValidationResults validate(ExperimentModel experiment, ProcessModel processModel) {
        var validationResults = new ValidationResults();
        validationResults.setValidationState(true);
        try {
            var validatorResultList = validateGroupResourceProfile(experiment, processModel);
            for (var result : validatorResultList) {
                if (!result.getResult()) {
                    validationResults.setValidationState(false);
                    break;
                }
            }
            validationResults.setValidationResultList(validatorResultList);
        } catch (RegistryException e) {
            throw new RuntimeException("Error while validating Group Resource Profile", e);
        }
        return validationResults;
    }

    private List<ValidatorResult> validateGroupResourceProfile(ExperimentModel experiment, ProcessModel processModel)
            throws RegistryException {
        var validatorResultList = new ArrayList<ValidatorResult>();
        var userConfigurationData = experiment.getUserConfigurationData();
        var computationalResourceScheduling = userConfigurationData.getComputationalResourceScheduling();

        var groupResourceProfileId = userConfigurationData.getGroupResourceProfileId();
        String computeResourceId;
        if (processModel == null) {
            computeResourceId = computationalResourceScheduling.getResourceHostId();
        } else {
            computeResourceId = processModel.getProcessResourceSchedule().getResourceHostId();
        }

        var batchQueueResourcePolicies = registryService.getGroupBatchQueueResourcePolicyList(groupResourceProfileId);
        var computeResourcePolicies = registryService.getGroupComputeResourcePolicyList(groupResourceProfileId);
        var groupComputeResourcePolicy = computeResourcePolicies.stream()
                .filter(computeResourcePolicy -> computeResourceId.equals(computeResourcePolicy.getComputeResourceId()))
                .findFirst()
                .get();

        if (groupComputeResourcePolicy != null) {
            var queueNameResult = new ValidatorResult();
            var ComputeResourcePolicyBatchQueues = groupComputeResourcePolicy.getAllowedBatchQueues();
            var queueName = computationalResourceScheduling.getQueueName().trim();
            if (ComputeResourcePolicyBatchQueues.contains(queueName)) {
                var batchQueueResourcePolicy = batchQueueResourcePolicies.stream()
                        .filter(bqResourcePolicy -> computeResourceId.equals(bqResourcePolicy.getComputeResourceId())
                                && queueName.equals(bqResourcePolicy.getQueuename()))
                        .findFirst()
                        .get();

                if (batchQueueResourcePolicy != null) {
                    validatorResultList.addAll(
                            batchQueuePolicyValidate(computationalResourceScheduling, batchQueueResourcePolicy));
                } else {
                    var batchQueuePolicyResult = new ValidatorResult();
                    logger.info(
                            "There is no batch queue resource policy specified for the group resource profile and queue name");
                    batchQueuePolicyResult.setResult(true);
                    validatorResultList.add(batchQueuePolicyResult);
                }
            } else {
                queueNameResult.setResult(false);
                queueNameResult.setErrorDetails("The specified queue " + queueName
                        + " does not exist in the list of allowed queues for the group resource profile.");
                validatorResultList.add(queueNameResult);
            }
        } else {
            var result = new ValidatorResult();
            logger.info("There is no compute resource policy specified for the group resource profile");
            result.setResult(true);
            validatorResultList.add(result);

            // verify if batchQueueResourcePolicy exists without computeResourcePolicy
            if (batchQueueResourcePolicies != null && !batchQueueResourcePolicies.isEmpty()) {
                var queueName = computationalResourceScheduling.getQueueName().trim();
                var batchQueueResourcePolicy = batchQueueResourcePolicies.stream()
                        .filter(bqResourcePolicy -> computeResourceId.equals(bqResourcePolicy.getComputeResourceId())
                                && queueName.equals(bqResourcePolicy.getQueuename()))
                        .findFirst()
                        .get();

                if (batchQueueResourcePolicy != null) {
                    validatorResultList.addAll(
                            batchQueuePolicyValidate(computationalResourceScheduling, batchQueueResourcePolicy));
                } else {
                    var batchQueuePolicyResult = new ValidatorResult();
                    logger.info(
                            "There is no batch queue resource policy specified for the group resource profile and queue name");
                    batchQueuePolicyResult.setResult(true);
                    validatorResultList.add(batchQueuePolicyResult);
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
        var batchQueuevalidatorResultList = new ArrayList<ValidatorResult>();
        int experimentWallTimeLimit = computationalResourceScheduling.getWallTimeLimit();
        int experimentNodeCount = computationalResourceScheduling.getNodeCount();
        int experimentCPUCount = computationalResourceScheduling.getTotalCPUCount();

        var wallTimeResult = new ValidatorResult();

        if (experimentWallTimeLimit > batchQueueResourcePolicy.getMaxAllowedWalltime()) {
            wallTimeResult.setResult(false);
            wallTimeResult.setErrorDetails("Job Execution walltime " + experimentWallTimeLimit
                    + " exceeds the allowable walltime for the group resource profile "
                    + batchQueueResourcePolicy.getMaxAllowedWalltime() + " for queue "
                    + batchQueueResourcePolicy.getQueuename());
        } else {
            wallTimeResult.setResult(true);
            wallTimeResult.setErrorDetails("");
        }

        var nodeCountResult = new ValidatorResult();

        if (experimentNodeCount > batchQueueResourcePolicy.getMaxAllowedNodes()) {
            nodeCountResult.setResult(false);
            nodeCountResult.setErrorDetails("Job Execution node count " + experimentNodeCount
                    + " exceeds the allowable node count for the group resource profile "
                    + batchQueueResourcePolicy.getMaxAllowedNodes() + " for queue "
                    + batchQueueResourcePolicy.getQueuename());
        } else {
            nodeCountResult.setResult(true);
            nodeCountResult.setErrorDetails("");
        }

        var cpuCountResult = new ValidatorResult();

        if (experimentCPUCount > batchQueueResourcePolicy.getMaxAllowedCores()) {
            cpuCountResult.setResult(false);
            cpuCountResult.setErrorDetails("Job Execution cpu count " + experimentCPUCount
                    + " exceeds the allowable cpu count for the group resource profile "
                    + batchQueueResourcePolicy.getMaxAllowedCores() + " for queue "
                    + batchQueueResourcePolicy.getQueuename());
        } else {
            cpuCountResult.setResult(true);
            cpuCountResult.setErrorDetails("");
        }

        batchQueuevalidatorResultList.add(wallTimeResult);
        batchQueuevalidatorResultList.add(nodeCountResult);
        batchQueuevalidatorResultList.add(cpuCountResult);
        return batchQueuevalidatorResultList;
    }
}
