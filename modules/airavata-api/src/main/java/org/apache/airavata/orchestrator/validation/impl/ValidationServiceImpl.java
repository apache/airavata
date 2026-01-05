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
package org.apache.airavata.orchestrator.validation.impl;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import org.apache.airavata.common.exception.LaunchValidationException;
import org.apache.airavata.common.exception.ValidationResults;
import org.apache.airavata.common.exception.ValidatorResult;
import org.apache.airavata.common.model.ErrorModel;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.orchestrator.Orchestrator;
import org.apache.airavata.orchestrator.context.OrchestratorContext;
import org.apache.airavata.orchestrator.exception.OrchestratorException;
import org.apache.airavata.orchestrator.utils.OrchestratorConstants;
import org.apache.airavata.orchestrator.validation.ValidationService;
import org.apache.airavata.orchestrator.validator.JobMetadataValidator;
import org.apache.airavata.registry.exception.RegistryServiceException;
import org.apache.airavata.service.registry.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Implementation of ValidationService.
 */
@Service
@Profile("!test")
@ConditionalOnExpression("${services.rest.enabled:false} == true || ${services.thrift.enabled:true} == true")
public class ValidationServiceImpl implements ValidationService {
    private static final Logger logger = LoggerFactory.getLogger(ValidationServiceImpl.class);

    private final Orchestrator orchestrator;
    private final RegistryService registryService;
    private final org.springframework.context.ApplicationContext applicationContext;

    public ValidationServiceImpl(
            @Lazy Orchestrator orchestrator,
            RegistryService registryService,
            org.springframework.context.ApplicationContext applicationContext) {
        this.orchestrator = orchestrator;
        this.registryService = registryService;
        this.applicationContext = applicationContext;
    }

    private OrchestratorContext getOrchestratorContext() {
        if (orchestrator instanceof org.apache.airavata.orchestrator.impl.AbstractOrchestrator) {
            return ((org.apache.airavata.orchestrator.impl.AbstractOrchestrator) orchestrator).getOrchestratorContext();
        }
        // Fallback: create a minimal context if orchestrator is not initialized yet
        logger.warn("OrchestratorContext not available, using default validation configuration");
        OrchestratorContext context = new OrchestratorContext();
        org.apache.airavata.orchestrator.OrchestratorConfiguration config =
                new org.apache.airavata.orchestrator.OrchestratorConfiguration();
        config.setEnableValidation(true);
        context.setOrchestratorConfiguration(config);
        return context;
    }

    private ValidationResults runValidators(
            ExperimentModel experiment, ProcessModel processModel, String errorType, String entityId)
            throws OrchestratorException {
        ValidationResults validationResults = new ValidationResults();
        validationResults.setValidationState(true);
        String errorMsg = "Validation Errors : ";

        OrchestratorContext orchestratorContext = getOrchestratorContext();
        if (orchestratorContext == null
                || orchestratorContext.getOrchestratorConfiguration() == null
                || !orchestratorContext.getOrchestratorConfiguration().isEnableValidation()) {
            return validationResults;
        }

        // Get all enabled JobMetadataValidator beans from Spring context
        Map<String, JobMetadataValidator> validatorBeans =
                applicationContext.getBeansOfType(JobMetadataValidator.class);

        // Use all enabled validators from Spring context
        for (JobMetadataValidator validator : validatorBeans.values()) {
            try {
                validationResults = validator.validate(experiment, processModel);

                if (validationResults.getValidationState()) {
                    logger.info("Validation of " + validator + " is SUCCESSFUL");
                } else {
                    List<ValidatorResult> validationResultList = validationResults.getValidationResultList();
                    for (ValidatorResult result : validationResultList) {
                        if (!result.getResult()) {
                            String validationError = result.getErrorDetails();
                            if (validationError != null) {
                                errorMsg += validationError + " ";
                            }
                        }
                    }
                    logger.error("Validation of " + validator + " for " + entityId + " is FAILED:[error]. " + errorMsg);
                    validationResults.setValidationState(false);
                    ErrorModel details = new ErrorModel();
                    details.setActualErrorMessage(errorMsg);
                    details.setCreationTime(Calendar.getInstance().getTimeInMillis());
                    try {
                        registryService.addErrors(errorType, details, entityId);
                    } catch (RegistryServiceException e) {
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
        ValidationResults validationResults =
                runValidators(experiment, null, OrchestratorConstants.EXPERIMENT_ERROR, experiment.getExperimentId());

        if (validationResults.getValidationState()) {
            return validationResults;
        } else {
            LaunchValidationException launchValidationException = new LaunchValidationException();
            launchValidationException.setValidationResult(validationResults);
            launchValidationException.setErrorMessage("Validation failed refer the validationResults list for "
                    + "detail error. Validation errors : " + validationResults.getValidationResultList());
            throw launchValidationException;
        }
    }

    @Override
    public ValidationResults validateProcess(ExperimentModel experiment, ProcessModel processModel)
            throws OrchestratorException, LaunchValidationException {
        ValidationResults validationResults = runValidators(
                experiment, processModel, OrchestratorConstants.PROCESS_ERROR, processModel.getProcessId());

        if (validationResults.getValidationState()) {
            return validationResults;
        } else {
            LaunchValidationException launchValidationException = new LaunchValidationException();
            launchValidationException.setValidationResult(validationResults);
            launchValidationException.setErrorMessage("Validation failed refer the validationResults "
                    + "list for detail error. Validation errors : " + validationResults.getValidationResultList());
            throw launchValidationException;
        }
    }
}
