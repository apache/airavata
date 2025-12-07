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
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.model.appcatalog.groupresourceprofile.BatchQueueResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.ComputeResourcePolicy;
import org.apache.airavata.model.error.ValidationResults;
import org.apache.airavata.model.error.ValidatorResult;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.service.RegistryService;
import org.apache.airavata.service.ServiceFactory;
import org.apache.airavata.service.ServiceFactoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupResourceProfileValidator implements JobMetadataValidator {

    private static final Logger logger = LoggerFactory.getLogger(GroupResourceProfileValidator.class);

    private RegistryService registryService;

    public GroupResourceProfileValidator()
            throws ApplicationSettingsException, IllegalAccessException, ClassNotFoundException, InstantiationException,
                    ServiceFactoryException {
        this.registryService = ServiceFactory.getInstance().getRegistryService();
    }

    @Override
    public ValidationResults validate(ExperimentModel experiment, ProcessModel processModel) {
        ValidationResults validationResults = new ValidationResults();
        validationResults.setValidationState(true);
        try {
            List<ValidatorResult> validatorResultList = validateGroupResourceProfile(experiment, processModel);
            for (ValidatorResult result : validatorResultList) {
                if (!result.isResult()) {
                    validationResults.setValidationState(false);
                    break;
                }
            }
            validationResults.setValidationResultList(validatorResultList);
        } catch (RegistryServiceException e) {
            throw new RuntimeException("Error while validating Group Resource Profile", e);
        }
        return validationResults;
    }

    private List<ValidatorResult> validateGroupResourceProfile(ExperimentModel experiment, ProcessModel processModel)
            throws RegistryServiceException {
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
                registryService.getGroupBatchQueueResourcePolicyList(groupResourceProfileId);
        List<ComputeResourcePolicy> computeResourcePolicies =
                registryService.getGroupComputeResourcePolicyList(groupResourceProfileId);
        ComputeResourcePolicy groupComputeResourcePolicy = computeResourcePolicies.stream()
                .filter(computeResourcePolicy -> computeResourceId.equals(computeResourcePolicy.getComputeResourceId()))
                .findFirst()
                .get();

        if (groupComputeResourcePolicy != null) {
            ValidatorResult queueNameResult = new ValidatorResult();
            List<String> ComputeResourcePolicyBatchQueues = groupComputeResourcePolicy.getAllowedBatchQueues();
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
                    ValidatorResult batchQueuePolicyResult = new ValidatorResult();
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
            ValidatorResult result = new ValidatorResult();
            logger.info("There is no compute resource policy specified for the group resource profile");
            result.setResult(true);
            validatorResultList.add(result);

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
                    ValidatorResult batchQueuePolicyResult = new ValidatorResult();
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
        List<ValidatorResult> batchQueuevalidatorResultList = new ArrayList<ValidatorResult>();
        int experimentWallTimeLimit = computationalResourceScheduling.getWallTimeLimit();
        int experimentNodeCount = computationalResourceScheduling.getNodeCount();
        int experimentCPUCount = computationalResourceScheduling.getTotalCPUCount();

        ValidatorResult wallTimeResult = new ValidatorResult();

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

        ValidatorResult nodeCountResult = new ValidatorResult();

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

        ValidatorResult cpuCountResult = new ValidatorResult();

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
