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
///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
//*/
//package org.apache.airavata.orchestrator.core.util;
//
//import org.apache.airavata.model.error.ValidationResults;
//import org.apache.airavata.model.error.ValidatorResult;
//import org.apache.airavata.model.experiment.ExperimentModel;
//import org.apache.airavata.model.experiment.TaskDetails;
//import org.apache.airavata.model.experiment.WorkflowNodeDetails;
//import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
//import org.apache.airavata.orchestrator.core.validator.JobMetadataValidator;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class TestValidator implements JobMetadataValidator {
//    private final static Logger logger = LoggerFactory.getLogger(TestValidator.class);
//
//    public ValidationResults validate(Experiment experiment, WorkflowNodeDetails workflowNodeDetail, TaskDetails taskID) {
//        ValidationResults validationResults = new ValidationResults();
//        validationResults.setValidationState(true);
//        if (experiment.getProjectID() == null) {
//            logger.error("Project ID is not set");
//            ValidatorResult validatorResult = new ValidatorResult(false);
//            validatorResult.setErrorDetails("Project ID is not set");
//            validationResults.addToValidationResultList(validatorResult);
//            validationResults.setValidationState(false);
//        } else if (experiment.getExperimentID() == null) {
//            logger.error("This experiment is wrong, no experimentID set");
//            ValidatorResult validatorResult = new ValidatorResult(false);
//            validatorResult.setErrorDetails("This experiment is wrong, no experimentID set");
//            validationResults.addToValidationResultList(validatorResult);
//            validationResults.setValidationState(false);
//        }
//        return validationResults;
//    }
//}
