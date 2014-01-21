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

/*
 * This file describes the definations of the Airavata Execution Data Structures. Each of the
 *   lagunage specific Airavata Client SDK's will translate this neutral data model into an 
 *   appropriate form for passing to the Airavata Server Execution API Calls.
*/

/**
 * A structure holding the experimemt metadata.
 *
 * userName:
 *   The user name of the targetted gateway end user on whos behalf the experiment is being created.
 *     the associated gateway identity can only be infered from the security hand-shake so as to avoid
 *     authorized Airavata Clients mimicing an unauthorized request. If a gateway is not registered with
 *     Airavata, an authorization exception is thrown.
 *
 * experimentName:
 *   The name of the expeiment as defined by the user. The name need not be unique as uniqueness is enforced
 *      by the generated experiment id.
 *
 * experimentDescription:
 *    The verbose description of the experiment. This is an optional parameter.
*/
struct ExperimentMetadata {
  1: required string userName,
  2: required string experimentName,
  3: optional string experimentDescription,
  4: optional bool shareExperimentPublicly = "false"
}

/**
 * A structure holding the required Security Information to execute expriements.
 *
 * airavataCredStoreToken:
 *   A requirement to execute experiments within Airavata is to first register the targetted remote computational account
 *     credentials with Airavata Credential Store. The administrative API (related to credential store) will return a 
 *     generated token associated with the registered credentials. The client has to securily posses this token id and is 
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
  1:required bool airavataAutoSchedule = "true"
  2:required bool overrideManualScheduledParams = "false",
  3:optional string resourceHostId,
  4:optional i32 cpuCount,
  4:optional i32 nodeCount,
  5:optional string queueName,
  6:optional i32 maxWalltime
}

/**
 * A structure holding specified input data handling.
 *
*/
struct InputDataHandling {
    1:optional bool stageInputFilesToWorkingDir = "false"
}

/**
 * A structure holding specified output data handling.
 *
*/
struct OutputDataHandling {
    2:optional string outputdataDir,
    3:optional string dataRegistryURL,
    4:optional bool persistOutputData = "true"
}

/**
 * A structure holding the configuration data of an experimemt.
 *
*/
struct ExperimentConfigurationData {
  1: required ComputationalResourceScheduling computationalResourceScheduling,
  2: optional InputDataHandling inputDataHandling,
  2: optional OutputDataHandling OutputDataHandling
}
