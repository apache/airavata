/**
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

import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.error.LaunchValidationException;
import org.apache.airavata.model.error.ValidationResults;
import org.apache.airavata.model.error.ValidatorResult;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.orchestrator.context.OrchestratorContext;
import org.apache.airavata.orchestrator.exception.OrchestratorException;
import org.apache.airavata.orchestrator.utils.OrchestratorConstants;
import org.apache.airavata.orchestrator.validation.ValidationService;
import org.apache.airavata.orchestrator.validator.JobMetadataValidator;
import org.apache.airavata.service.RegistryService;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;

/**
 * Implementation of ValidationService.
 */
@Service
public class ValidationServiceImpl implements ValidationService {
    private static final Logger logger = LoggerFactory.getLogger(ValidationServiceImpl.class);
    
    private final OrchestratorContext orchestratorContext;
    private final RegistryService registryService;
    
    public ValidationServiceImpl(OrchestratorContext orchestratorContext, RegistryService registryService) {
        this.orchestratorContext = orchestratorContext;
        this.registryService = registryService;
    }
    
    private ValidationResults runValidators(ExperimentModel experiment, ProcessModel processModel, String errorType, String entityId) throws OrchestratorException {
        ValidationResults validationResults = new ValidationResults();
        validationResults.setValidationState(true);
        String errorMsg = "Validation Errors : ";
        
        if (!orchestratorContext.getOrchestratorConfiguration().isEnableValidation()) {
            return validationResults;
        }
        
        List<String> validatorClasses = orchestratorContext.getOrchestratorConfiguration().getValidatorClasses();
        for (String validator : validatorClasses) {
            try {
                Class<? extends JobMetadataValidator> vClass =
                        Class.forName(validator.trim()).asSubclass(JobMetadataValidator.class);
                JobMetadataValidator jobMetadataValidator = vClass.newInstance();
                validationResults = jobMetadataValidator.validate(experiment, processModel);
                
                if (validationResults.isValidationState()) {
                    logger.info("Validation of " + validator + " is SUCCESSFUL");
                } else {
                    List<ValidatorResult> validationResultList = validationResults.getValidationResultList();
                    for (ValidatorResult result : validationResultList) {
                        if (!result.isResult()) {
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
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                logger.error("Error loading the validation class: " + validator, e);
                validationResults.setValidationState(false);
            }
        }
        
        return validationResults;
    }
    
    @Override
    public ValidationResults validateExperiment(ExperimentModel experiment) throws OrchestratorException, LaunchValidationException {
        ValidationResults validationResults = runValidators(experiment, null, OrchestratorConstants.EXPERIMENT_ERROR, experiment.getExperimentId());
        
        if (validationResults.isValidationState()) {
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
    public ValidationResults validateProcess(ExperimentModel experiment, ProcessModel processModel) throws OrchestratorException, LaunchValidationException {
        ValidationResults validationResults = runValidators(experiment, processModel, OrchestratorConstants.PROCESS_ERROR, processModel.getProcessId());
        
        if (validationResults.isValidationState()) {
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
