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
 * Component Programming Interface definition for Apache Airavata Orchestration Service.
 *
*/

include "../airavata-apis/airavata_errors.thrift"
include "../data-models/experiment-catalog-models/process_model.thrift"
include "../base-api/base_api.thrift"

namespace java org.apache.airavata.orchestrator.cpi

const string ORCHESTRATOR_CPI_VERSION = "0.18.0"

service OrchestratorService extends base_api.BaseAPI {

    /**
     * After creating the experiment Data user have the
     * experimentID as the handler to the experiment, during the launchExperiment
     * We just have to give the experimentID
     *
     * @param experimentID
     * @return sucess/failure
     *
    **/
  bool launchExperiment (1: required string experimentId, 2: required string gatewayId),

    /**
     * In order to run single applications users should create an associating 
     * process and hand it over for execution
     * along with a credential store token for sshKeyAuthentication
     *
     * @param processId
     * @param airavataCredStoreToken
     * @return sucess/failure
     *
    **/
  bool launchProcess (1: required string processId, 2: required string airavataCredStoreToken, 3: required string gatewayId),

    /**
     *
     * Validate funcations which can verify if the experiment is ready to be launced.
     *
     * @param experimentID
     * @return sucess/failure
     *
    **/
  bool validateExperiment(1: required string experimentId) throws (1: airavata_errors.LaunchValidationException lve)

  bool validateProcess(1: required string experimentId, 2: required list<process_model.ProcessModel> processes) throws (1: airavata_errors.LaunchValidationException lve)
    /**
     *
     * Terminate the running experiment.
     *
     * @param experimentID
     * @return sucess/failure
     *
    **/
  bool terminateExperiment (1: required string experimentId, 2: required string gatewayId)
}
