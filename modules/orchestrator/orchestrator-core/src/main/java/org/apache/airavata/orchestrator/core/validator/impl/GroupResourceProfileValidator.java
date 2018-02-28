package org.apache.airavata.orchestrator.core.validator.impl;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.appcatalog.groupresourceprofile.BatchQueueResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.ComputeResourcePolicy;
import org.apache.airavata.model.error.ValidationResults;
import org.apache.airavata.model.error.ValidatorResult;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
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

public class GroupResourceProfileValidator implements JobMetadataValidator {

    private final static Logger logger = LoggerFactory.getLogger(GroupResourceProfileValidator.class);

    private RegistryService.Client registryClient;

    public GroupResourceProfileValidator() throws TException, ApplicationSettingsException {
        this.registryClient = getRegistryServiceClient();
    }

    @Override
    public ValidationResults validate(ExperimentModel experiment, ProcessModel processModel) {
        ValidationResults validationResults = new ValidationResults();
        validationResults.setValidationState(true);
        try {
            List<ValidatorResult> validatorResultList = validateGroupResourceProfile(experiment, processModel);
            for (ValidatorResult result : validatorResultList){
                if (!result.isResult()){
                    validationResults.setValidationState(false);
                    break;
                }
            }
            validationResults.setValidationResultList(validatorResultList);
        } catch (TException e) {
            throw new RuntimeException("Error while validating Group Resource Profile", e);
        }
        return validationResults;
    }

    private List<ValidatorResult> validateGroupResourceProfile(ExperimentModel experiment, ProcessModel processModel) throws TException{
        List<ValidatorResult> validatorResultList = new ArrayList<ValidatorResult>();
        UserConfigurationDataModel userConfigurationData = experiment.getUserConfigurationData();
        ComputationalResourceSchedulingModel computationalResourceScheduling = userConfigurationData.getComputationalResourceScheduling();

        String groupResourceProfileId = userConfigurationData.getGroupResourceProfileId();
        String computeResourceId;
        if (processModel == null) {
            computeResourceId = computationalResourceScheduling.getResourceHostId();
        } else {
            computeResourceId = processModel.getProcessResourceSchedule().getResourceHostId();
        }

        List<BatchQueueResourcePolicy> batchQueueResourcePolicies = registryClient.getGroupBatchQueueResourcePolicyList(groupResourceProfileId);
        List<ComputeResourcePolicy> computeResourcePolicies = registryClient.getGroupComputeResourcePolicyList(groupResourceProfileId);
        ComputeResourcePolicy groupComputeResourcePolicy = computeResourcePolicies.stream()
                                                            .filter(computeResourcePolicy -> computeResourceId.equals(computeResourcePolicy.getComputeResourceId()))
                                                            .findFirst().get();

        if (groupComputeResourcePolicy != null) {
            ValidatorResult queueNameResult = new ValidatorResult();
            List<String> ComputeResourcePolicyBatchQueues = groupComputeResourcePolicy.getAllowedBatchQueues();
            String queueName = computationalResourceScheduling.getQueueName().trim();
            if (ComputeResourcePolicyBatchQueues.contains(queueName)) {
                BatchQueueResourcePolicy batchQueueResourcePolicy = batchQueueResourcePolicies.stream()
                                                                    .filter(bqResourcePolicy -> computeResourceId.equals(bqResourcePolicy.getComputeResourceId())
                                                                            && queueName.equals(bqResourcePolicy.getQueuename()))
                                                                    .findFirst().get();

                if (batchQueueResourcePolicy != null) {
                    validatorResultList.addAll(batchQueuePolicyValidate(computationalResourceScheduling, batchQueueResourcePolicy));
                } else {
                    ValidatorResult batchQueuePolicyResult = new ValidatorResult();
                    logger.info("There is no batch queue resource policy specified for the group resource profile and queue name");
                    batchQueuePolicyResult.setResult(true);
                    validatorResultList.add(batchQueuePolicyResult);
                }
            } else {
                queueNameResult.setResult(false);
                queueNameResult.setErrorDetails("The specified queue " + queueName +
                        " does not exist in the list of allowed queues for the group resource profile.");
                validatorResultList.add(queueNameResult);
            }
        } else {
            ValidatorResult result = new ValidatorResult();
            logger.info("There is no compute resource policy specified for the group resource profile");
            result.setResult(true);
            validatorResultList.add(result);

            // verify if batchQueueResourcePolicy exists without computeResourcePolicy
            if (batchQueueResourcePolicies != null && !batchQueueResourcePolicies.isEmpty()) {
                String queueName = computationalResourceScheduling.getQueueName().trim();
                BatchQueueResourcePolicy batchQueueResourcePolicy = batchQueueResourcePolicies.stream()
                        .filter(bqResourcePolicy -> computeResourceId.equals(bqResourcePolicy.getComputeResourceId())
                                && queueName.equals(bqResourcePolicy.getQueuename()))
                        .findFirst().get();

                if (batchQueueResourcePolicy != null) {
                    validatorResultList.addAll(batchQueuePolicyValidate(computationalResourceScheduling, batchQueueResourcePolicy));
                } else {
                    ValidatorResult batchQueuePolicyResult = new ValidatorResult();
                    logger.info("There is no batch queue resource policy specified for the group resource profile and queue name");
                    batchQueuePolicyResult.setResult(true);
                    validatorResultList.add(batchQueuePolicyResult);
                }
            } else {
                logger.info("There is no batch resource policy specified for the group resource profile");
            }
        }
        return validatorResultList;
    }

