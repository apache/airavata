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
import org.apache.airavata.common.exception.ValidationResults;
import org.apache.airavata.common.exception.ValidatorResult;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ExperimentState;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.config.conditional.ConditionalOnApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@ConditionalOnApiService
@Conditional(ComputeValidatorEnabledCondition.class)
public class ExperimentStatusValidator implements JobMetadataValidator {
    private static Logger log = LoggerFactory.getLogger(ExperimentStatusValidator.class);

    public ValidationResults validate(ExperimentModel experiment, ProcessModel processModel) {
        var error = "During the validation step experiment status should be CREATED, But this experiment status is : ";
        var validationResults = new ValidationResults();
        validationResults.setValidationState(true);
        var validatorResult = new ValidatorResult();
        var validatorResultList = new ArrayList<ValidatorResult>();
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
