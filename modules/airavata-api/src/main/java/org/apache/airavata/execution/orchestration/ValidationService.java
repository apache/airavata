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
package org.apache.airavata.execution.orchestration;

import java.util.ArrayList;
import org.apache.airavata.core.exception.ValidationExceptions.LaunchValidationException;
import org.apache.airavata.core.exception.ValidationExceptions.ValidationResults;
import org.apache.airavata.core.exception.ValidationExceptions.ValidatorResult;
import org.apache.airavata.status.model.ErrorModel;
import org.apache.airavata.core.util.IdGenerator;
import org.apache.airavata.config.ServerProperties;
import org.apache.airavata.research.experiment.model.ExperimentModel;
import org.apache.airavata.research.experiment.model.ExperimentState;
import org.apache.airavata.execution.model.ProcessModel;
import org.apache.airavata.core.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.status.service.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Service for experiment and process validation.
 */
@Service
@Profile({"!test", "orchestrator-integration"})
public class ValidationService {
    private static final Logger logger = LoggerFactory.getLogger(ValidationService.class);

    private final ServerProperties properties;
    private final StatusService errorService;

    public ValidationService(ServerProperties properties, StatusService errorService) {
        this.properties = properties;
        this.errorService = errorService;
    }

    private ValidationResults runValidators(
            ExperimentModel experiment, ProcessModel processModel, String errorType, String entityId)
            throws OrchestratorException {
        var validationResults = new ValidationResults();
        validationResults.setValidationState(true);
        var errorMsg = "Validation Errors : ";

        if (!properties.validationEnabled()) {
            return validationResults;
        }

        try {
            validationResults = validateExperimentStatus(experiment);

            if (validationResults.getValidationState()) {
                logger.info("Validation of experiment status is SUCCESSFUL");
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
                logger.error("Validation of experiment status for " + entityId + " is FAILED:[error]. " + errorMsg);
                validationResults.setValidationState(false);
                var details = new ErrorModel();
                details.setActualErrorMessage(errorMsg);
                details.setCreationTime(IdGenerator.getUniqueTimestamp().getTime());
                try {
                    errorService.addProcessError(details, entityId);
                } catch (RegistryException e) {
                    logger.error("Error while saving error details to registry", e);
                    throw new OrchestratorException("Error while saving error details to registry", e);
                }
            }
        } catch (OrchestratorException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error executing experiment status validation", e);
            validationResults.setValidationState(false);
        }

        return validationResults;
    }

    private ValidationResults validateExperimentStatus(ExperimentModel experiment) {
        var validationResults = new ValidationResults();
        validationResults.setValidationState(true);
        var validatorResult = new ValidatorResult();
        var validatorResultList = new ArrayList<ValidatorResult>();
        if (!ExperimentState.CREATED.equals(experiment.getState())) {
            String error = "During the validation step experiment status should be CREATED, But this experiment status is : "
                    + experiment.getState();
            logger.error(error);
            validatorResult.setErrorDetails(error);
            validatorResult.setResult(false);
            validationResults.setValidationState(false);
        } else {
            validatorResult.setResult(true);
        }
        validatorResultList.add(validatorResult);
        validationResults.setValidationResultList(validatorResultList);
        return validationResults;
    }

    public ValidationResults validateExperiment(ExperimentModel experiment)
            throws OrchestratorException, LaunchValidationException {
        var validationResults =
                runValidators(experiment, null, "EXPERIMENT_ERROR", experiment.getExperimentId());

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

    public ValidationResults validateProcess(ExperimentModel experiment, ProcessModel processModel)
            throws OrchestratorException, LaunchValidationException {
        var validationResults = runValidators(
                experiment, processModel, "PROCESS_ERROR", processModel.getProcessId());

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
