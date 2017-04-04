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
//import org.apache.airavata.orchestrator.core.validator.JobMetadataValidator;
//
//public class SecondValidator implements JobMetadataValidator {
//    public ValidationResults validate(Experiment experiment, WorkflowNodeDetails workflowNodeDetail, TaskDetails taskID) {
//        ValidationResults validationResults = new ValidationResults();
//        validationResults.setValidationState(true);
//        if(taskID.getTaskID() == null) {
//            ValidatorResult validatorResult = new ValidatorResult(false);
//            validatorResult.setErrorDetails("No taskID is set, so Validation failed");
//            validationResults.addToValidationResultList(validatorResult);
//            validationResults.setValidationState(false);
//        }
//        return validationResults;
//    }
//}
