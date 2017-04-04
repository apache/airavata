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

import org.apache.airavata.model.error.ValidationResults;
import org.apache.airavata.model.error.ValidatorResult;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.orchestrator.core.validator.JobMetadataValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ExperimentStatusValidator implements JobMetadataValidator {
    private static Logger log = LoggerFactory.getLogger(ExperimentStatusValidator.class);

    public ValidationResults validate(ExperimentModel experiment, ProcessModel processModel) {
        String error = "During the validation step experiment status should be CREATED, But this experiment status is : ";
        ValidationResults validationResults = new ValidationResults();
        validationResults.setValidationState(true);
        ValidatorResult validatorResult = new ValidatorResult();
        List<ValidatorResult> validatorResultList = new ArrayList<ValidatorResult>();
        if (!experiment.getExperimentStatus().get(0).getState().equals(ExperimentState.CREATED)) {
            error += experiment.getExperimentStatus().get(0).getState().toString();
            log.error(error);
            validatorResult.setErrorDetails(error);
            validatorResult.setResult(false);
            validationResults.setValidationState(false);
        }
        validatorResult.setResult(true);
        validatorResultList.add(validatorResult);
        validationResults.setValidationResultList(validatorResultList);
        return validationResults;
    }
}
