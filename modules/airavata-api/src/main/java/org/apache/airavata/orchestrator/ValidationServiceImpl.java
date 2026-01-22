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
package org.apache.airavata.orchestrator;

import org.apache.airavata.common.exception.LaunchValidationException;
import org.apache.airavata.common.exception.ValidationResults;
import org.apache.airavata.common.model.ErrorModel;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.config.conditional.ConditionalOnApiService;
import org.apache.airavata.orchestrator.exception.OrchestratorException;
import org.apache.airavata.orchestrator.validator.JobMetadataValidator;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.service.registry.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Implementation of ValidationService.
 */
@Service
@Profile({"!test", "orchestrator-integration"})
@ConditionalOnApiService
public class ValidationServiceImpl implements ValidationService {
    private static final Logger logger = LoggerFactory.getLogger(ValidationServiceImpl.class);

    private final Orchestrator orchestrator;
    private final RegistryService registryService;
    private final ApplicationContext applicationContext;

    public ValidationServiceImpl(
            @Lazy Orchestrator orchestrator, RegistryService registryService, ApplicationContext applicationContext) {
        this.orchestrator = orchestrator;
        this.registryService = registryService;
        this.applicationContext = applicationContext;
    }

    private OrchestratorContext getOrchestratorContext() {
        if (orchestrator instanceof AbstractOrchestrator abstractOrchestrator) {
            return abstractOrchestrator.getOrchestratorContext();
        }
        // Fallback: create a minimal context if orchestrator is not initialized yet
        logger.warn("OrchestratorContext not available, using default validation configuration");
        var context = new OrchestratorContext();
        var config = new OrchestratorConfiguration();
        config.setEnableValidation(true);
        context.setOrchestratorConfiguration(config);
        return context;
    }

    private ValidationResults runValidators(
            ExperimentModel experiment, ProcessModel processModel, String errorType, String entityId)
            throws OrchestratorException {
        var validationResults = new ValidationResults();
        validationResults.setValidationState(true);
        var errorMsg = "Validation Errors : ";

        var orchestratorContext = getOrchestratorContext();
        if (orchestratorContext == null
                || orchestratorContext.getOrchestratorConfiguration() == null
                || !orchestratorContext.getOrchestratorConfiguration().isEnableValidation()) {
            return validationResults;
        }

        // Get all enabled JobMetadataValidator beans from Spring context
        var validatorBeans = applicationContext.getBeansOfType(JobMetadataValidator.class);

        // Use all enabled validators from Spring context
        for (var validator : validatorBeans.values()) {
            try {
                validationResults = validator.validate(experiment, processModel);

                if (validationResults.getValidationState()) {
                    logger.info("Validation of " + validator + " is SUCCESSFUL");
                } else {
                    var validationResultList = validationResults.getValidationResultList();
                    for (var result : validationResultList) {
                        if (!result.getResult()) {
                            var validationError = result.getErrorDetails();
                            if (validationError != null) {
                                errorMsg += validationError + " ";
                            }
                        }
                    }
                    logger.error("Validation of " + validator + " for " + entityId + " is FAILED:[error]. " + errorMsg);
                    validationResults.setValidationState(false);
                    var details = new ErrorModel();
                    details.setActualErrorMessage(errorMsg);
                    details.setCreationTime(AiravataUtils.getUniqueTimestamp().getTime());
                    try {
                        registryService.addErrors(errorType, details, entityId);
                    } catch (RegistryException e) {
                        logger.error("Error while saving error details to registry", e);
                        throw new OrchestratorException("Error while saving error details to registry", e);
                    }
                    break;
                }
            } catch (Exception e) {
                logger.error("Error loading or executing the validation class: " + validator, e);
                validationResults.setValidationState(false);
            }
        }

        return validationResults;
    }

    @Override
    public ValidationResults validateExperiment(ExperimentModel experiment)
            throws OrchestratorException, LaunchValidationException {
        var validationResults =
                runValidators(experiment, null, OrchestratorConstants.EXPERIMENT_ERROR, experiment.getExperimentId());

        if (validationResults.getValidationState()) {
            return validationResults;
        } else {
            var launchValidationException = new LaunchValidationException();
            launchValidationException.setValidationResult(validationResults);
            launchValidationException.setErrorMessage("Validation failed refer the validationResults list for "
                    + "detail error. Validation errors : " + validationResults.getValidationResultList());
            throw launchValidationException;
        }
    }

    @Override
    public ValidationResults validateProcess(ExperimentModel experiment, ProcessModel processModel)
            throws OrchestratorException, LaunchValidationException {
        var validationResults = runValidators(
                experiment, processModel, OrchestratorConstants.PROCESS_ERROR, processModel.getProcessId());

        if (validationResults.getValidationState()) {
            return validationResults;
        } else {
            var launchValidationException = new LaunchValidationException();
            launchValidationException.setValidationResult(validationResults);
            launchValidationException.setErrorMessage("Validation failed refer the validationResults "
                    + "list for detail error. Validation errors : " + validationResults.getValidationResultList());
            throw launchValidationException;
        }
    }
}
