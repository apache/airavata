/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

namespace java org.apache.airavata.model.commons
namespace php Airavata.Model.Commons
namespace cpp apache.airavata.model.commons
namespace py airavata.model.commons

const string DEFAULT_ID = "DO_NOT_SET_AT_CLIENTS"

struct ErrorModel {
    1: required string errorId = DEFAULT_ID,
    2: optional i64 creationTime,
    3: optional string actualErrorMessage,
    4: optional string userFriendlyMessage,
    5: optional bool transientOrPersistent = 0,
    6: optional list<string> rootCauseErrorIdList
}


/**
* This data structure can be used to store the validation results
* captured during validation step and during the launchExperiment
* operation it can be easilly checked to see the errors occured
* during the experiment launch operation
**/

struct ValidatorResult {
    1: required bool result,
    2: optional string errorDetails
}


struct ValidationResults {
    1: required bool validationState,
    2: required list<ValidatorResult> validationResultList
}