    private List<ValidatorResult> batchQueuePolicyValidate(ComputationalResourceSchedulingModel computationalResourceScheduling, BatchQueueResourcePolicy batchQueueResourcePolicy) {
        List<ValidatorResult> batchQueuevalidatorResultList = new ArrayList<ValidatorResult>();
        int experimentWallTimeLimit = computationalResourceScheduling.getWallTimeLimit();
        int experimentNodeCount = computationalResourceScheduling.getNodeCount();
        int experimentCPUCount = computationalResourceScheduling.getTotalCPUCount();

        ValidatorResult wallTimeResult = new ValidatorResult();

        if (experimentWallTimeLimit > batchQueueResourcePolicy.getMaxAllowedWalltime()) {
            wallTimeResult.setResult(false);
            wallTimeResult.setErrorDetails("Job Execution walltime " + experimentWallTimeLimit +
                    " exceeds the allowable walltime for the group resource profile " + batchQueueResourcePolicy.getMaxAllowedWalltime() +
                    " for queue " + batchQueueResourcePolicy.getQueuename());
        } else {
            wallTimeResult.setResult(true);
            wallTimeResult.setErrorDetails("");
        }

        ValidatorResult nodeCountResult = new ValidatorResult();

        if (experimentNodeCount > batchQueueResourcePolicy.getMaxAllowedNodes()) {
            nodeCountResult.setResult(false);
            nodeCountResult.setErrorDetails("Job Execution node count " + experimentNodeCount +
                    " exceeds the allowable node count for the group resource profile " + batchQueueResourcePolicy.getMaxAllowedNodes() +
                    " for queue " + batchQueueResourcePolicy.getQueuename());
        } else {
            nodeCountResult.setResult(true);
            nodeCountResult.setErrorDetails("");
        }

        ValidatorResult cpuCountResult = new ValidatorResult();

        if (experimentCPUCount > batchQueueResourcePolicy.getMaxAllowedCores()) {
            cpuCountResult.setResult(false);
            cpuCountResult.setErrorDetails("Job Execution cpu count " + experimentCPUCount +
                    " exceeds the allowable cpu count for the group resource profile " + batchQueueResourcePolicy.getMaxAllowedCores() +
                    " for queue " + batchQueueResourcePolicy.getQueuename());
        } else {
            cpuCountResult.setResult(true);
            cpuCountResult.setErrorDetails("");
        }

        batchQueuevalidatorResultList.add(wallTimeResult);
        batchQueuevalidatorResultList.add(nodeCountResult);
        batchQueuevalidatorResultList.add(cpuCountResult);
        return batchQueuevalidatorResultList;
    }

    private RegistryService.Client getRegistryServiceClient() throws TException, ApplicationSettingsException {
        final int serverPort = Integer.parseInt(ServerSettings.getRegistryServerPort());
        final String serverHost = ServerSettings.getRegistryServerHost();
        try {
            return RegistryServiceClientFactory.createRegistryClient(serverHost, serverPort);
        } catch (RegistryServiceException e) {
            throw new RuntimeException("Unable to create registry client...", e);
        }
    }
}
