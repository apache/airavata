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

namespace java org.apache.airavata.datamodel

/*
 * This file describes the definitions of the Airavata Execution Data Structures. Each of the
 *   language specific Airavata Client SDK's will translate this neutral data model into an
 *   appropriate form for passing to the Airavata Server Execution API Calls.
*/

/**
 * A structure holding the experiment configuration.
 *
 * userName:
 *   The user name of the targeted gateway end user on whose behalf the experiment is being created.
 *     the associated gateway identity can only be inferred from the security hand-shake so as to avoid
 *     authorized Airavata Clients mimicking an unauthorized request. If a gateway is not registered with
 *     Airavata, an authorization exception is thrown.
 *
 * experimentName:
 *   The name of the experiment as defined by the user. The name need not be unique as uniqueness is enforced
 *      by the generated experiment id.
 *
 * experimentDescription:
 *    The verbose description of the experiment. This is an optional parameter.
*/
struct ExperimentConfiguration {
  1: required string userName,
  2: required string experimentName,
  3: optional string experimentDescription,
  4: optional bool shareExperimentPublicly = 0
}


/**
 * A structure holding the experiment metadata.
 *
 * userName:
 *   The user name of the targeted gateway end user on whose behalf the experiment is being created.
 *     the associated gateway identity can only be inferred from the security hand-shake so as to avoid
 *     authorized Airavata Clients mimicking an unauthorized request. If a gateway is not registered with
 *     Airavata, an authorization exception is thrown.
 *
 * experimentName:
 *   The name of the experiment as defined by the user. The name need not be unique as uniqueness is enforced
 *      by the generated experiment id.
 *
 * experimentDescription:
 *    The verbose description of the experiment. This is an optional parameter.
*/
struct ExperimentMetadata {
  1: required string userName,
  2: required string experimentName,
  3: optional string experimentDescription,
  4: optional bool shareExperimentPublicly = 0
}

/**
 * A structure holding the required Security Information to execute experiments.
 *
 * airavataCredStoreToken:
 *   A requirement to execute experiments within Airavata is to first register the targeted remote computational account
 *     credentials with Airavata Credential Store. The administrative API (related to credential store) will return a 
 *     generated token associated with the registered credentials. The client has to securely posses this token id and is
 *     required to pass it to Airavata Server for all execution requests.
*/
struct ExecutionSecurityParameters {
  1:optional string airavataCredStoreToken
}


/**
 * A structure holding the Computational Resource Scheduling.
 *
*/
struct ComputationalResourceScheduling {
  1:required bool airavataAutoSchedule = 1
  2:required bool overrideManualScheduledParams = 0,
  3:optional string resourceHostId,
  4:optional i32 cpuCount,
  5:optional i32 nodeCount,
  6:optional string queueName,
  7:optional i32 maxWalltime
}

/**
 * A structure holding specified input data handling.
 *
*/
struct InputDataHandling {
    1:optional bool stageInputFilesToWorkingDir = 0
}

/**
 * A structure holding specified output data handling.
 *
*/
struct OutputDataHandling {
    2:optional string outputdataDir,
    3:optional string dataRegistryURL,
    4:optional bool persistOutputData = 1
}

/**
 * A structure holding the configuration data of an experiment.
 *
*/
struct ExperimentConfigurationData {
  1: required ComputationalResourceScheduling computationalResourceScheduling,
  2: optional InputDataHandling inputDataHandling,
  3: optional OutputDataHandling OutputDataHandling,
  4: optional ExperimentMetadata experimentMetadata
}


